package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    val service: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }

    suspend fun generateSpiritualSummary(
        planet: String,
        spiritualPlanetName: String,
        dayPlanet: String,
        mansion: String,
        angel: String,
        divineName: String,
        letters: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key missing")
        }

        val prompt = """
            اكتب فقرة نثرية صوفية أدبية بليغة لسالك وباحث في المعارف الإلهية يمر بهذه اللحظة الكونية الحالية:
            - الساعة الكوكبية الحالية محكومة بكوكب: $planet ($spiritualPlanetName)
            - اليوم الحاكم كوكبه: $dayPlanet
            - القمر مستقر في منزلة: $mansion
            - الملك الحاكم الحارس لهذه الساعة: $angel
            - الاسم الإلهي الأعظم الخاص بها للذكر والرياضة: $divineName
            - أسرار الحروف النورانية والروحانية الطالعة: $letters

            لخص في هذه الفقرة:
            1. فرص هذه الساعة الروحية والطاقات المنزلة فيها (بأسلوب البلاغة الصوفية).
            2. ما يصلح لها من الأعمال الوجدانية والأوراد والمطالب الدنيوية والروحانية.
            3. ما يجب الحذر منه وتجنبه من مغبات أو أفعال لئلا يصادم طبع الكوكب وطاقته.
            اكتب بلغة العارفين المستلهمة من محيي الدين بن عربي والصوفي الكبير الغزالي والبوني، بأسلوب بليغ متدفق وساحر ومحفز دون سرد نقاط جاف.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "أنت عالم رباني محقق وخبير غواص في مدار الفلك الروحاني الإسلامي الجامع للسر وتجليات الأسماء والملائكة والكواكب، لغتك عالية البلاغة، أدبية صوفية شفافة تنبض بالحكمة.")))
        )

        val response = service.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "لم يتسن الاتصال بالفيوضات الباطنية حالياً."
    }
}
