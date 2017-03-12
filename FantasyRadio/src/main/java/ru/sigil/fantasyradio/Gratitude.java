package ru.sigil.fantasyradio;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Gratitude extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grtitude);
        TextView tv = (TextView) findViewById(R.id.thx_content);
        tv.setOnClickListener(CloselickListener);
    }

    private View.OnClickListener CloselickListener = v -> stop();

    private void stop() {
        finish();
    }

    public void rateButtonClick(View view) {
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            this.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void noThanksButtonClick(View view) {
        stop();
    }
}