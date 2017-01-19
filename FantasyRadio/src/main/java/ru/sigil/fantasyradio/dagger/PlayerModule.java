package ru.sigil.fantasyradio.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.ITrackFactory;
import ru.sigil.bassplayerlib.Player;
import ru.sigil.fantasyradio.saved.MP3Collection;
import ru.sigil.fantasyradio.utils.TrackFactory;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
@Module
public class PlayerModule {

    final Application mApplication;

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
    IPlayer providesPlayer(MP3Collection mp3Collection, ITrackFactory trackFactory) {
        return new Player(mp3Collection, trackFactory);
    }

    @Provides
    ITrackFactory providesTrackFactory() {
        return new TrackFactory();
    }
}