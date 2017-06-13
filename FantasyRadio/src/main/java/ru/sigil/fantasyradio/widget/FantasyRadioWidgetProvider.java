package ru.sigil.fantasyradio.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.bassplayerlib.listeners.IAuthorChangedListener;
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener;
import ru.sigil.bassplayerlib.listeners.IStreamChangedListener;
import ru.sigil.bassplayerlib.listeners.ITitleChangedListener;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.Bitrate;
import ru.sigil.fantasyradio.utils.RadioStream;
import ru.sigil.fantasyradio.utils.RadioStreamFactory;


/**
 * Created by namelessone
 * 30.08.14.
 */
public class FantasyRadioWidgetProvider extends AppWidgetProvider {
    //TODO А удаление листенеров?
    private static final String ACTION_BITRATE_CLICK_16 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_16";
    private static final String ACTION_BITRATE_CLICK_32 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_32";
    private static final String ACTION_BITRATE_CLICK_96 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_96";
    private static final String ACTION_BITRATE_CLICK_112 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_112";
    private static final String ACTION_PLAY_CLICK =
            "ru.sigil.fantasyradio.widget.ACTION_PLAY_CLICK";
    private static final String ACTION_STOP_CLICK =
            "ru.sigil.fantasyradio.widget.ACTION_STOP_CLICK";

    private static final int activeBitrateColor = Color.parseColor("#0C648C");
    private static final int activeBitrateTextColor = Color.parseColor("#EBECEC");
    private static final int defaultBitrateTextColor = Color.parseColor("#424242");
    private static String widgetTitle = "";
    private static String widgetAuthor = "";
    private Context context;

