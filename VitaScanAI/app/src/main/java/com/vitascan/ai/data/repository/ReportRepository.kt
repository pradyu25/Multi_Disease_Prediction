package com.vitascan.ai.data.repository

import android.net.Uri
import com.vitascan.ai.data.api.ApiService
import com.vitascan.ai.data.local.dao.*
import com.vitascan.ai.data.local.entities.*
import com.vitascan.ai.data.models.*
import com.vitascan.ai.utils.FileUtils
import com.vitascan.ai.utils.Result
import com.vitascan.ai.utils.TokenManager
import com.vitascan.ai.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val api: ApiService,
    private val reportDao: ReportDao,
    private val medicalValueDao: MedicalValueDao,
    private val predictionDao: PredictionDao,
    private val recommendationDao: RecommendationDao,
    private val tokenManager: TokenManager,
    private val fileUtils: FileUtils
) {

    // ─── Upload ────────────────────────────────────────────────────────────
    suspend fun uploadReport(uri: Uri): Result<UploadResponse> {
        val file = fileUtils.getFileFromUri(uri) ?: return Result.Error("Cannot read file")
        val mime = fileUtils.getMimeType(uri)
        val requestFile  = file.asRequestBody(mime.toMediaTypeOrNull())
        val multipartFile = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val userId       = tokenManager.getUserId()
            ?.toRequestBody("text/plain".toMediaTypeOrNull())
            ?: return Result.Error("Not authenticated")

        val result = safeApiCall { api.uploadReport(multipartFile, userId) }
        if (result is Result.Success) {
            reportDao.upsertReport(
                ReportEntity(
                    reportId   = result.data.reportId,
                    userId     = tokenManager.getUserId() ?: "",
                    fileUrl    = result.data.fileUrl,
                    fileType   = mime,
                    uploadDate = System.currentTimeMillis().toString()
                )
            )
        }
        return result
    }

    // ─── Extract ───────────────────────────────────────────────────────────
    suspend fun extractData(reportId: String): Result<ExtractedData> {
        val result = safeApiCall { api.extractData(reportId) }
        if (result is Result.Success) {
            val entities = result.data.parameters.map { (k, v) ->
                MedicalValueEntity(reportId = reportId, parameterName = k, value = v)
            }
            medicalValueDao.upsertValues(entities)
        }
        return result
    }

    // ─── Predict ───────────────────────────────────────────────────────────
    suspend fun predictDisease(reportId: String, parameters: Map<String, Double>): Result<PredictionResponse> {
        val result = safeApiCall { api.predictDisease(PredictionRequest(reportId, parameters)) }
        if (result is Result.Success) {
            predictionDao.upsertPrediction(
                PredictionEntity(
                    reportId        = reportId,
                    diabetesRisk    = result.data.diabetesRisk,
                    anemiaRisk      = result.data.anemiaRisk,
                    heartDiseaseRisk = result.data.heartDiseaseRisk
                )
            )
        }
        return result
    }

    // ─── Recommend ─────────────────────────────────────────────────────────
    suspend fun getRecommendations(
        reportId: String,
        parameters: Map<String, Double>,
        predictions: PredictionResponse
    ): Result<RecommendationResponse> {
        val result = safeApiCall {
            api.getRecommendations(RecommendationRequest(reportId, parameters, predictions))
        }
        if (result is Result.Success) {
            recommendationDao.upsertRecommendation(
                RecommendationEntity(
                    reportId  = reportId,
                    diet      = result.data.diet,
                    exercise  = result.data.exercise,
                    doctor    = result.data.doctor,
                    lifestyle = result.data.lifestyle
                )
            )
        }
        return result
    }

    // ─── History (local-first) ─────────────────────────────────────────────
    fun getLocalReports(): Flow<List<ReportEntity>> = reportDao.getAllReports()

    suspend fun fetchHistory(): Result<HistoryResponse> = safeApiCall { api.getHistory() }

    // ─── Compare ───────────────────────────────────────────────────────────
    suspend fun compareReports(id1: String, id2: String): Result<CompareResponse> =
        safeApiCall { api.compareReports(id1, id2) }

    suspend fun getLocalPrediction(reportId: String): PredictionEntity? =
        predictionDao.getPredictionForReport(reportId)

    suspend fun getLocalRecommendation(reportId: String): RecommendationEntity? =
        recommendationDao.getRecommendationForReport(reportId)

    fun getAllMedicalValuesWithDate(): Flow<List<com.vitascan.ai.data.local.dao.MedicalValueWithDate>> =
        medicalValueDao.getAllValuesWithDate()
}
