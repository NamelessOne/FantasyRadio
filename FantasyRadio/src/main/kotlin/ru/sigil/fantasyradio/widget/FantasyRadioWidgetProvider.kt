package ru.sigil.fantasyradio.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import ru.sigil.fantasyradio.utils.RadioStreamFactory
import javax.inject.Inject
import ru.sigil.fantasyradio.utils.RadioStream
import ru.sigil.bassplayerlib.IPlayer
import ru.sigil.fantasyradio.dagger.Bootstrap
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViews
import android.content.ComponentName
import android.view.View
import ru.sigil.bassplayerlib.PlayState
import ru.sigil.fantasyradio.R
import android.app.PendingIntent
import ru.sigil.bassplayerlib.listeners.IAuthorChangedListener
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener
import ru.sigil.bassplayerlib.listeners.IStreamChangedListener
import ru.sigil.bassplayerlib.listeners.ITitleChangedListener
import ru.sigil.fantasyradio.utils.Bitrate

private const val ACTION_BITRATE_CLICK_16 = "ACTION_BITRATE_CLICK_16"
private const val ACTION_BITRATE_CLICK_32 = "ACTION_BITRATE_CLICK_32"
private const val ACTION_BITRATE_CLICK_96 = "ACTION_BITRATE_CLICK_96"
private const val ACTION_BITRATE_CLICK_112 = "ACTION_BITRATE_CLICK_112"
private const val ACTION_PLAY_CLICK = "ACTION_PLAY_CLICK"
private const val ACTION_STOP_CLICK = "ACTION_STOP_CLICK"

/**
 * Created by namelessone
 * on 08.12.18.
 */
class FantasyRadioWidgetProvider: AppWidgetProvider() {

    init {
        Bootstrap.INSTANCE.getBootstrap().inject(this)
    }

    private val activeBitrateColor = Color.parseColor("#0C648C")
    private val activeBitrateTextColor = Color.parseColor("#EBECEC")
    private val defaultBitrateTextColor = Color.parseColor("#424242")
    private var widgetTitle = ""
    private var widgetAuthor = ""
    private var context: Context? = null

