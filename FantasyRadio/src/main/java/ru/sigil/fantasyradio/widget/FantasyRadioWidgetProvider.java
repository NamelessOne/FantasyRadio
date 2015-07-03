package ru.sigil.fantasyradio.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.un4seen.bass.BASS;

import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.utils.BASSUtil;
import ru.sigil.log.LogManager;


/**
 * Created by namelessone
 * 30.08.14.
 */
public class FantasyRadioWidgetProvider extends AppWidgetProvider {
    private static final String TAG = AppWidgetProvider.class.getSimpleName();

    private static final String ACTION_BITRATE_CLICK_16 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_16";
    private static final String ACTION_BITRATE_CLICK_32 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_32";
    private static final String ACTION_BITRATE_CLICK_64 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_64";
    private static final String ACTION_BITRATE_CLICK_96 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_96";
    private static final String ACTION_BITRATE_CLICK_112 =
            "ru.sigil.fantasyradio.widget.ACTION_BITRATE_CLICK_112";
    private static final String ACTION_PLAY_CLICK =
            "ru.sigil.fantasyradio.widget.ACTION_PLAY_CLICK";
    private static final String ACTION_STOP_CLICK =
            "ru.sigil.fantasyradio.widget.ACTION_STOP_CLICK";

    private enum PlayerState {stop, play}

    private enum Bitrate {aac_16, mp3_32, mp3_64, mp3_96, aac_112}

    private static Bitrate currentBitrate = Bitrate.aac_16;
    private static PlayerState playerState = PlayerState.stop;
    private static final int activeBitrateColor = Color.parseColor("#0C648C");
    private static final int activeBitrateTextColor = Color.parseColor("#EBECEC");
    private static final int defaultBitrateTextColor = Color.parseColor("#424242");

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled method called");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d(TAG, "onUpdate method called");
        // Get all ids

        ComponentName thisWidget = new ComponentName(context,
                FantasyRadioWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.fantasyradio_widget);

