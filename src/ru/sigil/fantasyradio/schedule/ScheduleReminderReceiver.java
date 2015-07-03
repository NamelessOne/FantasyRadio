package ru.sigil.fantasyradio.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Напоминает юзеру о том, что выбранная передача из расписания должна начаться
 */
public class ScheduleReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"OLOLOLO!!111",Toast.LENGTH_LONG);
    }
}
