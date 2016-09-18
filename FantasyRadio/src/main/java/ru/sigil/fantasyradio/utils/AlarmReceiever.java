package ru.sigil.fantasyradio.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.un4seen.bass.BASS;

import ru.sigil.fantasyradio.BackgroundService.IPlayer;

public class AlarmReceiever extends BroadcastReceiver {

    private static Handler sleepHandler;
    //TODO @Inject
    private IPlayer player;

    /**
     * Глушим звук и выключаем прогу
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BASS.BASS_ChannelStop(player.getChan());
        try {
            sleepHandler.sendEmptyMessage(0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void setSleepHandler(Handler sleepHandler) {
        AlarmReceiever.sleepHandler = sleepHandler;
    }
}