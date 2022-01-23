package ru.sigil.fantasyradio.saved

import android.app.AlertDialog
import android.os.Handler
import android.os.Message
import android.view.View
import ru.sigil.fantasyradio.AbstractListFragment
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.fantasyradio.utils.RadioStream
import ru.sigil.bassplayerlib.IPlayer
import javax.inject.Inject
import android.widget.SeekBar
import ru.sigil.bassplayerlib.PlayState
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import ru.sigil.fantasyradio.R
import android.widget.ImageButton
import android.appwidget.AppWidgetManager
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener
import ru.sigil.bassplayerlib.listeners.IVolumeChangedListener
import java.io.File
import java.util.TimerTask
import java.util.Timer
import kotlin.collections.HashMap

/**
 * Created by namelessone
 * on 02.12.18.
 */
class SavedFragment: AbstractListFragment() {
    init {
        Bootstrap.INSTANCE.getBootstrap().inject(this)
    }

    private var mp3EntityForDelete: MP3Entity? = null
    private var adapter: MP3ArrayAdapter? = null
    private var savedActivityView: View? = null
    @Inject
    lateinit var mp3Collection: MP3Collection
    @Inject
    lateinit var player: IPlayer<RadioStream>

    private val seekTask = object : TimerTask() {
        override fun run() {
            mp3ProgressHandler.sendEmptyMessage(0)
        }
    }

    private val seekTimer = Timer()

    fun notifyAdapter() {
        adapter = MP3ArrayAdapter(activity!!.baseContext,
                mp3Collection.getCursor()!!,
                View.OnClickListener(this::deleteClick), View.OnClickListener(this::playClick)
        )
        lv?.adapter = adapter
    }

    private val mp3ProgressHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val streamProgress = player?.progress
            try {
                if (player?.playState === PlayState.PLAY_FILE) {
                    val sb = lv?.findViewWithTag<SeekBar>(player?.currentMP3Entity?.directory)
                    sb?.progress = streamProgress!!.toInt()
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        savedActivityView = inflater.inflate(R.layout.mp3s, container, false)
        player.addPlayStateChangedListener(playStateChangedListener)
        player.addVolumeChangedListener(volumeChangedListener)
        lv = savedActivityView?.findViewById(R.id.MP3ListView)
        return savedActivityView
    }

    override fun onDestroyView() {
        player.removePlayStateChangedListener(playStateChangedListener)
        player.removeVolumeChangedListener(volumeChangedListener)
        super.onDestroyView()
    }

    override fun onResume() {
        notifyAdapter()
        super.onResume()
    }

    fun playClick(v: View) {
        updateWidget()
        if (player.isPaused) {
            if (player.currentMP3Entity != null && v.tag != null
                    && player.currentMP3Entity?.directory.equals((v.tag as MP3Entity).directory)) {
                // Это была пауза.
                player.resume()
                return
            }
        }
        try {
            seekTimer.scheduleAtFixedRate(seekTask, 0, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (player.playState === PlayState.PLAY || player.playState === PlayState.PLAY_FILE) {
            if (player.currentMP3Entity != null && v.tag != null
                    && player.currentMP3Entity?.directory == (v.tag as MP3Entity).directory) {
                //TODO !!!
                player.pause()
                val bv = v as ImageButton// !!!!!!!!!!!!!!!
                bv.setImageResource(R.drawable.play_states)
            } else {
                // Нажата кнопка плэй у другого трека
                player.playFile(v.tag as MP3Entity)
                adapter?.notifyDataSetChanged()
            }
        } else {
            player.playFile(v.tag as MP3Entity)
            adapter?.notifyDataSetChanged()
        }
    }

    fun deleteClick(v: View) {// Тут удаляем mp3
        val messageMap: HashMap<String, String> = v.tag as HashMap<String, String>
        mp3EntityForDelete = MP3Entity(messageMap["artist"], messageMap["title"],
                messageMap["directory"]!!, messageMap["time"])
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder
                .setTitle(getString(R.string.are_you_sure_want_delete))
        alertDialogBuilder.setPositiveButton(getString(R.string.yes)
        ) { dialog, _ ->
            mp3Collection.remove(mp3EntityForDelete ?: return@setPositiveButton) //TODO хрень какая-то. Разобраться
            if (player.currentMP3Entity != null) {
                if (mp3EntityForDelete?.directory.equals(player.currentMP3Entity?.directory)) {
                    player.stop()
                }
            }
            val f = File(mp3EntityForDelete?.directory)
            Log.v("dir", mp3EntityForDelete?.directory ?: "")
            val b = f.delete()
            Log.v("tf", b.toString())
            dialog.dismiss()
            onResume()
        }
        alertDialogBuilder.setNegativeButton(getString(R.string.no)
        ) { dialog, _ ->
            // here you can add functions
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
        this.onResume()
    }

    private fun updateWidget() {
        val intent = Intent(activity!!.applicationContext, FantasyRadioWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(activity!!.applicationContext).getAppWidgetIds(
                ComponentName(activity!!.applicationContext, FantasyRadioWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        activity?.applicationContext?.sendBroadcast(intent)
    }

    private val playStateChangedListener = object : IPlayStateChangedListener {
        override fun onPlayStateChanged(playState: PlayState) {
            activity?.runOnUiThread { adapter?.notifyDataSetChanged() }
         }
    }

    private val volumeChangedListener = object : IVolumeChangedListener {
        override fun onVolumeChanged(volume: Float) {
            activity?.runOnUiThread {
                if (player.currentMP3Entity != null) {
                    val volumeSeekBar = lv?.findViewWithTag<SeekBar>(player.currentMP3Entity?.directory + "volume")
                    volumeSeekBar?.progress = (volume * 100).toInt()
                }
            }
        }
    }
}