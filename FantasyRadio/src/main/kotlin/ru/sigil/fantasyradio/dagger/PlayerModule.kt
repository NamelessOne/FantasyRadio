package ru.sigil.fantasyradio.dagger

import ru.sigil.bassplayerlib.ITrackFactory
import dagger.Provides
import ru.sigil.bassplayerlib.Player
import ru.sigil.bassplayerlib.IPlayer
import ru.sigil.fantasyradio.saved.MP3Collection
import javax.inject.Singleton
import android.app.Application
import android.content.Context
import dagger.Module
import ru.sigil.bassplayerlib.listeners.IEndSyncListener
import ru.sigil.fantasyradio.currentstreraminfo.CurrentStreamInfoService
import ru.sigil.fantasyradio.currentstreraminfo.ICurrentStreamInfoService
import ru.sigil.fantasyradio.settings.ISettings
import ru.sigil.fantasyradio.settings.Settings
import ru.sigil.fantasyradio.utils.*

/**
 * Created by namelessone
 * on 01.12.18.
 */
@Module
class PlayerModule constructor(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun providesPlayer(mp3Collection: IFantasyRadioTracksCollection, trackFactory: ITrackFactory, radioStreamFactory: RadioStreamFactory): IPlayer<RadioStream> {
        val player = Player(mp3Collection, trackFactory, radioStreamFactory.createDefaultStream())
        val endSyncListener = object : IEndSyncListener {
            override fun endSync() {
                val next = mp3Collection.getNext(player.currentMP3Entity)
                player.stop()
                if (next != null) {
                   player.playFile(next)
                }
            }
        }
        player.addEndSyncListener(endSyncListener)
        return player
    }

    @Provides
    fun providesTrackFactory(): ITrackFactory {
        return TrackFactory()
    }

    @Provides
    fun providesSettings(context: Context): ISettings {
        return Settings(context)
    }

    @Provides
    fun providesFileDownloader(context: Context): IFileDownloader {
        return FileDownloader(context)
    }

    @Provides
    fun providesRadioStreamFactory(context: Context): IRadioStreamFactory {
        return RadioStreamFactory(context)
    }
    
    @Provides
    @Singleton
    fun providesFantasyRadioNotificationManager(context: Context, player: IPlayer<RadioStream>): IFantasyRadioNotificationManager {
        return FantasyRadioNotificationManager(context, player)
    }

    @Provides
    fun providesCurrentStreamInfoService(): ICurrentStreamInfoService {
        return CurrentStreamInfoService()
    }

    @Provides
    fun providesTrackCollection(context: Context): IFantasyRadioTracksCollection {
        return MP3Collection(context)
    }
}