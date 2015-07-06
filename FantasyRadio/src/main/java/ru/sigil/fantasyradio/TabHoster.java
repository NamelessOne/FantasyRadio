package ru.sigil.fantasyradio;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import ru.sigil.fantasyradio.archieve.ArchieveActivity;
import ru.sigil.fantasyradio.saved.MP3Collection;
import ru.sigil.fantasyradio.saved.MP3Saver;
import ru.sigil.fantasyradio.saved.SavedActivity;
import ru.sigil.fantasyradio.schedule.ScheduleActivity;
import ru.sigil.fantasyradio.settings.Settings;
import ru.sigil.fantasyradio.settings.SettingsActivity;
import ru.sigil.fantasyradio.utils.BASSUtil;
import ru.sigil.fantasyradio.utils.PlayerState;
import ru.sigil.fantasyradio.utils.ProgramNotification;

public class TabHoster extends TabActivity {
    private static final String TAG = TabHoster.class.getSimpleName();
    Context context;

    private static int current_menu;
    private TabHost tabHost;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getBaseContext();  // Get current context.
        // EasyTracker is now ready for use.
        ProgramNotification.setContext(getBaseContext());
        setCurrent_menu(R.menu.activity_main);
        MP3Saver.setMp3c(new MP3Collection(getBaseContext()));
        MP3Saver.getMp3c().Load();
        BASS.BASS_Free();
        BASS.BASS_Init(-1, 44100, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_NET_PLAYLIST, 1);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_NET_PREBUF, 0);
        BASS.BASS_SetVolume((float) 0.5);
        SharedPreferences settings = getPreferences(0);
        Settings.setSettings(settings);
        // -------------------------------------------------
        try {
            setContentView(R.layout.tabs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tabHost = getTabHost(); // tabHost is a private field
        Intent intent = new Intent(this, MainActivity.class);
        addTab(getString(R.string.first_tab_text), R.drawable.ephir, intent);
        intent = new Intent(this, ScheduleActivity.class);
        addTab(getString(R.string.second_tab_text), R.drawable.clocks, intent);
        intent = new Intent(this, ArchieveActivity.class);
        addTab(getString(R.string.third_tab_text), R.drawable.archive, intent);
        intent = new Intent(this, SavedActivity.class);
        addTab(getString(R.string.fourth_tab_text), R.drawable.downloadm,
                intent);


        context = getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        if (PlayerState.isPlaying()) {
            ProgramNotification
                    .createNotification();
        } else {
            BASS.BASS_ChannelStop(BASSUtil.getChan());
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        BASS.BASS_ChannelStop(BASSUtil.getChan());
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && PlayerState.isPlaying()) {
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
                BASS.BASS_ChannelStop(BASSUtil.getChan());
                BASS.BASS_StreamFree(BASSUtil.getChan());
                PlayerState.getInstance().setCurrentRadioEntity(null);
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
        if (ProgramNotification.notificationManager != null) {
            try {
                ProgramNotification.notificationManager
                        .cancel(ProgramNotification.MAIN_NOTIFICATION_ID);
                ProgramNotification.isShown = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    private void addTab(String label, int drawableId, Intent intent) {
        TabHost.TabSpec spec = tabHost.newTabSpec(label);
        View tabIndicator = LayoutInflater.from(this).inflate(
                R.layout.tab_indicator, getTabWidget(), false);
        TextView title = null;
        if (tabIndicator != null) {
            title = (TextView) tabIndicator
                    .findViewById(R.id.tabIndicatorTitle);
        }
        ImageView image = (ImageView) tabIndicator
                .findViewById(R.id.tabIndicatorImage);
        image.setImageResource(drawableId);
        title.setText(label);
        spec.setIndicator(tabIndicator);
        spec.setContent(intent);
        tabHost.addTab(spec);
    }
}