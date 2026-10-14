package com.example.myancast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myancast.ui.theme.GoldPrimary

/** ★ Phase 6 (#2) — offline ဖြစ်ပြီး data လုံးဝ မရှိရင် ပြမယ့် error (EmptyView မဟုတ်) */
const val OFFLINE_EMPTY_ERROR = "အင်တာနက် မရှိပါ — ချိတ်ဆက်ပြီး ပြန်ကြိုးစားပါ"

/**
 * Firestore က offline မှာ error မပေးဘဲ cache ထဲက data ပြန်ပေးတယ် — ပထမဆုံး install ပြီး offline ဆိုရင်
 * cache မရှိလို့ **list အလွတ်** ရတယ်။ အဲဒါကို "data မရှိသေးပါ" လို့ ပြရင် မှားတယ် — error ပြရမယ်။
 */
fun offlineErrorOrNull(isEmpty: Boolean, online: Boolean): String? =
    if (isEmpty && !online) OFFLINE_EMPTY_ERROR else null

/** Screen တိုင်းရဲ့ အပေါ်ဆုံး — လိုင်းပြတ်ရင် ပေါ်လာ၊ ပြန်ရရင် ပျောက် */
@Composable
fun OfflineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "အင်တာနက် မရှိပါ — သိမ်းထားတဲ့ data နဲ့ ဒေါင်းလုဒ်တွေကို ပြနေပါတယ်",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
