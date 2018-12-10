package ru.sigil.fantasyradio.settings

import android.content.SharedPreferences

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface ISettings {
    fun moveLocalSettingsToGlobal(localPreferences: SharedPreferences)
    fun getSaveDir(): String
    fun setSaveDir(dir: String)
    fun getLogin(): String
    fun setLogin(login: String)
    fun getPassword(): String
    fun setPassword(password: String)
    fun getGratitude(): Boolean
    fun setGratitude(gratitude: Boolean)
}