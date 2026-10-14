package com.example.myancast.data.admin

/**
 * ★ Phase 6 — Admin အကောင့်။
 *
 * App ထဲမှာ "admin" လို့ ရိုက်ပေမဲ့ Firebase Authentication မှာတော့ email/password အကောင့်
 * (`ADMIN_EMAIL`) နဲ့ ဝင်တယ်။ **စကားဝှက်ကို code ထဲ မထည့်ဘူး** — Firebase Console →
 * Authentication → Users မှာ အဲဒီ email နဲ့ အကောင့် ဖန်တီးပြီး စကားဝှက် သတ်မှတ်ရတယ်။
 *
 * Firestore / Storage rules (`firebase/firestore.rules`, `firebase/storage.rules`) ကလည်း ဒီ email ကိုပဲ ရေးခွင့်ပေးတယ် —
 * ဒီ email ပြောင်းရင် rules ထဲကပါ ပြောင်းရမယ်။
 */
object AdminConfig {
    const val USERNAME = "admin"
    const val EMAIL = "admin@myancast.app"

    /**
     * "admin" / " Admin " / "admin@myancast.app" → မှန် (စာလုံးအကြီးအသေး၊ ရှေ့နောက် space မကြည့်)။
     * Console မှာ email နဲ့ အကောင့် ဖန်တီးထားလို့ email အပြည့် ရိုက်တာလည်း လက်ခံရမယ်။
     */
    fun isAdminUsername(username: String): Boolean {
        val input = username.trim()
        return input.equals(USERNAME, ignoreCase = true) || input.equals(EMAIL, ignoreCase = true)
    }
}

/**
 * Admin ရေးတဲ့/တင်တဲ့ အလုပ် မအောင်မြင်ရင် — `message` က user ကို တိုက်ရိုက် ပြလို့ရတဲ့ မြန်မာစာ။
 * ViewModel က Firebase exception type တွေကို မသိရအောင် repository က ဒီ class အဖြစ် ပြောင်းပေးတယ်။
 */
class AdminException(message: String, cause: Throwable? = null) : Exception(message, cause)
