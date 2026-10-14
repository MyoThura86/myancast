package com.example.myancast.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.admin.AdminAuthRepository
import com.example.myancast.data.admin.AdminSignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminLoginUiState(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    /** Field တစ်ခုနဲ့ မဆိုင်တဲ့ error (စကားဝှက် မှား / အင်တာနက်) */
    val error: String? = null,
    val isLoading: Boolean = false,
    /** ဝင်ပြီး → Dashboard ကို သွား */
    val signedIn: Boolean = false
)

class AdminLoginViewModel(
    private val auth: AdminAuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminLoginUiState(signedIn = auth.isAdmin.value))
    val state: StateFlow<AdminLoginUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, usernameError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun togglePasswordVisible() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun signIn() {
        val current = _state.value
        if (current.isLoading) return

        val usernameError = if (current.username.isBlank()) "အသုံးပြုသူအမည် ထည့်ပါ" else null
        val passwordError = if (current.password.isEmpty()) "စကားဝှက် ထည့်ပါ" else null
        if (usernameError != null || passwordError != null) {
            _state.update { it.copy(usernameError = usernameError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = auth.signIn(current.username, current.password)
            _state.update {
                if (result == AdminSignInResult.SUCCESS) {
                    // စကားဝှက်ကို memory ထဲ ဆက်မထား
                    it.copy(isLoading = false, password = "", signedIn = true)
                } else {
                    it.copy(isLoading = false, error = result.toMessage())
                }
            }
        }
    }

    companion object {
        fun factory() = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                AdminLoginViewModel(app.adminAuth)
            }
        }
    }
}

/** Pure — login ရလဒ် → အသုံးပြုသူ ဖတ်ရမယ့် မြန်မာ message */
fun AdminSignInResult.toMessage(): String? = when (this) {
    AdminSignInResult.SUCCESS -> null
    AdminSignInResult.WRONG_CREDENTIALS -> "အသုံးပြုသူအမည် (သို့) စကားဝှက် မှားနေပါတယ်"
    AdminSignInResult.TOO_MANY_ATTEMPTS -> "အကြိမ်များစွာ မှားထားလို့ ခဏ ပိတ်ထားပါတယ် — နောက်မှ ပြန်ကြိုးစားပါ"
    AdminSignInResult.NETWORK_ERROR -> "အင်တာနက် ချိတ်ဆက်မှု မရှိပါ — ချိတ်ပြီး ပြန်ကြိုးစားပါ"
    AdminSignInResult.NOT_CONFIGURED ->
        "Firebase Authentication မှာ Email/Password ဝင်ခွင့် မဖွင့်ရသေးပါ"
    AdminSignInResult.UNKNOWN -> "အကောင့်ဝင်လို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ"
}
