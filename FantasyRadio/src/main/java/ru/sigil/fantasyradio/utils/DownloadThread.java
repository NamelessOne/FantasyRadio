package ru.sigil.fantasyradio.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import javax.inject.Inject;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.saved.MP3Entity;

public class DownloadThread extends Thread {
    private String fileUrl;
    private String fileDir;
    private String fileName;
    private Context context;
    private Handler finishedHandler;
    private MP3Entity mp3Entity;
    private Handler errorHandler;

    Object lock = new Object();
    private final static String[] ReservedChars = {"|", "\\", "?", "*", "<", "\"",
            ":", ">", "+", "[", "]", "/", "'", "%"};

    @Inject
    FileDownloader fileDownloader;

    public DownloadThread(String fileUrl, String fileDir, String fileName,
                          Context c, Handler h, MP3Entity entity, Handler errorHandler) {
        this.fileDir = fileDir;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.context = c;
        this.finishedHandler = h;
        this.errorHandler = errorHandler;
        mp3Entity = entity;
        Bootstrap.INSTANCE.getBootstrap().inject(this);
    }

    @Override
    public void run() {
        for (String s : ReservedChars) {
            fileName = fileName.replace(s, "_");
        }
        if(fileDownloader.DownloadFile(fileUrl, fileDir, fileName, context)) {
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
        else
        {
            errorHandler.sendEmptyMessage(0);
        }
    }
}
