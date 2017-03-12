package ru.sigil.fantasyradio.archieve;

import android.content.Context;
import android.support.annotation.NonNull;
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

    private List<ArchieveEntity> entities = new ArrayList<>();
    private View.OnClickListener downloadClickListener;

    public ArchieveListAdapter(Context context,
                               List<ArchieveEntity> objects, View.OnClickListener downloadClickListener) {
        super(context, R.layout.archieve_list_item, objects);
        this.entities = objects;
        this.downloadClickListener = downloadClickListener;
    }

    public int getCount() {
        return this.entities.size();
    }

    public ArchieveEntity getItem(int index) {
        return this.entities.get(index);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
        downloadButton.setOnClickListener(downloadClickListener);
        return row;
    }
}
