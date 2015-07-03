package ru.sigil.fantasyradio.settings;


import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

/**
 * Настройки
 */
public class Settings {
    private static SharedPreferences settings;
    private static String saveDir;
    private static String login;
    private static String password;

    public static SharedPreferences getSettings() {
        return settings;
    }

    /**
     * Установить текущие настройки
     *
     * @param settings Текущие настройки.
     */
    public static void setSettings(SharedPreferences settings) {
        setSaveDir(settings.getString("saveDir", "/fantasyradio/mp3/"));
        setLogin(settings.getString("login", ""));
        setPassword(settings.getString("password", ""));
        Settings.settings = settings;
    }

    public static String getSaveDir() {
        return saveDir;
    }

    public static void setSaveDir(String saveDir) {
        Settings.saveDir = saveDir;
    }

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        Settings.login = login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Settings.password = password;
    }

    /**
     * Сохраняет папку для сохраниния mp3 файлов
     *
     * @param saveDir Директория для записи mp3
     */
    public static void saveSaveDir(String saveDir) {
        SharedPreferences.Editor editor = Settings.getSettings().edit();
        File f = new File(Environment.getExternalStorageDirectory()
                + saveDir);
        boolean success = false;
        try {
            success = (f.mkdirs() | f.exists());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (success) {
            if (!saveDir.endsWith("/"))
                saveDir = saveDir + "/";
            editor.putString("saveDir", saveDir);
            editor.commit();
            setSaveDir(saveDir);
        }
    }

    /**
     * Сохраняет логин и пароль от сайта
     *
     * @param login    Логин
     * @param password Пароль
     */
    public static void saveLoginAndPassword(String login, String password) {
        setLogin(login);
        setPassword(password);
        SharedPreferences.Editor editor = Settings.getSettings().edit();
        editor.putString("login", getLogin());
        editor.putString("password", getPassword());
        editor.commit();
    }
}
