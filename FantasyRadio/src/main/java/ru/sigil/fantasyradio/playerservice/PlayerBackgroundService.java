package ru.sigil.fantasyradio.playerservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.RadioStream;

/**
 * Created by namelessone
 * on 16.06.17.
 */

public class PlayerBackgroundService extends Service {
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;

    @Inject
    IPlayer<RadioStream> player;

    PlayerBackgroundServiceBinder binder = new PlayerBackgroundServiceBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");
            wifiLock.acquire();
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyWakelockTag");
            wakeLock.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.addPlayStateChangedListener((state) ->
        {
            try {
                switch (state) {
                    case STOP:
                    case PAUSE:
                        wifiLock.release();
                        wakeLock.release();
                        break;
                    default:
                        wifiLock.acquire();
                        wakeLock.acquire();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public void onDestroy() {
        player.stop();
    }

    @Override
    public void onLowMemory() {

    }

    public class PlayerBackgroundServiceBinder extends Binder {
        public IPlayer<RadioStream> getPlayer() {
            return player;
        }
    }
}
