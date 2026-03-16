package com.vitascan.ai.data.api

import com.vitascan.ai.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ──────────────────────────────────────────────────────────────
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    // ─── Upload ────────────────────────────────────────────────────────────
    @Multipart
    @POST("upload/report")
    suspend fun uploadReport(
        @Part file: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): Response<UploadResponse>

    // ─── Extract ───────────────────────────────────────────────────────────
    @GET("extract/data")
    suspend fun extractData(
        @Query("report_id") reportId: String
    ): Response<ExtractedData>

    // ─── Predict ───────────────────────────────────────────────────────────
    @POST("predict/disease")
    suspend fun predictDisease(
        @Body request: PredictionRequest
    ): Response<PredictionResponse>

    // ─── Recommend ─────────────────────────────────────────────────────────
    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendationRequest
    ): Response<RecommendationResponse>

    // ─── Reports ───────────────────────────────────────────────────────────
    @GET("reports/history")
    suspend fun getHistory(
        @Query("page")  page: Int  = 1,
        @Query("limit") limit: Int = 10
    ): Response<HistoryResponse>

    @GET("reports/compare")
    suspend fun compareReports(
        @Query("report_id_1") reportId1: String,
        @Query("report_id_2") reportId2: String
    ): Response<CompareResponse>
}
