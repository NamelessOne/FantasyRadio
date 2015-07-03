package ru.sigil.fantasyradio.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import ru.sigil.fantasyradio.R;

/**
 * Служит для скачивания файлов из архива. Отображает прогресс в Notification.
 */
public abstract class FileDownloader {

    private final static int BUFFER_SIZE = 4096;
    private static Context context;
    private static Notification notification;
    private static NotificationManager notificationManager;
    private final static int DOWNLOAD_NOTIFICATION_ID = 364;

    /**
     * Скачиваем файл с сервера
     *
     * @param fileUrl  URl файла. Например http://fantasyradioru.no-ip.biz/Archive/Day/10-Radio_Fantasy_archive.mp3
     * @param fileDir  Директория для сохранения. Например /mnt/sdcard/fantasyradio/mp3/
     * @param fileName Имя файла. Например 31-Aug-201310-Radio_Fantasy_archive.mp3
     */
    public synchronized static void DownloadFile(String fileUrl,
                                                 String fileDir, String fileName) {
        Log.v("fileDir", fileDir);
        Log.v("fileUrl", fileUrl);
        Log.v("fileName", fileName);
        final Intent intent = new Intent(context, TabActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        notification = new Notification(R.drawable.clocks,
                context.getString(R.string.download_started),
                System.currentTimeMillis());
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        notification.contentView = new RemoteViews(context.getPackageName(),
                R.layout.progress_bar);
        notification.contentIntent = contentIntent;
        notification.contentView.setProgressBar(R.id.status_progress, 100, 0,
                false);
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification);// !!!
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
                    setNotificationProgress(progress, fileName);
                }
                x++;
            }
            setNotificationProgress(100, fileName);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);// !!!
        }
    }

    public static void setContext(Context context) {
        FileDownloader.context = context;
    }

    /**
     * Устанавливаети прогресс загрузки файла из архива
     *
     * @param progress прогресс загрузки в процентах (0 - 100)
     * @param fileName имя файла
     */
    private static void setNotificationProgress(int progress, String fileName) {
        // ================================================
        if (notification.contentView != null) {
            notification.contentView.setProgressBar(R.id.status_progress, 100,
                    progress, false);
        }
        notification.contentView.setTextViewText(R.id.text_progress, fileName);
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification);
    }

}
