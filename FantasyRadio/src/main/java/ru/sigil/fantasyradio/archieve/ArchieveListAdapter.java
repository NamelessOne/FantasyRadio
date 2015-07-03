package ru.sigil.fantasyradio.archieve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.sigil.fantasyradio.R;

class ArchieveListAdapter extends ArrayAdapter<ArchieveEntity> {

    public static int width;

    private List<ArchieveEntity> entities = new ArrayList<ArchieveEntity>();

    public ArchieveListAdapter(Context context,
                               List<ArchieveEntity> objects) {
        super(context, R.layout.archieve_list_item, objects);
        this.entities = objects;
    }

    public int getCount() {
        return this.entities.size();
    }

    public ArchieveEntity getItem(int index) {
        return this.entities.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.archieve_list_item, parent, false);
        }
        ArchieveEntity message = getItem(position);
        TextView time = null;
        if (row != null) {
            time = (TextView) row.findViewById(R.id.ArchieveItemTime);
        }
        time.setText(message.getTime());
        TextView title = (TextView) row.findViewById(R.id.ArchieveItemTitle);
        title.setText(message.getName());
        ImageView downloadButton = (ImageView) row
                .findViewById(R.id.archieveItemDownloadButton);
        downloadButton.setTag(message);
        return row;
    }
}
