package ru.sigil.fantasyradio.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import javax.inject.Inject;

import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.TabHoster;
import ru.sigil.fantasyradio.dagger.Bootstrap;

public class FantasyRadioNotificationManager {
    public boolean isShown = false;
    private Context context;
    public int MAIN_NOTIFICATION_ID = 36484;
    public NotificationManager notificationManager;
    private Notification notification;
    @Inject
    IPlayer player;

    public FantasyRadioNotificationManager(Context context)
    {
        this.context = context;
        Bootstrap.INSTANCE.getBootstrap().inject(this);
    }

    public void createNotification() {
        isShown = true;
        //---------------------------------------------
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText("");//TODO
        //-----------------------------------------------
        notification = new Notification(R.drawable.notification_icon,
                context.getString(R.string.app_name),
                System.currentTimeMillis());
        // ----------------------OLOLO-------------------------------------
        final Intent notifiacationIntent = new Intent(context,
                TabHoster.class);
        notifiacationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, notifiacationIntent, 0);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        notification.contentView = new RemoteViews(context
                .getPackageName(), R.layout.app_notification);
        notification.contentIntent = contentIntent;
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MAIN_NOTIFICATION_ID, notification);
        changeSongName(player.currentTitle(),
               player.currentArtist());
        // --------------------------------OLOLO----------------------------------
    }

    /**
     * Меняет название песни и исполнителя в Notification(когда приложение свёрнуто)
     *
     * @param song
     * @param artist
     */
    public void changeSongName(String song, String artist) {
        if (isShown) {
            try {
                if (artist == null)
                    artist = "";
                String text = artist;
                if (text.length() > 0)
                    text += " - ";
                text += song;
                if (notification.contentView != null) {
                    notification.contentView.setTextViewText(
                            R.id.appNotificationSong, text);
                }
                notificationManager.notify(MAIN_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
