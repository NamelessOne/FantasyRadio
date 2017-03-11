package ru.sigil.fantasyradio;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.un4seen.bass.BASS;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.log.DefaultLogger;
import ru.sigil.log.LogManager;

@ReportsCrashes(formKey = "", formUri = "http://fantasyradionotifications-sigil.rhcloud.com/crash", logcatArguments = {
        "-t", "50"})
public class MyApp extends MultiDexApplication {
    private float vol = (float) 0.5;

    @Override
    public void onCreate() {
        super.onCreate();
        Bootstrap.INSTANCE.setup(this);
        try {
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        vol = BASS.BASS_GetVolume();
                        BASS.BASS_SetVolume(0);
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        BASS.BASS_SetVolume(vol);
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }catch (Exception e) {
            //TODO лучше бы знать про это событие
            e.printStackTrace();
        }
        LogManager.addLogger(new DefaultLogger());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        ErrorReporter.getInstance().checkReportsOnApplicationStart();
    }
}