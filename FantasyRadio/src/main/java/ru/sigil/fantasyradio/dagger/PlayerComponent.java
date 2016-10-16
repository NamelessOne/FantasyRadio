package ru.sigil.fantasyradio.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.sigil.fantasyradio.FantasyRadioNotificationReceiver;
import ru.sigil.fantasyradio.MyApp;
import ru.sigil.fantasyradio.RadioFragment;
import ru.sigil.fantasyradio.TabHoster;
import ru.sigil.fantasyradio.archieve.ArchieveFragment;
import ru.sigil.fantasyradio.saved.MP3ArrayAdapter;
import ru.sigil.fantasyradio.saved.SavedFragment;
import ru.sigil.fantasyradio.schedule.ScheduleFragment;
import ru.sigil.fantasyradio.utils.DownloadThread;
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
@Singleton
@Component(modules = PlayerModule.class)
public interface PlayerComponent {
    void inject(RadioFragment radioFragment);

    void inject(SavedFragment savedFragment);

    void inject(FantasyRadioWidgetProvider fantasyRadioWidgetProvider);

    void inject(ArchieveFragment archieveFragment);

    void inject(TabHoster tabHoster);

    void inject(ScheduleFragment scheduleFragment);

    void inject(MP3ArrayAdapter mp3ArrayAdapter);

    void inject(DownloadThread downloadThread);

    void inject(FantasyRadioNotificationReceiver fantasyRadioNotificationReceiver);

    final class Initializer {
        private Initializer() {
        } // No instances.

        public static PlayerComponent init(MyApp app) {
            return DaggerPlayerComponent.builder()
                    .playerModule(new PlayerModule(app))
                    .build();
        }
    }
}