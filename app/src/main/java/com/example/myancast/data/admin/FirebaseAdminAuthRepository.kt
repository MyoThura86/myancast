package com.example.myancast.data.admin

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAdminAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AdminAuthRepository {

    private val _isAdmin = MutableStateFlow(auth.currentUser.isAdminUser())
    override val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        // Sign in / out / token သက်တမ်းကုန် — ဘယ်ကနေ ပြောင်းပြောင်း state လိုက်ပြောင်း
        auth.addAuthStateListener { _isAdmin.value = it.currentUser.isAdminUser() }
    }

    override suspend fun signIn(username: String, password: String): AdminSignInResult {
        // "admin" မဟုတ်ရင် network မခေါ်ဘဲ ငြင်း — ဘယ်အချက် မှားလဲ မပြောဘူး (လုံခြုံရေး)
        if (!AdminConfig.isAdminUsername(username)) {
            Log.w(TAG, "signIn rejected locally: username is not \"${AdminConfig.USERNAME}\" / ${AdminConfig.EMAIL}")
            return AdminSignInResult.WRONG_CREDENTIALS
        }

        return try {
            val user = auth.signInWithEmailAndPassword(AdminConfig.EMAIL, password).await().user
            if (user.isAdminUser()) {
                AdminSignInResult.SUCCESS
            } else {
                Log.w(TAG, "signed in as ${user?.email}, which is not ${AdminConfig.EMAIL}")
                auth.signOut()
                AdminSignInResult.WRONG_CREDENTIALS
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // စကားဝှက် မှား — (သို့) Authentication → Users မှာ အကောင့် မရှိ (email enumeration protection ဖွင့်ထားရင် ဒီ error ပဲ ပြန်)
            Log.w(TAG, "signIn failed: ${e.errorCode} — ${e.message}")
            AdminSignInResult.WRONG_CREDENTIALS
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.w(TAG, "signIn failed: ${e.errorCode} — account missing or disabled")
            AdminSignInResult.WRONG_CREDENTIALS   // အကောင့် မရှိ / ပိတ်ထား
        } catch (e: FirebaseTooManyRequestsException) {
            AdminSignInResult.TOO_MANY_ATTEMPTS
        } catch (e: FirebaseNetworkException) {
            AdminSignInResult.NETWORK_ERROR
        } catch (e: FirebaseAuthException) {
            Log.w(TAG, "signIn failed: ${e.errorCode}", e)
            if (e.errorCode == "ERROR_OPERATION_NOT_ALLOWED") AdminSignInResult.NOT_CONFIGURED
            else AdminSignInResult.UNKNOWN
        } catch (e: FirebaseException) {
            Log.w(TAG, "signIn failed: ${e.message}", e)
            if (e.message.orEmpty().contains("CONFIGURATION_NOT_FOUND")) AdminSignInResult.NOT_CONFIGURED
            else AdminSignInResult.UNKNOWN
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val TAG = "AdminAuth"
    }
}

private fun FirebaseUser?.isAdminUser(): Boolean =
    this != null && email.equals(AdminConfig.EMAIL, ignoreCase = true)
