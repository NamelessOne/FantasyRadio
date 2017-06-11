package ru.sigil.fantasyradio.schedule;

import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class ScheduleParser {
    private static final String URL = "https://www.googleapis.com/calendar/v3/calendars/fantasyradioru@gmail.com/events?key=AIzaSyDam413Hzm4l8GOEEg-NF8w8wdAbUsKEjM&maxResults=50&singleEvents=true&orderBy=startTime";
    //https://www.googleapis.com/calendar/v3/calendars/fantasyradioru@gmail.com/events?key=AIzaSyDam413Hzm4l8GOEEg-NF8w8wdAbUsKEjM&maxResults=50&singleEvents=true&orderBy=startTime&timeMin=2014-01-01T00:00:00Z&timeMax=2018-03-24T23:59:59Z
    /**
     * Парсим расписание.
     */

    @Inject
    public ScheduleParser()
    {

    }

    //Это должен быть не параметр, а возвращаемое значение
    public synchronized List<ScheduleEntity> ParseSchedule() {
        List<ScheduleEntity> scheduleEntityesCollection = new ArrayList<>();
        try {
            // -----------------------------------------------
            LocalDate ld = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYY'-'MM'-'dd'T'");
            String urlMin = "&timeMin=" + fmt.print(ld) + "00:00:00.000Z";
            String urlMax = "&timeMax=" + fmt.print(ld.plusDays(3))
                    + "00:00:00.000Z";
            // -----------------------------------------------
            // -------------------------------------
            JSONObject jsObject = getJSONFromUrl(URL + urlMin + urlMax);
            //JSONObject feedObject = jsObject.getJSONObject("feed");
            JSONArray entryArray = jsObject.getJSONArray("items");
            for (int i = 0; i < entryArray.length(); i++) {
                ScheduleEntity se = new ScheduleEntity();
                try {
                    se.setTitle(entryArray.getJSONObject(i).get("summary").toString());
                    //----------------------------------------------
                    Document doc = Jsoup.parse(entryArray.getJSONObject(i).get("description").toString().replace("ПОДРОБНЕЕ", ""));
                    Elements tds = doc.getElementsByTag("td");
                    Elements imgs = doc.getElementsByTag("img");
                    //Если tds = 0, значит там вообще не тэгов, одна голая надпись
                    if (tds.size() > 0) {
                        se.setImageURL(imgs.get(0).attr("src"));
                        se.setText(tds.get(tds.size() - 1).text());
                    } else {
                        se.setText(doc.text());
                    }
                    // ---------------------------------------------
                    DateTimeFormatter parser2 = DateTimeFormat.forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
                    String startDate = entryArray.getJSONObject(i).getJSONObject("start").get("dateTime").toString();
                    startDate = startDate.substring(0, startDate.indexOf("+"));
                    String endDate = entryArray.getJSONObject(i).getJSONObject("end").get("dateTime").toString();
                    endDate = endDate.substring(0, endDate.indexOf("+"));

                    //Добавляем в Schedule Entity
                    se.setStartDate(parser2.parseDateTime(startDate));
                    se.setEndDate(parser2.parseDateTime(endDate));

                    scheduleEntityesCollection.add(se);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Collections.reverse(scheduleEntityesCollection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scheduleEntityesCollection;
    }

    private static JSONObject getJSONFromUrl(String url) {
        JSONObject jObj = null;
        String json = "";
        StringBuilder sb = new StringBuilder();
        // Making HTTP request
        try {
            // defaultHttpClient
            URL url2 = new URL(url);
            URLConnection ucon = url2.openConnection();
            InputStream is = ucon.getInputStream();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"));
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }
}
