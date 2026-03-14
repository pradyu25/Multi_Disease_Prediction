package com.vitascan.ai.di

import android.content.Context
import androidx.room.Room
import com.vitascan.ai.data.local.VitaScanDatabase
import com.vitascan.ai.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VitaScanDatabase =
        Room.databaseBuilder(context, VitaScanDatabase::class.java, "vitascan.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: VitaScanDatabase): UserDao = db.userDao()
    @Provides fun provideReportDao(db: VitaScanDatabase): ReportDao = db.reportDao()
    @Provides fun provideMedicalValueDao(db: VitaScanDatabase): MedicalValueDao = db.medicalValueDao()
    @Provides fun providePredictionDao(db: VitaScanDatabase): PredictionDao = db.predictionDao()
    @Provides fun provideRecommendationDao(db: VitaScanDatabase): RecommendationDao = db.recommendationDao()
}
