package com.example.myancast.domain.util

import java.util.Calendar
import java.util.TimeZone

// ★ Phase 6 (Admin) — form စစ်ဆေးမှု pure function များ (Android မလို၊ JVM test လုပ်လို့ရ)

private val MP3_MIME_TYPES = setOf(
    "audio/mpeg", "audio/mp3", "audio/mpeg3", "audio/x-mpeg-3", "audio/x-mp3", "audio/x-mpeg"
)

/** `https://…` (သို့) `http://…` ဖြစ်ပြီး host ပါမှ မှန်တယ် — space ပါရင် မမှန် */
fun isValidWebUrl(text: String): Boolean {
    val url = text.trim()
    if (url.isEmpty() || url.any { it.isWhitespace() }) return false
    val match = Regex("^(https?)://([^/?#]+)(.*)$", RegexOption.IGNORE_CASE).matchEntire(url)
        ?: return false
    val host = match.groupValues[2].substringAfter('@').substringBefore(':')
    return host.isNotEmpty() && (host.contains('.') || host.equals("localhost", ignoreCase = true))
}

/**
 * MP3 ဟုတ်မဟုတ် — MIME type ကို အရင်ကြည့်၊ မသိရင် (null / octet-stream) ဖိုင် extension ကို ကြည့်။
 * Server တချို့က MP3 ကို `application/octet-stream` နဲ့ ပို့လို့ extension fallback လိုတယ်။
 */
fun isMp3(mimeType: String?, nameOrUrl: String): Boolean {
    val mime = mimeType?.substringBefore(';')?.trim()?.lowercase()
    if (mime != null && mime in MP3_MIME_TYPES) return true
    val knownOtherType = mime != null && mime.isNotEmpty() &&
        mime != "application/octet-stream" && mime != "binary/octet-stream"
    if (knownOtherType) return false
    val path = nameOrUrl.substringBefore('?').substringBefore('#')
    return path.endsWith(".mp3", ignoreCase = true)
}

/**
 * Admin ရိုက်ထည့်တဲ့ ကြာချိန် → စက္ကန့်။
 * "24:30" → 1470 · "1:02:05" → 3725 · "45" (မိနစ်) → 2700 · မှားရင် null
 * မြန်မာ ကီးဘုတ်နဲ့ ရိုက်တဲ့ "၂၄:၃၀" လည်း ရတယ် (`isDigit`/`toInt` က Unicode ဂဏန်း သိ)။
 */
fun parseDurationInput(text: String): Int? {
    val parts = text.trim().split(':')
    if (parts.any { it.isEmpty() || !it.all(Char::isDigit) || it.length > 4 }) return null
    val nums = parts.map { it.toInt() }
    val seconds = when (nums.size) {
        1 -> nums[0] * 60
        2 -> if (nums[1] < 60) nums[0] * 60 + nums[1] else return null
        3 -> if (nums[1] < 60 && nums[2] < 60) nums[0] * 3600 + nums[1] * 60 + nums[2] else return null
        else -> return null
    }
    return seconds.takeIf { it > 0 }
}

/** စက္ကန့် → form ထဲ ပြမယ့် "24:30" / "1:02:05" (ASCII — ရိုက်ပြင်လို့ရအောင်) */
fun formatDurationInput(seconds: Int): String =
    if (seconds <= 0) "" else formatDuration(seconds)

/**
 * DatePicker (UTC နေ့ချင်း သန်းခေါင်) + TimePicker (ဒေသ စံတော်ချိန်) → epoch millis။
 * DatePicker က ရက်ကို UTC နဲ့ ပေးလို့ ဖုန်းရဲ့ time zone မှာ ပြန်ဆောက်ရတယ် (မဟုတ်ရင် ရက် တစ်ရက် လွဲတတ်)။
 */
fun combineDateAndTime(
    dateUtcMillis: Long,
    hour: Int,
    minute: Int,
    zone: TimeZone = TimeZone.getDefault()
): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtcMillis }
    return Calendar.getInstance(zone).apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
    }.timeInMillis
}

/** epoch millis → DatePicker ပေးရမယ့် UTC သန်းခေါင် (ဖုန်း time zone ရဲ့ ရက်စွဲအတိုင်း) */
fun toDatePickerMillis(epochMillis: Long, zone: TimeZone = TimeZone.getDefault()): Long {
    val local = Calendar.getInstance(zone).apply { timeInMillis = epochMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** TimePicker ရဲ့ အစ တန်ဖိုး — (နာရီ 0–23, မိနစ်) */
fun hourAndMinute(epochMillis: Long, zone: TimeZone = TimeZone.getDefault()): Pair<Int, Int> {
    val cal = Calendar.getInstance(zone).apply { timeInMillis = epochMillis }
    return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
}

/** "၁၅ အောက်တိုဘာ ၂၀၂၆ · ၁၈:၃၀" — ထုတ်လွှင့်ရက် ခလုတ်မှာ ပြ */
fun formatPublishDateTime(epochMillis: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val cal = Calendar.getInstance(zone).apply { timeInMillis = epochMillis }
    val hh = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val mm = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
    val time = "$hh:$mm".map { c -> if (c in '0'..'9') '၀' + (c - '0') else c }.joinToString("")
    return "${formatFullDate(epochMillis, zone)} · $time"
}
