package ru.sigil.fantasyradio.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.ITrack;
import ru.sigil.bassplayerlib.ITrackFactory;
import ru.sigil.bassplayerlib.Player;
import ru.sigil.fantasyradio.saved.MP3Collection;
import ru.sigil.fantasyradio.utils.RadioStream;
import ru.sigil.fantasyradio.utils.RadioStreamFactory;
import ru.sigil.fantasyradio.utils.TrackFactory;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
@Module
public class PlayerModule {

    private final Application mApplication;

    public PlayerModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    IPlayer<RadioStream> providesPlayer(MP3Collection mp3Collection, ITrackFactory trackFactory, RadioStreamFactory radioStreamFactory) {
        IPlayer<RadioStream> player = new Player<>(mp3Collection, trackFactory, radioStreamFactory.createDefaultStream());
        player.addEndSyncListener(() -> {
            ITrack next = mp3Collection.getNext(player.getCurrentMP3Entity());
            player.stop();
            if (next != null) {
                player.playFile(next);
            }
        });
        return player;
    }

    @Provides
    ITrackFactory providesTrackFactory() {
        return new TrackFactory();
    }
}