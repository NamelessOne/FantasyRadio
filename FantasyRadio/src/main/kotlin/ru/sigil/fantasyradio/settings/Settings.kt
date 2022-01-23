package ru.sigil.fantasyradio.settings

import android.content.Context
import javax.inject.Inject
import android.content.SharedPreferences
import java.io.File
import android.os.Environment




private const val PREFERENCES_FILE_NAME = "Settings"
private const val PREFERENCES_SAVE_DIR_KEY = "saveDir"
private const val PREFERENCES_SAVE_DIR_DEFAULT = "fantasyradio/mp3/"
private const val PREFERENCES_GRATITUDE_KEY = "gratitude"

/**
 * Created by namelessone
 * on 08.12.18.
 */
class Settings @Inject constructor(context: Context): ISettings {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    private val externalStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)

    /**
     * Сохраняет папку для сохраниния mp3 файлов
     * @param dir Директория для записи mp3
     */
    override fun setSaveDir(dir: String) {
        var saveDir = dir
        if(!saveDir.startsWith("/")) {
            saveDir = "/$saveDir"
        }
        val f = File(externalStorageDir?.absolutePath + saveDir)
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

    override fun setGratitude(gratitude: Boolean) {
        preferences.edit().putBoolean(PREFERENCES_GRATITUDE_KEY, gratitude).apply()
    }

    override fun getSaveDir(): String {
        return preferences.getString(PREFERENCES_SAVE_DIR_KEY, PREFERENCES_SAVE_DIR_DEFAULT) ?: PREFERENCES_SAVE_DIR_DEFAULT
    }

    override fun getAbsoluteSaveDir(): String {
        return File(externalStorageDir?.absolutePath + getSaveDir()).absolutePath
    }

    override fun getGratitude(): Boolean {
        return preferences.getBoolean(PREFERENCES_GRATITUDE_KEY, false)
    }
}