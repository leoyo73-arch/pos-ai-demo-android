package com.example.posaidemo

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// ── 상담(RAG) ───────────────────────────────────────
data class ChatRequest(val message: String)
data class ChatResponse(
    val answer: String,
    val sources: List<String>? = null          // 서버가 안 보내도 앱이 죽지 않게 nullable
)

// ── 비서(에이전트) ─────────────────────────────────
data class AgentRequest(
    val message: String,
    @SerializedName("session_id") val sessionId: String
)

data class PendingAction(
    val id: Int,
    val kind: String,                           // CANCEL / NOTICE
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("paid_at") val paidAt: String,
    val amount: Int,
    val items: String,
    val detail: String?
) {
    val isCancel get() = kind == "CANCEL"
}

data class AgentResponse(
    val answer: String,
    @SerializedName("pending_actions") val pendingActions: List<PendingAction>? = null
)

data class DecisionRequest(val approve: Boolean)
data class DecisionResponse(val ok: Boolean, val message: String)

interface ChatApi {
    @POST("chat")
    suspend fun chat(@Body req: ChatRequest): ChatResponse

    @POST("agent")
    suspend fun agent(@Body req: AgentRequest): AgentResponse

    @GET("actions")
    suspend fun actions(): List<PendingAction>

    @POST("actions/{id}")
    suspend fun decide(@Path("id") id: Int, @Body req: DecisionRequest): DecisionResponse
}

object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)      // 비서는 여러 단계를 거쳐 오래 걸릴 수 있음
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY   // Logcat에서 'okhttp'로 검색
        })
        .build()

    val api: ChatApi = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChatApi::class.java)
}

/** 서버가 보낸 {"detail": "..."} 오류 문구를 꺼내 사용자에게 보여 줄 문장으로 만든다. */
fun Throwable.toUserMessage(): String {
    if (this is HttpException) {
        val detail = runCatching {
            Gson().fromJson(response()?.errorBody()?.string(), JsonObject::class.java)
                ?.get("detail")?.asString
        }.getOrNull()
        return detail ?: "서버 오류가 발생했어요. (HTTP ${code()})"
    }
    return "서버에 연결하지 못했어요. 서버가 켜져 있는지 확인해 주세요.\n($message)"
}
