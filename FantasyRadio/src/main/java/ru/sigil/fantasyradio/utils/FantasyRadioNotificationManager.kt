package ru.sigil.fantasyradio.utils

import android.content.Context
import ru.sigil.bassplayerlib.IPlayer
import javax.inject.Inject
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import ru.sigil.fantasyradio.TabHoster
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver
import ru.sigil.bassplayerlib.PlayState
import ru.sigil.fantasyradio.R

const val PLAY = "PLAY"
const val MAIN_NOTIFICATION_ID = 36484

/**
 * Created by namelessone
 * on 08.12.18.
 */
class FantasyRadioNotificationManager @Inject constructor(private val context: Context,
                                                          private val player: IPlayer<RadioStream>)
    : IFantasyRadioNotificationManager {
    private var isShown = false
    private var notificationManager: NotificationManager? = null

    override fun updateNotification(currentTitle: String, currentArtist: String, currentState: PlayState) {
        if (isShown) {
            val icon: Int
            val pIntent: PendingIntent
            val intent: Intent
            val notCancelable: Boolean
            when (currentState) {
                PlayState.BUFFERING, PlayState.PLAY, PlayState.PLAY_FILE -> {
                    intent = Intent(context, FantasyRadioNotificationReceiver::class.java)
                    intent.putExtra(NotificationConstants.ACTION, NotificationConstants.PAUSE)
                    pIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    icon = android.R.drawable.ic_media_pause
                    notCancelable = true
                }
                PlayState.PAUSE, PlayState.STOP -> {
                    intent = Intent(context, FantasyRadioNotificationReceiver::class.java)
                    intent.putExtra(NotificationConstants.ACTION, PLAY)
                    pIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    icon = android.R.drawable.ic_media_play
                    notCancelable = false
                }
                else -> {
                    intent = Intent(context, FantasyRadioNotificationReceiver::class.java)
                    intent.putExtra(NotificationConstants.ACTION, PLAY)
                    pIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    icon = android.R.drawable.ic_media_play
                    notCancelable = false
                }
            }
            val notificationIntent = Intent(context, TabHoster::class.java)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val tabHosterIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0)

            val notification = NotificationCompat.Builder(context)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(getText(currentTitle, currentArtist))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setAutoCancel(false)
                    .setContentIntent(tabHosterIntent)
                    .setOngoing(notCancelable)
                    .addAction(icon, "", pIntent).build()

            notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager?.notify(MAIN_NOTIFICATION_ID, notification)
        }
    }

    override fun createNotification(currentTitle: String, currentArtist: String, currentState: PlayState) {
        isShown = true
        updateNotification(currentTitle, currentArtist, currentState)
        player.addTitleChangedListener(titleChangedListener)
        player.addAuthorChangedListener(authorChangedListener)
        player.addPlayStateChangedListener(playStateChangedListener)
        player.addBufferingProgressChangedListener(bufferingProgressListener)
        player.addEndSyncListener(endSyncListener)
    }

    private fun getText(song: String, artist: String?): String {
        var text: String = artist ?: ""
        if (text.isNotEmpty())
            text += " - "
        text += song
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

    private val titleChangedListener = { _: String -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState()) }

    private val authorChangedListener = { _: String -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState()) }

    private val playStateChangedListener = { _: PlayState -> updateNotification(player.currentTitle(), player.currentArtist(), player.currentState()) }

    private val bufferingProgressListener = { progress: Long  -> updateNotification(String.format("BUFFERING... %d%%", progress), "", player.currentState()) }

    private val endSyncListener = { updateNotification(player.currentTitle(), player.currentArtist(), player.currentState()) }

}