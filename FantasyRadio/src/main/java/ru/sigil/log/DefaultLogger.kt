package ru.sigil.log

import android.util.Log

/**
 * Created by namelessone
 * on 08.12.18.
 */
class DefaultLogger : ICustomLogger {
    @Synchronized
    override fun handle(logLevel: LogLevel, tag: String, component: String, message: String, e: Exception) {
        when (logLevel) {
            LogLevel.DEBUG -> Log.d(tag, "($component) $message", e)
            LogLevel.WARNING -> Log.w(tag, "($component) $message", e)
            LogLevel.ERROR -> Log.e(tag, "($component) $message", e)
        }
    }
}