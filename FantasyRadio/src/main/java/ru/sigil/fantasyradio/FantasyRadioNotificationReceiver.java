package ru.sigil.fantasyradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.FantasyRadioNotificationManager;

/**
 * Created by namelessone
 * on 14.10.16.
 */

public class FantasyRadioNotificationReceiver extends BroadcastReceiver {

    @Inject
    IPlayer player;
    @Inject
    FantasyRadioNotificationManager notificationManager;

    public FantasyRadioNotificationReceiver()
    {
        Bootstrap.INSTANCE.getBootstrap().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("ACTION");
        if("PAUSE".equals(action))
        {
            //TODO
            notificationManager.createNotification(player.currentTitle(), player.currentArtist(), PlayState.STOP/*player.currentState()*/);
        }
        else
        {
            //TODO
            notificationManager.createNotification(player.currentTitle(), player.currentArtist(), PlayState.PLAY/*player.currentState()*/);
        }
    }
}
