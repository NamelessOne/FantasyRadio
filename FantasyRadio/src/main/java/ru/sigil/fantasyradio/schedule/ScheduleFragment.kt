package ru.sigil.fantasyradio.schedule

import android.support.v4.app.Fragment
import javax.inject.Inject
import android.widget.ExpandableListView
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import ru.sigil.fantasyradio.R
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.log.LogManager
import android.app.ProgressDialog
import android.os.AsyncTask
import org.joda.time.LocalDate
import java.util.*

/**
 * Created by namelessone
 * on 02.12.18.
 */
class ScheduleFragment: Fragment() {
    init {
        arguments = Bundle()
    }
    private val TAG = ScheduleFragment::class.java.simpleName
    private var lv: ExpandableListView? = null
    private var parseAsyncTask: ParseAsyncTask? = null
    private var arr = ArrayList<ArrayList<ScheduleEntity>>()
    private var scheduleEntitiesCollection: List<ScheduleEntity> = ArrayList()
    @set:Inject
    var scheduleParser: ScheduleParser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogManager.d(TAG, "onCreateView")
        Bootstrap.INSTANCE.getBootstrap().inject(this)
        val scheduleFragmentView = inflater.inflate(R.layout.schedule_layout, container, false)
        scheduleFragmentView.findViewById<View>(R.id.schedule_refresh_button).setOnClickListener(this::refreshClick)
        lv = scheduleFragmentView.findViewById(R.id.ScheduleListView)
        if (arr.size > 0) {
            val adapter = ScheduleListAdapter(activity, arr)
            lv?.setAdapter(adapter)
            val count = adapter.groupCount
            for (position in 1..count)
                lv?.expandGroup(position - 1)
        }
        return scheduleFragmentView
    }

    override fun onPause() {
        super.onPause()
    }

    /**
     * Парсим календарь в фоне и просим юзера подождать.
     */
    private inner class ParseAsyncTask : AsyncTask<Void, Int, Void>() {

        private var progress: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog(activity)
            progress?.setMessage(getString(R.string.load))
            progress?.setCancelable(false)
            progress?.setOnCancelListener {
                // STOP thread execution
                parseAsyncTask?.cancel(true)
            }
            progress?.show()
        }

        override fun doInBackground(vararg params: Void): Void? {
            scheduleEntitiesCollection = scheduleParser!!.parseSchedule()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            arr = ArrayList()
            for (i in 0..2) {
                val arr2 = ArrayList<ScheduleEntity>()
                var ld = LocalDate.now()
                ld = ld.plusDays(i)
                for (scheduleEntity in scheduleEntitiesCollection) {
                    if (scheduleEntity.startDate?.dayOfYear == ld.dayOfYear) {
                        arr2.add(scheduleEntity)
                    }
                }
                arr2.reverse()
                arr.add(arr2)
            }
            // ------------------------------------
            val adapter = ScheduleListAdapter(activity, arr)
            lv?.setAdapter(adapter)
            val count = adapter.groupCount
            for (position in 1..count)
                lv?.expandGroup(position - 1)
            if (progress?.isShowing == true) {
                progress?.dismiss()
            }
        }

        override fun onCancelled() {
            if (progress?.isShowing == true) {
                progress?.dismiss()
            }
        }
    }

    private fun refreshClick(v: View) {
        parseAsyncTask = ParseAsyncTask()
        parseAsyncTask.let { task -> task?.execute() }
    }

    override fun onResume() {
        LogManager.d(TAG, "onResume")
        super.onResume()
    }
}