package com.example.myancast.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * PlayerController ရဲ့ တကယ့် implementation — PlaybackService ထဲက ExoPlayer ကို
 * MediaController နဲ့ ထိန်းပြီး Player event တွေကို PlaybackState အဖြစ် ပြောင်းပေးတယ်။
 * App တစ်ခုလုံးမှာ တစ်ခုတည်း (MyanCastApp.playerController) — main thread ပေါ်မှာပဲ သုံး။
 */
class Media3PlayerController(context: Context) : PlayerController {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null

    // Service မချိတ်ရသေးခင် play() ခေါ်ရင် — မှတ်ထားပြီး ချိတ်မိမှ ဖွင့်
    private var pendingPlay: PendingPlay? = null

    /** Service မချိတ်ရသေးခင် play() ခေါ်ရင် — start position ပါ မှတ်ထားရမယ် */
    private data class PendingPlay(
        val queue: PlayerQueue,
        val podcastTitle: String,
        val startPositionMs: Long
    )

    // PlaybackState မှာ Media3 type မထည့်ချင်လို့ ဒီမှာ သိမ်းထား
    private var queue = PlayerQueue(emptyList(), 0)
    private var podcastTitle = ""

    private var positionJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState(player)
        }
    }

    /** ချိတ်နေတုန်းလား — တစ်ပြိုင်တည်း ၂ ခါ မချိတ်မိအောင် */
    private var connecting = false

    init {
        connect()
    }

    /**
     * Service ကို ချိတ်တယ်။ **မအောင်မြင်ရင် နောက်တစ်ခါ `play()` ခေါ်ချိန်မှာ ပြန်ကြိုးစားတယ်** —
     * app စတင်ချိန် (Activity မတက်ခင်) ချိတ်လို့ မရတတ်တာကြောင့် တစ်ခါတည်းနဲ့ လက်မလျှော့ရဘူး။
     */
    private fun connect() {
        if (connecting || controller != null) return
        connecting = true

        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        future.addListener({
            connecting = false
            val connected = try {
                future.get()
            } catch (e: Exception) {
                Log.e("Media3PlayerController", "connect failed: ${e.message}", e)
                _state.update { it.copy(error = "Player ကို စတင်လို့ မရပါ") }
                return@addListener
            }
            connected.addListener(listener)
            controller = connected

            val pending = pendingPlay
            pendingPlay = null
            if (pending != null) {
                _state.update { it.copy(error = null) }
                startPlayback(connected, pending.queue, pending.podcastTitle, pending.startPositionMs)
            } else {
                syncState(connected)
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    // ──────── PlayerController ────────

    override fun play(queue: PlayerQueue, podcastTitle: String, startPositionMs: Long) {
        if (queue.isEmpty) return

        this.queue = queue
        this.podcastTitle = podcastTitle

        val start = startPositionMs.coerceAtLeast(0L)

        // UI က episode ကို ချက်ချင်း မြင်ရအောင် — Media3 event မစောင့်
        _state.update {
            it.copy(
                queue = queue.episodes,
                currentIndex = queue.startIndex,
                podcastTitle = podcastTitle,
                isBuffering = true,
                positionMs = start,
                durationMs = 0L,
                error = null
            )
        }

        val c = controller
        if (c == null) {
            // Service မချိတ်ရသေး — start position ပါ မှတ်ထားပြီး ချိတ်ဖို့ ပြန်ကြိုးစား
            pendingPlay = PendingPlay(queue, podcastTitle, start)
            connect()
        } else {
            startPlayback(c, queue, podcastTitle, start)
        }
    }

    override fun togglePlayPause() {
        val c = controller ?: run {
            // မချိတ်ရသေးရင် ချိတ်ဖို့ ကြိုးစား — pending ရှိရင် ချိတ်မိတာနဲ့ ဖွင့်မယ်
            connect()
            return
        }
        if (c.isPlaying) {
            c.pause()
            return
        }
        when (c.playbackState) {
            Player.STATE_IDLE -> c.prepare()                 // error ပြီးရင် ပြန်စ
            Player.STATE_ENDED -> c.seekToDefaultPosition()  // ဆုံးသွားရင် အစက ပြန်ဖွင့်
            else -> Unit                                     // READY / BUFFERING — play() ပဲ လို
        }
        c.play()
    }

    override fun seekTo(positionMs: Long) {
        val c = controller ?: return
        c.seekTo(positionMs.coerceAtLeast(0L))
        _state.update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    override fun skipBack() {
        val c = controller ?: return
        seekTo(skipBackTarget(c.currentPosition))
    }

    override fun skipForward() {
        val c = controller ?: return
        seekTo(skipForwardTarget(c.currentPosition, c.knownDurationMs()))
    }

    override fun next() {
        val c = controller ?: return
        if (c.hasNextMediaItem()) c.seekToNextMediaItem()
    }

    override fun previous() {
        val c = controller ?: return
        if (c.hasPreviousMediaItem()) c.seekToPreviousMediaItem()
    }

    override fun setSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
    }

    // ──────── Private ────────

    private fun startPlayback(
        c: MediaController,
        queue: PlayerQueue,
        podcastTitle: String,
        startPositionMs: Long
    ) {
        c.setMediaItems(
            queue.episodes.map { it.toMediaItem(podcastTitle) },
            queue.startIndex,
            startPositionMs      // ← Media3 ကိုယ်တိုင် အဲဒီနေရာက စဖွင့်တယ် (seekTo ခေါ်စရာ မလို)
        )
        c.prepare()
        c.play()
    }

    /** Player event တိုင်း — PlaybackState ကို အသစ် ပြန်ဆောက် */
    private fun syncState(player: Player) {
        _state.update {
            it.copy(
                queue = queue.episodes,
                currentIndex = if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex,
                podcastTitle = podcastTitle,
                isPlaying = player.isPlaying,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.knownDurationMs(),
                speed = player.playbackParameters.speed,
                error = player.playerError?.let { "ဖွင့်လို့ မရပါ — အင်တာနက် ချိတ်ဆက်မှု စစ်ကြည့်ပါ" }
            )
        }
        updatePositionTicker(player.isPlaying)
    }

    /** Position က event နဲ့ မလာဘူး — ဖွင့်နေတုန်း 500ms တစ်ခါ update */
    private fun updatePositionTicker(isPlaying: Boolean) {
        if (!isPlaying) {
            positionJob?.cancel()
            positionJob = null
            return
        }
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (isActive) {
                controller?.let { c ->
                    _state.update {
                        it.copy(
                            positionMs = c.currentPosition.coerceAtLeast(0L),
                            durationMs = c.knownDurationMs()
                        )
                    }
                }
                delay(POSITION_UPDATE_MS)
            }
        }
    }

    private companion object {
        const val POSITION_UPDATE_MS = 500L
    }
}

/** C.TIME_UNSET (stream load နေဆဲ) → 0 = မသိရသေး */
private fun Player.knownDurationMs(): Long =
    duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L

private fun Episode.toMediaItem(podcastTitle: String): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(podcastTitle)
        .apply { if (coverUrl.isNotBlank()) setArtworkUri(Uri.parse(coverUrl)) }
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(audioUrl)
        .setMediaMetadata(metadata)
        .build()
}
