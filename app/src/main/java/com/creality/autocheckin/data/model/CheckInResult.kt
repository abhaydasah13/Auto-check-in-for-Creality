package com.creality.autocheckin.data.model

data class CheckInResult(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val continueDays: Int = 0,
    val hasDid: Boolean = false,
    val rewardValue: Int = 0,
    val rawJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
