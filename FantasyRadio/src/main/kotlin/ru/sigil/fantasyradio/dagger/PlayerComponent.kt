package ru.sigil.fantasyradio.dagger

import dagger.Component
import ru.sigil.fantasyradio.RadioFragment
import javax.inject.Singleton
import ru.sigil.fantasyradio.saved.SavedFragment
import ru.sigil.fantasyradio.playerservice.PlayerBackgroundService
import ru.sigil.fantasyradio.currentstreraminfo.CurrentStreamInfoService
import ru.sigil.fantasyradio.utils.AlarmReceiver
import ru.sigil.fantasyradio.utils.FantasyRadioNotificationManager
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver
import ru.sigil.fantasyradio.saved.MP3ArrayAdapter
import ru.sigil.fantasyradio.schedule.ScheduleFragment
import ru.sigil.fantasyradio.TabHoster
import ru.sigil.fantasyradio.archive.ArchiveFragment
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider
import ru.sigil.fantasyradio.MyApp
import ru.sigil.fantasyradio.settings.SettingsActivity

/**
 * Created by namelessone
 * on 01.12.18.
 */

@Singleton
@Component(modules = [PlayerModule::class])
interface PlayerComponent {
    fun inject(radioFragment: RadioFragment)

    fun inject(savedFragment: SavedFragment)

    fun inject(fantasyRadioWidgetProvider: FantasyRadioWidgetProvider)

    fun inject(archiveFragment: ArchiveFragment)

    fun inject(tabHoster: TabHoster)

    fun inject(scheduleFragment: ScheduleFragment)

    fun inject(mp3ArrayAdapter: MP3ArrayAdapter)

    fun inject(fantasyRadioNotificationReceiver: FantasyRadioNotificationReceiver)

    fun inject(fantasyRadioNotificationManager: FantasyRadioNotificationManager)

    fun inject(alarmReceiver: AlarmReceiver)

    fun inject(alarmReceiever: CurrentStreamInfoService)

    fun inject(playerBackgroundService: PlayerBackgroundService)

    fun inject(settingsASctivity: SettingsActivity)

    object Initializer {
        fun init(app: MyApp): PlayerComponent {
            return DaggerPlayerComponent.builder()
                    .playerModule(PlayerModule(app))
                    .build()
        }
    }// No instances.
}