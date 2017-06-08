package ru.sigil.fantasyradio;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.log.DefaultLogger;
import ru.sigil.log.LogManager;

@ReportsCrashes(formKey = "", formUri = "http://fantasyradionotifications-sigil.rhcloud.com/crash", logcatArguments = {
        "-t", "50"})
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bootstrap.INSTANCE.setup(this);
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