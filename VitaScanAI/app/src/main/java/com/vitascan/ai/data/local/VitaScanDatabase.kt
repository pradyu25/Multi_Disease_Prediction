package com.vitascan.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vitascan.ai.data.local.dao.*
import com.vitascan.ai.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        ReportEntity::class,
        MedicalValueEntity::class,
        PredictionEntity::class,
        RecommendationEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VitaScanDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reportDao(): ReportDao
    abstract fun medicalValueDao(): MedicalValueDao
    abstract fun predictionDao(): PredictionDao
    abstract fun recommendationDao(): RecommendationDao
}