            LogManager.d(TAG, "onUpdate");
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
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality64,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_64)
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
            //TODO сделать всем битрейтам дефолтный фон
            restoreDefaultBitrateColors(remoteViews);
            switch (currentBitrate) {
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
                case mp3_64:
                    remoteViews.setInt(R.id.toggleQuality64, "setBackgroundColor",
                            activeBitrateColor);
                    remoteViews.setInt(R.id.toggleQuality64, "setTextColor",
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
            switch (playerState) {
                case play:
                    remoteViews.setViewVisibility(R.id.widget_play, View.GONE);
                    remoteViews.setViewVisibility(R.id.widget_stop, View.VISIBLE);
                    break;
                case stop:
                    remoteViews.setViewVisibility(R.id.widget_play, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.widget_stop, View.GONE);
                    break;
            }
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private void restoreDefaultBitrateColors(RemoteViews remoteViews) {
        remoteViews.setInt(R.id.toggleQuality16, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality32, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality64, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality96, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality112, "setBackgroundColor",
                Color.TRANSPARENT);
        remoteViews.setInt(R.id.toggleQuality16, "setTextColor",
                defaultBitrateTextColor);
        remoteViews.setInt(R.id.toggleQuality32, "setTextColor",
                defaultBitrateTextColor);
        remoteViews.setInt(R.id.toggleQuality64, "setTextColor",
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
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_BITRATE_CLICK_16.equals(intent.getAction())) {
            currentBitrate = Bitrate.aac_16;
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_32.equals(intent.getAction())) {
            currentBitrate = Bitrate.mp3_32;
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_64.equals(intent.getAction())) {
            currentBitrate = Bitrate.mp3_64;
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_96.equals(intent.getAction())) {
            currentBitrate = Bitrate.mp3_96;
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_112.equals(intent.getAction())) {
            currentBitrate = Bitrate.aac_112;
            onUpdate(context);
        }
        if (ACTION_PLAY_CLICK.equals(intent.getAction())) {
            playerState = PlayerState.play;
            Play(context.getString(R.string.stream_url_MP396));
            onUpdate(context);
        }
        if (ACTION_STOP_CLICK.equals(intent.getAction())) {
            BASS.BASS_StreamFree(BASSUtil.getChan());
            playerState = PlayerState.stop;
            onUpdate(context);
        }
    }

    /**
     * Начинаем играть поток
     *
     * @param url URL потока. Не AAC
     */
    void Play(String url) {
        BASS.BASS_SetConfigPtr(BASS.BASS_CONFIG_NET_PROXY, null);
        try {
            new Thread(new OpenURL(url)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Магия BASS.dll
     */
    class OpenURL implements Runnable {
        String url;

        public OpenURL(String p) {
            url = p;
        }

        public void run() {
            LogManager.d(TAG, "play");
            int r;
            synchronized (lock) { // make sure only 1 thread at a time can
                // do
                // the following
                r = ++req; // increment the request counter for this request
            }
            BASS.BASS_StreamFree(BASSUtil.getChan()); // close old stream
            /*runOnUiThread(new Runnable() {
                public void run() {
                    ((TextView) findViewById(R.id.textView1))
                            .setText(R.string.connecting);
                }
            });*/
            int c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK
                            | BASS.BASS_STREAM_STATUS | BASS.BASS_STREAM_AUTOFREE,
                    BASSUtil.StatusProc, r); // open URL
            synchronized (lock) {
                if (r != req) { // there is a newer request, discard this
                    // stream
                    if (c != 0)
                        BASS.BASS_StreamFree(c);
                    return;
                }
                BASSUtil.setChan(c); // this is now the current stream
            }

            if (BASSUtil.getChan() != 0) { // failed to open
                handler.postDelayed(timer, 50); // start prebuffer
                // monitoring
            }
        }

    }

    private final Object lock = new Object();
    private int req; // request number/counter
    private Handler handler = new Handler();
    private Runnable timer = new Runnable() {
        public void run() {
            // monitor prebuffering progress
            long progress = BASS.BASS_StreamGetFilePosition(
                    BASSUtil.getChan(), BASS.BASS_FILEPOS_BUFFER)
                    * 100
                    / BASS.BASS_StreamGetFilePosition(BASSUtil.getChan(),
                    BASS.BASS_FILEPOS_END); // percentage of buffer
            // filled
            if (progress > 75
                    || BASS.BASS_StreamGetFilePosition(BASSUtil.getChan(),
                    BASS.BASS_FILEPOS_CONNECTED) == 0) { // over 75%
                // full
                // (or
                // end
                // of
                // download)
                // get the broadcast name and URL
                String[] icy = (String[]) BASS.BASS_ChannelGetTags(
                        BASSUtil.getChan(), BASS.BASS_TAG_ICY);
                if (icy == null)
                    icy = (String[]) BASS.BASS_ChannelGetTags(
                            BASSUtil.getChan(), BASS.BASS_TAG_HTTP); // no
                // ICY
                // tags,
                // try
                // HTTP
                // get the stream title and set sync for subsequent titles
                DoMeta();
                BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                        BASS.BASS_SYNC_META, 0, MetaSync, 0); // Shoutcast
                BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                        BASS.BASS_SYNC_OGG_CHANGE, 0, MetaSync, 0); // Icecast/OGG
                // set sync for end of stream
                BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                        BASS.BASS_SYNC_END, 0, EndSync, 0);
                // play it!
                BASS.BASS_ChannelPlay(BASSUtil.getChan(), false);
            } else {
                /*((TextView) findViewById(R.id.textView1)).setText(String
                        .format("buffering... %d%%", progress));*/
                handler.postDelayed(this, 50);
            }
        }
    };

    private void DoMeta() {
        //TODO написать
    }

    /**
     * Получаем метаданные (название, исполнитель и т.д.)
     */
    private BASS.SYNCPROC MetaSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            new Thread(new Runnable() {
                public void run() {
                    DoMeta();
                }
            }).start();
        }
    };

    /**
     * Выполняется после завершения проигрывания. В данный момент не используестя
     */
    private BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
        }
    };
}
