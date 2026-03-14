package com.vitascan.ai.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import retrofit2.Response

// ─── Network Result wrapper ──────────────────────────────────────────────
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ─── Safe API call helper ─────────────────────────────────────────────────
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { Result.Success(it) }
                ?: Result.Error("Empty response body")
        } else {
            Result.Error(
                message = response.errorBody()?.string() ?: "Unknown error",
                code    = response.code()
            )
        }
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Network error")
    }
}

// ─── Flow extensions ─────────────────────────────────────────────────────
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it.localizedMessage ?: "Unknown error")) }

// ─── Int risk formatting ─────────────────────────────────────────────────
fun Int.toRiskLabel(): String = when {
    this >= 70 -> "High Risk"
    this >= 40 -> "Moderate Risk"
    else -> "Low Risk"
}

fun Int.toRiskColor(
    high:   androidx.compose.ui.graphics.Color = com.vitascan.ai.ui.theme.RiskHigh,
    medium: androidx.compose.ui.graphics.Color = com.vitascan.ai.ui.theme.RiskMedium,
    low:    androidx.compose.ui.graphics.Color = com.vitascan.ai.ui.theme.RiskLow
): androidx.compose.ui.graphics.Color = when {
    this >= 70 -> high
    this >= 40 -> medium
    else -> low
}
