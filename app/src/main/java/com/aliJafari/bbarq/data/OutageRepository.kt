package com.aliJafari.bbarq.data

import android.util.Log
import com.aliJafari.bbarq.BillIDNot13Chars
import com.aliJafari.bbarq.BillIDNotFoundException
import com.aliJafari.bbarq.RequestUnsuccessful
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit


class OutageRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    fun sendRequest(billId: String, onResult: (List<Outage>) -> Unit) {
        if (billId.length!=13) throw BillIDNot13Chars()
        val requestBody = FormBody.Builder()
            .add("BillId", billId)
            .build()

        val request = Request.Builder()
            .url("https://khamooshi.tvedc.ir/public/map/v2/GetOutagesListByBillId")
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body.string()
                val json = JSONArray(body)
                parseApiResponse(json.toString()).first().apply {
                    onResult(
                        outages.map {
                            it.toOutage(
                                billId, address
                            )
                        }
                    )
                }

            } else {
                throw RequestUnsuccessful(null,response.message)
            }
        } catch (e: BillIDNotFoundException) {
            e.printStackTrace()
            throw BillIDNotFoundException()
        }catch (e: Exception) {
            e.printStackTrace()
            throw RequestUnsuccessful(e)
        }
    }

    private fun parseApiResponse(jsonString: String): List<ApiResponseModel> {
        val jsonArray = JSONArray(jsonString)
        val result = mutableListOf<ApiResponseModel>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val billId = if (obj.isNull("billId")) {
                throw BillIDNotFoundException()
            } else {
                obj.getString("billId")
            }

            val latitude = obj.getDouble("latitude")
            val longitude = obj.getDouble("longitude")
            val address = obj.getString("address")
            val outagesJson = obj.getJSONArray("outages")
            val outages = mutableListOf<ApiResponseOutageModel>()

            for (j in 0 until outagesJson.length()) {
                val outageObj = outagesJson.getJSONObject(j)
                outages.add(
                    ApiResponseOutageModel(
                        outageNumber = outageObj.getInt("outageNumber"),
                        consumerOutageReason = outageObj.getString("consumerOutageReason"),
                        outageDateTime = outageObj.getString("outageDateTime"),
                        outageDate = outageObj.getString("outageDate"),
                        outageDay = outageObj.getString("outageDay"),
                        outageTime = outageObj.getString("outageTime"),
                        persianApproximateConnecTime = outageObj.getString("persianApproximateConnecTime"),
                        isFutureOutage = outageObj.getBoolean("isFutureOutage")
                    )
                )
            }

            result.add(
                ApiResponseModel(
                    billId = billId,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    outages = outages
                )
            )
        }

        return result
    }

}

data class ApiResponseModel(
    val billId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val outages: List<ApiResponseOutageModel>
)

data class ApiResponseOutageModel(
    val outageNumber: Int,
    val consumerOutageReason: String,
    val outageDateTime: String,
    val outageDate: String,
    val outageDay: String,
    val outageTime: String,
    val persianApproximateConnecTime: String,
    val isFutureOutage: Boolean
)

fun ApiResponseOutageModel.toOutage(billId: String, address: String): Outage {
    return Outage(
        id = outageNumber,
        reason = consumerOutageReason ?: "N/A",
        date = outageDate,
        startTime = outageTime,
        endTime = persianApproximateConnecTime.split(" ")[1],
        billId = billId,
        address = address
    )
}