package ru.sigil.fantasyradio

import androidx.fragment.app.Fragment
import android.app.PendingIntent
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import ru.sigil.fantasyradio.utils.Bitrate
import android.util.SparseArray
import ru.sigil.fantasyradio.currentstreraminfo.ICurrentStreamInfoService
import javax.inject.Inject
import ru.sigil.fantasyradio.utils.IRadioStreamFactory
import ru.sigil.fantasyradio.utils.RadioStream
import ru.sigil.bassplayerlib.IPlayer
import java.util.*
import com.un4seen.bass.BASS
import ru.sigil.fantasyradio.utils.AlarmReceiver
import com.nostra13.universalimageloader.core.ImageLoader
import ru.sigil.bassplayerlib.PlayState
import android.content.Intent
import ru.sigil.fantasyradio.utils.SleepHandlerContainer
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.*
import ru.sigil.fantasyradio.dagger.Bootstrap
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import kotlinx.coroutines.*
import ru.sigil.bassplayerlib.listeners.*
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider

const val TIME_DIALOG_ID = 999
const val CURRENT_INFO_UPDATE_INTERVAL = 1000 * 60 //1 minute

@Suppress("UNNECESSARY_SAFE_CALL")
/**
 * Created by namelessone
 * on 09.12.18.
 */
class RadioFragment : Fragment() {
    private var hour: Int = 0
    private var minute: Int = 0
    private var am: AlarmManager? = null
    private var sender: PendingIntent? = null
    private var cb1: CheckBox? = null
    private var mainFragmentView: View? = null
    private var currentStreamAbout = ""
    private var currentStreamImageUrl = ""
    private var updateCurrentStreamInfoJob: Job? = null

    private var bitRates: SparseArray<Bitrate> = SparseArray()

    init {
        bitRates.put(0, Bitrate.AAC_16)
        bitRates.put(1, Bitrate.MP3_32)
        bitRates.put(2, Bitrate.MP3_96)
        bitRates.put(3, Bitrate.AAC_112)
    }

    @Inject
    lateinit var player: IPlayer<RadioStream>
    @Inject
    lateinit var radioStreamFactory: IRadioStreamFactory
    @Inject
    lateinit var currentStreamInfoService: ICurrentStreamInfoService


