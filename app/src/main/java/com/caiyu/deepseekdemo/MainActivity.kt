package com.caiyu.deepseekdemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.caiyu.deepseek_for_android.BalanceBody
import com.caiyu.deepseek_for_android.DeepSeekClient
import com.caiyu.deepseek_for_android.DeepSeekResponseBody
import com.caiyu.deepseek_for_android.Model
import com.caiyu.deepseekdemo.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.AttachListPopupView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.InterruptedIOException

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private lateinit var mDataBinding: ActivityMainBinding
    private lateinit var client: DeepSeekClient
    private val token = "sk-309dd380f0004e3790d7d4b58bcca28d"
    private val token_silicaonflow = "sk-fgsbouwmdyjyfbiyuyvpbdicvxiimafgeprkqvvivwhprift"
    private val handler = Handler(Looper.getMainLooper())
    private val menuTextList = arrayOf("查询余额")
    private val menuIconList = arrayOf(R.drawable.balance_check)
    private val gson = Gson()

    private lateinit var popupView: AttachListPopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding = ActivityMainBinding.inflate(layoutInflater)
        client = DeepSeekClient(token_silicaonflow)
        setContentView(mDataBinding.root)
        initData()
    }

    private fun initData() {
        mDataBinding.chatBtn.setOnClickListener {
            if (mDataBinding.chatTextarea.text.isNotEmpty()) {
                client.chat_with_siliconflow(mDataBinding.chatTextarea.text.toString(), Model.CHAT_SILICONFLOW, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (e is InterruptedIOException) {
                            handler.post {
                                Toast.makeText(this@MainActivity, "响应超时", Toast.LENGTH_SHORT).show()
                            }
                            call.enqueue(this)
                        }
                        Log.e(tag, "onResponse failed: $e")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.code != 200) {
                            handler.post {
                                Toast.makeText(this@MainActivity, "访问失败", Toast.LENGTH_SHORT).show()
                                Log.e(tag, "onResponse failed: $response")
                            }
                        } else {
                            response.body?.let {
                                val responseBodyJson = it.string()
                                val content = gson.fromJson(responseBodyJson, DeepSeekResponseBody::class.java)
                                val message = content.choices[0].message.content
                                Log.d(tag, "onResponse: \n$message")
                            }
                            handler.post {
                                Toast.makeText(this@MainActivity, response.message, Toast.LENGTH_SHORT).show()
                                mDataBinding.chatTextarea.text.clear()
                            }
                        }
                    }
                })
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
                .asAttachList(menuTextList, menuIconList.toIntArray()) { position, text ->
                    when (position) {
                        0 -> {
                            client.getBalance(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    handler.post {
                                        Toast.makeText(
                                            this@MainActivity,
                                            e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    if (response.code == 200 && response.body != null) {
                                        response.body?.let {
                                            val responseBodyJson = it.string()
                                            val balance = gson.fromJson(responseBodyJson, BalanceBody::class.java)
                                            if (!balance.isAvailable) {
                                                handler.post {
                                                    Toast.makeText(this@MainActivity, "余额不足", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }
                        else -> {

                        }
                    }
                }
        }
        popupView.show()
    }
}