package ru.sigil.fantasyradio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.KeyEvent;
import android.widget.ListView;

@SuppressLint("Registered")
public class AbstractListActivity extends Activity {
    private ListView lv;

    public ListView getLv() {
        return lv;
    }

    public void setLv(ListView lv) {
        this.lv = lv;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }
}
