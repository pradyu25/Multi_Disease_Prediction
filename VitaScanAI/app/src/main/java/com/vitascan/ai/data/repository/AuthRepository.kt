package com.vitascan.ai.data.repository

import com.vitascan.ai.data.api.ApiService
import com.vitascan.ai.data.local.dao.UserDao
import com.vitascan.ai.data.local.entities.UserEntity
import com.vitascan.ai.data.models.AuthResponse
import com.vitascan.ai.data.models.LoginRequest
import com.vitascan.ai.data.models.SignupRequest
import com.vitascan.ai.utils.Result
import com.vitascan.ai.utils.TokenManager
import com.vitascan.ai.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        val result = safeApiCall { api.login(LoginRequest(email, password)) }
        if (result is Result.Success) {
            persistSession(result.data)
        }
        return result
    }

    suspend fun signup(name: String, email: String, password: String): Result<AuthResponse> {
        val result = safeApiCall { api.signup(SignupRequest(name, email, password)) }
        if (result is Result.Success) {
            persistSession(result.data)
        }
        return result
    }

    private suspend fun persistSession(auth: AuthResponse) {
        tokenManager.saveToken(auth.accessToken)
        tokenManager.saveUserId(auth.user.userId)
        tokenManager.saveUserName(auth.user.name)
        userDao.upsertUser(
            UserEntity(
                userId    = auth.user.userId,
                name      = auth.user.name,
                email     = auth.user.email,
                createdAt = auth.user.createdAt
            )
        )
    }

    fun isLoggedIn() = tokenManager.isLoggedIn()
    fun logout()     = tokenManager.clearAll()
    fun getUserName() = tokenManager.getUserName() ?: "User"
}
