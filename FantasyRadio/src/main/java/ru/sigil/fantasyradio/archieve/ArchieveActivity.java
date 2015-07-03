package ru.sigil.fantasyradio.archieve;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.sigil.fantasyradio.AbstractListActivity;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException;
import ru.sigil.fantasyradio.saved.MP3Entity;
import ru.sigil.fantasyradio.saved.MP3Saver;
import ru.sigil.fantasyradio.settings.Settings;
import ru.sigil.fantasyradio.utils.DownladedEntityes;
import ru.sigil.fantasyradio.utils.DownloadThread;

public class ArchieveActivity extends AbstractListActivity {
    private AdView adView;
    private ArchieveListAdapter adapter;
    private ParseAsyncTask searchAsyncTasc;
    private AlertDialog.Builder ad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archieve_layout);
        adView = (AdView)this.findViewById(R.id.archieveAdView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        TextView tv = (TextView) findViewById(R.id.archive_text1);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        ad = new AlertDialog.Builder(this);
        setLv((ListView) findViewById(R.id.ArchieveListView));
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);   // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this); ;  // Add this method.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
    }

    public void refreshClick(@SuppressWarnings("UnusedParameters") View v) {
        //Сначала логинимся
        ad.setTitle(getString(R.string.enter));  // заголовок
        /*
        try {
            GoogleAnalytics.getInstance(this).sendEvent("Clicks", "ArchieveClicks", "EnterToaArchieveClick", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.login_dialog, null);
        ad.setTitle(R.string.enter)
                .setView(textEntryView);
        ((TextView) ((ViewGroup) textEntryView).getChildAt(0)).setText(Settings.getLogin());
        ((TextView) ((ViewGroup) textEntryView).getChildAt(1)).setText(Settings.getPassword());
        ad.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                String login = ((TextView) ((ViewGroup) textEntryView).getChildAt(0)).getText().toString();
                String password = ((TextView) ((ViewGroup) textEntryView).getChildAt(1)).getText().toString();
                Settings.saveLoginAndPassword(login, password);
                searchAsyncTasc = new ParseAsyncTask();
                try {
                    searchAsyncTasc.login = login;
                    searchAsyncTasc.password = password;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                searchAsyncTasc.execute();
            }
        });
        ad.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });
        ad.create();
        ad.show();

        //--------------------
    }

    private class ParseAsyncTask extends AsyncTask<Void, Integer, Void> {

        ProgressDialog progress;
        String password;
        String login;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                adapter.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }// !
            this.progress = new ProgressDialog(ArchieveActivity.this);
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
            try {
                ArchieveParser.ParseArchieve(login, password);
            } catch (WrongLoginOrPasswordException e) {
                //сообщение о неправильном логине/пароле
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getBaseContext(),
                                getString(R.string.wrong_login_or_password), Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter = new ArchieveListAdapter(getBaseContext(),
                    ArchieveEntityesCollection.getEntityes());
            getLv().setAdapter(adapter);
            // ---------------------------------------
            // ----------------------------------------
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            // Тут мб что-то дописать
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }

    public void downloadClick(View v) {
        /*
        try {
            EasyTracker.getTracker().sendEvent("Clicks", "ArchieveClicks", "DownloadFromArchieveClick", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        ArchieveEntity entity = (ArchieveEntity) v.getTag();
        Log.v("downloadClick", entity.getURL());
        MP3Entity mp3Entity = new MP3Entity();
        mp3Entity.setArtist(entity.getName());
        mp3Entity.setTime(entity.getTime());
        mp3Entity.setTitle(entity.getName());
        mp3Entity.setDirectory(Environment.getExternalStorageDirectory()
                + Settings.getSaveDir() + formattedDate + entity.getFileName());
        Toast toast = Toast.makeText(getBaseContext(),
                getString(R.string.download_started), Toast.LENGTH_LONG);
        toast.show();
        // -----------------------------------------------------------------------
        try {
            LinearLayout rl = (LinearLayout) v.getParent();
            ProgressBar pb = null;
            if (rl != null) {
                pb = (ProgressBar) rl.getChildAt(1);
            }
            if (pb != null) {
                pb.setVisibility(View.VISIBLE);
            }
            v.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DownladedEntityes.getDownloadedEntityes().add(entity.getURL());
        // -----------------------------------------------------------------------
        DownloadThread dt = new DownloadThread(entity.getURL(),
                Environment.getExternalStorageDirectory() + Settings.getSaveDir(),
                formattedDate + entity.getFileName(), getBaseContext(),
                downloadFinishedHandler, mp3Entity);
        dt.start();
        // ----------------------------
    }

    private Handler downloadFinishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast toast = Toast.makeText(getBaseContext(),
                    getString(R.string.download_finished), Toast.LENGTH_LONG);
            toast.show();
            //А тут мы пишем инфу о скачанном
            // файле в базу
            MP3Entity mp3Entity = new MP3Entity();
            Bundle b = msg.getData();
            if (b != null) {
                mp3Entity.setArtist(b.getString("artist"));
            }
            mp3Entity.setTitle(b.getString("title"));
            mp3Entity.setDirectory(b.getString("directory"));
            mp3Entity.setTime(b.getString("time"));
            MP3Saver.getMp3c()
                    .removeEntityByDirectory(mp3Entity.getDirectory());
            MP3Saver.getMp3c().add(mp3Entity);
            DownladedEntityes.getDownloadedEntityes()
                    .remove(b.getString("URL"));
            try {
                SeekBar sb = (SeekBar) getLv().findViewWithTag(b.getString("URL"));// ProgressSeekBar!!!
                LinearLayout ll = null;
                if (sb != null) {
                    ll = (LinearLayout) sb.getParent().getParent();
                }
                LinearLayout ll1 = null;
                if (ll != null) {
                    ll1 = (LinearLayout) ll.getChildAt(1);
                }
                RelativeLayout rl = null;
                if (ll1 != null) {
                    rl = (RelativeLayout) ll1.getChildAt(2);
                }
                ImageView arrow = null;
                if (rl != null) {
                    arrow = (ImageView) rl.getChildAt(0);
                }
                if (arrow != null) {
                    arrow.setVisibility(View.VISIBLE);
                }
                ProgressBar progress = (ProgressBar) rl.getChildAt(1);
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
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
