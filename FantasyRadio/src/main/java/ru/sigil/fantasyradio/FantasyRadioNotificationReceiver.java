package ru.sigil.fantasyradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.FantasyRadioNotificationManager;
import ru.sigil.fantasyradio.utils.RadioStream;
import ru.sigil.fantasyradio.utils.RadioStreamFactory;

/**
 * Created by namelessone
 * on 14.10.16.
 */

public class FantasyRadioNotificationReceiver extends BroadcastReceiver {

    @Inject
    IPlayer<RadioStream> player;
    @Inject
    FantasyRadioNotificationManager notificationManager;
    @Inject
    RadioStreamFactory radioStreamFactory;

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
            notificationManager.updateNotification(player.currentTitle(), player.currentArtist(), PlayState.STOP/*player.currentState()*/);
        } else {
            //TODO
            switch (player.currentState()) {
                case STOP:
                    switch (player.currentStream().getBitrate())
                    {
                        case aac_16:
                        case aac_112:
                            player.playAAC(radioStreamFactory.createStreamWithBitrate(player.currentStream().getBitrate()));
                            break;
                        case mp3_32:
                        case mp3_96:
                        default:
                            player.play(radioStreamFactory.createStreamWithBitrate(player.currentStream().getBitrate()));
                    }
                    break;
                case PAUSE:
                    player.resume();
            }
            notificationManager.updateNotification(player.currentTitle(), player.currentArtist(), PlayState.PLAY/*player.currentState()*/);
        }
    }
}
