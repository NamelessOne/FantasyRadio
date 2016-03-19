package ru.sigil.fantasyradio.schedule;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import ru.sigil.fantasyradio.BuildConfig;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.TabHoster;
import ru.sigil.log.LogManager;

public class ScheduleFragment extends Fragment {
    private String TAG = ScheduleFragment.class.getSimpleName();
    private ExpandableListView lv;
    private ParseAsyncTask searchAsyncTasc;
    ArrayList<ArrayList<ScheduleEntity>> arr = new ArrayList<>();
    private Random random;
    private static final int AD_SHOW_PROBABILITY_REFRESH = 25;

    public ScheduleFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        LogManager.d(TAG, "onCreateView");
        View scheduleFragmentView = inflater.inflate(R.layout.schedule_layout, container, false);
        scheduleFragmentView.findViewById(R.id.schedule_refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshClick(v);
            }
        });

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getBaseContext()).build();
        ImageLoader.getInstance().init(config);
        lv = (ExpandableListView) scheduleFragmentView.findViewById(R.id.ScheduleListView);
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

    private Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
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
            this.progress.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // stop thread execution
                    searchAsyncTasc.cancel(true);
                }
            });
            this.progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ScheduleParser.ParseSchedule();

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
                for (Iterator<ScheduleEntity> it = ScheduleEntityesCollection.getEntityes().iterator(); it.hasNext(); ) {
                    ScheduleEntity scheduleEntity = it.next();
                    if (scheduleEntity.getStartDate()!=null&&scheduleEntity.getStartDate().getDayOfYear() == ld
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
        if(BuildConfig.FLAVOR.equals("free") && getRandom().nextInt(100) < AD_SHOW_PROBABILITY_REFRESH) {
            if (((TabHoster)getActivity()).getmInterstitialAd().isLoaded()) {
                ((TabHoster)getActivity()).getmInterstitialAd().show();
            }
        }
        searchAsyncTasc = new ParseAsyncTask();
        searchAsyncTasc.execute();
    }

    @Override
    public void onResume() {
        LogManager.d(TAG, "onResume");
        super.onResume();
    }
}
