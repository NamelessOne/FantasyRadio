package ru.sigil.fantasyradio.currentstreraminfo;

import org.json.JSONObject;

import java.io.BufferedReader;
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

    private String imageURL = "";
    private String about = "";

    @Inject
    public CurrentStreamInfoService()
    {

    }

    public void updateInfo(ICurrentStreamInfoUpdater callback) {
        new Thread(() -> {
            try {
                URL url = new URL("http://fantasyradionotifications-sigil.rhcloud.com/current");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String resultJson = buffer.toString();

                JSONObject dataJsonObj = new JSONObject(resultJson);
                about = dataJsonObj.getString("about").length() > 0 ? dataJsonObj.getString("about") : "Описание отсутствует";
                imageURL = dataJsonObj.getString("image_url");
                callback.update(about, imageURL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getAbout() {
        return about;
    }
}
