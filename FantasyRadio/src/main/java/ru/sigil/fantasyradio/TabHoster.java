package ru.sigil.fantasyradio;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Locale;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.bassplayerlib.listeners.IPlayerErrorListener;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.playerservice.PlayerBackgroundService;
import ru.sigil.fantasyradio.settings.Settings;
import ru.sigil.fantasyradio.settings.SettingsActivity;
import ru.sigil.fantasyradio.utils.FantasyRadioNotificationManager;
import ru.sigil.fantasyradio.utils.RadioStream;


public class TabHoster extends FragmentActivity {
    private static final String TAG = TabHoster.class.getSimpleName();
    public SectionsPagerAdapter mSectionsPagerAdapter;
    private final int MY_PERMISSIONS_REQUEST = 1;
    @Inject
    IPlayer<RadioStream> player;
    @Inject
    FantasyRadioNotificationManager notificationManager;

    private static int current_menu;

    private void requestPermissionWithRationale() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permissions_request_title))
                .setMessage(getString(R.string.permissions_request))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // continue with delete
                    requestMyPermissions();
                })
                .setCancelable(false)
                .show();
    }

    private void requestMyPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                MY_PERMISSIONS_REQUEST);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        startService(new Intent(this, PlayerBackgroundService.class)); //Start
        player.addPlayerErrorListener(playerErrorListener);
        setContentView(R.layout.tabs);
        //-------------------------------------------------------
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionWithRationale();
        }
        //-------------------------------------------------------
        setCurrent_menu(R.menu.activity_main);
        SharedPreferences settings = getPreferences(0);
        Settings.setSettings(settings);
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getBaseContext())
                .defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
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
        player.removePlayerErrorListener(playerErrorListener);
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
            editor.apply();
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
                    editor.apply();
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

    public void fabButtonClick(View view) {
        openOptionsMenu();
    }

    private IPlayerErrorListener playerErrorListener = (message, errorCode) -> {
        final String s = String.format(Locale.getDefault(), "%s\n(error code: %d)", message, errorCode);
        runOnUiThread(() -> {
            try {
                Toast toast = Toast.makeText(getApplicationContext(),
                        s, Toast.LENGTH_SHORT);
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    };
}