package ru.sigil.fantasyradio.utils

import android.content.BroadcastReceiver
import android.content.Context
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.bassplayerlib.IPlayer
import javax.inject.Inject
import android.content.Intent



/**
 * Created by namelessone
 * on 08.12.18.
 */
class AlarmReceiver: BroadcastReceiver() {
    @set:Inject
    var player: IPlayer<RadioStream>? = null
    init {
        Bootstrap.INSTANCE.getBootstrap().inject(this)
    }

    /**
     * Глушим звук и выключаем прогу
     * @param context Контекст приложения
     * @param intent Интент вызова
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            player?.stop()
            SleepHandlerContainer.sleepHandler?.sendEmptyMessage(0)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }
}