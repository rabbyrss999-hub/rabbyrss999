package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GroundingSource(
    val title: String,
    val uri: String
)

data class GroundedSearchResponse(
    val query: String,
    val answer: String,
    val searchQueries: List<String>,
    val sources: List<GroundingSource>
)

class GeminiSearchService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiSearchService"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    suspend fun searchWithGoogleGrounding(prompt: String): Result<GroundedSearchResponse> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or is a placeholder.")
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is missing or not configured. Please add your key to the Secrets panel.")
            )
        }

        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        try {
            // Build the JSON request with Google Search tool enabled
            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            parts.put(partObj)
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            // Enable Google Search Grounding tool
            val tools = JSONArray()
            val searchTool = JSONObject()
            searchTool.put("googleSearch", JSONObject())
            tools.put(searchTool)
            root.put("tools", tools)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Request failed code=${response.code}, body=$responseBody")
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            // Parse response JSON
            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No answer returned from Gemini model."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val candidateContent = firstCandidate.optJSONObject("content")
            val candidateParts = candidateContent?.optJSONArray("parts")

            val answerTextBuilder = StringBuilder()
            if (candidateParts != null) {
                for (i in 0 until candidateParts.length()) {
                    val part = candidateParts.getJSONObject(i)
                    val text = part.optString("text")
                    if (text.isNotBlank()) {
                        answerTextBuilder.append(text)
                    }
                }
            }

            val answerText = answerTextBuilder.toString().ifBlank {
                "No text generated."
            }

            // Parse grounding metadata
            val searchQueries = mutableListOf<String>()
            val sources = mutableListOf<GroundingSource>()

            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val webSearchQueries = groundingMetadata.optJSONArray("webSearchQueries")
                if (webSearchQueries != null) {
                    for (i in 0 until webSearchQueries.length()) {
                        searchQueries.add(webSearchQueries.getString(i))
                    }
                }

                val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.getJSONObject(i)
                        val webObj = chunk.optJSONObject("web")
                        if (webObj != null) {
                            val uri = webObj.optString("uri")
                            val title = webObj.optString("title", uri)
                            if (uri.isNotBlank()) {
                                sources.add(GroundingSource(title = title, uri = uri))
                            }
                        }
                    }
                }
            }

            Result.success(
                GroundedSearchResponse(
                    query = prompt,
                    answer = answerText,
                    searchQueries = searchQueries,
                    sources = sources.distinctBy { it.uri }
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini search grounding", e)
            Result.failure(e)
        }
    }
}
