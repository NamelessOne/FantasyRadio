package ru.sigil.fantasyradio.schedule;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.log.LogManager;

public class ScheduleFragment extends Fragment {
    private String TAG = ScheduleFragment.class.getSimpleName();
    private ExpandableListView lv;
    private ParseAsyncTask searchAsyncTasc;
    ArrayList<ArrayList<ScheduleEntity>> arr = new ArrayList<>();
    private List<ScheduleEntity> scheduleEntityesCollection = new ArrayList<>();
    @Inject
    ScheduleParser scheduleParser;

    public ScheduleFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogManager.d(TAG, "onCreateView");
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        View scheduleFragmentView = inflater.inflate(R.layout.schedule_layout, container, false);
        scheduleFragmentView.findViewById(R.id.schedule_refresh_button).setOnClickListener(this::refreshClick);
        lv = scheduleFragmentView.findViewById(R.id.ScheduleListView);
        if (arr.size() > 0) {
            ScheduleListAdapter adapter = new ScheduleListAdapter(getActivity(), arr);
            lv.setAdapter(adapter);
            int count = adapter.getGroupCount();
            for (int position = 1; position <= count; position++)
                lv.expandGroup(position - 1);
        }
        return scheduleFragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Парсим календарь в фоне и просим юзера подождать.
     */
    private class ParseAsyncTask extends AsyncTask<Void, Integer, Void> {

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progress = new ProgressDialog(getActivity());
            this.progress.setMessage(getString(R.string.load));
            this.progress.setCancelable(false);
            this.progress.setOnCancelListener(dialog -> {
                // STOP thread execution
                searchAsyncTasc.cancel(true);
            });
            this.progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            scheduleEntityesCollection = scheduleParser.ParseSchedule();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            arr = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                ArrayList<ScheduleEntity> arr2 = new ArrayList<>();
                LocalDate ld = LocalDate.now();
                ld = ld.plusDays(i);
                for (ScheduleEntity scheduleEntity : scheduleEntityesCollection) {
                    if (scheduleEntity.getStartDate() != null && scheduleEntity.getStartDate().getDayOfYear() == ld
                            .getDayOfYear()) {
                        arr2.add(scheduleEntity);
                    }
                }
                Collections.reverse(arr2);
                arr.add(arr2);
            }
            // ------------------------------------
            ScheduleListAdapter adapter = new ScheduleListAdapter(getActivity(), arr);
            lv.setAdapter(adapter);
            int count = adapter.getGroupCount();
            for (int position = 1; position <= count; position++)
                lv.expandGroup(position - 1);
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }

    public void refreshClick(@SuppressWarnings("UnusedParameters") View v) {
        searchAsyncTasc = new ParseAsyncTask();
        searchAsyncTasc.execute();
    }

    @Override
    public void onResume() {
        LogManager.d(TAG, "onResume");
        super.onResume();
    }
}
