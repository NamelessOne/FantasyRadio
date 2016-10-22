package ru.sigil.fantasyradio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import javax.inject.Inject;

import ru.sigil.fantasyradio.BackgroundService.IPLayerErrorListener;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.archieve.ArchieveFragment;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.saved.MP3Collection;
import ru.sigil.fantasyradio.saved.SavedFragment;
import ru.sigil.fantasyradio.schedule.ScheduleFragment;
import ru.sigil.fantasyradio.settings.Settings;
import ru.sigil.fantasyradio.settings.SettingsActivity;
import ru.sigil.fantasyradio.utils.FantasyRadioNotificationManager;
import ru.sigil.log.LogManager;

public class TabHoster extends FragmentActivity {
    private static final String TAG = TabHoster.class.getSimpleName();
    Context context;
    public SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private InterstitialAd mInterstitialAd;

    @Inject
    IPlayer player;
    @Inject
    FantasyRadioNotificationManager notificationManager;

    private static int current_menu;

    private void requestNewInterstitial() {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            getmInterstitialAd().loadAd(adRequest);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        player.addErrorListener(playerErrorListener);
        setContentView(R.layout.tabs);
        // EasyTracker is now ready for use.
        setCurrent_menu(R.menu.activity_main);
        player.getMp3Saver().setMp3c(new MP3Collection(getBaseContext()));
        player.getMp3Saver().getMp3c().Load();
        SharedPreferences settings = getPreferences(0);
        Settings.setSettings(settings);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        context = getApplicationContext();
        if (BuildConfig.FLAVOR.equals("free")) {
            if (getmInterstitialAd() == null) {
                mInterstitialAd = new InterstitialAd(this);
                getmInterstitialAd().setAdUnitId(getString(R.string.admob_publisher_id));

                getmInterstitialAd().setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        LogManager.d("AD", "closed");
                        requestNewInterstitial();
                    }
                });
                requestNewInterstitial();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        if (player.currentState() == PlayState.PLAY
                || player.currentState() == PlayState.PLAY_FILE || player.currentState() == PlayState.BUFFERING) {
            notificationManager
                    .createNotification(player.currentTitle(), player.currentArtist(), player.currentState());
        } else {
            player.stop();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        player.removeErrorListener(playerErrorListener);
        player.stop();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && player.currentState() == PlayState.PLAY
                || player.currentState() == PlayState.PLAY_FILE || player.currentState() == PlayState.BUFFERING) {
            onPause();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        //--------------
        SharedPreferences settings = getPreferences(0);
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !settings.getBoolean("gratitude", false)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("gratitude", true);
            editor.commit();
            Intent i = new Intent(getApplicationContext(), Gratitude.class);
            startActivity(i);
            return true;
        }
        //--------------
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getCurrent_menu(), menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getCurrent_menu(), menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent i = new Intent(this, About.class);
                try {
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.settings:
                Intent j = new Intent(this, SettingsActivity.class);
                try {
                    startActivity(j);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.exit:
                player.stop();
                //--------------
                SharedPreferences settings = getPreferences(0);
                if (!settings.getBoolean("gratitude", false)) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("gratitude", true);
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), Gratitude.class);
                    startActivity(intent);
                    return true;
                }
                //--------------
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void setCurrent_menu(int current_menu) {
        TabHoster.current_menu = current_menu;
    }

    private static int getCurrent_menu() {
        return current_menu;
    }

    @Override
    public void onResume() {
        if (notificationManager.notificationManager != null) {
            try {

                notificationManager.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }

    public void fabButtonClick(View view) {
        openOptionsMenu();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private RadioFragment radioFragment = new RadioFragment();
        private ScheduleFragment scheduleFragment = new ScheduleFragment();
        private ArchieveFragment archieveFragment = new ArchieveFragment();
        private SavedFragment savedFragment = new SavedFragment();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            //TODO
            Fragment fragment = new Fragment();
            switch (position) {
                case 0:
                    fragment = radioFragment;
                    break;
                case 1:
                    fragment = scheduleFragment;
                    break;
                case 2:
                    fragment = archieveFragment;
                    break;
                case 3:
                    fragment = savedFragment;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.first_tab_text);
                case 1:
                    return getString(R.string.second_tab_text);
                case 2:
                    return getString(R.string.third_tab_text);
                case 3:
                    return getString(R.string.fourth_tab_text);
            }
            return null;
        }

        public void notifySavedFragment() {
            savedFragment.notifyAdapter();
        }
    }

    public boolean isFirstLaunch() {
        SharedPreferences settings = getPreferences(0);
        return !settings.getBoolean("gratitude", false);
    }

    private IPLayerErrorListener playerErrorListener = new IPLayerErrorListener() {
        @Override
        public void onError(String message, int errorCode) {
            final String s = String.format("%s\n(error code: %d)", message, errorCode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                s, Toast.LENGTH_SHORT);
                        toast.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}