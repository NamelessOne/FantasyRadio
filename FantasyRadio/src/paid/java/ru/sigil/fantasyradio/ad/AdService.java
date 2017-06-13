package ru.sigil.fantasyradio.ad;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by namelessone
 * on 13.06.17.
 */

@Singleton
public class AdService {
    @Inject
    public AdService(Context context) {
    }

    public void showAd(int probability) {
    }
}
