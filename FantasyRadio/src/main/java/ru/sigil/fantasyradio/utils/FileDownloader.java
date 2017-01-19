package ru.sigil.fantasyradio.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.inject.Inject;

import ru.sigil.fantasyradio.R;

/**
 * Служит для скачивания файлов из архива. Отображает прогресс в Notification.
 */
public class FileDownloader {

    private final static int BUFFER_SIZE = 4096;
    private NotificationManager notificationManager;
    private final static int DOWNLOAD_NOTIFICATION_ID = 364;
    private NotificationCompat.Builder builder;

    @Inject
    public FileDownloader()
    {

    }
    /**
     * Скачиваем файл с сервера
     *
     * @param fileUrl  URl файла. Например http://fantasyradioru.no-ip.biz/Archive/Day/10-Radio_Fantasy_archive.mp3
     * @param fileDir  Директория для сохранения. Например /mnt/sdcard/fantasyradio/mp3/
     * @param fileName Имя файла. Например 31-Aug-201310-Radio_Fantasy_archive.mp3
     */
    public synchronized boolean DownloadFile(String fileUrl,
                                                 String fileDir, String fileName, Context context) {
        Log.v("fileDir", fileDir);
        Log.v("fileUrl", fileUrl);
        Log.v("fileName", fileName);
        //---------------------------------------------------
        builder = new NotificationCompat.Builder(context)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentTitle(context.getString(R.string.download_started))
                .setContentText(fileName)
                .setSmallIcon(R.drawable.clocks)
                .setAutoCancel(false);
                //.setOngoing(false)
        //-------------------------------------------------
        //-----------------------------------------------------
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build());// !!!
        Bundle bundle = new Bundle();
        bundle.putString("title", fileName);
        Message titleMsg = new Message();
        titleMsg.setData(bundle);
        try {
            URL url = new URL(fileUrl);
            File direct = new File(fileDir);
            direct.mkdirs();
            File myFolder = new File(fileDir + fileName);
            try {
                myFolder.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            myFolder.createNewFile();
            URLConnection ucon = url.openConnection();
            int contentLength = ucon.getContentLength();
            int currentLength = 0;
            int progress;
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
            FileOutputStream fos = new FileOutputStream(myFolder);
            byte[] baf = new byte[BUFFER_SIZE];
            int length;
            int x = 0;
            while ((length = bis.read(baf)) != -1) {
                currentLength += length;
                fos.write(baf, 0, length);
                if (x % 1000 == 0) {
                    progress = currentLength / (contentLength / 100);
                    setNotificationProgress(progress);
                }
                x++;
            }
            setNotificationProgress(100);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);// !!!
        }
        return true;
    }

    /**
     * Устанавливаети прогресс загрузки файла из архива
     *
     * @param progress прогресс загрузки в процентах (0 - 100)
     */
    private void setNotificationProgress(int progress) {
        builder.setProgress(100,progress,false);
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build());
    }

}
