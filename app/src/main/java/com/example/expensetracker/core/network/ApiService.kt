package com.example.expensetracker.core.network

import com.example.expensetracker.data.models.CategoryListResponse
import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.models.ErrorResponse
import com.example.expensetracker.data.models.ForgotPasswordRequest
import com.example.expensetracker.data.models.ForgotPasswordResponse
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpRequest
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpResponse
import com.example.expensetracker.data.models.LoginRequest
import com.example.expensetracker.data.models.LoginResponse
import com.example.expensetracker.data.models.ScanInvoiceApiResponse
import com.example.expensetracker.data.models.RegisterRequest
import com.example.expensetracker.data.models.RegisterResponse
import com.example.expensetracker.data.models.ResetPasswordRequest
import com.example.expensetracker.data.models.UpdateNameRequest
import com.example.expensetracker.data.models.ProfileApiResponse
import com.example.expensetracker.data.models.UserProfileResponse
import com.example.expensetracker.data.models.VerifyOtpRequest
import com.example.expensetracker.data.models.VerifyOtpResponse
import com.example.expensetracker.features.transaction.*
import com.example.expensetracker.features.wallet.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ApiService — Interface định nghĩa các API endpoint.
 */
interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register/send-otp")
    suspend fun sendOtp(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/v1/auth/register/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("api/v1/auth/forgot-password/send-otp")
    suspend fun forgotPasswordSendOtp(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/v1/auth/forgot-password/verify-otp")
    suspend fun forgotPasswordVerifyOtp(@Body request: ForgotPasswordVerifyOtpRequest): Response<ForgotPasswordVerifyOtpResponse>

    @POST("api/v1/auth/forgot-password/resend-otp")
    suspend fun forgotPasswordResendOtp(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ForgotPasswordResponse>

    // ─── User Profile ────────────────────────────────────────────────────────

    @GET("api/v1/user/profile")
    suspend fun getProfile(): Response<ProfileApiResponse>

    @PATCH("api/v1/user/update-name")
    suspend fun updateName(@Body request: UpdateNameRequest): Response<ProfileApiResponse>

    @Multipart
    @PATCH("api/v1/user/update-avatar")
    suspend fun updateAvatar(@Part avatar: MultipartBody.Part?): Response<ProfileApiResponse>

    @PATCH("api/v1/user/update-fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<Void>

    // ─── Categories ──────────────────────────────────────────────────────────

    @GET("api/v1/categories")
    suspend fun getCategories(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("type") type: String
    ): Response<CategoryListResponse>

    @Multipart
    @POST("api/v1/categories")
    suspend fun createCategory(
        @Part("name") name: RequestBody,
        @Part("type") type: RequestBody,
        @Part icon: MultipartBody.Part?
    ): Response<CategoryItem>

    @Multipart
    @PATCH("api/v1/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Part("name") name: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part icon: MultipartBody.Part?
    ): Response<CategoryItem>

    @DELETE("api/v1/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<ErrorResponse>

    @GET("ping")
    suspend fun ping(): Any

    // ─── Wallet ──────────────────────────────────────────────────────────────

    @GET("api/v1/wallets")
    suspend fun getWallets(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20,
        @Query("sort") sort: String? = null,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<WalletListResponse>

    @POST("api/v1/wallets")
    suspend fun createWallet(@Body request: WalletCreateRequest): Response<WalletModel>

    @PATCH("api/v1/wallets/{id}")
    suspend fun updateWallet(
        @Path("id") id: Int,
        @Body request: WalletUpdateRequest
    ): Response<WalletModel>

    @DELETE("api/v1/wallets/{id}")
    suspend fun deleteWallet(@Path("id") id: Int): Response<WalletDeleteResponse>
    // ─── Transaction ─────────────────────────────────────────────────────────

    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int = Int.MAX_VALUE,
        @Query("sort") sort: String? = "date_desc",
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("wallet_id") walletId: Int? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("source") source: String? = null
    ): Response<TransactionListResponse>

    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Body request: TransactionCreateRequest
    ): Response<TransactionModel>

    @Multipart
    @POST("api/v1/transactions/scan-invoice")
    suspend fun scanInvoice(
        @Part image: MultipartBody.Part
    ): Response<ScanInvoiceApiResponse>

    @Multipart
    @PATCH("api/v1/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Int,
        @Part("wallet_id") walletId: RequestBody? = null,
        @Part("category_id") categoryId: RequestBody? = null,
        @Part("amount") amount: RequestBody? = null,
        @Part("transaction_date") transactionDate: RequestBody? = null,
        @Part("note") note: RequestBody? = null,
        @Part("currency") currency: RequestBody? = null,
        @Part("status") status: RequestBody? = null,
        @Part receiptImage: MultipartBody.Part? = null
    ): Response<TransactionModel>

    @DELETE("api/v1/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Void>

    // ─── Budget ──────────────────────────────────────────────────────────────

    @GET("api/v1/budgets")
    suspend fun getBudgets(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<com.example.expensetracker.features.plan.BudgetListResponse>

    @GET("api/v1/budgets/{id}")
    suspend fun getBudgetDetail(
        @Path("id") id: Int
    ): Response<com.example.expensetracker.features.plan.BudgetDetailResponse>

    @POST("api/v1/budgets")
    suspend fun createBudget(
        @Body request: com.example.expensetracker.features.plan.BudgetCreateRequest
    ): Response<com.example.expensetracker.features.plan.BudgetModel>

    @PATCH("api/v1/budgets/{id}/complete")
    suspend fun completeBudget(
        @Path("id") id: Int
    ): Response<com.example.expensetracker.features.plan.BudgetModel>

    @PATCH("api/v1/budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: Int,
        @Body request: com.example.expensetracker.features.plan.BudgetUpdateRequest
    ): Response<com.example.expensetracker.features.plan.BudgetModel>

    // ─── Statistic ──────────────────────────────────────────────────────────────

    @GET("api/v1/statistics/general")
    suspend fun getStatisticsGeneral(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<com.example.expensetracker.data.models.GeneralStatisticsResponse>

    @GET("api/v1/statistics/by-category")
    suspend fun getStatisticsByCategory(
        @Query("type") type: String, // INCOME | EXPENSE
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<com.example.expensetracker.data.models.StatisticsByCategoryResponse>

    @GET("api/v1/statistics/trend")
    suspend fun getStatisticsTrend(
        @Query("period") period: String, // daily | monthly
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<com.example.expensetracker.data.models.StatisticsTrendResponse>

    @GET("api/v1/statistics/expense-to-balance-ratio")
    suspend fun getStatisticsExpenseToBalanceRatio(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<com.example.expensetracker.data.models.StatisticsExpenseToBalanceRatioResponse>

    @GET("api/v1/statistics/income-vs-expense")
    suspend fun getStatisticsIncomeVsExpense(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<com.example.expensetracker.data.models.StatisticsIncomeVsExpenseResponse>

}
