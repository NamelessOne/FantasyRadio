package ru.sigil.fantasyradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.sigil.fantasyradio.BackgroundService.Bitrate;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.BitrtesResolver;
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
    @Inject
    BitrtesResolver bitratesResolver;

    public FantasyRadioNotificationReceiver() {
        Bootstrap.INSTANCE.getBootstrap().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(FantasyRadioNotificationManager.ACTION);
        if (FantasyRadioNotificationManager.PAUSE.equals(action)) {
            //TODO
            switch (player.currentState()) {
                case PLAY:
                case BUFFERING:
                    player.stop();
                    break;
                case PLAY_FILE:
                    player.pause();
            }
            notificationManager.createNotification(player.currentTitle(), player.currentArtist(), PlayState.STOP/*player.currentState()*/);
        } else {
            //TODO
            switch (player.currentState()) {
                case STOP:
                    String url = bitratesResolver.getUrl(player.currentBitrate());
                    player.play(url, player.currentBitrate());
                    break;
                case PAUSE:
                    player.resume();
            }
            notificationManager.createNotification(player.currentTitle(), player.currentArtist(), PlayState.PLAY/*player.currentState()*/);
        }
    }
}
