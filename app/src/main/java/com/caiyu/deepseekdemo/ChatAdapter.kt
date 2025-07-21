package com.caiyu.deepseekdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ChatAdapter(private val list: MutableList<ChatMessage>) : RecyclerView.Adapter<ViewHolder>() {
    private var currentResponseIndex: Int = -1


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
        when (val chatMessage = list[position]) {
            is ChatMessage.UserMessage ->
                (holder as UserMessageViewHolder).bind(chatMessage.content)
            is ChatMessage.ResponseMessage ->
                (holder as ResponseMessageViewHolder).bind(chatMessage.content)
            else -> {}
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
        if (chatMessage is ChatMessage.ResponseMessage) {
            currentResponseIndex = list.size - 1
        }
    }

    fun updateMessageStream(chunk: String) {
        if (currentResponseIndex >= 0 && currentResponseIndex < list.size) {
            val message = list[currentResponseIndex]
            if (message is ChatMessage.ResponseMessage) {
                message.content += chunk
                notifyItemChanged(currentResponseIndex, chunk)
            }
        }
    }

    fun release() {
        list.clear()
        currentResponseIndex = -1
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

        fun bind(content: String) {
            text.text = content
        }

        fun updateText(newContent: String?) {
            text.append(newContent)
        }
    }
}