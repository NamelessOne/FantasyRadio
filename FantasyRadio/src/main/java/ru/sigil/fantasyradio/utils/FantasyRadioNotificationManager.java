package ru.sigil.fantasyradio.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import ru.sigil.fantasyradio.BackgroundService.Bitrate;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.IPlayerEventListener;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.dagger.Bootstrap;

import static android.content.Context.NOTIFICATION_SERVICE;

public class FantasyRadioNotificationManager {
    public static final String ACTION = "ACTION";
    public static final String PAUSE = "PAUSE";
    public static final String PLAY = "PLAY";
    private boolean isShown = false;
    private Context context;
    public final int MAIN_NOTIFICATION_ID = 36484;
    //public final int NOTIFICATION_RECEIVER_REQUEST_CODE = 76008;
    public NotificationManager notificationManager;
    @Inject
    IPlayer player;

    public FantasyRadioNotificationManager(Context context) {
        this.context = context;
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        player.addEventListener(eventListener);
    }

    public void updateNotification(String currentTitle, String currentArtist, PlayState currentState) {
        if (isShown) {
            int icon;
            PendingIntent pIntent;
            Intent intent;
            boolean notCancelable;
            switch (currentState) {
                case BUFFERING:
                case PLAY:
                case PLAY_FILE:
                    //TODO
                    intent = new Intent(context, FantasyRadioNotificationReceiver.class);
                    intent.putExtra(ACTION, PAUSE);
                    pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    icon = R.drawable.ic_media_pause;
                    notCancelable = true;
                    break;
                case PAUSE:
                case STOP:
                default:
                    //TODO
                    intent = new Intent(context, FantasyRadioNotificationReceiver.class);
                    intent.putExtra(ACTION, PLAY);
                    pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    icon = R.drawable.ic_media_play;
                    notCancelable = false;
                    break;
            }

            Notification notification = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(getText(currentTitle, currentArtist))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setAutoCancel(false)
                    .setOngoing(notCancelable)
                    .addAction(icon, "", pIntent).build();

            notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(MAIN_NOTIFICATION_ID, notification);
        }
    }

    public void createNotification(String currentTitle, String currentArtist, PlayState currentState) {
        isShown = true;
        updateNotification(currentTitle, currentArtist, currentState);
    }

    private String getText(String song, String artist) {
        if (artist == null)
            artist = "";
        String text = artist;
        if (text.length() > 0)
            text += " - ";
        text += song;
        return text;
    }

    public void cancel() {
        isShown = false;
        notificationManager.cancel(MAIN_NOTIFICATION_ID);
    }

    //TODO эта штука должна быть в Receiver'е?
    private IPlayerEventListener eventListener = new IPlayerEventListener() {
        @Override
        public void onTitleChanged(String title) {
            updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());
        }

        @Override
        public void onAuthorChanged(String author) {
            updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());
        }

        @Override
        public void onPlayStateChanged(PlayState playState) {
            updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());
        }

        @Override
        public void onRecStateChanged(boolean isRec) {
        }

        @Override
        public void onBitrateChanged(Bitrate bitrate) {
        }

        @Override
        public void onBufferingProgress(long progress) {
            updateNotification(String.format("BUFFERING... %d%%", progress), "", player.currentState());
        }

        @Override
        public void endSync() {
            updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());
        }

        @Override
        public void onVolumeChanged(float volume) {
        }
    };
}
