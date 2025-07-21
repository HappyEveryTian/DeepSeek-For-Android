package com.caiyu.deepseekdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ChatAdapter(private val list: MutableList<ChatMessage>) : RecyclerView.Adapter<ViewHolder>() {
    // 数据处理器实例
    private val streamProcessor = StreamDataProcessor()

    init {
        // 设置数据消费者
        streamProcessor.setDataConsumer { mergedData ->
            if (list.isNotEmpty()) {
                updateMessageStream(mergedData)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            ChatMessage.VIEW_TYPE_USER -> {
                UserMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.chat_item_request_layout,
                        parent,
                        false))
            }
            ChatMessage.VIEW_TYPE_RESPONSE -> {
                ResponseMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.chat_item_response_layout,
                        parent,
                        false))
            }
            else -> {
                ResponseMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.chat_item_response_layout,
                        parent,
                        false))
            }
        }
    }

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int) = list[position].getItemType()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = list[position]
        val itemTpye = chatMessage.getItemType()
        when (itemTpye) {
            ChatMessage.VIEW_TYPE_USER -> {
                (holder as UserMessageViewHolder).apply {
                    bind((chatMessage as ChatMessage.UserMessage).content)
                }
            }
            else -> {
                (holder as ResponseMessageViewHolder).apply {
                    updateText((chatMessage as ChatMessage.ResponseMessage).content)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val content = payloads.last() as String?
            (holder as ResponseMessageViewHolder).updateText(content)
        }
    }

    fun setData(list: MutableList<ChatMessage>) {
        this.list.clear()
        this.list.addAll(list)
    }

    fun addData(chatMessage: ChatMessage) {
        list.add(chatMessage)
        this.notifyItemInserted(list.size - 1)
    }

    fun addStreamData(chunk: String) {
        streamProcessor.addData(chunk)
    }


    private fun updateMessageStream(chunk: String) {
        (list.lastOrNull() as ChatMessage.ResponseMessage).let { message ->
            synchronized(message) {
                message.content += chunk
                this.notifyItemChanged(list.size - 1, chunk)
            }
        }
    }

    fun release() {
        streamProcessor.release()
    }

    inner class UserMessageViewHolder(view: View) : ViewHolder(view) {
        private val text: TextView = view.findViewById(R.id.chat_request_text)
        private val avatar: ImageView = view.findViewById(R.id.chat_user_avatar)

        init {
            avatar.setBackgroundResource(R.drawable.user)
        }

        fun bind(textContent: String) {
            text.text = textContent
        }
    }

    inner class ResponseMessageViewHolder(view: View) : ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.chat_request_text)
        private val avatar: ImageView = view.findViewById(R.id.chat_user_avatar)

        init {
            avatar.setBackgroundResource(R.drawable.deepseek_icon)
        }

        fun updateText(newContent: String?) {
            newContent?.let {
                text.post {
                    text.text = buildString {
                        if (text.text != null) {
                            append(text.text.toString())
                        }
                        append(it)
                    }
                }
            }
        }
    }
}