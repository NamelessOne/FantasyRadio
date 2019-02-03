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
import kotlinx.coroutines.*
import ru.sigil.fantasyradio.settings.ISettings
import ru.sigil.fantasyradio.utils.IFileDownloader


/**
 * Created by namelessone
 * on 29.11.18.
 */
class ArchiveFragment : AbstractListFragment() {
    private var adapter: ArchiveListAdapter? = null
    private var ad: AlertDialog.Builder? = null
    private var archiveEntityes: List<ArchiveEntity> = ArrayList()
    private var parseArchieveJob: Job? = null

    @Inject
    lateinit var mp3Collection: MP3Collection
    @Inject
    lateinit var archiveGetter: ArchiveGetter
    @Inject
    lateinit var settings: ISettings
    @Inject
    lateinit var fileDownloader: IFileDownloader

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
        ((textEntryView as ViewGroup).getChildAt(0) as TextView).text = settings.getLogin()
        (textEntryView.getChildAt(1) as TextView).text = settings.getPassword()
        ad?.setPositiveButton(getString(R.string.enter)) { _, _ ->
            val login = (textEntryView.getChildAt(0) as TextView).text.toString()
            val password = (textEntryView.getChildAt(1) as TextView).text.toString()
            settings.setLogin(login)
            settings.setPassword(password)
            parseArchieveJob = GlobalScope.launch(Dispatchers.Main) {
                val progress = ProgressDialog(activity)
                try {
                    adapter?.clear()
                    progress.setMessage(getString(R.string.load))
                    progress.setCancelable(false)
                    progress.show()
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            archiveEntityes = archiveGetter.parseArchive(login, password)
                        } catch (e: WrongLoginOrPasswordException) {
                            //сообщение о неправильном логине/пароле
                            GlobalScope.launch(Dispatchers.Main) {
                                val toast = Toast.makeText(activity!!.baseContext,
                                        getString(R.string.wrong_login_or_password), Toast.LENGTH_LONG)
                                toast.show()
                            }
                        }
                    }.join()
                    adapter = ArchiveListAdapter(activity!!.baseContext, archiveEntityes, downloadClickListener)
                    lv?.adapter = adapter
                } finally {
                    if (progress.isShowing) {
                        progress.dismiss()
                    }
                }
            }
        }
        ad?.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        ad?.setCancelable(true)
        ad?.create()
        ad?.show()
    }

    override fun onDestroyView() {
        parseArchieveJob?.cancel()
        super.onDestroyView()
    }

    private val downloadClickListener = View.OnClickListener { v -> downloadClick(v) }

    private fun downloadClick(v: View) {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy")
        val formattedDate = df.format(c.time)
        val entity = v.tag as ArchiveEntity
        Log.v("downloadClick", entity.URL)
        val mp3Entity = MP3Entity(entity.Name, entity.Name,
                Environment.getExternalStorageDirectory().toString() + settings.getSaveDir() + formattedDate + entity.getFileName(),
                entity.Time)
        val toast = Toast.makeText(activity!!.baseContext,
                getString(R.string.download_started), Toast.LENGTH_LONG)
        toast.show()
        GlobalScope.launch(Dispatchers.Main) {
            val downloadFileJob = GlobalScope.async(Dispatchers.IO) {
                return@async fileDownloader.downloadFile(entity.URL, Environment.getExternalStorageDirectory().toString() + settings.getSaveDir(),
                        formattedDate + entity.getFileName(), mp3Entity.title, mp3Entity.time)
            }
            val mEntity = downloadFileJob.await()
            if(mEntity == null) {
                showErrorMessage()
            }
            else {
                downloadFinished(mEntity, entity.URL)
            }
        }
    }

    private fun showErrorMessage() {
        if (activity != null) {
            val toast = Toast.makeText(activity!!.baseContext,
                    "Ошибка при сохранении файла", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private fun downloadFinished(mEntity: MP3Entity, url: String) {
        if (activity != null) {
            val toast = Toast.makeText(activity!!.baseContext,
                    getString(R.string.download_finished), Toast.LENGTH_LONG)
            toast.show()
        }
        mp3Collection.remove(mEntity)
        mp3Collection.add(mEntity)
        try {
            val sb = lv?.findViewWithTag<SeekBar>(url)// ProgressSeekBar!!!
            val ll = sb?.parent?.parent as LinearLayout?
            val ll1 = ll?.getChildAt(1) as LinearLayout?
            val rl = ll1?.getChildAt(2) as RelativeLayout?
            val arrow = rl?.getChildAt(0) as ImageView?
            arrow?.visibility = View.VISIBLE // а при начале скачивания обратное действие не производится:/
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