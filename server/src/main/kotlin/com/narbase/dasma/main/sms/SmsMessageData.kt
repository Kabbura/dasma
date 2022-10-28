package com.narbase.dasma.main.sms

@Suppress("ArrayInDataClass")
data class SmsMessageData(
    val message: String,
    val phones: Array<Long>
)
