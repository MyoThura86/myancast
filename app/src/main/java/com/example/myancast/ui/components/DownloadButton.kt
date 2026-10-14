package com.example.myancast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.WifiFind
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myancast.data.network.NetworkStatus
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.LiveRed
import com.example.myancast.ui.theme.OkGreen

/** ★ Phase 6 (#4) — Episode row ရဲ့ ⬇ / Wi-Fi စောင့် / % / ✓ / ⚠ */
sealed interface DownloadButtonState {
    data object NotDownloaded : DownloadButtonState
    /** Wi-Fi only ဖွင့်ထားပြီး mobile data (သို့) offline — "Wi-Fi စောင့်နေ" */
    data object WaitingForWifi : DownloadButtonState
    data object Queued : DownloadButtonState
    data class Running(val percent: Int) : DownloadButtonState
    data object Done : DownloadButtonState
    data object Failed : DownloadButtonState
}

/** Pure — status + လက်ရှိ network → ခလုတ် ပုံစံ */
fun downloadButtonState(status: DownloadStatus?, network: NetworkStatus): DownloadButtonState = when (status) {
    null -> DownloadButtonState.NotDownloaded
    is DownloadStatus.Queued ->
        if (status.wifiOnly && (network.metered || !network.online)) DownloadButtonState.WaitingForWifi
        else DownloadButtonState.Queued
    is DownloadStatus.Running -> DownloadButtonState.Running(status.percent)
    is DownloadStatus.Done -> DownloadButtonState.Done
    DownloadStatus.Failed -> DownloadButtonState.Failed
}

/** Screen reader အတွက် + snackbar မှာ ပြလို့ရတဲ့ စာ */
fun DownloadButtonState.description(): String = when (this) {
    DownloadButtonState.NotDownloaded -> "ဒေါင်းလုဒ်လုပ်ရန်"
    DownloadButtonState.WaitingForWifi -> "Wi-Fi စောင့်နေပါတယ် — နှိပ်ရင် ရပ်မယ်"
    DownloadButtonState.Queued -> "ဒေါင်းရန် စောင့်နေပါတယ် — နှိပ်ရင် ရပ်မယ်"
    is DownloadButtonState.Running -> "ဒေါင်းနေပါတယ် ${percent.toMyanmarDigits()}% — နှိပ်ရင် ရပ်မယ်"
    DownloadButtonState.Done -> "ဒေါင်းပြီးပါပြီ — နှိပ်ရင် ဖျက်မယ်"
    DownloadButtonState.Failed -> "ဒေါင်းလို့ မရပါ — နှိပ်ပြီး ပြန်ကြိုးစားပါ"
}

@Composable
fun DownloadButton(
    state: DownloadButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        val description = state.description()
        when (state) {
            DownloadButtonState.NotDownloaded -> Icon(
                Icons.Outlined.Download, contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DownloadButtonState.WaitingForWifi -> Icon(
                Icons.Outlined.WifiFind, contentDescription = description, tint = GoldPrimary
            )
            DownloadButtonState.Queued -> CircularProgressIndicator(
                modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = GoldPrimary
            )
            is DownloadButtonState.Running -> Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.percent / 100f },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = GoldPrimary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
            DownloadButtonState.Done -> Icon(Icons.Filled.DownloadDone, contentDescription = description, tint = OkGreen)
            DownloadButtonState.Failed -> Icon(Icons.Filled.ErrorOutline, contentDescription = description, tint = LiveRed)
        }
    }
}
