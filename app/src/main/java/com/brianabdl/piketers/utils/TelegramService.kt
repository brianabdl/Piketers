package com.brianabdl.piketers.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class TelegramService {
    private val client = OkHttpClient()

    suspend fun sendMessage(
        botToken: String,
        chatId: String,
        text: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Base URL for Telegram Bot API
            val apiUrl = "https://api.telegram.org/bot$botToken/sendMessage"

            // Create the form body with parameters
            val formBody = FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", text)
                .add("parse_mode", "Markdown")
                .build()

            // Create the request
            val request = Request.Builder()
                .url(apiUrl)
                .post(formBody)
                .build()

            // Execute the request
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Unexpected response code: ${response.code}")
                    )
                }

                // Parse the response
                val responseBody = response.body?.string() ?: return@withContext Result.failure(
                    IOException("Empty response body")
                )

                val jsonObject = JSONObject(responseBody)
                if (jsonObject.getBoolean("ok")) {
                    return@withContext Result.success("Message sent successfully!")
                } else {
                    return@withContext Result.failure(
                        IOException("Failed to send message: ${jsonObject.optString("description")}")
                    )
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun getChatIdByUsername(
        botToken: String,
        username: String
    ): Result<String?> = withContext(Dispatchers.IO) {
        try {
            // Base URL for Telegram Bot API
            val apiUrl = "https://api.telegram.org/bot$botToken/getUpdates"

            // Create the request
            val request = Request.Builder()
                .url(apiUrl)
                .build()

            // Execute the request
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Unexpected response code: ${response.code}")
                    )
                }

                // Parse the response
                val responseBody = response.body?.string() ?: return@withContext Result.failure(
                    IOException("Empty response body")
                )

                val jsonObject = JSONObject(responseBody)
                if (jsonObject.getBoolean("ok")) {
                    val results = jsonObject.getJSONArray("result")

                    for (i in 0 until results.length()) {
                        val update = results.getJSONObject(i)
                        if (update.has("message")) {
                            val message = update.getJSONObject("message")
                            if (message.has("from")) {
                                val from = message.getJSONObject("from")
                                if (from.has("username") &&
                                    from.getString("username").equals(username.replace("@", ""), ignoreCase = true)) {
                                    return@withContext Result.success(from.getString("id"))
                                }
                            }
                        }
                    }

                    // Username not found
                    return@withContext Result.success(null)
                } else {
                    return@withContext Result.failure(
                        IOException("Failed to get updates: ${jsonObject.optString("description")}")
                    )
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}