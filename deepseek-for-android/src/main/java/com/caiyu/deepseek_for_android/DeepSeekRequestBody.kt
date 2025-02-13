package com.caiyu.deepseek_for_android

import com.google.gson.annotations.SerializedName

class DeepSeekRequestBody private constructor(
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("model") val model: String,
    @SerializedName("frequency_penalty") val frequencyPenalty: Number = 0,
    @SerializedName("max_tokens") val maxTokens: Int = 4096,
    @SerializedName("presence_penalty") val presencePenalty: Number = 0,
    @SerializedName("response_format") val responseFormat: ModelOutputFormat = ModelOutputFormat.TEXT,
    @SerializedName("stop") val stop: Any? = null,
    @SerializedName("stream") val stream: Boolean = false,
    @SerializedName("stream_options") val streamOptions: StreamOptions? = null,
    @SerializedName("temperature") val temperature: Number = 1,
    @SerializedName("top_p") val topP: Number = 1,
    @SerializedName("tools") val tools: List<Tool>? = null,
    @SerializedName("tool_choice") val toolChoice: Any,
    @SerializedName("logprobs") val logprobs: Boolean = false,
    @SerializedName("top_logprobs") val topLogprobs: Int? = null
) {
    init {
        require(Model.values().any { it.value == model }) {
            "Invalid model name: $model"
        }
        require(frequencyPenalty.toDouble() in -2.0..2.0) {
            "frequency_penalty must be between -2 and 2, but was $frequencyPenalty"
        }
        require(maxTokens in 2..8191) {
            "Value must be greater than 1 and less than 8192, but was $maxTokens."
        }
        require(presencePenalty.toDouble() in -2.0..2.0) {
            "frequency_penalty must be between -2 and 2, but was $presencePenalty"
        }
        if (streamOptions != null) {
            require(stream) {
                "When 'stream_options' is set, 'stream' must be true."
            }
        }
        require(temperature.toDouble() > 0 && temperature.toDouble() <= 2.0) {
            "temperature must be between 0 and 2.0, but was $temperature"
        }
        require(topP.toDouble() > 0 || topP.toDouble() <= 1.0) {
            "topP must be between 0 and 1.0, but was $topP"
        }
        if (tools.isNullOrEmpty()) {
            require(toolChoice == Choice.NONE.value) {
                "When 'tools' is not set, 'tool_choice' must be 'none'"
            }
        }
        if (topLogprobs != null) {
            require(logprobs && topLogprobs in 1..20) {
                "When 'topLogprobs' is set, 'logprobs' must be 'true', and 'topLogprobs' must be between 1 and 20"
            }
        }
    }
    companion object {
        fun create(
            messages: List<Message>,
            model: Model,
            frequencyPenalty: Number = 0,
            maxTokens: Int = 4096,
            presencePenalty: Number = 0,
            responseFormat: ModelOutputFormat = ModelOutputFormat.TEXT,
            stop: StringOrList? = null,
            stream: Boolean = false,
            streamOptions: StreamOptions? = null,
            temperature: Number = 1,
            topP: Number = 1,
            logprobs: Boolean = false,
            topLogprobs: Int? = null
        ): DeepSeekRequestBody {
            return DeepSeekRequestBody(
                messages,
                model.value,
                frequencyPenalty,
                maxTokens,
                presencePenalty,
                responseFormat,
                stop?.getValue(),
                stream,
                streamOptions,
                temperature,
                topP,
                null,
                ToolChoice.ChatCompletionToolChoice(Choice.NONE).getValue(),
                logprobs,
                topLogprobs
            )
        }

        fun create(
            messages: List<Message>,
            model: Model,
            frequencyPenalty: Number = 0,
            maxTokens: Int = 4096,
            presencePenalty: Number = 0,
            responseFormat: ModelOutputFormat = ModelOutputFormat.TEXT,
            stop: StringOrList? = null,
            stream: Boolean = false,
            streamOptions: StreamOptions? = null,
            temperature: Number = 1,
            topP: Number = 1,
            tools: List<Tool>,
            toolChoice: ToolChoice = ToolChoice.ChatCompletionToolChoice(Choice.AUTO),
            logprobs: Boolean = false,
            topLogprobs: Int? = null
        ): DeepSeekRequestBody {
            return DeepSeekRequestBody(
                messages,
                model.value,
                frequencyPenalty,
                maxTokens,
                presencePenalty,
                responseFormat,
                stop?.getValue(),
                stream,
                streamOptions,
                temperature,
                topP,
                tools,
                toolChoice.getValue(),
                logprobs,
                topLogprobs
            )
        }

        fun create(
            messages: List<Message>,
            model: Model,
            frequencyPenalty: Number = 0,
            maxTokens: Int = 4096,
            presencePenalty: Number = 0,
            responseFormat: ModelOutputFormat = ModelOutputFormat.TEXT,
            stop: StringOrList? = null,
            stream: Boolean = false,
            streamOptions: StreamOptions? = null,
            temperature: Number = 1,
            topP: Number = 1,
            tools: List<Tool>,
            toolChoice: ToolChoice.ChatCompletionNamedToolChoice,
            logprobs: Boolean = false,
            topLogprobs: Int? = null
        ): DeepSeekRequestBody {
            return DeepSeekRequestBody(
                messages,
                model.value,
                frequencyPenalty,
                maxTokens,
                presencePenalty,
                responseFormat,
                stop?.getValue(),
                stream,
                streamOptions,
                temperature,
                topP,
                tools,
                toolChoice.getValue(),
                logprobs,
                topLogprobs
            )
        }
    }
}

