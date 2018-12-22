package ru.sigil.fantasyradio.archive

import android.app.AlertDialog
import ru.sigil.fantasyradio.AbstractListFragment
import javax.inject.Inject
import ru.sigil.fantasyradio.saved.MP3Collection
import java.util.*
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import ru.sigil.fantasyradio.R
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException
import android.app.ProgressDialog
import android.os.*
import android.util.Log
import ru.sigil.fantasyradio.saved.MP3Entity
import java.text.SimpleDateFormat
import ru.sigil.fantasyradio.TabHoster
import android.os.Bundle
import android.widget.*
import ru.sigil.fantasyradio.settings.ISettings
import ru.sigil.fantasyradio.utils.IDownloadThreadFactory


/**
 * Created by namelessone
 * on 29.11.18.
 */
class ArchiveFragment: AbstractListFragment() {
    private var adapter: ArchiveListAdapter? = null
    private var searchAsyncTask: ParseAsyncTask? = null
    private var ad: AlertDialog.Builder? = null
    private var random: Random? = null
    private var archiveEntityes: List<ArchiveEntity> = ArrayList()
    @set:Inject
    var mp3Collection: MP3Collection? = null
    @set:Inject
    var archiveGetter: ArchiveGetter? = null
    @set:Inject
    var settings: ISettings? = null
    @set:Inject
    var downloadThreadFactory: IDownloadThreadFactory? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Bootstrap.INSTANCE.getBootstrap().inject(this)
        val archiveActivityView = inflater.inflate(R.layout.archieve_layout, container, false)
        val tv = archiveActivityView.findViewById<TextView>(R.id.archive_text1)
        tv.movementMethod = LinkMovementMethod.getInstance()
        archiveActivityView.findViewById<View>(R.id.archieve_refresh_button).setOnClickListener({ v -> refreshClick(v) })
        ad = AlertDialog.Builder(activity)
        lv = archiveActivityView.findViewById(R.id.ArchieveListView)
        adapter = ArchiveListAdapter(activity!!.baseContext,
                archiveEntityes, downloadClickListener)
        lv?.adapter = adapter
        return archiveActivityView
    }

    private fun refreshClick(v: View) {
        //Сначала логинимся
        ad?.setTitle(getString(R.string.enter))  // заголовок
        val factory = LayoutInflater.from(activity)
        val textEntryView = factory.inflate(R.layout.login_dialog, null)
        ad?.setTitle(R.string.enter)?.setView(textEntryView)
        ((textEntryView as ViewGroup).getChildAt(0) as TextView).text = settings?.getLogin()
        (textEntryView.getChildAt(1) as TextView).text = settings?.getPassword()
        ad?.setPositiveButton(getString(R.string.enter)) { _, _ ->
            val login = (textEntryView.getChildAt(0) as TextView).text.toString()
            val password = (textEntryView.getChildAt(1) as TextView).text.toString()
            settings?.setLogin(login)
            settings?.setPassword(login)
            searchAsyncTask = ParseAsyncTask()
            try {
                searchAsyncTask?.login = login
                searchAsyncTask?.password = password
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            searchAsyncTask?.execute()
        }
        ad?.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        ad?.setCancelable(true)
        ad?.setOnCancelListener { _ -> }
        ad?.create()
        ad?.show()
        //--------------------
    }

    private fun getRandom(): Random {
        if (random == null)
            random = Random()
        return random as Random
    }

    private inner class ParseAsyncTask : AsyncTask<Void, Int, Void>() {
        internal var progress: ProgressDialog? = null
        internal var password: String = ""
        internal var login: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
            try {
                adapter?.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // !
            progress = ProgressDialog(activity)
            progress?.setMessage(getString(R.string.load))
            progress?.setCancelable(false)
            progress?.setOnCancelListener { _ ->
                // STOP thread execution
                searchAsyncTask?.cancel(true)
            }
            progress?.show()
        }

        override fun doInBackground(vararg params: Void): Void? {
            try {
                archiveEntityes = archiveGetter!!.parseArchive(login, password)
            } catch (e: WrongLoginOrPasswordException) {
                //сообщение о неправильном логине/пароле
                activity!!.runOnUiThread {
                    val toast = Toast.makeText(activity!!.baseContext,
                            getString(R.string.wrong_login_or_password), Toast.LENGTH_LONG)
                    toast.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (activity != null) {
                adapter = ArchiveListAdapter(activity!!.baseContext,
                        archiveEntityes, downloadClickListener)
                lv?.adapter = adapter
            }
            if (progress?.isShowing == true) {
                progress?.dismiss()
            }
        }

        override fun onCancelled() {
            // Тут мб что-то дописать
            if (progress?.isShowing == true) {
                progress?.dismiss()
            }
        }
    }

    val downloadClickListener = View.OnClickListener{ v -> downloadClick(v) }

    private fun downloadClick(v: View) {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy")
        val formattedDate = df.format(c.time)
        val entity = v.tag as ArchiveEntity
        Log.v("downloadClick", entity.URL)
        val mp3Entity = MP3Entity(entity.Name, entity.Name,
                Environment.getExternalStorageDirectory().toString() + settings?.getSaveDir() + formattedDate + entity.getFileName(),
                entity.Time)
        val toast = Toast.makeText(activity!!.baseContext,
                getString(R.string.download_started), Toast.LENGTH_LONG)
        toast.show()
        val dt = downloadThreadFactory?.createDownloadThread(entity.URL,
                Environment.getExternalStorageDirectory().toString() + settings?.getSaveDir(),
                formattedDate + entity.getFileName(),
                downloadFinishedHandler, mp3Entity, errorHandler)
        dt?.start()
    }

    private val errorHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (activity != null) {
                val toast = Toast.makeText(activity!!.baseContext,
                        "Ошибка при сохранении файла", Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }

    private val downloadFinishedHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (activity != null) {
                val toast = Toast.makeText(activity!!.baseContext,
                        getString(R.string.download_finished), Toast.LENGTH_LONG)
                toast.show()
            }
            //А тут мы пишем инфу о скачанном
            // файле в базу
            var artist: String? = ""
            val b = msg.data
            if (b != null) {
                artist = b.getString("artist")
            }
            val mp3Entity = MP3Entity(artist, b!!.getString("title"), b.getString("directory"),
                    b.getString("time"))
            mp3Collection?.remove(mp3Entity)
            mp3Collection?.add(mp3Entity)
            try {
                val sb = lv?.findViewWithTag<SeekBar>(b.getString("URL"))// ProgressSeekBar!!!
                val ll = sb?.parent?.parent as LinearLayout?
                val ll1 = ll?.getChildAt(1) as LinearLayout?
                val rl = ll1?.getChildAt(2) as RelativeLayout?
                val arrow = rl?.getChildAt(0) as ImageView?
                arrow?.visibility = View.VISIBLE
                val progress = rl?.getChildAt(1) as ProgressBar?
                progress?.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                (activity as TabHoster).mSectionsPagerAdapter?.notifySavedFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}