    @set:Inject
    var player: IPlayer<RadioStream>? = null
    @set:Inject
    var radioStreamFactory: RadioStreamFactory? = null

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray) {

        // Get all ids

        val thisWidget = ComponentName(context,
                FantasyRadioWidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        for (widgetId in allWidgetIds) {

            val remoteViews = RemoteViews(context?.packageName,
                    R.layout.fantasyradio_widget)

            val intent = Intent(context, FantasyRadioWidgetProvider::class.java)

            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)

            //-----------------------------------------------------------
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality16,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_16)
            )
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality32,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_32)
            )
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality96,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_96)
            )
            remoteViews.setOnClickPendingIntent(R.id.toggleQuality112,
                    getPendingSelfIntent(context,
                            ACTION_BITRATE_CLICK_112)
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_play,
                    getPendingSelfIntent(context,
                            ACTION_PLAY_CLICK)
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_stop,
                    getPendingSelfIntent(context,
                            ACTION_STOP_CLICK)
            )
            //-----------------------------------------------------------
            restoreDefaultBitrateColors(remoteViews)
            when (player?.stream?.bitrate) {
                Bitrate.AAC_16 -> {
                    remoteViews.setInt(R.id.toggleQuality16, "setBackgroundColor",
                            activeBitrateColor)
                    remoteViews.setInt(R.id.toggleQuality16, "setTextColor",
                            activeBitrateTextColor)
                }
                Bitrate.MP3_32 -> {
                    remoteViews.setInt(R.id.toggleQuality32, "setBackgroundColor",
                            activeBitrateColor)
                    remoteViews.setInt(R.id.toggleQuality32, "setTextColor",
                            activeBitrateTextColor)
                }
                Bitrate.MP3_96 -> {
                    remoteViews.setInt(R.id.toggleQuality96, "setBackgroundColor",
                            activeBitrateColor)
                    remoteViews.setInt(R.id.toggleQuality96, "setTextColor",
                            activeBitrateTextColor)
                }
                Bitrate.AAC_112 -> {
                    remoteViews.setInt(R.id.toggleQuality112, "setBackgroundColor",
                            activeBitrateColor)
                    remoteViews.setInt(R.id.toggleQuality112, "setTextColor",
                            activeBitrateTextColor)
                }
            }
            when (player?.playState) {
                PlayState.PLAY -> {
                    remoteViews.setViewVisibility(R.id.widget_play, View.GONE)
                    remoteViews.setViewVisibility(R.id.widget_stop, View.VISIBLE)
                }
                else //TODO case STOP:???
                -> {
                    widgetAuthor = ""
                    widgetTitle = ""
                    remoteViews.setViewVisibility(R.id.widget_play, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.widget_stop, View.GONE)
                }
            }
            remoteViews.setTextViewText(R.id.widget_author, widgetAuthor)
            remoteViews.setTextViewText(R.id.widget_title, widgetTitle)
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }

    private fun restoreDefaultBitrateColors(remoteViews: RemoteViews) {
        remoteViews.setInt(R.id.toggleQuality16, "setBackgroundColor",
                Color.TRANSPARENT)
        remoteViews.setInt(R.id.toggleQuality32, "setBackgroundColor",
                Color.TRANSPARENT)
        remoteViews.setInt(R.id.toggleQuality96, "setBackgroundColor",
                Color.TRANSPARENT)
        remoteViews.setInt(R.id.toggleQuality112, "setBackgroundColor",
                Color.TRANSPARENT)
        remoteViews.setInt(R.id.toggleQuality16, "setTextColor",
                defaultBitrateTextColor)
        remoteViews.setInt(R.id.toggleQuality32, "setTextColor",
                defaultBitrateTextColor)
        remoteViews.setInt(R.id.toggleQuality96, "setTextColor",
                defaultBitrateTextColor)
        remoteViews.setInt(R.id.toggleQuality112, "setTextColor",
                defaultBitrateTextColor)
    }

    private fun getPendingSelfIntent(context: Context?, action: String): PendingIntent {
        // An explicit intent directed at the current class (the "self").
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun onUpdate(context: Context?) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // Uses getClass().getName() rather than MyWidget.class.getName() for
            // portability into any App Widget Provider Class
            val thisAppWidgetComponentName = ComponentName(context?.packageName, javaClass.name
            )
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    thisAppWidgetComponentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        this.context = context
        player?.addTitleChangedListener(titleChangedListener)
        player?.addAuthorChangedListener(authorChangedListener)
        player?.addPlayStateChangedListener(playStateChangedListener)
        player?.addStreamChangedListener(streamChangedListener)
        if (ACTION_BITRATE_CLICK_16 == intent.action) {
            val stream = radioStreamFactory!!.createStreamWithBitrate(Bitrate.AAC_16)
            player?.stream = stream
            if (player?.playState === PlayState.PLAY) {
                player?.stop()
                player?.playStream(stream)
            }
            onUpdate(context)
        }
        if (ACTION_BITRATE_CLICK_32 == intent.action) {
            val stream = radioStreamFactory!!.createStreamWithBitrate(Bitrate.MP3_32)
            player?.stream = stream
            if (player?.playState === PlayState.PLAY) {
                player?.stop()
                player?.playStream(stream)
            }
            onUpdate(context)
        }
        if (ACTION_BITRATE_CLICK_96 == intent.action) {
            val stream = radioStreamFactory!!.createStreamWithBitrate(Bitrate.MP3_96)
            player?.stream = stream
            if (player?.playState === PlayState.PLAY) {
                player?.stop()
                player?.playStream(stream)
            }
            onUpdate(context)
        }
        if (ACTION_BITRATE_CLICK_112 == intent.action) {
            val stream = radioStreamFactory!!.createStreamWithBitrate(Bitrate.AAC_112)
            player?.stream = stream
            if (player?.playState === PlayState.PLAY) {
                player?.stop()
                player?.playStream(stream)
            }
            onUpdate(context)
        }

        if (ACTION_PLAY_CLICK == intent.action) {
            val stream = radioStreamFactory!!.createStreamWithBitrate(player?.stream?.bitrate ?: Bitrate.AAC_16)
            player?.playStream(stream)
            onUpdate(context)
        }
        if (ACTION_STOP_CLICK == intent.action) {
            player?.stop()
        }
    }

    private val titleChangedListener = object : ITitleChangedListener {
        override fun onTitleChanged(title: String) {
            widgetTitle = title
            onUpdate(context)
        }
    }

    private val authorChangedListener = object : IAuthorChangedListener {
        override fun onAuthorChanged(author: String) {
            widgetTitle = author
            onUpdate(context)
        }
    }

    private val playStateChangedListener = object : IPlayStateChangedListener {
        override fun onPlayStateChanged(playState: PlayState) {
            onUpdate(context)
        }
    }

    private val streamChangedListener = object : IStreamChangedListener<RadioStream> {
        override fun onStreamChanged(stream: RadioStream?) {
            onUpdate(context)
        }
    }
}