package ru.sigil.fantasyradio.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.Player;
import ru.sigil.fantasyradio.saved.MP3Saver;
import ru.sigil.fantasyradio.schedule.ScheduleEntityesCollection;
import ru.sigil.fantasyradio.schedule.ScheduleParser;
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
    MP3Saver providesMP3Saver()
    {
        return new MP3Saver();
    }

    @Provides
    @Singleton
    IPlayer providesPlayer(MP3Saver mp3Saver)
    {
        return new Player(mp3Saver);
    }

    @Provides
    @Singleton
    ScheduleEntityesCollection providesScheduleEntityesCollection()
    {
        return new ScheduleEntityesCollection();
    }

    @Provides
    ScheduleParser providesScheduleParser(ScheduleEntityesCollection scheduleEntityesCollection)
    {
        return new ScheduleParser(scheduleEntityesCollection);
    }

    @Provides
    FileDownloader providesFileDownloader()
    {
        return new FileDownloader();
    }
}