    /**
     * Хэндлер, закрывающий приложение
     * @see AlarmReceiver
     */
    private val sleepTimerHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (activity != null) {
                activity!!.runOnUiThread { activity!!.finish() }
            } else {
                BASS.BASS_Free()
            }
        }
    }

    private fun updateCurrentStreamInfo(about: String, imageUrl: String) {
        GlobalScope.launch(Dispatchers.Main) {
            mainFragmentView?.findViewById<TextView>(R.id.currentInfoAbout)?.text = about
            if (imageUrl.isNotEmpty()) {
                ImageLoader.getInstance().displayImage("http://fantasyradio.ru/$imageUrl", mainFragmentView?.findViewById<ImageView>(R.id.currentInfoImage))
            } else {
                (mainFragmentView?.findViewById(R.id.currentInfoImage) as ImageView).setImageDrawable(null)
            }
        }
    }

    /**
     * Кликнули на кнопку PLAY. Начинаем проигрывать выбранный поток.
     */
    fun streamButtonClick(v: View) {
        updateWidget()
        if (player.playState !== PlayState.PLAY) {
            val stream = radioStreamFactory.createStreamWithBitrate(player.stream?.bitrate
                    ?: Bitrate.AAC_16)
            player.playStream(stream)
        } else {
            player.stop()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainFragmentView = inflater.inflate(R.layout.activity_main, container, false)
        Bootstrap.INSTANCE.getBootstrap().inject(this)
        player.addTitleChangedListener(titleChangedListener)
        player.addAuthorChangedListener(authorChangedListener)
        player.addPlayStateChangedListener(playStateChangedListener)
        player.addRecStateChangedListener(recStateChangedListener)
        player.addBufferingProgressChangedListener(bufferingProgressListener)
        player.addVolumeChangedListener(volumeChangedListener)
        //------------------------------------------------------------------------------------------
        val streamButton = mainFragmentView?.findViewById<ImageView>(R.id.streamButton)
        streamButton?.setOnClickListener(this::streamButtonClick)
        val recordButton = mainFragmentView?.findViewById<ImageView>(R.id.recordButton)
        recordButton?.setOnClickListener({ _ -> streamRecordClick() })
        mainFragmentView?.findViewById<View>(R.id.tvChangeTime)?.setOnClickListener(this::onTimerClick)
        //------------------------------------------------------------------------------------------
        //setContentView(R.layout.activity_main);
        SleepHandlerContainer.sleepHandler = sleepTimerHandler
        cb1 = mainFragmentView?.findViewById(R.id.checkBox1)
        val intent = Intent(activity!!.baseContext, AlarmReceiver::class.java)
        sender = PendingIntent.getBroadcast(activity!!.baseContext, 192837, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        am = activity!!.baseContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cb1?.setOnClickListener { v ->
            val calNow = Calendar.getInstance()
            val cal = Calendar.getInstance()
            // add 5 minutes to the calendar object
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.SECOND, 0)
            var alarmMillis = cal.timeInMillis
            // is chkIos checked?
            if (calNow.after(cal)) {
                alarmMillis += 86400000L // Add 1 day if time selected
                // before now
                cal.timeInMillis = alarmMillis
            }
            if ((v as CheckBox).isChecked) {
                am?.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis,
                        sender)
            } else {
                am?.cancel(sender)
            }
        }
        setTimer(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar
                .getInstance().get(Calendar.MINUTE))
        return mainFragmentView
    }

    private fun initView() {
        val spinner = mainFragmentView?.findViewById<Spinner>(R.id.stream_quality_spinner)
        spinner?.setSelection(bitRates.keyAt(bitRates.indexOfValue(player.stream?.bitrate)))
        spinner?.onItemSelectedListener = bitRateSelected
        val sb = mainFragmentView?.findViewById<SeekBar>(R.id.mainVolumeSeekBar)
        sb?.progress = (player.volume * 100).toInt()

        sb?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.volume = progress.toFloat() / 100
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val rib = mainFragmentView?.findViewById<ImageView>(R.id.recordButton)
        if (player.isRecActive) {
            rib?.setImageResource(R.drawable.rec_active)
        } else {
            rib?.setImageResource(R.drawable.rec)
        }
        val iv = mainFragmentView?.findViewById<ImageView>(R.id.streamButton)

        when (player.playState) {
            PlayState.PLAY -> {
                if (player.author?.isNotEmpty() == true) {
                    (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = player.author + " - " + player?.title
                } else {
                    (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = player.title
                }
                iv?.setImageResource(R.drawable.pause_states)
            }
            PlayState.BUFFERING -> {
                iv?.setImageResource(R.drawable.pause_states)
            }
            PlayState.PAUSE, PlayState.PLAY_FILE -> {
                (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = ""
                iv?.setImageResource(R.drawable.play_states)
            }
            PlayState.STOP -> {
                (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = ""
                iv?.setImageResource(R.drawable.play_states)
            }
        }
        updateCurrentStreamInfo(currentStreamAbout, currentStreamImageUrl)
    }


    override fun onDestroyView() {
        player.removeTitleChangedListener(titleChangedListener)
        player.removeAuthorChangedListener(authorChangedListener)
        player.removePlayStateChangedListener(playStateChangedListener)
        player.removeRecStateChangedListener(recStateChangedListener)
        player.removeBufferingProgressChangedListener(bufferingProgressListener)
        player.removeVolumeChangedListener(volumeChangedListener)
        super.onDestroyView()
    }

    private val bitRateSelected: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position != bitRates.keyAt(bitRates.indexOfValue(player.stream?.bitrate))) {
                player.stream = radioStreamFactory.createStreamWithBitrate(bitRates.get(position))
                if (player.playState == PlayState.PLAY) {
                    val b = mainFragmentView?.findViewById<ImageView>(R.id.streamButton)
                    b?.performClick()
                    b?.performClick()
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    override fun onResume() {
        initView()
        updateCurrentStreamInfoJob = GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val streamInfo = currentStreamInfoService.getInfo()
                updateCurrentStreamInfo(streamInfo.about, streamInfo.imageURL)
                delay(CURRENT_INFO_UPDATE_INTERVAL.toLong())
            }
        }
        super.onResume()
    }

    override fun onPause() {
        updateCurrentStreamInfoJob?.cancel()
        super.onPause()
    }

    /**
     * Запись потока.
     */
    private fun streamRecordClick() {
        if (player.isRecActive) {
            player.rec(false)
        } else {
            player.rec(true)
        }
    }

    /**
     * Установка времени в вьюху
     * @param h Часы
     * @param m Минуты
     */
    private fun setTimer(h: Int, m: Int) { //TODO нормальный формат
        hour = h
        minute = m
        val timeTv = mainFragmentView?.findViewById<TextView>(R.id.tvChangeTime)
        var str = ""
        if (minute < 10)
            str = "0"
        timeTv?.text = hour.toString() + ":" + str + minute
    }

    private val timePickerListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        am?.cancel(sender)
        setTimer(hourOfDay, minute)
        val calNow = Calendar.getInstance()
        val cal = Calendar.getInstance()
        // add 5 minutes to the calendar object
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.SECOND, 0)
        var alarmMillis = cal.timeInMillis
        // is chkIos checked?
        if (calNow.after(cal)) {
            alarmMillis += 86400000L // Add 1 day if time selected
            // before now
            cal.timeInMillis = alarmMillis
        }
        if (cb1?.isChecked == true) {
            am?.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, sender)
        }
    }

    private fun showDialog(id: Int) {
        when (id) {
            TIME_DIALOG_ID ->
                // set time picker as current time
                TimePickerDialog(activity, timePickerListener, Calendar
                        .getInstance().get(Calendar.HOUR_OF_DAY), Calendar
                        .getInstance().get(Calendar.MINUTE), true).show()
        }
    }

    fun onTimerClick(v: View) {
        showDialog(TIME_DIALOG_ID)
    }

    private val titleChangedListener: ITitleChangedListener = object : ITitleChangedListener {
        override fun onTitleChanged(title: String) {
            activity?.runOnUiThread {
                mainFragmentView?.findViewById<TextView>(R.id.textView1)?.text = title
            }
        }
    }

    private val authorChangedListener: IAuthorChangedListener = object : IAuthorChangedListener {
        override fun onAuthorChanged(author: String) {
            activity?.runOnUiThread {
                if (author.isNotEmpty()) {
                    (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = author + " - " + player.title
                } else {
                    (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = player.author
                }
            }
        }
    }

    private val playStateChangedListener: IPlayStateChangedListener = object : IPlayStateChangedListener {
        override fun onPlayStateChanged(playState: PlayState) {
            activity?.runOnUiThread {
                val iv = mainFragmentView?.findViewById<ImageView>(R.id.streamButton)
                when (playState) {
                    PlayState.PLAY, PlayState.BUFFERING -> {
                        iv?.setImageResource(R.drawable.pause_states)
                    }
                    PlayState.PAUSE, PlayState.PLAY_FILE -> {
                        (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = ""
                    }
                    PlayState.STOP -> {
                        iv?.setImageResource(R.drawable.play_states)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private val recStateChangedListener: IRecStateChangedListener = object : IRecStateChangedListener {
        override fun onRecStateChanged(isRec: Boolean) {
            activity?.runOnUiThread {
                val rib = mainFragmentView?.findViewById<ImageView>(R.id.recordButton)
                if (isRec) {
                    rib?.setImageResource(R.drawable.rec_active)
                } else {
                    rib?.setImageResource(R.drawable.rec)
                }
            }
        }
    }

    private val bufferingProgressListener: IBufferingProgressListener = object : IBufferingProgressListener {
        override fun onBufferingProgress(progress: Long) {
            activity?.runOnUiThread { (mainFragmentView?.findViewById<TextView>(R.id.textView1))?.text = String.format("BUFFERING... %d%%", progress) }
        }
    }

    private val volumeChangedListener: IVolumeChangedListener = object : IVolumeChangedListener {
        override fun onVolumeChanged(volume: Float) {
            activity?.runOnUiThread { (mainFragmentView?.findViewById<SeekBar>(R.id.mainVolumeSeekBar))?.progress = (volume * 100).toInt() }
        }
    }

    private fun updateWidget() {
        val intent = Intent(activity?.applicationContext, FantasyRadioWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(activity?.applicationContext).getAppWidgetIds(
                ComponentName(activity!!.applicationContext, FantasyRadioWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        activity?.applicationContext?.sendBroadcast(intent)
    }
}