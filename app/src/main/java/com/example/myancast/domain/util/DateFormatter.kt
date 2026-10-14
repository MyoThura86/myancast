package com.example.myancast.domain.util

import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.TimeZone

private val MYANMAR_MONTHS = arrayOf(
    "ဇန်နဝါရီ", "ဖေဖော်ဝါရီ", "မတ်", "ဧပြီ", "မေ", "ဇွန်",
    "ဇူလိုင်", "သြဂုတ်", "စက်တင်ဘာ", "အောက်တိုဘာ", "နိုဝင်ဘာ", "ဒီဇင်ဘာ"
)

/** Timestamp → "ခုလေးတင်" / "၅ မိနစ် အရင်က" / ... ၊ null ဆိုရင် "" */
fun formatRelativeDate(
    timestamp: Timestamp?,
    now: Long = System.currentTimeMillis()
): String {
    if (timestamp == null) return ""
    return formatRelativeMillis(timestamp.toDate().time, now)
}

/** Test လုပ်ရလွယ်အောင် millis နဲ့ ခွဲထားတယ် */
fun formatRelativeMillis(epochMillis: Long, now: Long): String {
    val diff = now - epochMillis
    if (diff < 60_000) return "ခုလေးတင်"          // အနာဂတ် ရက်စွဲ (diff < 0) လည်း ဒီမှာ ဝင်တယ်
    val min = diff / 60_000
    val hour = min / 60
    val day = hour / 24
    return when {
        min < 60  -> "${min.toMyanmarDigits()} မိနစ် အရင်က"
        hour < 24 -> "${hour.toMyanmarDigits()} နာရီ အရင်က"
        day < 2   -> "မနေ့က"
        day < 30  -> "${day.toMyanmarDigits()} ရက် အရင်က"
        else      -> formatFullDate(epochMillis)
    }
}

/** "၁၅ စက်တင်ဘာ ၂၀၂၅" — Locale မမှီခိုဘူး (Admin ရဲ့ ထုတ်လွှင့်ရက် ခလုတ်ကလည်း သုံး) */
internal fun formatFullDate(epochMillis: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val cal = Calendar.getInstance(zone).apply {
        timeInMillis = epochMillis
    }
    val d = cal.get(Calendar.DAY_OF_MONTH).toMyanmarDigits()
    val m = MYANMAR_MONTHS[cal.get(Calendar.MONTH)]
    val y = cal.get(Calendar.YEAR).toMyanmarDigits()
    return "$d $m $y"
}
