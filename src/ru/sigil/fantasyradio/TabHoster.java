package ru.sigil.fantasyradio;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.un4seen.bass.BASS;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "899597899807";
    String regid;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
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

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        Log.d(TAG, "regid1 = " + regid);
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
        checkPlayServices();
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
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(TabHoster.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                Log.d(TAG, "regid2 = " + regid);
                return msg;
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}