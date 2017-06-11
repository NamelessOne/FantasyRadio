package ru.sigil.fantasyradio.schedule;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import ru.sigil.fantasyradio.R;

class ScheduleListAdapter extends BaseExpandableListAdapter {

    private String[] item;
    private Context context;

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("HH':'mm");
    private ArrayList<ArrayList<ScheduleEntity>> entities;

    public ScheduleListAdapter(Context context,
                               ArrayList<ArrayList<ScheduleEntity>> objects) {
        this.context = context;
        this.entities = objects;
        Resources res = context.getResources();
        if (res != null)
            item = res.getStringArray(R.array.week);
        else
            item = new String[]{"", "", "", "", "", "", ""};
    }

    @Override
    public int getGroupCount() {
        return entities.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return entities.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return entities.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return entities.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_view, null);
        }
        TextView textGroup = null;
        if (convertView != null) {
            textGroup = convertView
                    .findViewById(R.id.textGroup);
        }
        LocalDate ld = LocalDate.now();
        ld = ld.plusDays(groupPosition);
        textGroup.setText(item[ld.getDayOfWeek() - 1] + ", "
                + ld.getDayOfMonth() + "." + ld.getMonthOfYear());
        return convertView;

    }

    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.schedule_list_item, parent, false);
        }
        final ScheduleEntity message = entities.get(groupPosition).get(childPosition);
        TextView startTime = null;
        if (row != null) {
            startTime = row.findViewById(R.id.ScheduleItemStartTime);
        }
        startTime.setText(fmt.print(message.getStartDate()) + " - " + fmt.print(message.getEndDate()));
        TextView title = row.findViewById(R.id.ScheduleItemTitle);
        title.setText(message.getTitle());
        final String titleString = message.getTitle();
        final String messageString = message.getText();
        final String imageURLString = message.getImageURL();
        //----------------------------------------
        row.setOnClickListener(v -> {
            //Кликнули на элемент списка. Показываем окошко с подробностями
            final Dialog alertDialog = new Dialog(context);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setContentView(R.layout.schedule_dialog);
            LinearLayout ll = (alertDialog.findViewById(R.id.scheduleDialogLayout));
            ll.setOnClickListener(v1 -> alertDialog.cancel());
            LinearLayout ll2 = (alertDialog.findViewById(R.id.scheduleDialogInternalLayout));
            ll2.setOnClickListener(v12 -> alertDialog.cancel());
            TextView tv = (alertDialog.findViewById(R.id.schedule_dialog_text));
            tv.setText(messageString);
            ImageView im = (alertDialog.findViewById(R.id.schedule_dialog_image));
            TextView tv2 = (alertDialog.findViewById(R.id.schedule_dialog_title));
            tv2.setText(titleString);
            try {
                alertDialog.setCancelable(true);
                alertDialog.setCanceledOnTouchOutside(true);
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(imageURLString, im);
            } catch (Exception e) {
                e.printStackTrace();
            }
            alertDialog.show();
        });
        //------------------------------------------------------------
        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
