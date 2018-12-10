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

    override fun updateInfo(callback: ICurrentStreamInfoUpdater) {
        Thread {
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
                    callback.update(about, imageURL)
                    return@Thread
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
                callback.update(about, imageURL)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
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