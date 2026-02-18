package com.secretcom.app.di

import android.content.Context
import androidx.room.Room
import com.secretcom.app.BuildConfig
import com.secretcom.app.data.local.dao.AppSettingDao
import com.secretcom.app.data.local.dao.RecordingDao
import com.secretcom.app.data.local.dao.UserDao
import com.secretcom.app.data.local.database.SecretcomDatabase
import com.secretcom.app.data.preferences.TokenManager
import com.secretcom.app.data.remote.api.ApiService
import com.secretcom.app.data.remote.api.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SecretcomDatabase {
        return Room.databaseBuilder(
            context,
            SecretcomDatabase::class.java,
            "secretcom_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(database: SecretcomDatabase): UserDao = database.userDao()

    @Provides
    fun provideRecordingDao(database: SecretcomDatabase): RecordingDao = database.recordingDao()

    @Provides
    fun provideAppSettingDao(database: SecretcomDatabase): AppSettingDao = database.appSettingDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
