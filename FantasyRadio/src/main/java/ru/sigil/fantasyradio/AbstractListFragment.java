package ru.sigil.fantasyradio;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.widget.ListView;

@SuppressLint("Registered")
public class AbstractListFragment extends Fragment {
    private ListView lv;

    protected ListView getLv() {
        return lv;
    }

    protected void setLv(ListView lv) {
        this.lv = lv;
    }
}
