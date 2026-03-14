package com.vitascan.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val createdAt: String
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val reportId: String,
    val userId: String,
    val fileUrl: String,
    val fileType: String,
    val uploadDate: String
)

@Entity(tableName = "medical_values")
data class MedicalValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: String,
    val parameterName: String,
    val value: Double?
)

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey val reportId: String,
    val diabetesRisk: Int,
    val anemiaRisk: Int,
    val heartDiseaseRisk: Int
)

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey val reportId: String,
    val diet: String,
    val exercise: String,
    val doctor: String,
    val lifestyle: String
)
