package com.caiyu.deepseekdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private lateinit var mDataBinding: ActivityMainBinding
    private val viewModel by lazy { ViewModelProvider(this)[MainActivityViewModel::class.java] }
    private val menuTextList = arrayOf("查询余额")
    private val menuIconList = arrayOf(R.drawable.balance_check)
    private val chatList: MutableList<ChatMessage> = mutableListOf()
    private val adapter = ChatAdapter(chatList)
    private lateinit var popupView: AttachListPopupView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mDataBinding.root)
        initData()

        observers()
    }

    private fun initData() {
        mDataBinding.chatBtn.setOnClickListener { chatClick() }

        mDataBinding.settingsBtn.setOnClickListener { showPopupView() }

        mDataBinding.chatList.layoutManager = LinearLayoutManager(this)
        mDataBinding.chatList.adapter = adapter
    }

    private fun observers() {
        lifecycleScope.launch {
            mDataBinding.chatTextArea.textChanges()
                .collect { text ->
                    viewModel.setText(text.toString())
                }
        }

        lifecycleScope.launch {
            viewModel.chatTextArea.collect { newText ->
                if (mDataBinding.chatTextArea.text.toString() != newText) {
                    mDataBinding.chatTextArea.setText(newText)
                }
            }
        }

    }

    private fun chatClick() {
        if (mDataBinding.chatTextArea.text.isNotEmpty()) {
            val message = viewModel.getText()
            viewModel.clearText()
            adapter.addData(ChatMessage.UserMessage(message))
            val request = viewModel.createChatRequest(listOf(UserMessage(message)))
            adapter.addData(ChatMessage.ResponseMessage(""))
            lifecycleScope.launch {
                try {
                    viewModel.performStreamRequestFlow(request)
                        .collect { content ->
                            delay(30)
                            adapter.updateMessageStream(content)
                        }
                    BubbleMessageToast.show(this@MainActivity, "请求成功", BubbleMessageToast.SUCCESS)
                } catch (e: Exception) {
                    Log.d(tag, "error: ${e.message}")
                    BubbleMessageToast.show(this@MainActivity, e.message ?: "请求失败", BubbleMessageToast.FAILED)
                }
            }
        }
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
                                val result = viewModel.getBalance()
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

    override fun onDestroy() {
        adapter.release()
        super.onDestroy()
    }

    fun EditText.textChanges(): Flow<CharSequence> = callbackFlow {
        val listener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                trySend(s)
            }
        }

        addTextChangedListener(listener)

        awaitClose { removeTextChangedListener(listener) }
    }
}