package ru.sigil.fantasyradio.schedule

import android.util.Log
import org.joda.time.LocalDate
import javax.inject.Inject
import org.json.JSONException
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import java.util.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * Created by namelessone
 * on 04.12.18.
 */

private const val URL = "https://www.googleapis.com/calendar/v3/calendars/fantasyradioru@gmail.com/events?key=AIzaSyDam413Hzm4l8GOEEg-NF8w8wdAbUsKEjM&maxResults=50&singleEvents=true&orderBy=startTime"

class ScheduleParser @Inject constructor() {
    @Synchronized
    fun parseSchedule(): List<ScheduleEntity> {
        val scheduleEntitiesCollection = ArrayList<ScheduleEntity>()
        try {
            // -----------------------------------------------
            val ld = LocalDate.now()
            val fmt = DateTimeFormat.forPattern("YYYY'-'MM'-'dd'T'")
            val urlMin = "&timeMin=" + fmt.print(ld) + "00:00:00.000Z"
            val urlMax = ("&timeMax=" + fmt.print(ld.plusDays(3))
                    + "00:00:00.000Z")
            // -----------------------------------------------
            // -------------------------------------
            val jsObject = getJSONFromUrl(URL + urlMin + urlMax)
            //JSONObject feedObject = jsObject.getJSONObject("feed");
            val entryArray = jsObject?.getJSONArray("items") ?: return scheduleEntitiesCollection
            for (i in 0 until entryArray.length()) {
                try {
                    val text: String
                    var imageURL: String? = null
                    val title = entryArray.getJSONObject(i)?.get("summary").toString()
                    //----------------------------------------------
                    val doc = Jsoup.parse(entryArray.getJSONObject(i).get("description").toString().replace("ПОДРОБНЕЕ", ""))
                    val tds = doc.getElementsByTag("td")
                    val imgs = doc.getElementsByTag("img")
                    //Если tds = 0, значит там вообще не тэгов, одна голая надпись
                    if (tds.size > 0) {
                        if (imgs.size > 0) {
                            imageURL = imgs[0].attr("src")
                        }
                        text = tds[tds.size - 1].text()
                    } else {
                        text = doc.text()
                    }
                    // ---------------------------------------------
                    val parser2 = DateTimeFormat.forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss")
                    var startDate = entryArray.getJSONObject(i).getJSONObject("start").get("dateTime").toString()
                    startDate = startDate.substring(0, startDate.indexOf("+"))
                    var endDate = entryArray.getJSONObject(i).getJSONObject("end").get("dateTime").toString()
                    endDate = endDate.substring(0, endDate.indexOf("+"))

                    val se = ScheduleEntity(parser2.parseDateTime(startDate), parser2.parseDateTime(endDate), title, imageURL, text)
                    scheduleEntitiesCollection.add(se)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            scheduleEntitiesCollection.reverse()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scheduleEntitiesCollection
    }

    private fun getJSONFromUrl(url: String): JSONObject? {
        var jObj: JSONObject? = null
        var json = ""
        val sb = StringBuilder()
        // Making HTTP request
        try {
            // defaultHttpClient
            val url2 = URL(url)
            val ucon = url2.openConnection()
            val inputStream = ucon.getInputStream()
            try {

                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { r ->
                    r.lineSequence().forEach {
                        sb.append(it)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            json = sb.toString()
        } catch (e: Exception) {
            Log.e("Buffer Error", "Error converting result " + e.toString())
        }

        // try parse the string to a JSON object
        try {
            jObj = JSONObject(json)
        } catch (e: JSONException) {
            Log.e("JSON Parser", "Error parsing data " + e.toString())
        }

        // return JSON String
        return jObj
    }
}