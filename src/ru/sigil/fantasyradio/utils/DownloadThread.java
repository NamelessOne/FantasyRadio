package ru.sigil.fantasyradio.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import ru.sigil.fantasyradio.saved.MP3Entity;

public class DownloadThread extends Thread {
    private String fileUrl;
    private String fileDir;
    private String fileName;
    private Context context;
    private Handler finishedHandler;
    private MP3Entity mp3Entity;

    Object lock = new Object();
    private final static String[] ReservedChars = {"|", "\\", "?", "*", "<", "\"",
            ":", ">", "+", "[", "]", "/", "'", "%"};

    public DownloadThread(String fileUrl, String fileDir, String fileName,
                          Context c, Handler h, MP3Entity entity) {
        this.fileDir = fileDir;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.context = c;
        this.finishedHandler = h;
        mp3Entity = entity;
    }

    @Override
    public void run() {
        for (String s : ReservedChars) {
            fileName = fileName.replace(s, "_");
        }
        FileDownloader.setContext(context);
        FileDownloader.DownloadFile(fileUrl, fileDir, fileName);
        mp3Entity.setDirectory(fileDir + fileName);
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("artist", fileName.substring(0, 11));
        b.putString("title", mp3Entity.getTitle());
        b.putString("directory", mp3Entity.getDirectory());
        b.putString("time", mp3Entity.getTime());
        // --------------------------------
        b.putString("URL", fileUrl);
        // --------------------------------
        msg.setData(b);
        finishedHandler.sendMessage(msg);
    }
}
