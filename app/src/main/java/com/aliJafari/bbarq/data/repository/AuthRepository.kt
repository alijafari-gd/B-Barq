package com.aliJafari.bbarq.data.repository

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthRepository {

    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    fun sendOtp(mobile: String): OtpSendResponse? {
        val body = JSONObject().put("mobile", mobile).toString().toRequestBody(jsonType)

        val request = Request.Builder()
            .url("https://uiapi.saapa.ir/api/otp/sendCode")
            .post(body)
            .addHeader("Content-Type", "text/plain")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val json = JSONObject(response.body?.string() ?: return null)
            return OtpSendResponse(
                status = json.getInt("status"),
                SessionKey = json.optString("SessionKey"),
                message = json.getString("message"),
                data = json.optString("data")
            )
        }
    }

    fun verifyOtp(mobile: String, code: String): OtpVerifyResponse? {
        val body = JSONObject()
            .put("mobile", mobile)
            .put("code", code)
            .put("request_source", 5)
            .put("device_token", "")
            .toString()
            .toRequestBody(jsonType)

        val request = Request.Builder()
            .url("https://uiapi.saapa.ir/api/otp/verifyCode")
            .post(body)
            .addHeader("Content-Type", "text/plain")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val json = JSONObject(response.body?.string() ?: return null)

            val data = json.optJSONObject("data")?.let {
                VerifyData(
                    Token = it.getString("Token"),
                    Type = it.getString("Type")
                )
            }

            return OtpVerifyResponse(
                status = json.getInt("status"),
                SessionKey = json.optString("SessionKey"),
                message = json.getString("message"),
                data = data
            )
        }
    }
}
data class OtpSendResponse(
    val status: Int,
    val SessionKey: String?,
    val message: String,
    val data: String?
)

data class OtpVerifyResponse(
    val status: Int,
    val SessionKey: String?,
    val message: String,
    val data: VerifyData?
)

data class VerifyData(
    val Token: String,
    val Type: String
)

