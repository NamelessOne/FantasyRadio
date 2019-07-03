package ru.sigil.fantasyradio.currentstreraminfo

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject


private const val ALTERNATE_URL = "https://infinite-everglades-80645.herokuapp.com/currentstream"
private const val MAIN_URL = "http://31.163.196.172:36484/CurrentStreamInformation/Last"
private const val CONNECTION_TIMEOUT = 10000

/**
 * Created by namelessone
 * on 30.11.18.
 */
class CurrentStreamInfoService @Inject constructor() : ICurrentStreamInfoService {
    override fun getInfo(): CurrentStreamInfo {
        return getFromURL(MAIN_URL, "imageURL") ?: getFromURL(ALTERNATE_URL, "image_url")
        ?: CurrentStreamInfo("Описание отсутствует", "")
    }

    @Throws(IOException::class, JSONException::class)
    private fun getJson(urlConnection: HttpURLConnection): JSONObject {
        val inputStream = urlConnection.inputStream
        val buffer = StringBuilder()

        BufferedReader(InputStreamReader(inputStream)).use { r ->
            r.lineSequence().forEach {
                buffer.append(it)
            }
        }

        val resultJson = buffer.toString()
        return JSONObject(resultJson)
    }

    private fun getFromURL(address: String, imageURLTag: String): CurrentStreamInfo? {
        try {
            val urlConnection = URL(address).openConnection() as HttpURLConnection
            urlConnection.apply { requestMethod = "GET" }.apply { connectTimeout = CONNECTION_TIMEOUT }
            urlConnection.connect()

            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val dataJsonObj = getJson(urlConnection)
                val about = if (dataJsonObj.getString("about").isNotEmpty()) dataJsonObj.getString("about") else "Описание отсутствует"
                val imageURL = dataJsonObj.getString(imageURLTag)
                return CurrentStreamInfo(about, imageURL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}