package ru.sigil.fantasyradio.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
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
    private final String TAG = AppWidgetProvider.class.getSimpleName();

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
    private static final String ACTION_PLAY_PAUSE_CLICK =
            "ru.sigil.fantasyradio.widget.ACTION_PLAY_PAUSE_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.w(TAG, "onUpdate method called");
        // Get all ids

        ComponentName thisWidget = new ComponentName(context,
                FantasyRadioWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.fantasyradio_widget);

            //remoteViews.setTextViewText(R.id.author, String.valueOf("Роберт Хайнлайн"));
            //remoteViews.setTextViewText(R.id.title, String.valueOf("Прыжок в вечность"));

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
                            ACTION_PLAY_PAUSE_CLICK)
            );
            //-----------------------------------------------------------
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
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
                new ComponentName(context.getPackageName(),getClass().getName()
                );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_BITRATE_CLICK_16.equals(intent.getAction())) {
           LogManager.d(TAG, "16");
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_32.equals(intent.getAction())) {
            LogManager.d(TAG, "32");
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_64.equals(intent.getAction())) {
            LogManager.d(TAG, "64");
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_96.equals(intent.getAction())) {
            LogManager.d(TAG, "96");
            onUpdate(context);
        }
        if (ACTION_BITRATE_CLICK_112.equals(intent.getAction())) {
            LogManager.d(TAG, "112");
            onUpdate(context);
        }
        if (ACTION_PLAY_PAUSE_CLICK.equals(intent.getAction())) {
            Play(context.getString(R.string.stream_url_MP332));
            LogManager.d(TAG, "Play");
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

    private Object lock = new Object();
    private int req; // request number/counter
    private Handler handler = new Handler();
    private Runnable timer;
}
