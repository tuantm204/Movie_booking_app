package com.example.movie_booking_app.utils

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import java.util.Date

object ZaloPayUtils {
    private const val APP_ID = 553
    private const val MAC_KEY = "9phuAOYhan4urywHTh0ndEXiV3pKHr5Q"
    private const val URL_CREATE_ORDER = "https://sandbox.zalopay.com.vn/v001/tpe/createorder"

    // Hàm tạo đơn hàng ZaloPay
    suspend fun createOrder(amount: Int): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val appTime = Date().time.toString()
            val appTransId = Helpers.getAppTransId()
            val appUser = "Android_Demo"
            val embedData = "{}"
            val items = "[]"
            val bankCode = "zalopayapp"
            val description = "Merchant pay for order #$appTransId"
            val inputHMac = "$APP_ID|$appTransId|$appUser|$amount|$appTime|$embedData|$items"
            val mac = Helpers.getMac(MAC_KEY, inputHMac)

            val formBody = FormBody.Builder()
                .add("appid", APP_ID.toString())
                .add("appuser", appUser)
                .add("apptime", appTime)
                .add("amount", amount.toString())
                .add("apptransid", appTransId)
                .add("embeddata", embedData)
                .add("item", items)
                .add("bankcode", bankCode)
                .add("description", description)
                .add("mac", mac)
                .build()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(URL_CREATE_ORDER)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && body != null) {
                return@withContext JSONObject(body)
            } else {
                Log.e("ZaloPayUtils", "Create order failed: $body")
            }
        } catch (e: Exception) {
            Log.e("ZaloPayUtils", "Exception: ${e.message}", e)
        }
        null
    }

    // Hàm gọi ZaloPay SDK để thanh toán
    fun payOrder(activity: Activity, token: String, callback: (success: Boolean, error: String?) -> Unit) {
        ZaloPaySDK.getInstance().payOrder(activity, token, "moviebookingapp://app", object : PayOrderListener {
            override fun onPaymentSucceeded(transactionId: String?, transToken: String?, appTransID: String?) {
                callback(true, null)
            }
            override fun onPaymentCanceled(zpTransToken: String?, appTransID: String?) {
                callback(false, "User canceled")
            }
            override fun onPaymentError(zaloPayError: ZaloPayError?, zpTransToken: String?, appTransID: String?) {
                callback(false, "Payment error: $zaloPayError")
            }
        })
    }
}

// Helpers cho HMAC và AppTransId
object Helpers {
    private var transIdDefault = 1
    @Synchronized
    fun getAppTransId(): String {
        if (transIdDefault >= 100000) transIdDefault = 1
        transIdDefault += 1
        val format = java.text.SimpleDateFormat("yyMMdd_hhmmss")
        val timeString = format.format(Date())
        return String.format("%s%06d", timeString, transIdDefault)
    }
    fun getMac(key: String, data: String): String {
        return HMacUtil.hmacSha256Hex(key, data)
    }
}

// HMAC SHA256 util
object HMacUtil {
    fun hmacSha256Hex(key: String, data: String): String {
        try {
            val hmacSHA256 = javax.crypto.Mac.getInstance("HmacSHA256")
            val secretKey = javax.crypto.spec.SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            hmacSHA256.init(secretKey)
            val hash = hmacSHA256.doFinal(data.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
} 