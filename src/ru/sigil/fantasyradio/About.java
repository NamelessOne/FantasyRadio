package ru.sigil.fantasyradio;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;

/**
 * Окошко "О программе"
 */
public class About extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView tv = (TextView) findViewById(R.id.about_content);
        try {
            tv.append(getString(R.string.versiontext) + " "
                    + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        }catch(Exception e){}
        tv.setOnClickListener(CloseClickListener);
    }

    private View.OnClickListener CloseClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            stop();
        }
    };

    private void stop() {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);    // Add this method.
    }
}