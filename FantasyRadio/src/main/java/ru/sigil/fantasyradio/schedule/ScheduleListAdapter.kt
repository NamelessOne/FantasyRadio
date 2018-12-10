package ru.sigil.fantasyradio.schedule

import android.app.Dialog
import android.content.Context
import android.widget.BaseExpandableListAdapter
import org.joda.time.format.DateTimeFormat
import ru.sigil.fantasyradio.R
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.joda.time.LocalDate
import com.nostra13.universalimageloader.core.ImageLoader
import android.widget.LinearLayout
import android.view.Window
import android.widget.ImageView

/**
 * Created by namelessone
 * on 03.12.18.
 */
class ScheduleListAdapter(private val context: Context?, private val entities: ArrayList<ArrayList<ScheduleEntity>>): BaseExpandableListAdapter() {
    private val item: Array<String>
    private val fmt = DateTimeFormat.forPattern("HH':'mm")

    init {
        val res = context?.resources
        item = if (res != null)
            res.getStringArray(R.array.week)
        else
            arrayOf("", "", "", "", "", "", "")
    }

    override fun getGroupCount(): Int {
        return entities.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return entities[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any {
        return entities[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return entities[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean,
                     convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.group_view, null)
        }
        val textGroup = view?.findViewById<TextView>(R.id.textGroup)
        var ld = LocalDate.now()
        ld = ld.plusDays(groupPosition)
        textGroup!!.text = (item[ld.dayOfWeek - 1] + ", " + ld.dayOfMonth + "." + ld.monthOfYear)
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int,
                              isLastChild: Boolean, convertView: View, parent: ViewGroup): View {
        var row: View? = convertView
        if (row == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            row = inflater.inflate(R.layout.schedule_list_item, parent, false)
        }
        val (startDate, endDate, titleString, imageURLString, messageString) = entities[groupPosition][childPosition]
        var startTime: TextView? = null
        if (row != null) {
            startTime = row.findViewById(R.id.ScheduleItemStartTime)
        }
        startTime!!.text = fmt.print(startDate) + " - " + fmt.print(endDate)
        val title = row!!.findViewById<TextView>(R.id.ScheduleItemTitle)
        title.text = titleString
        row.setOnClickListener { _ ->
            //Кликнули на элемент списка. Показываем окошко с подробностями
            val alertDialog = Dialog(context)
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alertDialog.setContentView(R.layout.schedule_dialog)
            val ll = alertDialog.findViewById<LinearLayout>(R.id.scheduleDialogLayout)
            ll.setOnClickListener({ _ -> alertDialog.cancel() })
            val ll2 = alertDialog.findViewById<LinearLayout>(R.id.scheduleDialogInternalLayout)
            ll2.setOnClickListener({ _ -> alertDialog.cancel() })
            val tv = alertDialog.findViewById<TextView>(R.id.schedule_dialog_text)
            tv.text = messageString
            val im = alertDialog.findViewById<ImageView>(R.id.schedule_dialog_image)
            val tv2 = alertDialog.findViewById<TextView>(R.id.schedule_dialog_title)
            tv2.text = titleString
            try {
                alertDialog.setCancelable(true)
                alertDialog.setCanceledOnTouchOutside(true)
                val imageLoader = ImageLoader.getInstance()
                imageLoader.displayImage(imageURLString, im)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            alertDialog.show()
        }
        return row
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}