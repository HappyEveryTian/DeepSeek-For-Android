package com.caiyu.deepseekdemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.caiyu.deepseek_for_android.beans.Model
import com.caiyu.deepseek_for_android.core.DeepSeekClient
import com.caiyu.deepseekdemo.databinding.ActivityMainBinding
import com.google.gson.Gson
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
    private val handler = Handler(Looper.getMainLooper())
    private val menuTextList = arrayOf("查询余额")
    private val menuIconList = arrayOf(R.drawable.balance_check)
    private val gson = Gson()

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
        mDataBinding.chatBtn.setOnClickListener {
            if (mDataBinding.chatTextarea.text.isNotEmpty()) {
                val message = mDataBinding.chatTextarea.text.toString()
                mDataBinding.chatTextarea.text.clear()
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = client.chat(message)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
                        Log.d("TAG", "result: \n$result")
                    }
                }

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