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
    @set:Inject
    var player: IPlayer<RadioStream>? = null
    @set:Inject
    var notificationManager: IFantasyRadioNotificationManager? = null
    @set:Inject
    var radioStreamFactory: IRadioStreamFactory? = null

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(NotificationConstants.ACTION)
        if (NotificationConstants.PAUSE == action) {
            when (player?.currentState()) {
                PlayState.PLAY, PlayState.BUFFERING -> player?.stop()
                PlayState.PLAY_FILE -> player?.pause()
                else -> {
                }
            }
            notificationManager?.updateNotification(player?.currentTitle() ?: "", player?.currentArtist() ?: "", PlayState.STOP)
        } else {
            when (player?.currentState()) {
                PlayState.STOP -> player?.playStream(radioStreamFactory?.createStreamWithBitrate(player?.currentStream()?.getBitrate() ?: Bitrate.AAC_16))
                PlayState.PAUSE -> player?.resume()
                else -> {
                }
            }
            notificationManager?.updateNotification(player?.currentTitle() ?: "", player?.currentArtist() ?: "", PlayState.PLAY)
        }
    }
}