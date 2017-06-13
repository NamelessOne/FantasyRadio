package ru.sigil.fantasyradio.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.bassplayerlib.listeners.IAuthorChangedListener;
import ru.sigil.bassplayerlib.listeners.IBufferingProgressListener;
import ru.sigil.bassplayerlib.listeners.IEndSyncListener;
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener;
import ru.sigil.bassplayerlib.listeners.ITitleChangedListener;
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.TabHoster;

import static android.content.Context.NOTIFICATION_SERVICE;

@Singleton
public class FantasyRadioNotificationManager {
    public static final String ACTION = "ACTION";
    public static final String PAUSE = "PAUSE";
    public static final String PLAY = "PLAY";
    private boolean isShown = false;
    private Context context;
    public final int MAIN_NOTIFICATION_ID = 36484;
    //public final int NOTIFICATION_RECEIVER_REQUEST_CODE = 76008;
    public NotificationManager notificationManager;
    private IPlayer<RadioStream> player;

    @Inject
    public FantasyRadioNotificationManager(Context context, IPlayer<RadioStream> player) {
        this.context = context;
        this.player = player;
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
                    intent = new Intent(context, FantasyRadioNotificationReceiver.class);
                    intent.putExtra(ACTION, PAUSE);
                    pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    icon = android.R.drawable.ic_media_pause;
                    notCancelable = true;
                    break;
                case PAUSE:
                case STOP:
                default:
                    intent = new Intent(context, FantasyRadioNotificationReceiver.class);
                    intent.putExtra(ACTION, PLAY);
                    pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    icon = android.R.drawable.ic_media_play;
                    notCancelable = false;
                    break;
            }
            Intent notificationIntent = new Intent(context, TabHoster.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent tabHosterIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(context)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(getText(currentTitle, currentArtist))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setAutoCancel(false)
                    .setContentIntent(tabHosterIntent)
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
        player.addTitleChangedListener(titleChangedListener);
        player.addAuthorChangedListener(authorChangedListener);
        player.addPlayStateChangedListener(playStateChangedListener);
        player.addBufferingProgressChangedListener(bufferingProgressListener);
        player.addEndSyncListener(endSyncListener);
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
        player.removeTitleChangedListener(titleChangedListener);
        player.removeAuthorChangedListener(authorChangedListener);
        player.removePlayStateChangedListener(playStateChangedListener);
        player.removeBufferingProgressChangedListener(bufferingProgressListener);
        player.removeEndSyncListener(endSyncListener);
        isShown = false;
        notificationManager.cancel(MAIN_NOTIFICATION_ID);
    }

    private final ITitleChangedListener titleChangedListener = (title) -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());

    private final IAuthorChangedListener authorChangedListener = (author) -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());

    private final IPlayStateChangedListener playStateChangedListener = (playState) -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());

    private final IBufferingProgressListener bufferingProgressListener = (progress) -> updateNotification(String.format("BUFFERING... %d%%", progress), "", player.currentState());

    private final IEndSyncListener endSyncListener = () -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState());
}
