package com.vitascan.ai.data.local.dao

import androidx.room.*
import com.vitascan.ai.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Upsert
    suspend fun upsertReport(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY uploadDate DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE reportId = :reportId")
    suspend fun getReportById(reportId: String): ReportEntity?

    @Query("DELETE FROM reports WHERE reportId = :reportId")
    suspend fun deleteReport(reportId: String)
}

@Dao
interface MedicalValueDao {
    @Upsert
    suspend fun upsertValues(values: List<MedicalValueEntity>)

    @Query("SELECT * FROM medical_values WHERE reportId = :reportId")
    suspend fun getValuesForReport(reportId: String): List<MedicalValueEntity>

    @Query("""
        SELECT mv.parameterName, mv.value, r.uploadDate
        FROM medical_values mv
        INNER JOIN reports r ON mv.reportId = r.reportId
        ORDER BY r.uploadDate DESC
    """)
    fun getAllValuesWithDate(): Flow<List<MedicalValueWithDate>>
}

data class MedicalValueWithDate(
    val parameterName: String,
    val value: Double?,
    val uploadDate: String
)

@Dao
interface PredictionDao {
    @Upsert
    suspend fun upsertPrediction(prediction: PredictionEntity)

    @Query("SELECT * FROM predictions WHERE reportId = :reportId")
    suspend fun getPredictionForReport(reportId: String): PredictionEntity?

    @Query("SELECT * FROM predictions")
    fun getAllPredictions(): Flow<List<PredictionEntity>>
}

@Dao
interface RecommendationDao {
    @Upsert
    suspend fun upsertRecommendation(recommendation: RecommendationEntity)

    @Query("SELECT * FROM recommendations WHERE reportId = :reportId")
    suspend fun getRecommendationForReport(reportId: String): RecommendationEntity?
}

@Dao
interface UserDao {
    @Upsert
    suspend fun upsertUser(user: com.vitascan.ai.data.local.entities.UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): com.vitascan.ai.data.local.entities.UserEntity?
}
