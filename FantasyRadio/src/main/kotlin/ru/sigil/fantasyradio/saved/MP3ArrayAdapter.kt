package ru.sigil.fantasyradio.saved

import android.content.Context
import android.database.Cursor
import android.view.View
import android.widget.CursorAdapter
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.fantasyradio.utils.RadioStream
import ru.sigil.bassplayerlib.IPlayer
import javax.inject.Inject
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.sigil.fantasyradio.R
import ru.sigil.bassplayerlib.PlayState
import android.widget.SeekBar
import android.widget.ImageButton
import android.widget.TextView
import ru.sigil.fantasyradio.saved.DbConstants.ARTIST
import ru.sigil.fantasyradio.saved.DbConstants.DIRECTORY
import ru.sigil.fantasyradio.saved.DbConstants.TIME
import ru.sigil.fantasyradio.saved.DbConstants.TITLE


/**
 * Created by namelessone
 * on 02.12.18.
 */
class MP3ArrayAdapter(context: Context, private val dbCursor: Cursor, private val deleteClickListener: View.OnClickListener,
                      private val playClickListener: View.OnClickListener): CursorAdapter(context, dbCursor, 0) {
    init {
        Bootstrap.INSTANCE.getBootstrap().inject(this)
    }

    @set:Inject
    var player: IPlayer<RadioStream>? = null

    override fun getCount(): Int {
        return dbCursor.count
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.mp3_list_item_layout, parent, false)
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    override fun bindView(view: View, context: Context, cursor: Cursor) {

        val message = MP3Entity(cursor.getString(cursor.getColumnIndexOrThrow(ARTIST)),
                cursor.getString(cursor.getColumnIndexOrThrow(TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DIRECTORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(TIME)))

        val messageArtistView = view.findViewById<TextView>(R.id.MP3artist)
        val progressSeekBar = view.findViewById<SeekBar>(R.id.MP3SeekBar1)
        val volumeSeekBar = view.findViewById<SeekBar>(R.id.volumeSeekBar)
        val artist = if (message.artist?.isNotEmpty() == true)
            message.artist + " / "
        else
            ""
        val time = if (message.time?.isNotEmpty() == true)
            message.time + " / "
        else
            ""
        messageArtistView.text = String.format("%s%s%s", artist, time, message.title)
        val deleteBtn = view.findViewById<ImageButton>(R.id.deleteMP3Button)
        val messageMap = HashMap<String, String?>()
        messageMap["artist"] = message.artist
        messageMap["directory"] = message.directory
        messageMap["time"] = message.time
        messageMap["title"] = message.title
        deleteBtn.tag = messageMap
        deleteBtn.setOnClickListener(deleteClickListener)
        val playBtn = view.findViewById<ImageButton>(R.id.MP3buttonPlay)
        playBtn.tag = message
        playBtn.setOnClickListener(playClickListener)
        progressSeekBar.tag = message.directory
        progressSeekBar.progress = player?.progress?.toInt() ?: 0
        progressSeekBar
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar,
                                                   progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            player?.progress = progress.toLong()
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
        volumeSeekBar.tag = message.directory + "volume"
        volumeSeekBar.progress = (player!!.volume * 100).toInt()
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                if (fromUser) {
                    player?.volume = progress.toFloat() / 100
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        playBtn.setImageResource(R.drawable.play_states)
        if (player?.currentMP3Entity != null && message.directory == player?.currentMP3Entity?.directory &&
                (player?.playState=== PlayState.PLAY_FILE || player?.playState === PlayState.PAUSE)) {
            progressSeekBar.visibility = View.VISIBLE
            volumeSeekBar.visibility = View.VISIBLE
            if (player?.playState === PlayState.PLAY_FILE) {
                playBtn.setImageResource(R.drawable.pause_states)
            }
        } else {
            progressSeekBar.visibility = View.INVISIBLE
            volumeSeekBar.visibility = View.INVISIBLE
        }
    }
}