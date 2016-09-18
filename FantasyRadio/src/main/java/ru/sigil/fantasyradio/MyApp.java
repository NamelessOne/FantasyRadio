package ru.sigil.fantasyradio;

import android.app.Application;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.un4seen.bass.BASS;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.log.DefaultLogger;
import ru.sigil.log.LogManager;

@ReportsCrashes(formKey = "", formUri = "http://fantasyradionotifications-sigil.rhcloud.com/crash", logcatArguments = {
        "-t", "50"})
public class MyApp extends Application {
    private float vol = (float) 0.5;

    @Override
    public void onCreate() {
        super.onCreate();
        Bootstrap.INSTANCE.setup(this);
        GoogleAnalytics.getInstance(this).newTracker("UA-43435942-1").enableAutoActivityTracking(true);
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
        LogManager.addLogger(new DefaultLogger());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        ErrorReporter.getInstance().checkReportsOnApplicationStart();
    }

    private Tracker mTracker = null;

    synchronized public Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.analytics);
        }

        return mTracker;
    }
}