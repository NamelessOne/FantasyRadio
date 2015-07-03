package ru.sigil.fantasyradio.schedule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import ru.sigil.fantasyradio.MyApp;
import ru.sigil.fantasyradio.R;

public class ScheduleActivity extends Activity {
    private String TAG = ScheduleActivity.class.getSimpleName();
    private AdView adView;
    private ExpandableListView lv;
    private ParseAsyncTask searchAsyncTasc;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);
        adView = (AdView)this.findViewById(R.id.scheduleAdView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getBaseContext()).build();
        ImageLoader.getInstance().init(config);
        lv = (ExpandableListView) findViewById(R.id.ScheduleListView);
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);    // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);    // Add this method.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Парсим календарь в фоне и просим юзера подождать.
     */
    private class ParseAsyncTask extends AsyncTask<Void, Integer, Void> {

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progress = new ProgressDialog(ScheduleActivity.this);
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
            ArrayList<ArrayList<ScheduleEntity>> arr = new ArrayList<ArrayList<ScheduleEntity>>();
            for (int i = 0; i < 3; i++) {
                ArrayList<ScheduleEntity> arr2 = new ArrayList<ScheduleEntity>();
                LocalDate ld = LocalDate.now();
                ld = ld.plusDays(i);
                for (Iterator<ScheduleEntity> it = ScheduleEntityesCollection.getEntityes().iterator(); it.hasNext(); ) {
                    ScheduleEntity scheduleEntity = it.next();
                    if (scheduleEntity.getStartDate().getDayOfYear() == ld
                            .getDayOfYear()) {
                        arr2.add(scheduleEntity);
                    }
                }
                Collections.reverse(arr2);
                arr.add(arr2);
            }
            // ------------------------------------
            ScheduleListAdapter adapter = new ScheduleListAdapter(ScheduleActivity.this, arr);
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

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
    }

    public void refreshClick(@SuppressWarnings("UnusedParameters") View v) {
        searchAsyncTasc = new ParseAsyncTask();
        searchAsyncTasc.execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }
    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }
    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
    }

}
