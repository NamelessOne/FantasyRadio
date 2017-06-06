package ru.sigil.fantasyradio.saved;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.HashMap;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.RadioStream;

public class MP3ArrayAdapter extends CursorAdapter {

    @Inject
    public IPlayer<RadioStream> player;

    private Cursor mCursor;
    private View.OnClickListener deleteCLickListener;
    private View.OnClickListener playCLickListener;

    public MP3ArrayAdapter(Context context, Cursor cursor, View.OnClickListener deleteClickListener,
                           View.OnClickListener playClickListener) {
        super(context, cursor, 0);
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        mCursor = cursor;
        this.deleteCLickListener = deleteClickListener;
        this.playCLickListener = playClickListener;
    }

    public int getCount() {
        return mCursor.getCount();
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.mp3_list_item_layout, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        MP3Entity message = new MP3Entity(cursor.getString(cursor.getColumnIndexOrThrow(MP3Collection.ARTIST)),
                cursor.getString(cursor.getColumnIndexOrThrow(MP3Collection.TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MP3Collection.DIRECTORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(MP3Collection.TIME)));

        TextView messageArtistView = (TextView) view.findViewById(R.id.MP3artist);
        SeekBar progressSeekBar = (SeekBar) view.findViewById(R.id.MP3SeekBar1);
        SeekBar volumeSeekBar = (SeekBar) view.findViewById(R.id.volumeSeekBar);
        String artist = message.getArtist() != null && message.getArtist().length() > 0 ?
                message.getArtist() + " / " : "";
        String time = message.getTime() != null && message.getTime().length() > 0 ?
                message.getTime() + " / " : "";
        messageArtistView.setText(artist + time + message.getTitle());
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.deleteMP3Button);
        HashMap<String, String> messageMap = new HashMap<>();
        messageMap.put("artist", message.getArtist());
        messageMap.put("directory", message.getDirectory());
        messageMap.put("time", message.getTime());
        messageMap.put("title", message.getTitle());
        deleteBtn.setTag(messageMap);
        deleteBtn.setOnClickListener(deleteCLickListener);
        final ImageButton playBtn = (ImageButton) view.findViewById(R.id.MP3buttonPlay);
        playBtn.setTag(message);
        playBtn.setOnClickListener(playCLickListener);
        progressSeekBar.setTag(message.getDirectory());
        progressSeekBar.setProgress((int) (BASS.BASS_ChannelGetPosition(
                player.getChan(), BASS.BASS_POS_BYTE)
                * 100
                / BASS.BASS_ChannelGetLength(player.getChan(),
                BASS.BASS_POS_BYTE)));
        progressSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        if (fromUser) {
                            player.setProgress(progress);
                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
        volumeSeekBar.setTag(message.getDirectory() + "volume");
        volumeSeekBar.setProgress((int) (player.getVolume() * 100));
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    player.setVolume(((float) progress) / 100);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        playBtn.setImageResource(R.drawable.play_states);
        if (player.getCurrentMP3Entity() != null && message.getDirectory().equals(player.getCurrentMP3Entity().getDirectory()) &&
                (player.currentState() == PlayState.PLAY_FILE || player.currentState() == PlayState.PAUSE)) {
            progressSeekBar.setVisibility(View.VISIBLE);
            volumeSeekBar.setVisibility(View.VISIBLE);
            if (player.currentState() == PlayState.PLAY_FILE) {
                playBtn.setImageResource(R.drawable.pause_states);
            }
        } else {
            progressSeekBar.setVisibility(View.INVISIBLE);
            volumeSeekBar.setVisibility(View.INVISIBLE);
        }
    }
}