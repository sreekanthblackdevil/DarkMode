package com.nightcoder.darkmode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class StartActivity extends AppCompatActivity {

    private InterstitialAd interstitialAd;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        RelativeLayout container = findViewById(R.id.container);
        container.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        container.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        MobileAds.initialize(this, initializationStatus -> {
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial2));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                Log.d("Status", "Failed to load");
                super.onAdFailedToLoad(i);
                loadComplete();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitialAd.show();
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadComplete();
            }
        });

        handler.postDelayed(runnable, (1000 * 15));
    }

    private Runnable runnable = () -> {
        loadComplete();
        Log.d("Status", "TimeOut");
    };

    private void loadComplete() {
        startActivity(new Intent(this, BaseActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        handler.removeCallbacks(runnable);
    }

}
