package ru.sigil.fantasyradio.playerservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.RadioStream;
import ru.sigil.log.LogManager;

/**
 * Created by namelessone
 * on 16.06.17.
 */

public class PlayerBackgroundService extends Service {
    private static final String TAG = PlayerBackgroundService.class.getSimpleName();

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
        player.addTitleChangedListener((title) -> LogManager.e(TAG, title));
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
