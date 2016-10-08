package ru.sigil.fantasyradio.saved;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.un4seen.bass.BASS;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import ru.sigil.fantasyradio.AbstractListFragment;
import ru.sigil.fantasyradio.BackgroundService.Bitrate;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.IPlayerEventListener;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider;

public class SavedFragment extends AbstractListFragment {
    private MP3Entity mp3EntityForDelete;
    private MP3ArrayAdapter adapter;
    private int nextPos;
    private View savedActivityView;
    @Inject
    IPlayer player;

    private TimerTask seekTask = new TimerTask() {
        public void run() {
            mp3ProgressHandler.sendEmptyMessage(0);
        }
    };
    private Timer seekTimer = new Timer();

    public void notifyAdapter() {
        View.OnClickListener deleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClick(v);
            }
        };
        View.OnClickListener playClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick(v);
            }
        };
        adapter = new MP3ArrayAdapter(getActivity().getBaseContext(),
                R.layout.mp3_list_item_layout, player.getMp3Saver().getMp3c()
                .getMp3entityes(), deleteClickListener, playClickListener
        );
        getLv().setAdapter(adapter);
    }

    private Handler mp3ProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long streamProgress = player.getProgress();
            try {
                if (player.currentState() == PlayState.PLAY_FILE) {
                    SeekBar sb = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory());
                    if (sb != null) {
                        sb.setProgress((int) streamProgress);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        savedActivityView = inflater.inflate(R.layout.mp3s, container, false);
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        player.addEventListener(eventListener);
        //CurrentControls.setRewindMP3Handler(rewindMp3Handler);
        setLv((ListView) savedActivityView.findViewById(R.id.MP3ListView));
        return savedActivityView;
    }

    @Override
    public void onDestroy() {
        player.removeEventListener(eventListener);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        notifyAdapter();
        super.onResume();
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
        getActivity().runOnUiThread(new RunnableParam(s) {
            public void run() {
                try {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            (String) param, Toast.LENGTH_SHORT);
                    toast.show();
                } catch (Exception e) {
                }
            }
        });
    }

    public void playClick(View v) {
        updateWidget();
        if (player.isPaused()) {
            if (player.getCurrentMP3Entity() == v.getTag()) {
                // Это была пауза.
                player.resume();
                ImageButton bv = (ImageButton) getLv().findViewWithTag(player.getCurrentMP3Entity());
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
        if (player.currentState() == PlayState.PLAY || player.currentState() == PlayState.PLAY_FILE) {
            if (player.getCurrentMP3Entity() == v.getTag()) {
                //TODO !!!
                player.pause();
                ImageButton bv = (ImageButton) v;// !!!!!!!!!!!!!!!
                bv.setImageResource(R.drawable.play_states);
            } else {
                // Нажата кнопка плэй у другого трека
                offPreviousMP3ListRow();
                nextPos = adapter
                        .getPosition(player.getCurrentMP3Entity()); // почему-то 0
                player.playFile((MP3Entity) v.getTag());
                onCurrentMP3ListRow();
            }
        } else {
            if (player.isPaused()) {
                offPreviousMP3ListRow();
                //TODO скорее всего по событию сделать
            }
            nextPos = adapter.getPosition((MP3Entity) v.getTag());
            player.playFile((MP3Entity) v.getTag());
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
                .setTitle(getString(R.string.are_you_sure_want_delete));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        player.getMp3Saver().getMp3c().removeEntityByDirectory(
                                mp3EntityForDelete.getDirectory());
                        if (player.getCurrentMP3Entity() != null) {
                            if (mp3EntityForDelete.getDirectory().equals(player.getCurrentMP3Entity().getDirectory())) {
                                player.stop();
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
            ImageButton oldB = (ImageButton) getLv().findViewWithTag(player.getCurrentMP3Entity());
            if (oldB != null) {
                oldB.setImageResource(R.drawable.play_states);
            }
            SeekBar sb = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory());
            if (sb != null)
                sb.setVisibility(View.INVISIBLE);
            SeekBar volumeSeekBar = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory() + "volume");
            if (volumeSeekBar != null)
                volumeSeekBar.setVisibility(View.INVISIBLE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void onCurrentMP3ListRow() {
        try {
            ImageButton bv = (ImageButton) getLv().findViewWithTag(player.getCurrentMP3Entity());
            if (bv != null) {
                bv.setImageResource(R.drawable.pause_states);
            }
            SeekBar sb = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory());
            if (sb != null) {
                sb.setVisibility(View.VISIBLE);
            }
            if (sb != null) {
                sb.setProgress(0);
            }
            SeekBar volumeSeekBar = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory() + "volume");
            if (volumeSeekBar != null) {
                volumeSeekBar.setVisibility(View.VISIBLE);
            }
            //CurrentControls.setCurrentMP3SeekBar(sb);
            //CurrentControls.setCurrentVolumeSeekBar(volumeSeekBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWidget() {
        Intent intent = new Intent(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getActivity().getApplicationContext()).getAppWidgetIds(
                new ComponentName(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getActivity().getApplicationContext().sendBroadcast(intent);
    }

    private IPlayerEventListener eventListener = new IPlayerEventListener() {
        @Override
        public void onTitleChanged(String title) {

        }

        @Override
        public void onAuthorChanged(String author) {

        }

        @Override
        public void onPlayStateChanged(PlayState playState) {

        }

        @Override
        public void onRecStateChanged(boolean isRec) {

        }

        @Override
        public void onBitrateChanged(Bitrate bitrate) {

        }

        @Override
        public void onBufferingProgress(long progress) {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void endSync() {
            getActivity().runOnUiThread(new Runnable() {
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
                                            (LinearLayout) savedActivityView.findViewById(R.id.mp3sLinearLayout));
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
                            player.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        player.stop();
                    }
                }
            });
        }

        @Override
        public void onVolumeChanged(final float volume) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    SeekBar volumeSeekBar = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory() + "volume");
                    volumeSeekBar.setProgress((int) (volume * 100));
                }
            });
        }
    };
}