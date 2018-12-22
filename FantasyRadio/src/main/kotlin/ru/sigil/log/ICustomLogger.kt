package ru.sigil.log

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface ICustomLogger {
    fun handle(logLevel: LogLevel, tag: String, component: String, message: String, e: Exception?)
}