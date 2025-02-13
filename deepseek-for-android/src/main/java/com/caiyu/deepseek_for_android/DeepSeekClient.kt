package com.caiyu.deepseek_for_android

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


class DeepSeekClient (private val token: String) {
    private val tag = "DeepSeekClient"
    // deep seek api
    private val baseUrl = "https://api.deepseek.com/"
    private val baseUrlBeta = "https://api.deepseek.com/beta/"
    private val baseUrlWithSiliconFlow = "https://api.siliconflow.cn/v1/"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun chat(message: String, callback: Callback) {
        val messages = ArrayList<Message>()
        messages.add(UserMessage(message))
//        messages.add(SystemMessage("you are a helpful assistant"))
        val body = DeepSeekRequestBody.create(messages, Model.CHAT)
        val jsonBody =  gson.toJson(body, DeepSeekRequestBody::class.java)
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url(baseUrl + "chat/completions")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        client.newCall(request).enqueue(callback)
    }

    fun chat_with_siliconflow(message: String, model: Model, callback: Callback) {
        val messages = ArrayList<Message>()
        messages.add(UserMessage(message))
//        messages.add(SystemMessage("you are a helpful assistant"))
        val body = BasicRequestBody.create(messages, model)
        val jsonBody =  gson.toJson(body, BasicRequestBody::class.java)
        Log.d(tag, "requestBody: \n $jsonBody")
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url(baseUrlWithSiliconFlow + "chat/completions")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
//            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        client.newCall(request).enqueue(callback)
    }

    fun getBalance(callback: Callback) {
        val request = Request.Builder()
            .url(baseUrl + "user/balance")
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build();
        client.newCall(request).enqueue(callback)
    }
}