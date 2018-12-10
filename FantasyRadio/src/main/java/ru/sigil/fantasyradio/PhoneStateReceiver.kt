package ru.sigil.fantasyradio

import com.un4seen.bass.BASS
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context


/**
 * Created by namelessone
 * on 08.12.18.
 */
class PhoneStateReceiver : BroadcastReceiver() {
    private var vol = 0.5.toFloat()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val str = intent.action
            if ("android.intent.action.PHONE_STATE" == str)
                inComing(intent)
        } catch (e: Exception) //TODO уточнить тип
        {
            e.printStackTrace()
        }
    }

    private fun inComing(intent: Intent) {
        val callState = intent.getStringExtra("state")
        if ("RINGING" == callState || "OFFHOOK" == callState) {
            vol = BASS.BASS_GetVolume()
            BASS.BASS_SetVolume(0f)
        } else if ("IDLE" == callState) {
            BASS.BASS_SetVolume(vol)
        }
    }
}