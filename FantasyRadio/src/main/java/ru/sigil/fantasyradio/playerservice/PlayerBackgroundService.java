package ru.sigil.fantasyradio.playerservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.RadioStream;

/**
 * Created by namelessone
 * on 16.06.17.
 */

public class PlayerBackgroundService extends Service {
    private WifiManager.WifiLock lock;

    @Inject
    IPlayer<RadioStream> player;
    @Inject
    Context context;

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
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");
            lock.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.addPlayStateChangedListener((state) ->
        {
            try {
                switch (state) {
                    case STOP:
                    case PAUSE:
                        lock.release();
                    default:
                        lock.acquire();
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
