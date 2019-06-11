package ru.sigil.fantasyradio

import org.acra.ACRA
import ru.sigil.log.DefaultLogger
import android.app.Application
import android.content.Context
import org.acra.sender.HttpSender
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.annotation.AcraCore
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.log.LogManager

/**
 * Created by namelessone
 * on 08.12.18.
 */
@AcraCore(buildConfigClass = BuildConfig::class, reportFormat = StringFormat.JSON)
//@AcraHttpSender(uri = "https://collector.tracepot.com/64a4a681", httpMethod = HttpSender.Method.POST)
@AcraHttpSender(uri = "http://31.163.196.172:36484/Crash", httpMethod = HttpSender.Method.POST)
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!ACRA.isACRASenderServiceProcess()) {
            Bootstrap.INSTANCE.setup(this)
            LogManager.addLogger(DefaultLogger())
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        // The following line triggers the initialization of ACRA
        ACRA.init(this)
    }
}