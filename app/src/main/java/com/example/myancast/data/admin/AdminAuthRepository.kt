package com.example.myancast.data.admin

import kotlinx.coroutines.flow.StateFlow

/** Admin login ရလဒ် — ViewModel က မြန်မာ message ပြောင်းပြ */
enum class AdminSignInResult {
    SUCCESS,
    WRONG_CREDENTIALS,
    TOO_MANY_ATTEMPTS,
    NETWORK_ERROR,
    NOT_CONFIGURED,     // Firebase Console မှာ Email/Password ဝင်ခွင့် မဖွင့်ရသေး
    UNKNOWN
}

/**
 * ★ Phase 6 — Admin login။ ViewModel က Firebase Auth ကို မသိဘူး (test မှာ fake ထည့်)။
 * Login session ကို Firebase က ဖုန်းထဲ သိမ်းထားလို့ app ပြန်ဖွင့်လည်း ဝင်ထားဆဲ။
 */
interface AdminAuthRepository {
    val isAdmin: StateFlow<Boolean>
    suspend fun signIn(username: String, password: String): AdminSignInResult
    fun signOut()
}
