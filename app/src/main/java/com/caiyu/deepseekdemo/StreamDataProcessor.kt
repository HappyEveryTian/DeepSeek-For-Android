package com.caiyu.deepseekdemo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList

class StreamDataProcessor {
    // 数据缓存队列（使用LinkedList实现FIFO）
    private val dataQueue = LinkedList<String>()

    // 队列最大容量（防止OOM）
    private val maxQueueSize = 100

    // 刷新间隔（50ms更新一次）
    private val refreshInterval = 50L

    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 定时刷新任务
    private var refreshJob: Job? = null

    // 数据消费者回调
    private var dataConsumer: ((String) -> Unit)? = null

    init {
        // 启动定时刷新任务
        startRefreshTask()
    }

    // 接收流式数据
    fun addData(chunk: String) {
        // 同步访问队列保证线程安全
        synchronized(dataQueue) {
            dataQueue.add(chunk)

            // 超出容量时丢弃最早的数据（FIFO策略）
            if (dataQueue.size > maxQueueSize) {
                dataQueue.pollFirst()
            }
        }
    }

    // 设置数据消费者
    fun setDataConsumer(consumer: (String) -> Unit) {
        dataConsumer = consumer
    }

    // 启动定时刷新任务
    private fun startRefreshTask() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (isActive) {
                // 等待刷新间隔
                delay(refreshInterval)

                // 合并队列中的数据
                val mergedData = mergeDataFromQueue()
                if (mergedData.isNotBlank()) {
                    // 切换到主线程更新UI
                    withContext(Dispatchers.Main) {
                        dataConsumer?.invoke(mergedData)
                    }
                }
            }
        }
    }

    // 从队列中合并数据
    private fun mergeDataFromQueue(): String {
        val result = StringBuilder()
        synchronized(dataQueue) {
            while (dataQueue.isNotEmpty()) {
                result.append(dataQueue.pollFirst())
            }
        }
        return result.toString()
    }

    // 释放资源
    fun release() {
        refreshJob?.cancel()
        scope.cancel()
    }
}