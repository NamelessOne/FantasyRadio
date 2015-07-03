package ru.sigil.fantasyradio.utils;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.un4seen.bass.BASS;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ru.sigil.fantasyradio.RadioEntity;
import ru.sigil.fantasyradio.saved.MP3Entity;

/**
 * God object. Информацию о проигрываемом файле/потоке.
 * Singleton.
 */
public class PlayerState {
    private final static String[] ReservedChars = {"|", "\\", "?", "*", "<", "\"",
            ":", ">", "+", "[", "]", "/", "'", "%"};
    public static final int AAC16 = 0;
    public static final int MP332 = 1;
    public static final int MP364 = 2;
    public static final int AAC112 = 3;
    public static final int MP396 = 4;
    private static PlayerState instance = new PlayerState();
    private int current_stream = 0;
    private RadioEntity currentRadioEntity;
    private MP3Entity currentMP3Entity;
    private String CurrentArtist = "";
    private String CurrentSong = "";
    private Handler disablePlayerHandler;
    private boolean recActive;
    private Handler recordFinishedHandler;
    // ----------------------------rec-------------------------
    private String recArtist;
    private String recTitle;
    private String recDirectory;
    private String recTime;
    private String recURL;
    private File f;

    private PlayerState() {
    }

    public static boolean isPlaying() {
        return BASS.BASS_ChannelIsActive(BASSUtil.getChan()) == BASS.BASS_ACTIVE_PLAYING
                | BASS.BASS_ChannelIsActive(BASSUtil.getChan()) == BASS.BASS_ACTIVE_STALLED;
    }

    public RadioEntity getCurrentRadioEntity() {
        return currentRadioEntity;
    }

    public void setCurrentRadioEntity(RadioEntity newCurrentEntity) {
        currentRadioEntity = newCurrentEntity;
        if (newCurrentEntity != null) {
            CurrentArtist = currentRadioEntity.getArtist();
            CurrentSong = currentRadioEntity.getTitle();
            currentMP3Entity = null;
        }
        ProgramNotification.changeSongName(getCurrentSong(),
                getCurrentArtist());
    }

    public MP3Entity getCurrentMP3Entity() {
        return currentMP3Entity;
    }

    public void setCurrentMP3Entity(MP3Entity newCurrentMP3Entity) {
        currentRadioEntity = null;
        getDisablePlayerHandler().sendEmptyMessage(0);
        currentMP3Entity = newCurrentMP3Entity;
        if (newCurrentMP3Entity != null) {
            CurrentArtist = currentMP3Entity.getArtist();
            CurrentSong = currentMP3Entity.getTitle();
        }
        ProgramNotification.changeSongName(getCurrentSong(),
                getCurrentArtist());// !!!!
    }

    public String getCurrentArtist() {
        return CurrentArtist;
    }

    public String getCurrentSong() {
        return CurrentSong;
    }

    public int getCurrent_stream() {
        return current_stream;
    }

    public void setCurrent_stream(int current_stream) {
        this.current_stream = current_stream;
    }

    private Handler getDisablePlayerHandler() {
        return disablePlayerHandler;
    }

    public void setDisablePlayerHandler(Handler disablePlayerHandler) {
        this.disablePlayerHandler = disablePlayerHandler;
    }

    public boolean isRecActive() {
        return recActive;
    }

    /**
     * Устанавливает состояние записи.
     *
     * @param newRecActive Если true - создаётся временный файл, куда пишется поток. Иначе выполняется
     *                     RecordFinishedHandler
     * @see ru.sigil.fantasyradio.MainActivity#recordFinishedHandler
     */
    public void setRecActive(boolean newRecActive) {
        // -------------------------------------
        if (!newRecActive) {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("artist", getRecArtist());
            b.putString("title", getRecTitle());
            b.putString("directory", getRecDirectory());
            b.putString("time", getRecTime());
            // --------------------------------
            b.putString("URL", getRecURL());
            // --------------------------------
            msg.setData(b);
            recordFinishedHandler.sendMessage(msg);
        } else {
            File dir = new File(Environment.getExternalStorageDirectory()
                    + "/fantasyradio/records/");
            dir.mkdirs();
            String fileName = this.getCurrentSong();
            if (fileName == null)
                fileName = "rec";
            if (fileName.length() < 2)
                fileName = "rec";
            try {
                for (String s : ReservedChars) {
                    fileName = fileName.replace(s, "_");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileName = new String(fileName.getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            f = new File(dir.toString() + "/" + fileName);
            try {
                if (!f.createNewFile()) {
                    File f2 = new File(f.toString()
                            + System.currentTimeMillis());
                    f = f2;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setRecDirectory(f.toString());
        }
        // -------------------------------------
        this.recActive = newRecActive;
    }

    public void setRecordFinishedHandler(Handler recordFinishedHandler) {
        this.recordFinishedHandler = recordFinishedHandler;
    }

    private String getRecArtist() {
        return recArtist;
    }

    public void setRecArtist(String recArtist) {
        this.recArtist = recArtist;
    }

    private String getRecTitle() {
        return recTitle;
    }

    public void setRecTitle(String recTitle) {
        this.recTitle = recTitle;
    }

    private String getRecDirectory() {
        return recDirectory;
    }

    private void setRecDirectory(String recDirectory) {
        this.recDirectory = recDirectory;
    }

    private String getRecTime() {
        return recTime;
    }

    public void setRecTime(String recTime) {
        this.recTime = recTime;
    }

    private String getRecURL() {
        return recURL;
    }

    public void setRecURL(String recURL) {
        this.recURL = recURL;
    }

    public File getF() {
        return f;
    }

    public static PlayerState getInstance() {
        return instance;
    }

}
