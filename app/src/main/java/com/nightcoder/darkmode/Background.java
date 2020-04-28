package com.nightcoder.darkmode;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Background extends Application {

    private InterstitialAd interstitialAd;
    private Activity mCurrentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
        int random = new Random().nextInt((3 - 1) + 1) + 1;
        Log.d("Random", random + "");
        if (random == 2) {
            interstitialAd.loadAd(new AdRequest.Builder().build());
            Log.d("Ad", "Loading..");
        }
    }

    public boolean adLoaded() {
        return interstitialAd.isLoaded();
    }

    public void showAd() {
        interstitialAd.show();
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }
}
