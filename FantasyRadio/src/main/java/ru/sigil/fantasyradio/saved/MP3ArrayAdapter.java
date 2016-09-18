package ru.sigil.fantasyradio.saved;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.utils.PlayerState;

class MP3ArrayAdapter extends ArrayAdapter<MP3Entity> {

    public static int width;

    //TODO @Inject
    private IPlayer player;

    private List<MP3Entity> MP3s = new ArrayList<>();
    private View.OnClickListener deleteCLickListener;
    private View.OnClickListener playCLickListener;

    public MP3ArrayAdapter(Context context, int textViewResourceId,
                           List<MP3Entity> objects, View.OnClickListener deleteClickListener, View.OnClickListener playClickListener) {
        super(context, textViewResourceId, objects);
        this.MP3s = objects;
        this.deleteCLickListener = deleteClickListener;
        this.playCLickListener = playClickListener;
    }

    public int getCount() {
        return this.MP3s.size();
    }

    public MP3Entity getItem(int index) {
        return this.MP3s.get(getCount() - index - 1);// !!!!!!!!!!!!!
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater
                    .inflate(R.layout.mp3_list_item_layout, parent, false);
        }
        // Get item
        MP3Entity message = getItem(position);
        TextView messageArtistView = null;
        if (row != null) {
            messageArtistView = (TextView) row.findViewById(R.id.MP3artist);
        }
        SeekBar progressSeekBar = (SeekBar) row.findViewById(R.id.MP3SeekBar1);
        SeekBar volumeSeekBar = (SeekBar) row.findViewById(R.id.volumeSeekBar);
        messageArtistView.setText(message.getArtist() + " / "
                + message.getTime() + " / " + message.getTitle());
        ImageButton deleteBtn = (ImageButton) row.findViewById(R.id.deleteMP3Button);
        HashMap<String, String> messageMap = new HashMap<>();
        messageMap.put("artist", message.getArtist());
        messageMap.put("directory", message.getDirectory());
        messageMap.put("time", message.getTime());
        messageMap.put("title", message.getTitle());
        deleteBtn.setTag(messageMap);
        deleteBtn.setOnClickListener(deleteCLickListener);
        ImageButton playBtn = (ImageButton) row.findViewById(R.id.MP3buttonPlay);
        playBtn.setTag(message);
        playBtn.setOnClickListener(playCLickListener);
        progressSeekBar.setTag(message.getDirectory());
        volumeSeekBar.setTag(message.getDirectory() + "volume");
        playBtn.setImageResource(R.drawable.play_states);
        if (message == PlayerState.getInstance().getCurrentMP3Entity()) {
            progressSeekBar.setVisibility(View.VISIBLE);
            volumeSeekBar.setVisibility(View.VISIBLE);
            //CurrentControls.setCurrentMP3SeekBar(progressSeekBar);
            //CurrentControls.setCurrentVolumeSeekBar(volumeSeekBar);
            if (player.currentState()== PlayState.PAUSE)
                playBtn.setImageResource(R.drawable.pause_states);
        } else {
            progressSeekBar.setVisibility(View.INVISIBLE);
            volumeSeekBar.setVisibility(View.INVISIBLE);
            volumeSeekBar.setVisibility(View.INVISIBLE);
        }
        return row;
    }
}