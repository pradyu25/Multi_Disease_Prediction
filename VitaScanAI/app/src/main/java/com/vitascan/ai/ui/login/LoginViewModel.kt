package com.vitascan.ai.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.repository.AuthRepository
import com.vitascan.ai.domain.usecases.LoginUseCase
import com.vitascan.ai.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val passwordVisible: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(e: String)    { _uiState.value = _uiState.value.copy(email = e, error = null) }
    fun onPasswordChange(p: String) { _uiState.value = _uiState.value.copy(password = p, error = null) }
    fun togglePasswordVisibility()  { _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible) }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val r = loginUseCase(_uiState.value.email, _uiState.value.password)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Result.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = r.message)
                else -> {}
            }
        }
    }

    fun isAlreadyLoggedIn() = authRepo.isLoggedIn()
}
