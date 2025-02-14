package com.caiyu.deepseek_for_android.core

import com.caiyu.deepseek_for_android.beans.BalanceBody
import com.caiyu.deepseek_for_android.beans.BasicRequestBody
import com.caiyu.deepseek_for_android.beans.DeepSeekResponseBody
import com.caiyu.deepseek_for_android.beans.Message
import com.caiyu.deepseek_for_android.beans.Model
import com.caiyu.deepseek_for_android.beans.UserMessage
import com.caiyu.deepseek_for_android.utils.ApiConstants
import com.caiyu.deepseek_for_android.utils.Header
import com.caiyu.deepseek_for_android.utils.MediaType
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit


class DeepSeekClient private constructor (
    private var token: String,
    private var model: Model
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun setModel(model: Model) {
        this.model = model
    }

    fun setToken(token: String) {
        this.token = token
    }

    /**
     * 基础的对话接口，采用user角色
     */
    suspend fun chat(message: String): String {
        val messages = ArrayList<Message>()
        messages.add(UserMessage(message))
        return chatWithMuitiplyMessage(messages, model)
    }

    /**
     * 进阶的对话接口，开发者可以自定义传入的Message类型，不限于user角色
     * 注意: Message列表每种Message类型最多出现一次，否则可能会出现不可预料的结果
     */
    suspend fun chatWithMuitiplyMessage(messages: List<Message>, model: Model): String {
        val requestBody = createChatRequestBody(messages, model)
        val request: Request = Request.Builder()
            .url(ApiConstants.API_CHAT_COMPLETIONS)
            .post(requestBody)
            .addHeader(Header.CONTENT_TYPE, "application/json")
            .addHeader(Header.ACCEPT, "application/json")
            .addHeader(Header.AUTHORIZATION, "Bearer $token")
            .build()
        return performNetworkRequest(request) {
            val responseBodyString = gson.fromJson(it, DeepSeekResponseBody::class.java)
            return@performNetworkRequest responseBodyString.choices[0].message.content
        }
    }

    /**
     * siliconflow api 接口。
     * 请确保用户一个silicaonflow账号并且设置正确的token。
     * 该接口功能与chat功能相同，没有回调
     */
    suspend fun chatWithSiliconflow(message: String): String {
        val messages = ArrayList<Message>()
        messages.add(UserMessage(message))
        val requestBody = createChatRequestBody(messages, model)
        val request: Request = Request.Builder()
            .url(ApiConstants.API_CHAT_COMPLETIONS_WITH_SILICONFLOW)
            .post(requestBody)
            .addHeader(Header.CONTENT_TYPE, "application/json")
            .addHeader(Header.AUTHORIZATION, "Bearer $token")
            .build()
        return performNetworkRequest(request) {
            val responseBodyString = gson.fromJson(it, DeepSeekResponseBody::class.java)
            return@performNetworkRequest responseBodyString.choices[0].message.content
        }
    }

    /**
     * deepseek api 账户余额查询, 返回字符串结果
     */
    suspend fun getBalanceStringResponse(): String {
        val request = Request.Builder()
            .url(ApiConstants.API_USER_BALANCE)
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        return performNetworkRequest(request) { balanceString ->
            return@performNetworkRequest balanceFormatOutput(gson.fromJson(balanceString, BalanceBody::class.java))
        }
    }

    suspend fun getBalanceResponse(): BalanceBody? {
        val request = Request.Builder()
            .url(ApiConstants.API_USER_BALANCE)
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        val result = performNetworkRequest(request) {
            return@performNetworkRequest it
        }
        return try {
            gson.fromJson(result, BalanceBody::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun createChatRequestBody(messages: List<Message>, model: Model): RequestBody {
        val body = BasicRequestBody.create(messages, model)
        val jsonBody =  gson.toJson(body, BasicRequestBody::class.java)
        val mediaType = MediaType.APPLICATION_JSON.toMediaTypeOrNull()
        return jsonBody.toRequestBody(mediaType)
    }

    private fun balanceFormatOutput(balanceBody: BalanceBody): String {
        if (!balanceBody.isAvailable) {
            return "余额不足"
        }
        val totalBalance = balanceBody.balanceInfos[0].totalBalance
        val grantedBalance = balanceBody.balanceInfos[0].grantedBalance
        val toppedUpBalance = balanceBody.balanceInfos[0].toppedUpBalance

        val unit =  if (balanceBody.balanceInfos[0].currency == "CNY") "￥" else "$"
        return "总余额: $totalBalance$unit, 未过期的增金余额: $grantedBalance$unit, 充值余额: $toppedUpBalance$unit"
    }

    private suspend fun performNetworkRequest(request: Request, onProcessBodyString: (String) -> String): String = withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                "Unexpected code ${response.code}"
            }
            val bodyString = response.body?.string() ?: throw IOException("Empty response body")
            onProcessBodyString(bodyString)
        } catch (e: IOException) {
            "network error: ${e.message}"
        }
    }

    class Builder {
        private var token: String? = null
        private var model: Model = Model.CHAT
        fun setToken(token: String) = apply {
            this.token = token
        }

        fun setModel(model: Model) = apply {
            this.model = model
        }
        fun build(): DeepSeekClient {
            require(!token.isNullOrEmpty()) {
                "the token has not set"
            }
            return DeepSeekClient(token!!, model)
        }
    }
}