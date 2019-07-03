package ru.sigil.fantasyradio.settings

import android.content.Context
import javax.inject.Inject
import android.content.SharedPreferences
import android.os.Environment.getExternalStorageDirectory
import java.io.File

private const val PREFERENCES_FILE_NAME = "Settings"
private const val PREFERENCES_SAVE_DIR_KEY = "saveDir"
private const val PREFERENCES_SAVE_DIR_DEFAULT = "fantasyradio/mp3/"
private const val PREFERENCES_LOGIN_KEY = "login"
private const val PREFERENCES_PASSWORD_KEY = "password"
private const val PREFERENCES_GRATITUDE_KEY = "gratitude"

/**
 * Created by namelessone
 * on 08.12.18.
 */
class Settings @Inject constructor(context: Context): ISettings {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * Сохраняет папку для сохраниния mp3 файлов
     * @param dir Директория для записи mp3
     */
    override fun setSaveDir(dir: String) {
        var saveDir = dir
        val f = File(getExternalStorageDirectory().toString() + saveDir)
        var success = false
        try {
            success = f.mkdirs() or f.exists()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (success) {
            if (!saveDir.endsWith("/"))
                saveDir = "$saveDir/"
            preferences.edit().putString(PREFERENCES_SAVE_DIR_KEY, saveDir).apply()
        }
    }

    override fun setLogin(login: String) {
        preferences.edit().putString(PREFERENCES_LOGIN_KEY, login).apply()
    }

    override fun setPassword(password: String) {
        preferences.edit().putString(PREFERENCES_PASSWORD_KEY, password).apply()
    }

    override fun setGratitude(gratitude: Boolean) {
        preferences.edit().putBoolean(PREFERENCES_GRATITUDE_KEY, gratitude).apply()
    }

    override fun getSaveDir(): String {
        return preferences.getString(PREFERENCES_SAVE_DIR_KEY, PREFERENCES_SAVE_DIR_DEFAULT) ?: PREFERENCES_SAVE_DIR_DEFAULT
    }

    override fun getLogin(): String {
        return preferences.getString(PREFERENCES_LOGIN_KEY, "")  ?: ""
    }

    override fun getPassword(): String {
        return preferences.getString(PREFERENCES_PASSWORD_KEY, "") ?: ""
    }

    override fun getGratitude(): Boolean {
        return preferences.getBoolean(PREFERENCES_GRATITUDE_KEY, false)
    }
}