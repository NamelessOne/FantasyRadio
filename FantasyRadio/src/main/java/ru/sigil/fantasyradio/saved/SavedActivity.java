package ru.sigil.fantasyradio.saved;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_AAC;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.sigil.fantasyradio.AbstractListActivity;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.utils.BASSUtil;
import ru.sigil.fantasyradio.utils.PlayerState;

public class SavedActivity extends AbstractListActivity {
    private AdView adView;
    private MP3Entity mp3EntityForDelete;
    private MP3ArrayAdapter adapter;
    private int nextPos;
    private TimerTask seekTask = new TimerTask() {
        public void run() {
            mp3ProgressHandler.sendEmptyMessage(0);
        }
    };
    private Timer seekTimer = new Timer();
    private Handler rewindMp3Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int streamOffset = msg.arg1;
            BASS.BASS_ChannelSetPosition(BASSUtil.getChan(), streamOffset,
                    BASS.BASS_POS_BYTE);
            if (!(BASS.BASS_ChannelIsActive(BASSUtil.getChan()) == BASS.BASS_ACTIVE_PAUSED)) {
                BASS.BASS_ChannelPlay(BASSUtil.getChan(), false);
            }
        }
    };

    private Handler mp3ProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long streamProgress = BASS.BASS_ChannelGetPosition(
                    BASSUtil.getChan(), BASS.BASS_POS_BYTE)
                    * 100
                    / BASS.BASS_ChannelGetLength(BASSUtil.getChan(),
                    BASS.BASS_POS_BYTE);
            try {
                if (PlayerState
                        .getInstance().getCurrentMP3Entity() != null) {
                    SeekBar sb = (SeekBar) getLv().findViewWithTag(PlayerState
                            .getInstance().getCurrentMP3Entity().getDirectory());
                    if (sb != null) {
                        sb.setProgress((int) streamProgress);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp3s);
        adView = (AdView)this.findViewById(R.id.mp3sAdView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        CurrentControls.setRewindMP3Handler(rewindMp3Handler);
        setLv((ListView) findViewById(R.id.MP3ListView));
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);    // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);    // Add this method.
    }


    @Override
    protected void onResume() {
        adapter = new MP3ArrayAdapter(getBaseContext(),
                R.layout.mp3_list_item_layout, MP3Saver.getMp3c()
                .getMp3entityes());
        getLv().setAdapter(adapter);
        super.onResume();
        adView.resume();
    }

    class RunnableParam implements Runnable {
        Object param;

        RunnableParam(Object p) {
            param = p;
        }

        public void run() {
        }
    }

    /**
     * Показываем сообщение об ошибке
     *
     * @param es Сообщение об ошибке
     */
    void Error(String es) {
        // get error code in current thread for display in UI thread
        String s = String.format("%s\n(error code: %d)", es,
                BASS.BASS_ErrorGetCode());
        runOnUiThread(new RunnableParam(s) {
            public void run() {
                try {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            (String) param, Toast.LENGTH_SHORT);
                    toast.show();
                } catch (Exception e) {
                }
            }
        });
    }


    private BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            runOnUiThread(new Runnable() {
                public void run() {
                    // Заканчивается воспроизведение. Переходим на следующий трек.
                    offPreviousMP3ListRow();
                    if (nextPos < adapter.getCount()) {
                        try {
                            LinearLayout ll1 = (LinearLayout) adapter
                                    .getView(
                                            // nextPos + 1,
                                            adapter.getCount() - nextPos,
                                            null,
                                            (LinearLayout) findViewById(R.id.mp3sLinearLayout));
                            LinearLayout ll2 = null;
                            if (ll1 != null) {
                                ll2 = (LinearLayout) ll1.getChildAt(1);
                            }
                            ImageButton b = null;
                            if (ll2 != null) {
                                b = (ImageButton) ll2.getChildAt(0);
                            }
                            if (b != null) {
                                b.performClick();
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            PlayerState.getInstance().setCurrentMP3Entity(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        PlayerState.getInstance().setCurrentMP3Entity(null);
                    }
                }
            });
        }
    };

    BASS.SYNCPROC PosSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }
    };

    public void playClick(View v) {
        if (BASS.BASS_ChannelIsActive(BASSUtil.getChan()) == BASS.BASS_ACTIVE_PAUSED) {
            if (PlayerState.getInstance().getCurrentMP3Entity() == v.getTag()) {
                // Это была пауза.
                BASS.BASS_ChannelPlay(BASSUtil.getChan(), false);
                ImageButton bv = (ImageButton) getLv().findViewWithTag(PlayerState
                        .getInstance().getCurrentMP3Entity());
                if (bv != null) {
                    bv.setImageResource(R.drawable.pause_states);
                }
                return;
            }
        }
        try {
            seekTimer.scheduleAtFixedRate(seekTask, 0, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (PlayerState.isPlaying()) {
            if (PlayerState.getInstance().getCurrentMP3Entity() == v.getTag()) {
                BASS.BASS_ChannelPause(BASSUtil.getChan());
                ImageButton bv = (ImageButton) v;// !!!!!!!!!!!!!!!
                bv.setImageResource(R.drawable.play_states);
            } else {
                // Нажата кнопка плэй у другого трека
                offPreviousMP3ListRow();
                PlayerState.getInstance().setCurrentMP3Entity((MP3Entity) v.getTag());
                nextPos = adapter
                        .getPosition(PlayerState.getInstance().getCurrentMP3Entity()); // почему-то 0
                BASS.BASS_StreamFree(BASSUtil.getChan());
                // -------------------------------------------------
                String file = PlayerState.getInstance().getCurrentMP3Entity().getDirectory();
                int x;
                if ((x = BASS.BASS_StreamCreateFile(file, 0, 0, 0)) == 0
                        && (x = BASS.BASS_MusicLoad(file, 0, 0,
                        BASS.BASS_MUSIC_RAMP, 0)) == 0) {
                    if ((x = BASS_AAC.BASS_AAC_StreamCreateFile(file, 0, 0, 0)) == 0) {
                        // whatever it is, it ain't playable
                        BASSUtil.setChan(x);
                        Error("Can't play the file");
                        return;
                    }
                }
                BASSUtil.setChan(x);
                BASS.BASS_ChannelPlay(BASSUtil.getChan(), false);
                BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                        BASS.BASS_SYNC_END, 0, EndSync, null);
                // -------------------------------------------------
                onCurrentMP3ListRow();
            }
        } else {
            if (BASS.BASS_ChannelIsActive(BASSUtil.getChan()) == BASS.BASS_ACTIVE_PAUSED) {
                offPreviousMP3ListRow();
            }
            PlayerState.getInstance().setCurrentMP3Entity((MP3Entity) v.getTag());
            nextPos = adapter.getPosition(PlayerState.getInstance().getCurrentMP3Entity());
            BASS.BASS_StreamFree(BASSUtil.getChan());
            // -------------------------------------------------
            String file = PlayerState.getInstance().getCurrentMP3Entity().getDirectory();
            int x;
            if ((x = BASS.BASS_StreamCreateFile(file, 0, 0, 0)) == 0
                    && (x = BASS.BASS_MusicLoad(file, 0, 0,
                    BASS.BASS_MUSIC_RAMP, 0)) == 0) {
                if ((x = BASS_AAC.BASS_AAC_StreamCreateFile(file, 0, 0, 0)) == 0) {
                    // whatever it is, it ain't playable
                    BASSUtil.setChan(x);
                    Error("Can't play the file");
                    return;
                }
            }
            BASSUtil.setChan(x);
            BASS.BASS_ChannelPlay(BASSUtil.getChan(), false);
            BASS.BASS_ChannelSetSync(BASSUtil.getChan(), BASS.BASS_SYNC_END, 0,
                    EndSync, null);
            // -------------------------------------------------
            onCurrentMP3ListRow();
        }
    }

    public void deleteClick(View v) {// Тут удаляем mp3
        HashMap<String, String> messageMap;
        messageMap = (HashMap<String, String>) v.getTag();
        MP3Entity mp3entity = new MP3Entity();
        mp3entity.setArtist(messageMap.get("artist"));
        mp3entity.setDirectory(messageMap.get("directory"));
        mp3entity.setTime(messageMap.get("time"));
        mp3entity.setTitle(messageMap.get("title"));
        mp3EntityForDelete = mp3entity;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(getString(R.string.are_you_sure_want_delete));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MP3Saver.getMp3c().removeEntityByDirectory(
                                mp3EntityForDelete.getDirectory());
                        if (PlayerState.getInstance().getCurrentMP3Entity() != null) {
                            if (mp3EntityForDelete.getDirectory().equals(PlayerState
                                    .getInstance().getCurrentMP3Entity().getDirectory())) {
                                BASS.BASS_ChannelStop(BASSUtil.getChan());
                            }
                        }
                        File f = new File(mp3EntityForDelete.getDirectory());
                        Log.v("dir", mp3EntityForDelete.getDirectory());
                        Boolean b = f.delete();
                        Log.v("tf", b.toString());
                        dialog.dismiss();
                        onResume();
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // here you can add functions
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.create().show();
        this.onResume();
    }

    private void offPreviousMP3ListRow() {
        try {
            ImageButton oldB = (ImageButton) getLv().findViewWithTag(PlayerState
                    .getInstance().getCurrentMP3Entity());
            if (oldB != null) {
                oldB.setImageResource(R.drawable.play_states);
            }
            SeekBar sb = (SeekBar) getLv().findViewWithTag(PlayerState
                    .getInstance().getCurrentMP3Entity().getDirectory());
            if (sb != null)
                sb.setVisibility(View.INVISIBLE);
            SeekBar volumeSeekBar = (SeekBar) getLv().findViewWithTag(PlayerState
                    .getInstance().getCurrentMP3Entity().getDirectory() + "volume");
            if (volumeSeekBar != null)
                volumeSeekBar.setVisibility(View.INVISIBLE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void onCurrentMP3ListRow() {
        try {
            ImageButton bv = (ImageButton) getLv().findViewWithTag(PlayerState
                    .getInstance().getCurrentMP3Entity());
            if (bv != null) {
                bv.setImageResource(R.drawable.pause_states);
            }
            SeekBar sb = (SeekBar) getLv().findViewWithTag(PlayerState
                    .getInstance().getCurrentMP3Entity().getDirectory());
            if (sb != null) {
                sb.setVisibility(View.VISIBLE);
            }
            if (sb != null) {
                sb.setProgress(0);
            }
            SeekBar volumeSeekBar = (SeekBar) getLv().findViewWithTag(PlayerState
                    .getInstance().getCurrentMP3Entity().getDirectory() + "volume");
            if (volumeSeekBar != null) {
                volumeSeekBar.setVisibility(View.VISIBLE);
            }
            CurrentControls.setCurrentMP3SeekBar(sb);
            CurrentControls.setCurrentVolumeSeekBar(volumeSeekBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }
}