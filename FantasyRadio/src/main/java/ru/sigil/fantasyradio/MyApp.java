package ru.sigil.fantasyradio;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.log.DefaultLogger;
import ru.sigil.log.LogManager;

@ReportsCrashes(reportType = HttpSender.Type.JSON, formUri = "https://collector.tracepot.com/64a4a681", logcatArguments = {
        "-t", "50"})
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(!ACRA.isACRASenderServiceProcess()) {
            Bootstrap.INSTANCE.setup(this);
            LogManager.addLogger(new DefaultLogger());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}