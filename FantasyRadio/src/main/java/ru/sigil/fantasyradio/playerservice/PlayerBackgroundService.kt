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
import ru.sigil.fantasyradio.dagger.Bootstrap

/**
 * Created by namelessone
 * on 01.12.18.
 */
class PlayerBackgroundService: Service() {
    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null

    @set:Inject
    var player: IPlayer<RadioStream>? = null

    var binder = PlayerBackgroundServiceBinder()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //TODO do something useful
        return Service.START_NOT_STICKY
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
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyWakelockTag")
            wakeLock?.acquire()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        player?.addPlayStateChangedListener { state ->
            try {
                when (state) {
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
            }


        }
    }

    override fun onDestroy() {
        player?.stop()
    }

    override fun onLowMemory() {

    }

    inner class PlayerBackgroundServiceBinder : Binder() {
        fun getPlayer(): IPlayer<RadioStream>? {
            return player
        }
    }
}