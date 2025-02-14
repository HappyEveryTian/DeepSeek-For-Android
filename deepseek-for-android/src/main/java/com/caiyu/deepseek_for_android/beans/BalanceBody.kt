package com.caiyu.deepseek_for_android.beans

import com.google.gson.annotations.SerializedName

data class BalanceBody(
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("balance_infos") val balanceInfos: List<BalanceInFo>
)

data class BalanceInFo(
    @SerializedName("currency") val currency: String,
    @SerializedName("total_balance") val totalBalance: String,
    @SerializedName("granted_balance") val grantedBalance: String,
    @SerializedName("topped_up_balance") val toppedUpBalance: String,
)