package ru.sigil.fantasyradio.playerservice

import android.app.Service
import android.content.Context
import android.os.PowerManager
import android.net.wifi.WifiManager
import ru.sigil.fantasyradio.utils.RadioStream
import ru.sigil.bassplayerlib.IPlayer
import ru.sigil.bassplayerlib.PlayState
import javax.inject.Inject
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener
import ru.sigil.fantasyradio.dagger.Bootstrap
import android.os.Build
import ru.sigil.fantasyradio.utils.IFantasyRadioNotificationManager
import ru.sigil.fantasyradio.utils.NotificationConstants.MAIN_NOTIFICATION_ID


/**
 * Created by namelessone
 * on 01.12.18.
 */
class PlayerBackgroundService: Service() {
    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var player: IPlayer<RadioStream>

    @Inject
    lateinit var notificationManager: IFantasyRadioNotificationManager

    private var binder = PlayerBackgroundServiceBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //TODO do something useful
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O) {
            startForeground(MAIN_NOTIFICATION_ID, notificationManager.buildNotification(player.title, player.author, player.playState))
        }
        if (intent?.action.equals("StopService")) {
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(arg0: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Bootstrap.INSTANCE.getBootstrap().inject(this)
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag")
            wifiLock?.acquire()
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag")
            wakeLock?.acquire()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val playerStateChangedListener = object : IPlayStateChangedListener {
            override fun onPlayStateChanged(playState: PlayState) {
                try {
                    when (playState) {
                        PlayState.STOP, PlayState.PAUSE -> {
                            wifiLock?.release()
                            wakeLock?.release()
                        }
                        else -> {
                            wifiLock?.acquire()
                            wakeLock?.acquire()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }            }

        }
        player.addPlayStateChangedListener(playerStateChangedListener)
    }

    override fun onDestroy() {
        player.stop()
    }

    override fun onLowMemory() {

    }

    inner class PlayerBackgroundServiceBinder : Binder() {
        fun getPlayer(): IPlayer<RadioStream>? {
            return player
        }
    }
}