open class Message(
    @SerializedName("content") open val content: String,
    @SerializedName("role") open val role: String,
    @SerializedName("name") open val name: String? = null,
    @SerializedName("prefix") open val prefix: Boolean? = null,
    @SerializedName("reasoning_content") open val reasoningContent: String? = null,
    @SerializedName("tool_call_id") open val toolCallId: String? = null
)

class SystemMessage(
    content: String,
    name: String? = null
) : Message(content = content, role = "system", name = name, prefix = null, reasoningContent = null, toolCallId = null)

class UserMessage(
    content: String,
    name: String? = null
) : Message(content = content, role = "user", name = name, prefix = null, reasoningContent = null, toolCallId = null)

class AssistantMessage(
    content: String,
    name: String? = null,
    prefix: Boolean? = null,
    reasoningContent: String? = null,
) : Message(content = content, role = "assistant", name = name, prefix = prefix, reasoningContent = reasoningContent, toolCallId = null) {
    init {
        if (!reasoningContent.isNullOrEmpty()) {
            require(prefix == true) {
                "When 'reasoning_content' is set, 'prefix' must be true."
            }
        }
    }
}

class ToolMessage(
    content: String,
    name: String? = null,
    toolCallId: String
) : Message(content = content, role = "tool", name = name, prefix = null, reasoningContent = null, toolCallId = toolCallId)

enum class Model(val value: String) {
    CHAT("deepseek-chat"),
    REASONER("deepseek-reasoner"),
    CHAT_SILICONFLOW("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B")
}

data class ModelOutputFormat private constructor(
    @SerializedName("type") val type: String
) {
    companion object {
        val TEXT = ModelOutputFormat("text")
        val JSON_OBJECT = ModelOutputFormat("json_object")

        fun fromType(type: String): ModelOutputFormat {
            return when (type) {
                "text" -> TEXT
                "json_object" -> JSON_OBJECT
                else -> throw IllegalArgumentException("Invalid output format: $type. Possible values are 'text' or 'json_object'.")
            }
        }
    }
}

sealed class StringOrList {
    data class Single(val values: String) : StringOrList()
    data class Multiple(val values: List<String>) : StringOrList() {
        init {
            require(values.isNotEmpty()) { "List of strings must not be empty." }
        }
    }
    fun getValue(): Any {
        return when (this) {
            is Single -> this.values
            is Multiple -> this.values
        }
    }
}

data class StreamOptions(
    @SerializedName("include_usage") val includeUsage: Boolean
)

sealed class ToolChoice {
    data class ChatCompletionToolChoice(val choice: Choice) : ToolChoice()
    data class ChatCompletionNamedToolChoice(val tool: Tool) : ToolChoice()

    fun getValue(): Any {
        return when(this) {
            is ChatCompletionToolChoice -> this.choice.value
            is ChatCompletionNamedToolChoice -> this.tool
        }
    }
}

data class Tool(
    @SerializedName("type") val type: String,
    @SerializedName("function") val function: Function,
)

enum class Choice(val value: String) {
    NONE("none"),
    AUTO("auto"),
    REQUIRED("required")
}

data class Function(
    val description: String? = null,
    val name: String,
    val parameters: Any? = null
)