package com.vitascan.ai.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.domain.usecases.SignupUseCase
import com.vitascan.ai.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signupUseCase: SignupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(v: String)            { _uiState.value = _uiState.value.copy(name = v, error = null) }
    fun onEmailChange(v: String)           { _uiState.value = _uiState.value.copy(email = v, error = null) }
    fun onPasswordChange(v: String)        { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, error = null) }

    fun signup() {
        val s = _uiState.value
        if (s.password != s.confirmPassword) {
            _uiState.value = s.copy(error = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            when (val r = signupUseCase(s.name, s.email, s.password)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Result.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = r.message)
                else -> {}
            }
        }
    }
}
