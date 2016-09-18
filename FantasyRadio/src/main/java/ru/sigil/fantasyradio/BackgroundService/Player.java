package ru.sigil.fantasyradio.BackgroundService;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_AAC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.sigil.fantasyradio.utils.PlayerState;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
public class Player implements IPlayer {
    private final List<IPlayerEventListener> eventListeners = new ArrayList<>();
    private String title;
    private String author;
    private Bitrate bitrate;
    private PlayState playState = PlayState.STOP;
    private boolean rec = false;
    private String recDirectory;

    private int chan;

    public int getChan() {
        return chan;
    }

    public float getVolume() {
        return BASS.BASS_GetVolume();
    }

    @Override
    public void rewind(int offset) {
        BASS.BASS_ChannelSetPosition(getChan(), offset,
                BASS.BASS_POS_BYTE);
        if (!(BASS.BASS_ChannelIsActive(getChan()) == BASS.BASS_ACTIVE_PAUSED)) {
            BASS.BASS_ChannelPlay(getChan(), false);
        }
    }

    @Override
    public void rec(boolean isActive) {
            // -------------------------------------
            if (!isActive) {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("artist", author);
                b.putString("title", title);
                b.putString("directory", recDirectory);
                b.putString("time", "");
                // --------------------------------
                b.putString("URL", "");
                // --------------------------------
                msg.setData(b);
            } else {
                File dir = new File(Environment.getExternalStorageDirectory()
                        + "/fantasyradio/records/");
                dir.mkdirs();
                String fileName = title;
                if (fileName == null)
                    fileName = "rec";
                if (fileName.length() < 2)
                    fileName = "rec";
                try {
                    for (String s : RESERVED_CHARS) {
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
                File f = new File(dir.toString() + "/" + fileName);
                try {
                    if (!f.createNewFile()) {
                        File f2 = new File(f.toString()
                                + System.currentTimeMillis());
                        f = f2;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recDirectory = f.toString();
            }
            // -------------------------------------
            setRecActive(isActive);
    }

    @Override
    public long getFileLength() {
        return BASS.BASS_ChannelGetLength(
                getChan(), BASS.BASS_POS_BYTE);
    }

    public void setChan(int chan) {
        this.chan = chan;
    }

    public void playAAC(String url,  Bitrate bitrate) {
        try {
            setBitrate(bitrate);
            new Thread(new OpenURLAAC(url)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playFile(String file) {
        //TODO
    }

    public Player()
    {
        BASS.BASS_Free();
        BASS.BASS_Init(-1, 44100, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_NET_PLAYLIST, 1);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_NET_PREBUF, 0);
        BASS.BASS_SetVolume((float) 0.5);
    }

    @Override
    public void stop() {
        BASS.BASS_StreamFree(getChan());
        setPlayState(PlayState.STOP);
        setAuthor("");
        setTitle("");
        for (IPlayerEventListener listener : eventListeners) {
            try {
                listener.onStop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addEventListener(IPlayerEventListener listener) {
        eventListeners.add(listener);
    }

    @Override
    public void removeEventListener(IPlayerEventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        eventListeners.clear();
    }

    @Override
    public String currentTitle() {
        return title;
    }

    @Override
    public String currentArtist() {
        return author;
    }

    @Override
    public Bitrate currentBitrate() {
        return bitrate;
    }

    @Override
    public PlayState currentState() {
        //TODO
        return playState;
    }

    private void setPlayState(PlayState state) {
        playState = state;
        for (IPlayerEventListener listener : eventListeners) {
            try {
                listener.onPlayStateChanged(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isRecActive() {
        //TODO
        return rec;
    }

    private void setBitrate(Bitrate bitrate) {
        this.bitrate = bitrate;
    }

    /**
     * Начинаем играть поток
     *
     * @param url URL потока. Не AAC
     */
    @Override
    public void play(String url, Bitrate bitrate) {
        try {
            setBitrate(bitrate);
            new Thread(new OpenURL(url)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRecActive(boolean recActive) {
        rec = recActive;
        for (IPlayerEventListener listener : eventListeners) {
            try {
                listener.onRecStateChanged(recActive);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Магия BASS.dll для AAC потока
     */
    private class OpenURLAAC implements Runnable {
        String url;

        public OpenURLAAC(String p) {
            url = p;
        }

        public void run() {
            int r;
            synchronized (lock) { // make sure only 1 thread at a time can
                // do
                // the following
                r = ++req; // increment the request counter for this request
            }
            BASS.BASS_StreamFree(chan); // close old stream
            int c = BASS_AAC
                    .BASS_AAC_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK
                            | BASS.BASS_STREAM_STATUS
                            | BASS.BASS_STREAM_AUTOFREE, StatusProc, r); // open
            // URL
            synchronized (lock) {
                if (r != req) { // there is a newer request, discard this
                    // stream
                    if (c != 0)
                        BASS.BASS_StreamFree(c);
                    return;
                }
                setChan(c); // this is now the current stream
            }

            if (chan != 0) {
                handler.postDelayed(timer, 50);
            } // start prebuffer
            // monitoring
        }
    }

    /**
     * Магия BASS.dll
     */
    private class OpenURL implements Runnable {
        String url;

        public OpenURL(String p) {
            url = p;
        }

        public void run() {
            int r;
            synchronized (lock) { // make sure only 1 thread at a time can
                // do
                // the following
                r = ++req; // increment the request counter for this request
            }
            BASS.BASS_StreamFree(getChan()); // close old stream
            int c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK
                            | BASS.BASS_STREAM_STATUS | BASS.BASS_STREAM_AUTOFREE,
                    StatusProc, r); // open URL
            synchronized (lock) {
                if (r != req) { // there is a newer request, discard this
                    // stream
                    if (c != 0)
                        BASS.BASS_StreamFree(c);
                    return;
                }
                setChan(c); // this is now the current stream
            }

            if (getChan() != 0) { // failed to open
                handler.postDelayed(timer, 50); // start prebuffer
                // monitoring
            }
        }

    }

    private final Object lock = new Object();
    private int req; // request number/counter
    private Handler handler = new Handler();
    private Runnable timer = new Runnable() {
        public void run() {
            // monitor prebuffering progress
            long progress = BASS.BASS_StreamGetFilePosition(
                    getChan(), BASS.BASS_FILEPOS_BUFFER)
                    * 100
                    / BASS.BASS_StreamGetFilePosition(getChan(),
                    BASS.BASS_FILEPOS_END); // percentage of buffer
            // filled
            if (progress > 75
                    || BASS.BASS_StreamGetFilePosition(getChan(),
                    BASS.BASS_FILEPOS_CONNECTED) == 0) { // over 75%
                // full
                // (or
                // end
                // of
                // download)
                // get the broadcast name and URL
                String[] icy = (String[]) BASS.BASS_ChannelGetTags(
                        getChan(), BASS.BASS_TAG_ICY);
                if (icy == null)
                    icy = (String[]) BASS.BASS_ChannelGetTags(
                            getChan(), BASS.BASS_TAG_HTTP); // no
                // ICY
                // tags,
                // try
                // HTTP
                // get the stream title and set sync for subsequent titles
                DoMeta();
                BASS.BASS_ChannelSetSync(getChan(),
                        BASS.BASS_SYNC_META, 0, MetaSync, 0); // Shoutcast
                BASS.BASS_ChannelSetSync(getChan(),
                        BASS.BASS_SYNC_OGG_CHANGE, 0, MetaSync, 0); // Icecast/OGG
                // set sync for end of stream
                BASS.BASS_ChannelSetSync(getChan(),
                        BASS.BASS_SYNC_END, 0, EndSync, 0);
                // PLAY it!
                BASS.BASS_ChannelPlay(getChan(), false);
            } else {
                setBufferingProgress(progress);
                handler.postDelayed(this, 50);
            }
        }
    };

    private void setBufferingProgress(long progress) {
        for (IPlayerEventListener listener : eventListeners) {
            try {
                listener.onBufferingProgress(progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setTitle(String title) {
        this.title = title;
        for (IPlayerEventListener listener : eventListeners) {
            try {
                listener.onTitleChanged(title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setAuthor(String author) {
        this.author = author;
        //List<IPlayerEventListener> wrongListeners = new ArrayList<>();
        for (IPlayerEventListener listener : eventListeners) {
            try {
                listener.onAuthorChanged(author);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void DoMeta() {
        String meta = (String) BASS.BASS_ChannelGetTags(getChan(),
                BASS.BASS_TAG_META);
        if (meta != null) { // got Shoutcast metadata
            int ti = meta.indexOf("StreamTitle='");
            if (ti >= 0) {
                try {
                    String title = meta.substring(ti + 13, meta.indexOf("'", ti + 13));
                    title = new String(title.getBytes("cp-1252"), "cp-1251");
                    setTitle(title);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String[] ogg = (String[]) BASS.BASS_ChannelGetTags(
                        getChan(), BASS.BASS_TAG_OGG);
                if (ogg != null) { // got Icecast/OGG tags
                    String artist = null, title = null;
                    for (String s : ogg) {
                        if (s.regionMatches(true, 0, "artist=", 0, 7)) {
                            artist = s.substring(7);
                        } else if (s.regionMatches(true, 0, "title=", 0, 6)) {
                            title = s.substring(6);
                        }
                    }
                    if (title != null) {
                        setTitle(title);
                    }
                    if (artist != null)
                        setAuthor(artist);

                }
            }
        } else {
            setAuthor("");
            setTitle("");
        }
    }

    /**
     * Получаем метаданные (название, исполнитель и т.д.)
     */
    private BASS.SYNCPROC MetaSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            new Thread(new Runnable() {
                public void run() {
                    DoMeta();
                }
            }).start();
        }
    };

    /**
     * Выполняется после завершения проигрывания. В данный момент не используестя
     */
    private BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {

        }
    };

    private BASS.DOWNLOADPROC StatusProc = new BASS.DOWNLOADPROC() {
        /**
         * Тут можно получить байты потока. Используется для записи.
         * @param buffer Данные потока
         * @param length Длина куска данных потока
         * @param user BASS.dll магия. ХЗ что это
         */
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user) {
            if (rec) {
                byte[] ba = new byte[length];
                FileOutputStream fos = null;
                try {
                    buffer.get(ba);
                    //1111
                    fos = new FileOutputStream(recDirectory, true);
                    fos.write(ba);
                    PlayerState.getInstance().setRecArtist(author);
                    PlayerState.getInstance().setRecTime("");
                    PlayerState.getInstance().setRecTitle(title);
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
