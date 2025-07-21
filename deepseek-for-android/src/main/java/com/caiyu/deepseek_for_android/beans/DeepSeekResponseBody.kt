package com.caiyu.deepseek_for_android.beans

import com.google.gson.annotations.SerializedName

data class DeepSeekResponseBody(
    @SerializedName("id") val id: String,
    @SerializedName("choices") val choices: List<Completion>,
    @SerializedName("created") val created: Int,
    @SerializedName("model") val model: String,
    @SerializedName("system_fingerprint") val systemFingerPrint: String,
    @SerializedName("object") val objectN: String,
    @SerializedName("usage") val usage: Usage,
)

class Completion(
    @SerializedName("delta") val delta: Delta,
    @SerializedName("finish_reason") val finishReason: String,
    @SerializedName("index") val index: Int,
)

data class Usage(
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("prompt_cache_hit_tokens") val promptCacheHitTokens: Int,
    @SerializedName("prompt_cache_miss_tokens") val promptCacheMissTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int,
    @SerializedName("completion_tokens_details") val completionTokensDetails: TokenDetails,
)

data class Delta(
    @SerializedName("content") val content: String?,
    @SerializedName("reasoning_content") val reasoningContent: String?,
    @SerializedName("role") val role: String
)

data class TokenDetails(
    @SerializedName("reasoning_tokens") val reasoningTokens: Int
)