package ru.sigil.fantasyradio.archive

import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import ru.sigil.fantasyradio.R


/**
 * Created by namelessone
 * on 27.11.18.
 */
internal class ArchiveListAdapter(context: Context,
                                  private val entities: List<ArchiveEntity>,
                                  private val downloadClickListener: View.OnClickListener)
                                                : ArrayAdapter<ArchiveEntity>(context, R.layout.archieve_list_item, entities) {

    override fun getCount(): Int {
        return this.entities.size
    }

    override fun getItem(index: Int): ArchiveEntity {
        return this.entities[index]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row: View
        row = if (convertView == null) {
            // ROW INFLATION
            val inflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.archieve_list_item, parent, false)
        }
        else {
            convertView
        }
        val message = getItem(position)
        val time = row.findViewById<TextView>(R.id.ArchieveItemTime)
        time?.text = message.Time
        val title = row.findViewById<TextView>(R.id.ArchieveItemTitle)
        title?.text = message.Name
        val downloadButton = row.findViewById<ImageView>(R.id.archieveItemDownloadButton)
        downloadButton.tag = message
        downloadButton.setOnClickListener(downloadClickListener)
        return row
    }
}