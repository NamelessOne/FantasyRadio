package ru.sigil.fantasyradio.archieve;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import ru.sigil.fantasyradio.AbstractListFragment;
import ru.sigil.fantasyradio.BuildConfig;
import ru.sigil.fantasyradio.R;
import ru.sigil.fantasyradio.TabHoster;
import ru.sigil.fantasyradio.ad.AdService;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException;
import ru.sigil.fantasyradio.saved.MP3Collection;
import ru.sigil.fantasyradio.saved.MP3Entity;
import ru.sigil.fantasyradio.settings.Settings;
import ru.sigil.fantasyradio.utils.DownloadThread;

public class ArchieveFragment extends AbstractListFragment {
    private ArchieveListAdapter adapter;
    private ParseAsyncTask searchAsyncTasc;
    private AlertDialog.Builder ad;
    private Random random;
    private static final int AD_SHOW_PROBABILITY_REFRESH = 25;
    private List<ArchieveEntity> archieveEntityes = new ArrayList<>();
    @Inject
    MP3Collection mp3Collection;
    @Inject
    ArchieveGetter archieveGetter;
    @Inject
    AdService adService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        View archieveActivityView = inflater.inflate(R.layout.archieve_layout, container, false);
        TextView tv = archieveActivityView.findViewById(R.id.archive_text1);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        archieveActivityView.findViewById(R.id.archieve_refresh_button).setOnClickListener(v -> refreshClick(v));
        ad = new AlertDialog.Builder(getActivity());
        setLv(archieveActivityView.findViewById(R.id.ArchieveListView));
        adapter = new ArchieveListAdapter(getActivity().getBaseContext(),
                archieveEntityes, downloadClickListener);
        getLv().setAdapter(adapter);
        return archieveActivityView;
    }

    public void refreshClick(View v) {
        //Сначала логинимся
        ad.setTitle(getString(R.string.enter));  // заголовок
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.login_dialog, null);
        ad.setTitle(R.string.enter)
                .setView(textEntryView);
        ((TextView) ((ViewGroup) textEntryView).getChildAt(0)).setText(Settings.getLogin());
        ((TextView) ((ViewGroup) textEntryView).getChildAt(1)).setText(Settings.getPassword());
        ad.setPositiveButton(getString(R.string.enter), (dialog, arg1) -> {
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
        });
        ad.setNegativeButton(getString(R.string.cancel), (dialog, arg1) -> {
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(dialog -> {
        });
        ad.create();
        ad.show();
        //--------------------
    }

    private Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
    }

    private class ParseAsyncTask extends AsyncTask<Void, Integer, Void> {

        ProgressDialog progress;
        String password;
        String login;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adService.showAd(AD_SHOW_PROBABILITY_REFRESH);
            try {
                adapter.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }// !
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
            try {
                archieveEntityes = archieveGetter.ParseArchieve(login, password);
            } catch (WrongLoginOrPasswordException e) {
                //сообщение о неправильном логине/пароле
                getActivity().runOnUiThread(() -> {
                    Toast toast = Toast.makeText(getActivity().getBaseContext(),
                            getString(R.string.wrong_login_or_password), Toast.LENGTH_LONG);
                    toast.show();
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
            if (getActivity() != null) {
                adapter = new ArchieveListAdapter(getActivity().getBaseContext(),
                        archieveEntityes, downloadClickListener);
                getLv().setAdapter(adapter);
            }
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

    final View.OnClickListener downloadClickListener = v -> downloadClick(v);

    public void downloadClick(View v) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        ArchieveEntity entity = (ArchieveEntity) v.getTag();
        Log.v("downloadClick", entity.getURL());
        MP3Entity mp3Entity = new MP3Entity(entity.getName(), entity.getName(),
                Environment.getExternalStorageDirectory() + Settings.getSaveDir() + formattedDate + entity.getFileName(),
                entity.getTime());
        Toast toast = Toast.makeText(getActivity().getBaseContext(),
                getString(R.string.download_started), Toast.LENGTH_LONG);
        toast.show();
        DownloadThread dt = new DownloadThread(entity.getURL(),
                Environment.getExternalStorageDirectory() + Settings.getSaveDir(),
                formattedDate + entity.getFileName(), getActivity().getBaseContext(),
                downloadFinishedHandler, mp3Entity, errorHandler);
        dt.start();
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() != null) {
                Toast toast = Toast.makeText(getActivity().getBaseContext(),
                        "Ошибка при сохранении файла", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    private Handler downloadFinishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() != null) {
                Toast toast = Toast.makeText(getActivity().getBaseContext(),
                        getString(R.string.download_finished), Toast.LENGTH_LONG);
                toast.show();
            }
            //А тут мы пишем инфу о скачанном
            // файле в базу
            String artist = "";
            Bundle b = msg.getData();
            if (b != null) {
                artist = b.getString("artist");
            }
            MP3Entity mp3Entity = new MP3Entity(artist, b.getString("title"), b.getString("directory"),
                    b.getString("time"));
            mp3Collection.remove(mp3Entity);
            mp3Collection.add(mp3Entity);
            try {
                SeekBar sb = getLv().findViewWithTag(b.getString("URL"));// ProgressSeekBar!!!
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
            try {
                ((TabHoster) getActivity()).mSectionsPagerAdapter.notifySavedFragment();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
