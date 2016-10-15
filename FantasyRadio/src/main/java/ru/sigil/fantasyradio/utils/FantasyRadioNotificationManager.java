package ru.sigil.fantasyradio.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver;
import ru.sigil.fantasyradio.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class FantasyRadioNotificationManager {
    public boolean isShown = false;
    private Context context;
    public final int MAIN_NOTIFICATION_ID = 36484;
    //public final int NOTIFICATION_RECEIVER_REQUEST_CODE = 76008;
    public NotificationManager notificationManager;
    private Notification notification;

    public FantasyRadioNotificationManager(Context context)
    {
        this.context = context;
    }

    public void createNotification(String currentTitle, String currentArtist, PlayState currentState) {
        isShown = true;
        //---------------------------------------------
        int icon;
        PendingIntent pIntent;
        Intent intent;
        switch (currentState)
        {
            case BUFFERING:
            case PLAY:
            case PLAY_FILE:
                //TODO
                intent = new Intent(context, FantasyRadioNotificationReceiver.class);
                intent.putExtra("ACTION", "PAUSE");
                pIntent = PendingIntent.getBroadcast(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                icon = R.drawable.ic_media_pause;
                break;
            case PAUSE:
            case STOP:
            default:
                //TODO
                intent = new Intent(context, FantasyRadioNotificationReceiver.class);
                intent.putExtra("ACTION", "PLAY");
                pIntent = PendingIntent.getBroadcast(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                icon = R.drawable.ic_media_play;
                break;
        }

        notification  = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getText(currentTitle, currentArtist))
                .setSmallIcon(R.drawable.notification_icon)
                //.setContentIntent(pIntent)
                //.setAutoCancel(true)
                .addAction(icon, "", pIntent).build();

        notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(MAIN_NOTIFICATION_ID, notification);
    }

    private String getText(String song, String artist)
    {
        if (artist == null)
            artist = "";
        String text = artist;
        if (text.length() > 0)
            text += " - ";
        text += song;
        return text;
    }
}
