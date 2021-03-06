package ru.sigil.fantasyradio.settings

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface ISettings {
    fun getSaveDir(): String
    fun setSaveDir(dir: String)
    fun getLogin(): String
    fun setLogin(login: String)
    fun getPassword(): String
    fun setPassword(password: String)
    fun getGratitude(): Boolean
    fun setGratitude(gratitude: Boolean)
}