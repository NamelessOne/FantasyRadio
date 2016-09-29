package ru.sigil.fantasyradio.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.TabHoster;

public abstract class ProgramNotification {
    public static boolean isShown = false;
    private static Context context;
    public static int MAIN_NOTIFICATION_ID = 36484;
    public static NotificationManager notificationManager;
    private static Notification notification;

    public static void createNotification() {
        isShown = true;
        notification = new Notification(R.drawable.notification_icon,
                getContext().getString(R.string.app_name),
                System.currentTimeMillis());
        // ----------------------OLOLO-------------------------------------
        final Intent notifiacationIntent = new Intent(getContext(),
                TabHoster.class);
        notifiacationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(
                getContext(), 0, notifiacationIntent, 0);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        notification.contentView = new RemoteViews(getContext()
                .getPackageName(), R.layout.app_notification);
        notification.contentIntent = contentIntent;
        notificationManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MAIN_NOTIFICATION_ID, notification);
        changeSongName("", "");
        //TODO changeSongName(player.getCurrentSong(),
        //TODO        player.getCurrentArtist());
        // --------------------------------OLOLO----------------------------------
    }

    /**
     * Меняет название песни и исполнителя в Notification(когда приложение свёрнуто)
     *
     * @param song
     * @param artist
     */
    public static void changeSongName(String song, String artist) {
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

    private static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ProgramNotification.context = context;
    }
}
