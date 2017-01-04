package ru.sigil.fantasyradio.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.fantasyradio.dagger.Bootstrap;

public class AlarmReceiever extends BroadcastReceiver {

    private static Handler sleepHandler;
    @Inject
    IPlayer player;

    public AlarmReceiever()
    {
        super();
        Bootstrap.INSTANCE.getBootstrap().inject(this);
    }

    /**
     * Глушим звук и выключаем прогу
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            player.stop();
            sleepHandler.sendEmptyMessage(0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void setSleepHandler(Handler sleepHandler) {
        AlarmReceiever.sleepHandler = sleepHandler;
    }
}