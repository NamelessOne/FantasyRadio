package ru.sigil.fantasyradio.ad;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.sigil.fantasyradio.R;
import ru.sigil.log.LogManager;

/**
 * Created by namelessone
 * on 13.06.17.
 */

@Singleton
public class AdService {
    private InterstitialAd mInterstitialAd;
    private Random random;


    private Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
    }

    public void showAd(int probability) {
        if (getRandom().nextInt(100) < probability && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Inject
    public AdService(Context context) {
        if (mInterstitialAd == null) {
            mInterstitialAd = new InterstitialAd(context);
            mInterstitialAd.setAdUnitId(context.getString(R.string.admob_publisher_id));
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    LogManager.d("AD", "closed");
                    requestNewInterstitial();
                }
            });
            requestNewInterstitial();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }
}
