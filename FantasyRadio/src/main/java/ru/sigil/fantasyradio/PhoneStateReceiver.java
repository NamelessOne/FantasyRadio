package ru.sigil.fantasyradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.un4seen.bass.BASS;

/**
 * Created by namelessone
 * on 08.06.17.
 */

public class PhoneStateReceiver extends BroadcastReceiver {
    private float vol = (float) 0.5;

    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        try {
            String str = intent.getAction();
            if ("android.intent.action.PHONE_STATE".equals(str))
                inComing(intent);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void inComing(Intent intent) {
        String callState = intent.getStringExtra("state");
        if ("RINGING".equals(callState) || "OFFHOOK".equals(callState)) {
            vol = BASS.BASS_GetVolume();
            BASS.BASS_SetVolume(0);
        } else if ("IDLE".equals(callState)) {
            BASS.BASS_SetVolume(vol);
        }
    }
}