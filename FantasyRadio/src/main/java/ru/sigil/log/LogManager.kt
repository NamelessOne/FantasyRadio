package ru.sigil.log

import java.util.*

private const val AppTAG = "FantasyRadio"

/**
 * Created by namelessone
 * on 09.12.18.
 */
object LogManager {
    private val TAG = LogManager::class.java.simpleName
    private val loggers = LinkedList<ICustomLogger>()

    private var isDebug = true

    private fun handle(logLevel: LogLevel, tag: String, component: String, message: String, e: Exception?) {
        synchronized(loggers) {
            if (isDebug) {
                for (logger in loggers) {
                    logger.handle(logLevel, tag, component, message, e!!)
                }
            }
        }
    }

    fun addLogger(logger: ICustomLogger?) {
        synchronized(loggers) {
            if (null == logger) {
                e(TAG, "CmdLogHandler cannot be null")
            } else {
                loggers.add(logger)
            }
        }
    }

    fun d(component: String, message: String) {
        handle(LogLevel.DEBUG, AppTAG, component, message, null)
    }

    fun d(component: String, message: String, vararg args: Any) {
        handle(LogLevel.DEBUG, AppTAG, component, String.format(message, *args), null)
    }

    fun d(component: String, message: String, e: Exception) {
        handle(LogLevel.DEBUG, AppTAG, component, message, e)
    }

    fun e(component: String, message: String, e: Exception) {
        handle(LogLevel.ERROR, AppTAG, component, message, e)
    }

    fun e(component: String, message: String) {
        handle(LogLevel.ERROR, AppTAG, component, message, null)
    }

    fun w(component: String, message: String) {
        handle(LogLevel.WARNING, AppTAG, component, message, null)
    }

    fun w(component: String, message: String, vararg args: Any) {
        handle(LogLevel.WARNING, AppTAG, component, String.format(message, *args), null)
    }
}