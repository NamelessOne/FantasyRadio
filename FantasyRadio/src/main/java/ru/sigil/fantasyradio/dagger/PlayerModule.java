package ru.sigil.fantasyradio.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.Player;
import ru.sigil.fantasyradio.saved.MP3Collection;
import ru.sigil.fantasyradio.schedule.ScheduleEntityesCollection;
import ru.sigil.fantasyradio.schedule.ScheduleParser;
import ru.sigil.fantasyradio.utils.BitratesResolver;
import ru.sigil.fantasyradio.utils.FantasyRadioNotificationManager;
import ru.sigil.fantasyradio.utils.FileDownloader;

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
    MP3Collection providesMP3Collection() {
        return new MP3Collection(mApplication.getBaseContext());
    }

    @Provides
    @Singleton
    IPlayer providesPlayer(MP3Collection mp3Collection) {
        return new Player(mp3Collection);
    }

    @Provides
    @Singleton
    ScheduleEntityesCollection providesScheduleEntityesCollection() {
        return new ScheduleEntityesCollection();
    }

    @Provides
    ScheduleParser providesScheduleParser(ScheduleEntityesCollection scheduleEntityesCollection) {
        return new ScheduleParser(scheduleEntityesCollection);
    }

    @Provides
    FileDownloader providesFileDownloader() {
        return new FileDownloader();
    }

    @Provides
    @Singleton
    FantasyRadioNotificationManager providesNotificationManager() {
        return new FantasyRadioNotificationManager(mApplication.getBaseContext());
    }

    @Provides
    BitratesResolver providesBitratesResolver() {
        return new BitratesResolver(mApplication.getBaseContext());
    }
}