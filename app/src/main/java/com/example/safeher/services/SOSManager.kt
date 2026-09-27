package com.example.safeher.services

import android.content.Context
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log

class SOSManager(private val context: Context){
    fun sendSOSMessages(
        contacts: List<String>,
        trackingUrl : String,
        onSmsUnavailable: () -> Unit
    ){
        if (!canSendSms()) {
            onSmsUnavailable()
            return
        }
        val msg = "I need help! Track my live location here: $trackingUrl"

        val smsManager = context.getSystemService(SmsManager::class.java)

        contacts.forEach { phoneNum ->
            val number = phoneNum.trim().let {
                when {
                    it.startsWith("+91") -> it
                    it.length == 10 -> "+91$it"
                    else -> it
                }
            }
            try {
                smsManager.sendTextMessage(
                    number,  // recipient
                    null,         // service center (null = default)
                    msg,      // message body
                    null,         // sent intent
                    null          // delivery intent
                )
            }
            catch (e: Exception){
                Log.e("SafeHer", "Failed to send SMS to $phoneNum: ${e.message}")
            }
        }

    }

    fun sendSafeMessage(
        contacts: List<String>,
        onSuccess: () -> Unit,
        onSmsUnavailable: () -> Unit

    ) {
        if (!canSendSms()) {
            onSmsUnavailable()
            return
        }
        val msg = "I'm safe. Don't worry"
        val smsManager = context.getSystemService(SmsManager::class.java)

        contacts.forEach { phoneNum ->
            try {
                smsManager.sendTextMessage(
                    phoneNum,
                    null,
                    msg,
                    null,
                    null
                )

            } catch (e: Exception) {
                Log.e("SafeHer", "Failed to send safe message: ${e.message}")
            }
        }
        onSuccess()

    }
    private fun canSendSms(): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_NONE &&
                telephonyManager.simState == TelephonyManager.SIM_STATE_READY
    }

}