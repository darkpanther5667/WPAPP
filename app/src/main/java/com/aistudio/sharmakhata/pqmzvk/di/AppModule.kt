package com.aistudio.sharmakhata.pqmzvk.di

import android.content.Context
import com.aistudio.sharmakhata.pqmzvk.data.local.AppDatabase
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.get(context)

    @Provides
    fun provideCacheDao(db: AppDatabase) = db.cacheDao()

    @Provides
    fun providePendingDao(db: AppDatabase) = db.pendingDao()

    @Provides
    fun provideItemDao(db: AppDatabase) = db.itemDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase) = db.expenseDao()

    @Provides
    fun providePurchaseDao(db: AppDatabase) = db.purchaseDao()

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiClient.apiService
}
