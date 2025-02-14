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
) {
}

class Completion(
    @SerializedName("finish_reason") val finishReason: String,
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: CompletionMessage,
    @SerializedName("logprobs") val logprobs: LogGrobs,
)

data class Usage(
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("prompt_cache_hit_tokens") val promptCacheHitTokens: Int,
    @SerializedName("prompt_cache_miss_tokens") val promptCacheMissTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int,
    @SerializedName("completion_tokens_details") val completionTokensDetails: TokenDetails,
)

data class LogGrobs(
    @SerializedName("content") val content: List<TokenInfo>
)

data class TokenDetails(
    @SerializedName("reasoning_tokens") val reasoningTokens: Int
)

data class TokenInfo(
    @SerializedName("token") val token: String,
    @SerializedName("logprob") val logprob: Number,
    @SerializedName("bytes") val bytes: List<Int>,
    @SerializedName("top_logprobs") val top_logprobs: List<TopLogGrobs>,
)

data class TopLogGrobs(
    @SerializedName("token") val token: String,
    @SerializedName("logprob") val logprob: Number,
    @SerializedName("bytes") val bytes: List<Int>,
)

data class CompletionMessage(
    @SerializedName("content") val content: String,
    @SerializedName("reasoning_content") val reasoningContent: String,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>,
    @SerializedName("role") val role: String
)

data class ToolCall(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("function") val function: ModelFunction,
)

data class ModelFunction(
    @SerializedName("name") val name: String,
    @SerializedName("arguments") val arguments: String
)