package ru.sigil.fantasyradio

import android.content.BroadcastReceiver
import android.content.Context
import javax.inject.Inject
import ru.sigil.bassplayerlib.IPlayer
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.bassplayerlib.PlayState
import android.content.Intent
import ru.sigil.fantasyradio.utils.*


/**
 * Created by namelessone
 * on 08.12.18.
 */
class FantasyRadioNotificationReceiver: BroadcastReceiver() {
    init {
        Bootstrap.INSTANCE.getBootstrap().inject(this)
    }
    @Inject
    lateinit var player: IPlayer<RadioStream>
    @Inject
    lateinit var notificationManager: IFantasyRadioNotificationManager
    @Inject
    lateinit var radioStreamFactory: IRadioStreamFactory

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(NotificationConstants.ACTION)) {
            NotificationConstants.PAUSE -> {
                when (player.playState) {
                    PlayState.PLAY, PlayState.BUFFERING -> player.stop()
                    PlayState.PLAY_FILE -> player.pause()
                    else -> {
                    }
                }
                notificationManager.updateNotification(player.title ?: "", player.author ?: "", PlayState.STOP)
            }
            NotificationConstants.STOP -> {
                notificationManager.cancel()
                player.stop()
            }
            else -> {
                when (player.playState) {
                    PlayState.STOP -> player.playStream(radioStreamFactory.createStreamWithBitrate(player.stream?.bitrate ?: Bitrate.AAC_16))
                    PlayState.PAUSE -> player.resume()
                    else -> {
                    }
                }
                notificationManager.updateNotification(player.title ?: "", player.author ?: "", PlayState.PLAY)
            }
        }
    }
}