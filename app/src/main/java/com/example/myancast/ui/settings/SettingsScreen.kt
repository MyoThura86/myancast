package com.example.myancast.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myancast.domain.model.AppSettings
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.MyanCastTheme

/**
 * Setting ပြောင်းတာနဲ့ ဖုန်းထဲ သိမ်းတယ် (★ Phase 6 #1 — app ပိတ်ပြီး ပြန်ဖွင့်လည်း မပျောက်)။
 * State ကို `SettingsRepository` က ကိုင်တယ် — ဒီ screen က ပြရုံ/ပြောင်းခိုင်းရုံ။
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onAdminClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ဆက်တင်",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader("အပြင်အဆင်")
            SettingsToggle(
                title = "ဇော်ဂျီ စာလုံး",
                subtitle = "Unicode စံနှုန်းကိုသာ အသုံးပြုသည်",
                checked = false,
                enabled = false,
                onCheckedChange = { /* ဆုံးဖြတ်ချက် — EncodingUtil.kt ကြည့် */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SettingsToggle(
                title = "အမှောင် အပြင်အဆင်",
                subtitle = if (settings.darkMode) "အမှောင် ဖွင့်ထားသည်" else "အလင်း ဖွင့်ထားသည်",
                checked = settings.darkMode,
                onCheckedChange = { on -> onSettingsChange { it.copy(darkMode = on) } }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // ★ Phase 6 (#4, #5) — mobile data ဈေးကြီးတဲ့ မြန်မာ့ အခြေအနေအတွက်
            SettingsSectionHeader("ဒေတာ နဲ့ ဒေါင်းလုဒ်")
            SettingsToggle(
                title = "ဒေတာ ချွေတာ မုဒ်",
                subtitle = "Mobile data သုံးနေချိန် ပုံအသစ် မဒေါင်းပါ · ဒေါင်းမထားတဲ့ အပိုင်း မဖွင့်ခင် မေးပါမယ်",
                checked = settings.dataSaver,
                onCheckedChange = { on -> onSettingsChange { it.copy(dataSaver = on) } }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SettingsToggle(
                title = "Wi-Fi နဲ့သာ ဒေါင်းလုဒ်လုပ်ရန်",
                subtitle = if (settings.wifiOnlyDownload) "Wi-Fi ရမှ ဒေါင်းပါမယ် — mobile data မကုန်ပါ"
                else "Mobile data နဲ့လည်း ဒေါင်းပါမယ်",
                checked = settings.wifiOnlyDownload,
                onCheckedChange = { on -> onSettingsChange { it.copy(wifiOnlyDownload = on) } }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // ★ Phase 6 — Admin panel ဝင်ပေါက်
            SettingsSectionHeader("စီမံခန့်ခွဲမှု")
            SettingsLink(
                title = "စီမံခန့်ခွဲသူ",
                subtitle = if (isAdmin) "ဝင်ထားပါတယ် — Podcast နဲ့ အပိုင်းများ စီမံရန်"
                else "Podcast နဲ့ အပိုင်းများ တင်ရန်၊ ပြင်ရန် (Admin သာ)",
                icon = Icons.Filled.AdminPanelSettings,
                onClick = onAdminClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(24.dp))
            Text(
                text = "MyanCast Personal · ဗားရှင်း ၁.၀",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = GoldPrimary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = GoldPrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun SettingsLink(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun SettingsScreenPreview() {
    MyanCastTheme {
        SettingsScreen(settings = AppSettings(dataSaver = true), onSettingsChange = {})
    }
}
