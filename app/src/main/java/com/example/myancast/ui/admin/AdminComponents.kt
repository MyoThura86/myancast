package com.example.myancast.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myancast.data.network.DATA_SAVER_EXEMPT
import com.example.myancast.domain.util.isValidWebUrl
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.LiveRed
import com.example.myancast.ui.theme.OkGreen

// ★ Phase 6 (Admin) — screen ၂ ခုလုံး သုံးတဲ့ form အစိတ်အပိုင်းများ
// မြန်မာစာ ပါတဲ့ Text အားလုံး body*/title* style (Poppins မှာ မြန်မာ glyph မရှိ)

/** Section ခေါင်းစဉ် + ရှင်းလင်းချက် */
@Composable
fun AdminSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** OutlinedTextField + error / အကူအညီ စာ — label နဲ့ စာသား အကုန် Padauk */
@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supporting: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
        isError = error != null,
        supportingText = (error ?: supporting)?.let { text ->
            { Text(text, style = MaterialTheme.typography.bodySmall) }
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)
    )
}

/** "ဖုန်းထဲက" / "လင့်ခ်" / ("Podcast ပုံ") ရွေးတဲ့ segmented ခလုတ် */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceModeSelector(
    options: List<Pair<SourceMode, String>>,
    selected: SourceMode,
    onSelect: (SourceMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

/**
 * ပုံ ရွေး/လင့်ခ် ထည့်တဲ့ field — preview ပါ။
 * URL mode မှာ ပုံ တကယ် ဖွင့်လို့ရ/မရ ကို [onUrlLoadResult] နဲ့ ပြန်ပြောတယ် (ပျက်နေတဲ့ ပုံ မသိမ်းမိအောင်)။
 *
 * @param fallbackUrl NONE mode မှာ ပြမယ့် ပုံ (အပိုင်း → Podcast ပုံ)
 */
@Composable
fun ImageSourceField(
    field: MediaField,
    onModeChange: (SourceMode) -> Unit,
    onPicked: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onUrlLoadResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    allowNone: Boolean = false,
    fallbackUrl: String = "",
    error: String? = null,
    enabled: Boolean = true
) {
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri.toString())
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val options = buildList {
            if (allowNone) add(SourceMode.NONE to "Podcast ပုံ")
            add(SourceMode.DEVICE to "ဖုန်းထဲက ပုံ")
            add(SourceMode.URL to "ပုံ လင့်ခ်")
        }
        SourceModeSelector(options, field.mode, onModeChange, enabled = enabled)

        Row(verticalAlignment = Alignment.CenterVertically) {
            val previewModel: String? = when (field.mode) {
                SourceMode.NONE -> fallbackUrl.ifBlank { null }
                SourceMode.DEVICE -> field.localUri
                SourceMode.URL -> field.urlText.trim().takeIf(::isValidWebUrl)
            }
            ImagePreview(
                model = previewModel,
                size = 96.dp,
                onResult = { ok -> if (field.mode == SourceMode.URL) onUrlLoadResult(ok) }
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                when (field.mode) {
                    SourceMode.NONE -> Text(
                        text = "အပိုင်း ပုံ မထည့်ရင် Podcast ရဲ့ ပုံကို ပြပါမယ်",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SourceMode.DEVICE -> {
                        OutlinedButton(
                            onClick = {
                                pickImage.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = enabled
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (field.localUri == null) "ပုံ ရွေးမည်" else "ပုံ ပြောင်းမည်",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "1080px JPEG အဖြစ် ချုံ့ပြီး တင်ပါမယ်",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SourceMode.URL -> Text(
                        text = "အင်တာနက်ပေါ်က ပုံ လင့်ခ် (.jpg / .png / .webp)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (field.mode == SourceMode.URL) {
            AdminTextField(
                value = field.urlText,
                onValueChange = onUrlChange,
                label = "ပုံ လင့်ခ်",
                placeholder = "https://…",
                keyboardType = KeyboardType.Uri,
                enabled = enabled,
                error = error
            )
        } else if (error != null) {
            FieldError(error)
        }
    }
}

/**
 * MP3 ရွေး/လင့်ခ် ထည့်ပြီး စစ်ဆေးတဲ့ field။
 * ဖုန်းထဲက ဖိုင် ရွေးတာနဲ့ အလိုအလျောက် စစ်တယ် · လင့်ခ်ကတော့ "စစ်ဆေးမည်" နှိပ် (သို့) သိမ်းချိန်မှာ စစ်။
 */
@Composable
fun AudioSourceField(
    field: MediaField,
    check: AudioCheck,
    onModeChange: (SourceMode) -> Unit,
    onPicked: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onVerifyUrl: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            // Save မနှိပ်ခင် ကြာသွားလည်း ဖိုင် ဖတ်ခွင့် မပျောက်အောင်
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onPicked(uri.toString())
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SourceModeSelector(
            options = listOf(SourceMode.DEVICE to "ဖုန်းထဲက MP3", SourceMode.URL to "MP3 လင့်ခ်"),
            selected = field.mode,
            onSelect = onModeChange,
            enabled = enabled
        )

        when (field.mode) {
            SourceMode.URL -> Row(verticalAlignment = Alignment.Top) {
                AdminTextField(
                    value = field.urlText,
                    onValueChange = onUrlChange,
                    label = "MP3 လင့်ခ်",
                    placeholder = "https://…/episode.mp3",
                    keyboardType = KeyboardType.Uri,
                    enabled = enabled,
                    // စစ်ပြီး မအောင်မြင်ရင် အောက်က AudioCheckRow က အကြောင်းရင်း ပြပြီးသား — ၂ ခါ မပြ
                    error = error.takeIf { check !is AudioCheck.Failed },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onVerifyUrl,
                    enabled = enabled && check != AudioCheck.Checking && isValidWebUrl(field.urlText),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("စစ်ဆေးမည်", style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                OutlinedButton(
                    onClick = { pickAudio.launch(arrayOf("audio/mpeg", "audio/mp3")) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (field.localUri == null) "MP3 ဖိုင် ရွေးမည်" else "တခြား MP3 ဖိုင် ရွေးမည်",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (error != null && check !is AudioCheck.Failed) FieldError(error)
            }
        }

        AudioCheckRow(check = check, fileName = field.localName.takeIf { field.mode == SourceMode.DEVICE })
    }
}

@Composable
private fun AudioCheckRow(check: AudioCheck, fileName: String?) {
    when (check) {
        AudioCheck.Idle -> Unit
        AudioCheck.Checking -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GoldPrimary)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "အသံဖိုင်ကို စစ်ဆေးနေပါတယ်…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is AudioCheck.Ok -> Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AudioFile, contentDescription = null, tint = GoldPrimary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (!fileName.isNullOrBlank()) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = audioSummary(check.info),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Filled.CheckCircle, contentDescription = "MP3 မှန်ကန်ပါတယ်", tint = OkGreen)
            }
        }
        is AudioCheck.Failed -> FieldError(check.message)
    }
}

@Composable
fun FieldError(message: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = LiveRed, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = LiveRed)
    }
}

/** ပုံ preview — ပုံ မရှိ/ပျက်ရင် icon ပြ */
@Composable
fun ImagePreview(
    model: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    onResult: (Boolean) -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (model != null) {
            val context = LocalContext.current
            val request = remember(model) {
                ImageRequest.Builder(context)
                    .data(model)
                    .setParameter(DATA_SAVER_EXEMPT, true)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = "ပုံ preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
                onSuccess = { onResult(true) },
                onError = { onResult(false) }
            )
        }
    }
}

/** ပုံ/အသံ တင်နေ (သို့) သိမ်းနေတုန်း — form အောက်ခြေမှာ ပြ */
@Composable
fun SaveProgressCard(status: SaveStatus, modifier: Modifier = Modifier) {
    if (status == SaveStatus.Idle) return
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            val label = when (status) {
                is SaveStatus.Uploading -> {
                    val percent = (status.fraction * 100).toInt().coerceIn(0, 100)
                    "${status.label} ${percent.toMyanmarDigits()}%"
                }
                SaveStatus.Saving -> "သိမ်းနေပါတယ်…"
                SaveStatus.Deleting -> "ဖျက်နေပါတယ်…"
                SaveStatus.Idle -> ""
            }
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            if (status is SaveStatus.Uploading) {
                LinearProgressIndicator(
                    progress = { status.fraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldPrimary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldPrimary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/** ဖျက်ခြင်း / ထွက်ခြင်း လို ပြန်ပြင်လို့ မရတဲ့ အလုပ် မလုပ်ခင် မေး */
@Composable
fun AdminConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "မလုပ်တော့ပါ",
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (destructive) LiveRed else GoldPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel, style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

/** မသိမ်းရသေးတဲ့ ပြင်ဆင်မှု / တင်နေဆဲ ဖိုင် ရှိရင် ထွက်ခါနီး မေး */
@Composable
fun UnsavedChangesDialog(
    isBusy: Boolean,
    onLeave: () -> Unit,
    onStay: () -> Unit
) {
    AdminConfirmDialog(
        title = if (isBusy) "သိမ်းနေဆဲပါ" else "မသိမ်းရသေးပါ",
        text = if (isBusy) "အခု ထွက်ရင် ဖိုင် တင်တာ ရပ်သွားပြီး မသိမ်းရပါဘူး။"
        else "ပြင်ထားတာတွေ ပျောက်သွားပါမယ်။ ထွက်မှာ သေချာပါသလား?",
        confirmLabel = if (isBusy) "ရပ်ပြီး ထွက်မည်" else "မသိမ်းဘဲ ထွက်မည်",
        dismissLabel = if (isBusy) "ဆက်စောင့်မည်" else "ဆက်ပြင်မည်",
        onConfirm = onLeave,
        onDismiss = onStay,
        destructive = true
    )
}
