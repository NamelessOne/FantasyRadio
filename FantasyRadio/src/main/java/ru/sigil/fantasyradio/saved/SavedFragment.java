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
import android.widget.ListView;
import android.widget.SeekBar;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.IPlayerEventListener;
import ru.sigil.bassplayerlib.ITrack;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.fantasyradio.AbstractListFragment;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.Bitrate;
import ru.sigil.fantasyradio.utils.RadioStream;
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider;

public class SavedFragment extends AbstractListFragment {
    private MP3Entity mp3EntityForDelete;
    private MP3ArrayAdapter adapter;
    private View savedActivityView;
    @Inject
    MP3Collection mp3Collection;
    @Inject
    IPlayer<RadioStream> player;

    private TimerTask seekTask = new TimerTask() {
        public void run() {
            mp3ProgressHandler.sendEmptyMessage(0);
        }
    };
    private Timer seekTimer = new Timer();

    public SavedFragment()
    {
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        player.addEventListener(endSyncEventListener);
    }

    public void notifyAdapter() {
        adapter = new MP3ArrayAdapter(getActivity().getBaseContext(),
                mp3Collection.getCursor(),
                v -> deleteClick(v), v -> playClick(v)
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
        player.addEventListener(eventListener);
        //CurrentControls.setRewindMP3Handler(rewindMp3Handler);
        setLv((ListView) savedActivityView.findViewById(R.id.MP3ListView));
        return savedActivityView;
    }

    @Override
    public void onDestroyView() {
        player.removeEventListener(eventListener);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        notifyAdapter();
        super.onResume();
    }


    public void playClick(View v) {
        updateWidget();
        if (player.isPaused()) {
            if (player.getCurrentMP3Entity() != null && v.getTag() != null
                    && player.getCurrentMP3Entity().getDirectory().equals(((MP3Entity)v.getTag()).getDirectory())) {
                // Это была пауза.
                player.resume();
                return;
            }
        }
        try {
            seekTimer.scheduleAtFixedRate(seekTask, 0, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (player.currentState() == PlayState.PLAY || player.currentState() == PlayState.PLAY_FILE) {
            if (player.getCurrentMP3Entity() != null && v.getTag() != null
                    && player.getCurrentMP3Entity().getDirectory().equals(((MP3Entity)v.getTag()).getDirectory())) {
                //TODO !!!
                player.pause();
                ImageButton bv = (ImageButton) v;// !!!!!!!!!!!!!!!
                bv.setImageResource(R.drawable.play_states);
            } else {
                // Нажата кнопка плэй у другого трека
                player.playFile((MP3Entity) v.getTag());
                adapter.notifyDataSetChanged();
            }
        } else {
            player.playFile((MP3Entity) v.getTag());
            // -------------------------------------------------
            adapter.notifyDataSetChanged();
        }
    }

    public void deleteClick(View v) {// Тут удаляем mp3
        HashMap<String, String> messageMap;
        messageMap = (HashMap<String, String>) v.getTag();
        mp3EntityForDelete = new MP3Entity(messageMap.get("artist"), messageMap.get("title"),
                messageMap.get("directory"), messageMap.get("time"));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
                .setTitle(getString(R.string.are_you_sure_want_delete));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                (dialog, which) -> {
                    mp3Collection.remove(
                            mp3EntityForDelete);
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
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.no),
                (dialog, which) -> {
                    // here you can add functions
                    dialog.dismiss();
                });
        alertDialogBuilder.create().show();
        this.onResume();
    }

    private void updateWidget() {
        Intent intent = new Intent(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getActivity().getApplicationContext()).getAppWidgetIds(
                new ComponentName(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getActivity().getApplicationContext().sendBroadcast(intent);
    }

    //TODO по хорошему, должно быть статическим. Придумать лучшее решение
    private IPlayerEventListener endSyncEventListener = new IPlayerEventListener<RadioStream>() {
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
        public void onStreamChanged(RadioStream stream) {

        }

        @Override
        public void onBufferingProgress(long progress) {

        }

        @Override
        public void endSync() {
            getActivity().runOnUiThread(() -> {
                // Заканчивается воспроизведение. Переходим на следующий трек.
                //adapter.notifyDataSetChanged();
                ITrack next = mp3Collection.getNext(player.getCurrentMP3Entity());
                player.stop();
                if (next != null) {
                    player.playFile(next);
                }
            });
        }

        @Override
        public void onVolumeChanged(float volume) {

        }
    };

    private IPlayerEventListener eventListener = new IPlayerEventListener<RadioStream>() {
        @Override
        public void onTitleChanged(String title) {

        }

        @Override
        public void onAuthorChanged(String author) {

        }

        @Override
        public void onPlayStateChanged(PlayState playState) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onRecStateChanged(boolean isRec) {

        }

        @Override
        public void onStreamChanged(RadioStream stream) {

        }

        @Override
        public void onBufferingProgress(long progress) {

        }

        @Override
        public void endSync() {

        }

        @Override
        public void onVolumeChanged(final float volume) {
            getActivity().runOnUiThread(() -> {
                SeekBar volumeSeekBar = (SeekBar) getLv().findViewWithTag(player.getCurrentMP3Entity().getDirectory() + "volume");
                volumeSeekBar.setProgress((int) (volume * 100));
            });
        }
    };
}