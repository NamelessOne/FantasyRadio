package ru.sigil.fantasyradio.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.sigil.fantasyradio.BackgroundService.Player;
import ru.sigil.fantasyradio.TabHoster;
import ru.sigil.fantasyradio.settings.Settings;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
@Module
public class PlayerModule {

    Application mApplication;

    public PlayerModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Player providesPlayer()
    {
        return new Player();
    }
}