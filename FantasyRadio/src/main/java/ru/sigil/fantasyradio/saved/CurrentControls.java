package ru.sigil.fantasyradio.saved;

import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.un4seen.bass.BASS;

import ru.sigil.fantasyradio.utils.BASSUtil;

public abstract class CurrentControls {
    private static Handler rewindMP3Handler;
    private static SeekBar currentMP3SeekBar;
    private static SeekBar currentVolumeSeekBar;

    /**
     * Хэндлер перемотки
     *
     * @see ru.sigil.fantasyradio.saved.SavedActivity#rewindMp3Handler
     */
    private static Handler getRewindMP3Handler() {
        return rewindMP3Handler;
    }

    /**
     * @param rewindMP3Handler Хэндер перемотки сохранённой mp3.
     * @see ru.sigil.fantasyradio.saved.SavedActivity#rewindMp3Handler
     */
    public static void setRewindMP3Handler(Handler rewindMP3Handler) {
        CurrentControls.rewindMP3Handler = rewindMP3Handler;
    }

    /**
     * Устанавливаем текущий контроллер перемотки. Нужно при переключении между Activity.
     *
     * @param newCurrentMP3SeekBar текущий контроллер перемотки
     */
    public static void setCurrentMP3SeekBar(SeekBar newCurrentMP3SeekBar) {
        currentMP3SeekBar = newCurrentMP3SeekBar;
        currentMP3SeekBar
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        long file_length = 0;
                        long pos;
                        // --------------------------------------------
                        try {
                            file_length = BASS.BASS_ChannelGetLength(
                                    BASSUtil.getChan(), BASS.BASS_POS_BYTE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // BASSUtil.streamOffset = ((seekBar.getProgress() *
                        // BASSUtil
                        // .getOriginalLength()) / 100);
                        pos = (seekBar.getProgress() * file_length) / 100;
                        Message progressMsg = new Message();
                        progressMsg.arg1 = (int) pos;
                        getRewindMP3Handler().sendMessage(progressMsg);
                    }
                });
    }

    /**
     * Устанавливаем текущий контроллер громкости и ставим на нём текущий уровень Нужно при переключении между Activity.
     *
     * @param newCurrentVolumeSeekBar текущий контроллер громкости
     */
    public static void setCurrentVolumeSeekBar(SeekBar newCurrentVolumeSeekBar) {
        currentVolumeSeekBar = newCurrentVolumeSeekBar;
        currentVolumeSeekBar.setProgress((int) (BASS.BASS_GetVolume() * 100));
        currentVolumeSeekBar
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        if (fromUser)
                            BASS.BASS_SetVolume(((float) progress) / 100);
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }

    /**
     * Текущий уровень громкости BASS
     *
     * @return Текущий уровень громкости BASS
     */
    public static float getCurrentVolume() {
        return BASS.BASS_GetVolume();
    }
}
