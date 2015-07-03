package ru.sigil.fantasyradio.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.lamerman.FileDialog;

import ru.sigil.fantasyradio.R;

/**
 * Activity настроек
 */
public class SettingsActivity extends Activity {
    private int REQUEST_SAVE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        EditText et = (EditText) findViewById(R.id.settingsMP3SaveFolder);
        et.setText(String.valueOf(Settings.getSaveDir()));
        // --------------------------------------------------------
        findViewById(R.id.settingsButtonCancel).setOnClickListener(
                CancelClickListener);
        findViewById(R.id.settingsButtonSave).setOnClickListener(
                SaveClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);    // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);  ;  // Add this method.
    }


    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
    private View.OnClickListener SaveClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            EditText et = (EditText) findViewById(R.id.settingsMP3SaveFolder);
            String s = et.getText().toString();
            Settings.saveSaveDir(s);
            finish();
        }
    };

    public void SelectDirClick(@SuppressWarnings("UnusedParameters") View v) {
        Intent intent = new Intent(getBaseContext(), FileDialog.class);
        startActivityForResult(intent, REQUEST_SAVE);
    }

    public synchronized void onActivityResult(final int requestCode,
                                              int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            int REQUEST_LOAD = 1;
            if (requestCode == REQUEST_SAVE) {
                System.out.println("Saving...");
            } else if (requestCode == REQUEST_LOAD) {
                System.out.println("Loading...");
            }
            String filePath = data.getStringExtra("RESULT_FOLDER");
            String mntSDcard = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            Log.v("garbage", mntSDcard);
            if (filePath != null) {
                filePath = filePath.replace(mntSDcard, "");
            }
            Log.v("folder", filePath);
            EditText et = (EditText) findViewById(R.id.settingsMP3SaveFolder);
            et.setText(filePath);
        }
    }
}