    @Inject
    IPlayer<RadioStream> player;
    @Inject
    RadioStreamFactory radioStreamFactory;

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);
    }

    public FantasyRadioWidgetProvider() {
        super();
        Bootstrap.INSTANCE.getBootstrap().inject(this);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        // Get all ids

        ComponentName thisWidget = new ComponentName(context,
                FantasyRadioWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.fantasyradio_widget);

            Intent intent = new Intent(context, FantasyRadioWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            //-----------------------------------------------------------
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality16,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_16)
            );
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality32,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_32)
            );
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality96,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_96)
            );
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality112,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_112)
            );
            remoteViews.setOnClickPendingIntent(R.id.widget_play,
                    getPendingSelfIntent(context,
                            ACTION_PLAY_CLICK)
            );
            remoteViews.setOnClickPendingIntent(R.id.widget_stop,
                    getPendingSelfIntent(context,
                            ACTION_STOP_CLICK)
            );
            //-----------------------------------------------------------
            restoreDefaultBitrateColors(remoteViews);
            switch (player.currentStream().getBitrate()) {
                case aac_16:
                    remoteViews.setInt(R.id.toggleQuality16, "setBackgroundColor",
                            activeBitrateColor);
                    remoteViews.setInt(R.id.toggleQuality16, "setTextColor",
                            activeBitrateTextColor);
                    break;
                case mp3_32:
                    remoteViews.setInt(R.id.toggleQuality32, "setBackgroundColor",
                            activeBitrateColor);
                    remoteViews.setInt(R.id.toggleQuality32, "setTextColor",
                            activeBitrateTextColor);
                    break;
                case mp3_96:
                    remoteViews.setInt(R.id.toggleQuality96, "setBackgroundColor",
                            activeBitrateColor);
                    remoteViews.setInt(R.id.toggleQuality96, "setTextColor",
                            activeBitrateTextColor);
                    break;
                case aac_112:
                    remoteViews.setInt(R.id.toggleQuality112, "setBackgroundColor",
                            activeBitrateColor);
                    remoteViews.setInt(R.id.toggleQuality112, "setTextColor",
                            activeBitrateTextColor);
                    break;
            }
            switch (player.currentState()) {
                case PLAY:
                    remoteViews.setViewVisibility(R.id.widget_play, View.GONE);
                    remoteViews.setViewVisibility(R.id.widget_stop, View.VISIBLE);
                    break;
                default: //TODO case STOP:???
                    widgetAuthor = "";
                    widgetTitle = "";
                    remoteViews.setViewVisibility(R.id.widget_play, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.widget_stop, View.GONE);
                    break;

            }
            remoteViews.setTextViewText(R.id.widget_author, widgetAuthor);
            remoteViews.setTextViewText(R.id.widget_title, widgetTitle);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private void restoreDefaultBitrateColors(RemoteViews remoteViews) {
        remoteViews.setInt(R.id.toggleQuality16, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality32, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality96, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality112, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality16, "setTextColor",
                defaultBitrateTextColor);
        remoteViews.setInt(R.id.toggleQuality32, "setTextColor",
                defaultBitrateTextColor);
        remoteViews.setInt(R.id.toggleQuality96, "setTextColor",
                defaultBitrateTextColor);
        remoteViews.setInt(R.id.toggleQuality112, "setTextColor",
                defaultBitrateTextColor);
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        // An explicit intent directed at the current class (the "self").
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void onUpdate(Context context) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance
                    (context);

            // Uses getClass().getName() rather than MyWidget.class.getName() for
            // portability into any App Widget Provider Class
            ComponentName thisAppWidgetComponentName =
                    new ComponentName(context.getPackageName(), getClass().getName()
                    );
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    thisAppWidgetComponentName);
            onUpdate(context, appWidgetManager, appWidgetIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        this.context = context;
        player.addTitleChangedListener(titleChangedListener);
        player.addAuthorChangedListener(authorChangedListener);
        player.addPlayStateChangedListener(playStateChangedListener);
        player.addStreamChangedListener(streamChangedListener);
        if (ACTION_BITRATE_CLICK_16.equals(intent.getAction())) {
            RadioStream stream = radioStreamFactory.createStreamWithBitrate(Bitrate.aac_16);
            player.setStream(stream);
            if (player.currentState() == PlayState.PLAY) {
                player.stop();
                player.playAAC(stream);
            }
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_32.equals(intent.getAction())) {
            RadioStream stream = radioStreamFactory.createStreamWithBitrate(Bitrate.mp3_32);
            player.setStream(stream);
            if (player.currentState() == PlayState.PLAY) {
                player.stop();
                player.play(stream);
            }
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_96.equals(intent.getAction())) {
            RadioStream stream = radioStreamFactory.createStreamWithBitrate(Bitrate.mp3_96);
            player.setStream(stream);
            if (player.currentState() == PlayState.PLAY) {
                player.stop();
                player.play(stream);
            }
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_112.equals(intent.getAction())) {
            RadioStream stream = radioStreamFactory.createStreamWithBitrate(Bitrate.aac_112);
            player.setStream(stream);
            if (player.currentState() == PlayState.PLAY) {
                player.stop();
                player.playAAC(stream);
            }
            onUpdate(context);
        }

        if (ACTION_PLAY_CLICK.equals(intent.getAction())) {
            RadioStream stream = radioStreamFactory.createStreamWithBitrate(player.currentStream().getBitrate());
            switch (player.currentStream().getBitrate()) {
                case aac_16:
                case aac_112:
                    player.playAAC(stream);
                    break;
                case mp3_32:
                case mp3_96:
                    player.play(stream);
                    break;
            }
            onUpdate(context);
        }
        if (ACTION_STOP_CLICK.equals(intent.getAction())) {
            player.stop();
        }
    }

    private final ITitleChangedListener titleChangedListener = (title) -> {
        widgetTitle = title;
        onUpdate(context);
    };

    private final IAuthorChangedListener authorChangedListener = (author) -> {
        widgetTitle = author;
        onUpdate(context);
    };

    private final IPlayStateChangedListener playStateChangedListener = (state) -> onUpdate(context);

    private final IStreamChangedListener<RadioStream> streamChangedListener = (bitrate) -> onUpdate(context);

    //TODO buffering progress listener???

}
