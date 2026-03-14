package com.vitascan.ai.domain.usecases

import com.vitascan.ai.data.models.*
import com.vitascan.ai.data.repository.AuthRepository
import com.vitascan.ai.data.repository.ReportRepository
import com.vitascan.ai.utils.Result
import android.net.Uri
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        if (email.isBlank() || password.isBlank()) return Result.Error("Fields cannot be empty")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Result.Error("Invalid email address")
        return repo.login(email, password)
    }
}

class SignupUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<AuthResponse> {
        if (name.isBlank() || email.isBlank() || password.isBlank())
            return Result.Error("All fields required")
        if (password.length < 8) return Result.Error("Password must be ≥ 8 characters")
        return repo.signup(name, email, password)
    }
}

class UploadReportUseCase @Inject constructor(private val repo: ReportRepository) {
    suspend operator fun invoke(uri: Uri): Result<UploadResponse> = repo.uploadReport(uri)
}

class ExtractDataUseCase @Inject constructor(private val repo: ReportRepository) {
    suspend operator fun invoke(reportId: String): Result<ExtractedData> =
        repo.extractData(reportId)
}

class PredictDiseaseUseCase @Inject constructor(private val repo: ReportRepository) {
    suspend operator fun invoke(
        reportId: String,
        parameters: Map<String, Double>
    ): Result<PredictionResponse> = repo.predictDisease(reportId, parameters)
}

class GetRecommendationsUseCase @Inject constructor(private val repo: ReportRepository) {
    suspend operator fun invoke(
        reportId: String,
        parameters: Map<String, Double>,
        predictions: PredictionResponse
    ): Result<RecommendationResponse> = repo.getRecommendations(reportId, parameters, predictions)
}

class GetHistoryUseCase @Inject constructor(private val repo: ReportRepository) {
    suspend operator fun invoke(): Result<HistoryResponse> = repo.fetchHistory()
    fun localFlow() = repo.getLocalReports()
}

class CompareReportsUseCase @Inject constructor(private val repo: ReportRepository) {
    suspend operator fun invoke(id1: String, id2: String): Result<CompareResponse> =
        repo.compareReports(id1, id2)
}
