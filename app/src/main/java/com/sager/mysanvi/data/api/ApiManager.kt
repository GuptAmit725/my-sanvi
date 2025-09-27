// ApiManager.kt
package com.sager.mysanvi.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiManager {
    private const val SAGER_BASE_URL = "http://10.0.2.2:8000/" // SaGer backend
    private const val MANDII_BASE_URL = "http://10.0.2.2:8001/" // Mandii backend

    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // SaGer Retrofit instance
    private val saGerRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(SAGER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Mandii Retrofit instance
    private val mandiiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(MANDII_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val saGerApiService: SaGerApiService = saGerRetrofit.create(SaGerApiService::class.java)
    val mandiiApiService: MandiiApiService = mandiiRetrofit.create(MandiiApiService::class.java)

    // Mock function for demo purposes
    suspend fun checkUserStatus(phone: String): UserStatusResponse {
        // This is a mock implementation for demo
        // In real app, you would call both APIs and combine results
        return UserStatusResponse(
            phone = phone,
            sager = SaGerUserInfo(
                exists = true,
                user_data = SaGerUserData(
                    id = 1,
                    name = "Demo User",
                    shop_name = "Demo Shop",
                    is_phone_verified = true
                )
            ),
            mandii = MandiiUserInfo(
                exists = true,
                user_data = MandiiUserData(
                    id = 1,
                    name = "Demo User",
                    is_shop = true,
                    otp_verified = true,
                    shop_id = 1
                )
            )
        )
    }
}

// === API INTERFACES ===

// SaGer API interface
interface SaGerApiService {
    // Auth endpoints
    @POST("api/auth/otp/send/")
    suspend fun sendOtp(@Body request: SendOtpRequest): OtpResponse

    @POST("api/auth/otp/verify/")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthResponse

    @POST("api/auth/token/")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // Sales endpoints
    @GET("api/sales/")
    suspend fun getSalesRecords(@Header("Authorization") token: String): List<SalesRecord>

    @POST("api/sales/")
    suspend fun createSalesRecord(
        @Header("Authorization") token: String,
        @Body record: SalesRecordRequest
    ): SalesRecord

    @GET("api/debts/")
    suspend fun getDebts(
        @Header("Authorization") token: String,
        @Query("days") days: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<DebtSummary>

    @GET("api/predictions/")
    suspend fun getPredictions(@Header("Authorization") token: String): List<Prediction>

    @GET("api/daily-summary/")
    suspend fun getDailySummary(
        @Header("Authorization") token: String,
        @Query("days") days: Int = 14,
        @Query("top") top: Int = 10
    ): DailySummaryResponse

    @POST("api/check-user-status/")
    suspend fun checkUserStatus(@Body request: UserStatusRequest): Response<UserStatusResponse>
}

// Mandii API interface
interface MandiiApiService {
    @POST("v1/check-user-status/")
    suspend fun checkUserStatus(@Body request: UserStatusRequest): Response<UserStatusResponse>
}

// === DATA CLASSES ===

// Auth related
data class SendOtpRequest(val phone: String)
data class VerifyOtpRequest(val phone: String, val code: String)
data class LoginRequest(val username: String, val password: String)

data class OtpResponse(
    val detail: String,
    val otp: String? = null
)

data class AuthResponse(
    val access: String,
    val refresh: String,
    val user: User? = null,
    val detail: String? = null
)

data class User(
    val id: Int,
    val username: String,
    val phone: String? = null,
    val name: String? = null,
    val shop_name: String? = null,
    val is_phone_verified: Boolean = false
)

// User Status related
data class UserStatusRequest(val phone: String)

data class UserStatusResponse(
    val phone: String,
    val sager: SaGerUserInfo,
    val mandii: MandiiUserInfo
)

data class SaGerUserInfo(
    val exists: Boolean,
    val user_data: SaGerUserData?
)

data class SaGerUserData(
    val id: Int?,
    val name: String?,
    val shop_name: String?,
    val is_phone_verified: Boolean
)

data class MandiiUserInfo(
    val exists: Boolean,
    val user_data: MandiiUserData?
)

data class MandiiUserData(
    val id: Int?,
    val name: String?,
    val is_shop: Boolean,
    val otp_verified: Boolean,
    val shop_id: Int?
)

// Sales related
data class SalesRecord(
    val id: Int? = null,
    val customer_id: String,
    val date: String,
    val product_bought: String,
    val amount: Double,
    val paid: Boolean,
    val payment_mode: String? = null
)

data class SalesRecordRequest(
    val customer_id: String,
    val date: String,
    val product_bought: String,
    val amount: Double,
    val paid: Boolean = false,
    val payment_mode: String? = null
)

data class DebtSummary(
    val customer_id: String,
    val total_unpaid_amount: Double,
    val oldest_unpaid_date: String,
    val unpaid_count: Int
)

data class Prediction(
    val id: Int,
    val customer_id: String,
    val predicted_product: String,
    val score: Double,
    val created_at: String
)

data class DailySummaryResponse(
    val totals: List<DailyTotal>,
    val products: List<ProductSummary>
)

data class DailyTotal(
    val date: String,
    val total_amount: Double,
    val total_count: Int
)

data class ProductSummary(
    val product: String,
    val sales_count: Int,
    val total_amount: Double
)

// === TOKEN MANAGER ===

object TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    fun getAccessToken(): String? = accessToken
    fun getRefreshToken(): String? = refreshToken

    fun getAuthHeader(): String? {
        return accessToken?.let { "Bearer $it" }
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    fun hasValidTokens(): Boolean {
        return !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()
    }
}