package com.aliJafari.bbarq.ui.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.aliJafari.bbarq.App
import com.aliJafari.bbarq.data.local.AuthStorage
import com.aliJafari.bbarq.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object EnterPhone : LoginUiState()
    object EnterCode : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class Success(val token: String) : LoginUiState()
}

open class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.EnterPhone)
    open val uiState: StateFlow<LoginUiState> = _uiState

    var mobile: String = ""
    var sessionKey: String? = null

    fun sendOtp(phone: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _uiState.value = LoginUiState.Loading
            val res = repo.sendOtp(phone)
            Log.e("DBG", "sendOtp: $res ${res?.message}", )
            if (res?.status == 200) {
                mobile = phone
                sessionKey = res.SessionKey
                _uiState.value = LoginUiState.EnterCode
            } else {
                _uiState.value = LoginUiState.Error(res?.message ?: "Send OTP failed")
            }
        }
    }

    fun verifyOtp(context: Context,code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _uiState.value = LoginUiState.Loading
            val res = repo.verifyOtp(mobile, code)
            if (res?.status == 200 && res.data != null) {
                _uiState.value = LoginUiState.Success(res.data.Token)
                AuthStorage(context).saveToken(res.data.Token)
            } else {
                _uiState.value = LoginUiState.Error(res?.message ?: "Verify failed")
            }
        }
    }
}
