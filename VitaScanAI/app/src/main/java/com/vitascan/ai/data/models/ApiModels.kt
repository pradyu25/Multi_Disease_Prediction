package com.vitascan.ai.data.models

import com.google.gson.annotations.SerializedName

// ─── Auth ───────────────────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("user_id") val userId: String,
    val name: String,
    val email: String,
    @SerializedName("created_at") val createdAt: String
)

// ─── Report Upload ───────────────────────────────────────────────────────
data class UploadResponse(
    @SerializedName("report_id") val reportId: String,
    @SerializedName("file_url")  val fileUrl: String,
    val message: String
)

// ─── Extraction ─────────────────────────────────────────────────────────
data class ExtractedData(
    @SerializedName("report_id")         val reportId: String,
    val parameters: Map<String, Double?>,
    @SerializedName("detected_type")     val detectedType: String   // pdf | docx | image
)

// ─── Prediction ─────────────────────────────────────────────────────────
data class PredictionRequest(
    @SerializedName("report_id")  val reportId: String,
    val parameters: Map<String, Double>
)

data class PredictionResponse(
    @SerializedName("report_id")          val reportId: String,
    @SerializedName("diabetes_risk")      val diabetesRisk: Int,
    @SerializedName("anemia_risk")        val anemiaRisk: Int,
    @SerializedName("heart_disease_risk") val heartDiseaseRisk: Int,
    val confidence: Map<String, Double>
)

// ─── Recommendation ─────────────────────────────────────────────────────
data class RecommendationRequest(
    @SerializedName("report_id")   val reportId: String,
    val parameters: Map<String, Double>,
    val predictions: PredictionResponse
)

data class RecommendationResponse(
    @SerializedName("report_id") val reportId: String,
    val diet: String,
    val exercise: String,
    val doctor: String,
    val lifestyle: String
)

// ─── History ─────────────────────────────────────────────────────────────
data class ReportSummary(
    @SerializedName("report_id")   val reportId: String,
    @SerializedName("file_url")    val fileUrl: String,
    @SerializedName("upload_date") val uploadDate: String,
    @SerializedName("file_type")   val fileType: String,
    val predictions: PredictionResponse?
)

data class HistoryResponse(
    val reports: List<ReportSummary>,
    val total: Int
)

// ─── Comparison ─────────────────────────────────────────────────────────
data class CompareResponse(
    @SerializedName("report_id_previous") val previousReportId: String,
    @SerializedName("report_id_current")  val currentReportId: String,
    val changes: List<ParameterChange>
)

data class ParameterChange(
    val parameter: String,
    val previous: Double?,
    val current: Double?,
    val change: Double?,
    val status: String   // improved | worsened | stable | new
)

// ─── API generic wrapper ─────────────────────────────────────────────────
data class ApiError(
    val detail: String
)
