package com.example.posaidemo

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// 서버(main.py)의 ChatRequest / ChatResponse와 필드 이름이 같아야 합니다.
data class ChatRequest(val message: String)
data class ChatResponse(
    val answer: String,
    val sources: List<String>? = null   // 서버가 안 보내도 앱이 죽지 않게 nullable
)

interface ChatApi {
    @POST("chat")
    suspend fun chat(@Body req: ChatRequest): ChatResponse
}

object ApiClient {
    private val client = OkHttpClient.Builder()
        // LLM 답변은 몇 초 걸릴 수 있어 넉넉하게 잡습니다.
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY   // Logcat에서 요청·응답 확인용
        })
        .build()

    val api: ChatApi = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChatApi::class.java)
}
