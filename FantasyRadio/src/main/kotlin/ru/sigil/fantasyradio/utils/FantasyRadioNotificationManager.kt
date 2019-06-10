package ru.sigil.fantasyradio.utils

import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import ru.sigil.bassplayerlib.IPlayer
import javax.inject.Inject
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import ru.sigil.fantasyradio.TabHoster
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver
import ru.sigil.bassplayerlib.PlayState
import ru.sigil.bassplayerlib.listeners.IAuthorChangedListener
import ru.sigil.bassplayerlib.listeners.IBufferingProgressListener
import ru.sigil.bassplayerlib.listeners.IEndSyncListener
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener
import ru.sigil.bassplayerlib.listeners.ITitleChangedListener
import ru.sigil.fantasyradio.R
import ru.sigil.fantasyradio.utils.NotificationConstants.MAIN_NOTIFICATION_ID

const val PLAY = "PLAY"
const val CHANNEL_ID = "FANTASY_RADIO_36484"

/**
 * Created by namelessone
 * on 08.12.18.
 */
class FantasyRadioNotificationManager @Inject constructor(private val context: Context,
                                                          private val player: IPlayer<RadioStream>)
    : IFantasyRadioNotificationManager {
    private var isShown = false
    private var notificationManager: NotificationManager? = null

    override fun updateNotification(currentTitle: String?, currentArtist: String?, currentState: PlayState) {
        if (isShown) {
            notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O && notificationManager?.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, "Радио фантастики", NotificationManager.IMPORTANCE_DEFAULT) //TODO в ресурсы
                        .apply { setSound(null, null) }
                notificationManager?.createNotificationChannel(channel)
            }

            val notification = buildNotification(currentTitle, currentArtist, currentState)

            notificationManager?.notify(MAIN_NOTIFICATION_ID, notification)
        }
        player.addTitleChangedListener(titleChangedListener)
        player.addAuthorChangedListener(authorChangedListener)
        player.addPlayStateChangedListener(playStateChangedListener)
        player.addBufferingProgressChangedListener(bufferingProgressListener)
        player.addEndSyncListener(endSyncListener)
    }

    override fun buildNotification(currentTitle: String?, currentArtist: String?, currentState: PlayState) : Notification {
        isShown = true
        val icon: Int
        val pIntent: PendingIntent
        val intent: Intent
        val notCancelable: Boolean
        val actionText: String
        val cancelIntent = Intent(context, FantasyRadioNotificationReceiver::class.java)
        cancelIntent.putExtra(NotificationConstants.ACTION, NotificationConstants.STOP)
        val pCancelIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        when (currentState) {
            PlayState.BUFFERING, PlayState.PLAY, PlayState.PLAY_FILE -> {
                intent = Intent(context, FantasyRadioNotificationReceiver::class.java)
                intent.putExtra(NotificationConstants.ACTION, NotificationConstants.PAUSE)
                pIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
                icon = android.R.drawable.ic_media_pause
                actionText = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) NotificationConstants.PAUSE else ""
                notCancelable = true
            }
            PlayState.PAUSE, PlayState.STOP -> {
                intent = Intent(context, FantasyRadioNotificationReceiver::class.java)
                intent.putExtra(NotificationConstants.ACTION, PLAY)
                pIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
                icon = android.R.drawable.ic_media_play
                actionText = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) PLAY else ""
                notCancelable = false
            }
            else -> {
                intent = Intent(context, FantasyRadioNotificationReceiver::class.java)
                intent.putExtra(NotificationConstants.ACTION, PLAY)
                pIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
                icon = android.R.drawable.ic_media_play
                actionText = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) PLAY else ""
                notCancelable = false
            }
        }

        val notificationIntent = Intent(context, TabHoster::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val tabHosterIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0)

        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getText(currentTitle, currentArtist))
                .setSmallIcon(R.drawable.notification_icon)
                .setAutoCancel(false)
                .setContentIntent(tabHosterIntent)
                .setDeleteIntent(pCancelIntent)
                .setOngoing(notCancelable)
                .addAction(icon, actionText, pIntent).build()
    }

    override fun createNotification(currentTitle: String?, currentArtist: String?, currentState: PlayState) {
        isShown = true
        updateNotification(currentTitle, currentArtist, currentState)
    }

    private fun getText(song: String?, artist: String?): String {
        var text: String = artist ?: ""
        if (text.isNotEmpty())
            text += " - "
        text += song ?: ""
        return text
    }

    override fun cancel() {
        if(notificationManager!=null) { //TODO нужна ли эта проверка?
            player.removeTitleChangedListener(titleChangedListener)
            player.removeAuthorChangedListener(authorChangedListener)
            player.removePlayStateChangedListener(playStateChangedListener)
            player.removeBufferingProgressChangedListener(bufferingProgressListener)
            player.removeEndSyncListener(endSyncListener)
            isShown = false
            notificationManager?.cancel(MAIN_NOTIFICATION_ID)
        }
    }

    private val titleChangedListener = object : ITitleChangedListener {
        override fun onTitleChanged(title: String) = updateNotification(player.title, player.author, player.playState)
    }

    private val authorChangedListener = object : IAuthorChangedListener {
        override fun onAuthorChanged(author: String) = updateNotification(player.title, player.author, player.playState)
    }

    private val playStateChangedListener = object : IPlayStateChangedListener
    {
        override fun onPlayStateChanged(playState: PlayState) = updateNotification(player.title, player.author, player.playState)
    }

    private val bufferingProgressListener = object : IBufferingProgressListener {
        override fun onBufferingProgress(progress: Long) = updateNotification(String.format("BUFFERING... %d%%", progress), "", player.playState)
    }

    private val endSyncListener = object : IEndSyncListener {
        override fun endSync() = updateNotification(player.title, player.author, player.playState)

    }
}