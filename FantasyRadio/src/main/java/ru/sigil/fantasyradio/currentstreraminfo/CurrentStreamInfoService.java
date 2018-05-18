package ru.sigil.fantasyradio.currentstreraminfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;


/**
 * Created by
 * namelessone on 02.06.17.
 */

public class CurrentStreamInfoService {
    private static final String ALTERNATE_URL = "https://infinite-everglades-80645.herokuapp.com/currentstream";
    private static final String MAIN_URL = "http://31.163.196.172:36484/CurrentStreamInformation/Last";

    private String imageURL = "";
    private String about = "";

    @Inject
    public CurrentStreamInfoService()
    {

    }

    public void updateInfo(ICurrentStreamInfoUpdater callback) {
        new Thread(() -> {
            try {
                URL url = new URL(MAIN_URL);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JSONObject dataJsonObj = getJson(urlConnection);

                    about = dataJsonObj.getString("about").length() > 0 ? dataJsonObj.getString("about") : "Описание отсутствует";
                    imageURL = dataJsonObj.getString("imageURL");
                    callback.update(about, imageURL);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                URL alternateURL = new URL(ALTERNATE_URL);

                HttpURLConnection alternateUrlConnection = (HttpURLConnection) alternateURL.openConnection();
                alternateUrlConnection.setRequestMethod("GET");
                alternateUrlConnection.connect();

                JSONObject dataJsonObj = getJson(alternateUrlConnection);
                about = dataJsonObj.getString("about").length() > 0 ? dataJsonObj.getString("about") : "Описание отсутствует";
                imageURL = dataJsonObj.getString("image_url");
                callback.update(about, imageURL);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JSONObject getJson(HttpURLConnection urlConnection) throws IOException, JSONException
    {
        InputStream inputStream = urlConnection.getInputStream();
        StringBuilder buffer = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        String resultJson = buffer.toString();
        return new JSONObject(resultJson);
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getAbout() {
        return about;
    }
}
