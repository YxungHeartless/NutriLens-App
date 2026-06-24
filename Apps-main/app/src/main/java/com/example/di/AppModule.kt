package com.example.di

import com.example.data.database.AppDatabase
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.MockAuthRepository
import com.example.domain.repository.AuthRepository
import com.example.data.billing.PremiumManager
import com.heartless.foodtrackerglow.BuildConfig
import com.example.data.api.RecipeGeneratorService
import com.example.data.api.UsdaApiService
import com.example.data.api.UsdaInterceptor
import com.example.ui.viewmodel.LoginViewModel
import com.example.ui.viewmodel.AiScannerViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {
    // AuthRepository
    single<AuthRepository> {
        try {
            FirebaseAuthRepository(FirebaseAuth.getInstance())
        } catch (e: Throwable) {
            MockAuthRepository()
        }
    }

    // Room Database
    single { AppDatabase.getDatabase(androidContext()) }

    // DAOs
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().foodItemDao() }
    single { get<AppDatabase>().mealLogDao() }
    single { get<AppDatabase>().recipeCatalogDao() }
    single { get<AppDatabase>().foodLogDao() }

    // Premium Manager
    single { PremiumManager(get(), get()) }

    // AI Services
    single { RecipeGeneratorService() }

    // Network Layer
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val apiKey = BuildConfig.USDA_API_KEY
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1, Protocol.HTTP_2))
            .addInterceptor(logging)
            .addInterceptor(UsdaInterceptor(apiKey))
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(UsdaApiService::class.java)
    }

    single(org.koin.core.qualifier.named("GooglePlacesRetrofit")) {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>(org.koin.core.qualifier.named("GooglePlacesRetrofit")).create(com.example.data.api.GooglePlacesApiService::class.java)
    }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { AiScannerViewModel(get(), get(), get()) }
}
