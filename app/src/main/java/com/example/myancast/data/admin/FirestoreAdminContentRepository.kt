package com.example.myancast.data.admin

import android.util.Log
import com.example.myancast.data.firebase.toObjectWithId
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreAdminContentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminContentRepository {

    private val podcasts get() = db.collection(FirestoreSchema.PODCASTS)
    private val episodes get() = db.collection(FirestoreSchema.EPISODES)

    override fun newPodcastId(): String = podcasts.document().id
    override fun newEpisodeId(): String = episodes.document().id

    override suspend fun getEpisode(episodeId: String): Episode? = firestoreCall {
        val snapshot = episodes.document(episodeId).get().await()
        if (snapshot.exists()) snapshot.toObjectWithId(Episode::class.java) { ep, id -> ep.copy(id = id) }
        else null
    }

    override suspend fun savePodcast(podcast: Podcast, isNew: Boolean) = firestoreCall {
        val data = podcast.toEditableFields() +
            (FirestoreSchema.PodcastField.UPDATED_AT to FieldValue.serverTimestamp())
        val ref = podcasts.document(podcast.id)
        if (isNew) {
            ref.set(data + (FirestoreSchema.PodcastField.EPISODE_COUNT to 0)).await()
        } else {
            // merge — Console ကနေ ထည့်ထားတဲ့ တခြား field တွေ မပျက်အောင်
            ref.set(data, SetOptions.merge()).await()
        }
        Unit
    }

    override suspend fun deletePodcast(podcastId: String) = firestoreCall {
        val episodeDocs = episodes
            .whereEqualTo(FirestoreSchema.EpisodeField.PODCAST_ID, podcastId)
            .get().await()
            .documents

        // Batch တစ်ခုမှာ write ၅၀၀ အထိပဲ ရ — podcast doc အတွက် နေရာ ချန်ထား
        episodeDocs.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        podcasts.document(podcastId).delete().await()
        Unit
    }

    override suspend fun saveEpisode(episode: Episode, isNew: Boolean) {
        firestoreCall {
            val batch = db.batch()
            val episodeRef = episodes.document(episode.id)
            if (isNew) batch.set(episodeRef, episode.toFirestoreFields())
            else batch.set(episodeRef, episode.toFirestoreFields(), SetOptions.merge())

            val podcastUpdate = mutableMapOf<String, Any>(
                FirestoreSchema.PodcastField.UPDATED_AT to FieldValue.serverTimestamp()
            )
            if (isNew) podcastUpdate[FirestoreSchema.PodcastField.EPISODE_COUNT] = FieldValue.increment(1)
            // update() — podcast မရှိရင် fail (မိဘ မရှိတဲ့ episode မဖြစ်အောင်)
            batch.update(podcasts.document(episode.podcastId), podcastUpdate)
            batch.commit().await()
        }
        syncEpisodeCount(episode.podcastId)
    }

    override suspend fun deleteEpisode(episode: Episode) {
        firestoreCall {
            val batch = db.batch()
            batch.delete(episodes.document(episode.id))
            batch.update(
                podcasts.document(episode.podcastId),
                mapOf(
                    FirestoreSchema.PodcastField.EPISODE_COUNT to FieldValue.increment(-1),
                    FirestoreSchema.PodcastField.UPDATED_AT to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
        syncEpisodeCount(episode.podcastId)
    }

    /**
     * Console မှာ လက်နဲ့ ထည့်ခဲ့တုန်းက `episodeCount` လွဲနေနိုင်တယ် — တကယ့် အရေအတွက်ကို ရေတွက်ပြီး ပြင်။
     * မအောင်မြင်လည်း (increment က ပြင်ပြီးသား) save ကို fail မလုပ်ဘူး။
     */
    private suspend fun syncEpisodeCount(podcastId: String) {
        try {
            val count = episodes
                .whereEqualTo(FirestoreSchema.EpisodeField.PODCAST_ID, podcastId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count
            podcasts.document(podcastId)
                .update(FirestoreSchema.PodcastField.EPISODE_COUNT, count)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "syncEpisodeCount($podcastId) failed: ${e.message}")
        }
    }

    private suspend fun <T> firestoreCall(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: FirebaseFirestoreException) {
        Log.e(TAG, "Firestore ${e.code}: ${e.message}", e)
        throw AdminException(e.code.toMessage(), e)
    } catch (e: Exception) {
        Log.e(TAG, "Firestore write failed: ${e.message}", e)
        throw AdminException("သိမ်းလို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ", e)
    }

    private companion object {
        const val TAG = "AdminContent"
        const val BATCH_LIMIT = 450
    }
}

private fun FirebaseFirestoreException.Code.toMessage(): String = when (this) {
    FirebaseFirestoreException.Code.PERMISSION_DENIED,
    FirebaseFirestoreException.Code.UNAUTHENTICATED ->
        "ခွင့်ပြုချက် မရှိပါ — Admin အဖြစ် ပြန်ဝင်ပြီး Firestore rules ကို စစ်ပါ"
    FirebaseFirestoreException.Code.UNAVAILABLE,
    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
        "အင်တာနက် ချိတ်ဆက်မှု မရပါ — ပြန်ကြိုးစားကြည့်ပါ"
    FirebaseFirestoreException.Code.NOT_FOUND ->
        "Podcast ကို ရှာမတွေ့ပါ — ဖျက်ထားပြီး ဖြစ်နိုင်ပါတယ်"
    else -> "သိမ်းလို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ"
}
