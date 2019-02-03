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
class CurrentStreamInfoService @Inject constructor(): ICurrentStreamInfoService {
    private var imageURL = ""
    private var about = ""

    override fun getInfo() : CurrentStreamInfo {
        try {
            val url = URL(MAIN_URL)

            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = CONNECTION_TIMEOUT
            urlConnection.connect()

            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val dataJsonObj = getJson(urlConnection)
                about = if (dataJsonObj.getString("about").isNotEmpty()) dataJsonObj.getString("about") else "Описание отсутствует"
                imageURL = dataJsonObj.getString("imageURL")
                return CurrentStreamInfo(about, imageURL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val alternateURL = URL(ALTERNATE_URL)

            val alternateUrlConnection = alternateURL.openConnection() as HttpURLConnection
            alternateUrlConnection.requestMethod = "GET"
            alternateUrlConnection.connectTimeout = CONNECTION_TIMEOUT
            alternateUrlConnection.connect()

            val dataJsonObj = getJson(alternateUrlConnection)
            about = if (dataJsonObj.getString("about").isNotEmpty()) dataJsonObj.getString("about") else "Описание отсутствует"
            imageURL = dataJsonObj.getString("image_url")
            return CurrentStreamInfo(about, imageURL)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return CurrentStreamInfo("Описание отсутствует", "")
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
}