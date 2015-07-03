package ru.sigil.fantasyradio.utils;

import com.un4seen.bass.BASS;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public abstract class BASSUtil {
    private static int chan;

    public static int getChan() {
        return chan;
    }

    public static void setChan(int chan) {
        BASSUtil.chan = chan;
    }

    public static BASS.DOWNLOADPROC StatusProc = new BASS.DOWNLOADPROC() {
        /**
         * Тут можно получить байты потока. Используется для записи.
         * @param buffer Данные потока
         * @param length Длина куска данных потока
         * @param user BASS.dll магия. ХЗ что это
         */
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user) {
            if (PlayerState.getInstance().isRecActive()) {
                byte[] ba = new byte[length];
                FileOutputStream fos = null;
                try {
                    buffer.get(ba);
                    //1111
                    fos = new FileOutputStream(PlayerState.getInstance().getF().toString(), true);
                    fos.write(ba);
                    PlayerState.getInstance().setRecArtist("");
                    PlayerState.getInstance().setRecTime("");
                    PlayerState.getInstance().setRecTitle(PlayerState.getInstance().getCurrentSong());
                    PlayerState.getInstance().setRecURL("");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    };
}
