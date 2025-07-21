package com.caiyu.deepseekdemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.caiyu.bubblemessagetoast.BubbleMessageToast
import com.caiyu.deepseek_for_android.beans.Model
import com.caiyu.deepseek_for_android.beans.UserMessage
import com.caiyu.deepseek_for_android.core.DeepSeekClient
import com.caiyu.deepseekdemo.databinding.ActivityMainBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.AttachListPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private lateinit var mDataBinding: ActivityMainBinding
    private lateinit var client: DeepSeekClient
    private val token by lazy { BuildConfig.DEEPSEEK_API_KEY }
    private val menuTextList = arrayOf("查询余额")
    private val menuIconList = arrayOf(R.drawable.balance_check)
    private val chatList: MutableList<ChatMessage> = mutableListOf()
    private val adapter = ChatAdapter(chatList)
    private lateinit var popupView: AttachListPopupView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding = ActivityMainBinding.inflate(layoutInflater)
        client = DeepSeekClient.Builder()
            .setToken(token)
            .setModel(Model.DeepSeek_R1)
            .build()

        setContentView(mDataBinding.root)
        initData()
    }

    private fun initData() {
        mDataBinding.chatBtn.setOnClickListener { chatClick() }

        mDataBinding.settingsBtn.setOnClickListener { showPopupView() }

        mDataBinding.chatList.layoutManager = LinearLayoutManager(this)
        mDataBinding.chatList.adapter = adapter
    }

    private fun chatClick() {
        if (mDataBinding.chatTextarea.text.isNotEmpty()) {
            val message = mDataBinding.chatTextarea.text.toString()
            mDataBinding.chatTextarea.text.clear()
            adapter.addData(ChatMessage.UserMessage(message))
            val request = client.createChatRequest(listOf(UserMessage(message)))
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    adapter.addData(ChatMessage.ResponseMessage(""))
                }
                var content: String?
                var resoningContent: String?
                client.performStreamRequest(
                    request = request,
                    onData = { response ->
                        content = response.choices.firstOrNull()?.delta?.content
                        resoningContent = response.choices.firstOrNull()?.delta?.reasoningContent
                        Log.d(tag, "receivedContent: $content")
                        Log.d(tag, "receivedReasoningContent: $resoningContent")
                        withContext(Dispatchers.Main) {
                            resoningContent?.let {
                                adapter.addStreamData(it)
                            }
                            content?.let {
                                adapter.addStreamData(it)
                            }
                        }
                    },
                    onError = { error ->
                        Log.d(tag, "error: $error\n")
                        withContext(Dispatchers.Main) {
                            BubbleMessageToast.show(this@MainActivity, error.toString(), BubbleMessageToast.FAILED)
                        }
                    },
                    onComplete = {
                        withContext(Dispatchers.Main) {
                            BubbleMessageToast.show(this@MainActivity, "请求成功", BubbleMessageToast.SUCCESS)
                        }
                    }
                )
            }
        }

        mDataBinding.settingsBtn.setOnClickListener { showPopupView() }
    }

    private fun showPopupView() {
        if (!::popupView.isInitialized) {
            popupView = XPopup.Builder(this)
                .atView(mDataBinding.settingsBtn)
                .hasShadowBg(false)
                .customHostLifecycle(lifecycle)
                .animationDuration(200)
                .asAttachList(menuTextList, menuIconList.toIntArray()) { position, _ ->
                    when (position) {
                        0 -> {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val result = client.getBalanceStringResponse()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else -> {

                        }
                    }
                }
        }
        popupView.show()
    }
}