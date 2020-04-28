package com.nightcoder.darkmode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class BaseActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, Serializable {

    private RadioGroup radioGroup;
    private UiModeManager uiModeManager;
    private SharedPreferences sharedPreferences;
    private UnifiedNativeAd nativeAd;
    protected Background myApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiModeManager = ContextCompat.getSystemService(this, UiModeManager.class);
        radioGroup = findViewById(R.id.radioGroup);
        myApp = (Background) this.getApplicationContext();
        MobileAds.initialize(this, initializationStatus -> {
        });

        RelativeLayout container = findViewById(R.id.container);
        container.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        container.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (savedInstanceState == null) updateRadioGroup();
        radioGroup.setOnCheckedChangeListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById(R.id.warn_carmode).setVisibility(View.GONE);
        }
        if (uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
            findViewById(R.id.info_oems).setVisibility(View.GONE);
        } else {
            findViewById(R.id.warn_disable).setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TextView error = findViewById(R.id.info_error);
            error.setVisibility(View.VISIBLE);
            error.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setTitle(R.string.alert_title)
                    .setMessage(R.string.msg_android_version_compatible)
                    .setNegativeButton(getString(R.string.close), ((dialog, which) -> super.onBackPressed()))
                    .setPositiveButton(R.string.uninstall, (dialog, which) -> {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.setData(Uri.fromParts("package",
                                BaseActivity.this.getPackageName(), null));
                        startActivity(i);
                        BaseActivity.this.finish();
                    })
                    .show());

            LinearLayout share = findViewById(R.id.share);
            ImageButton shareBtn = findViewById(R.id.share_btn);
            shareBtn.setOnClickListener(v -> shareApp());
            share.setOnClickListener(v -> shareApp());

        }

        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        refreshAd();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refreshAd, 60, 60, TimeUnit.SECONDS);
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Dark Mode");
            String shareMessage = "\n\n*Introducing Dark Mode to everyone.*\n " +
                    "Change Majority of your Apps to Dark Mode\n" +
                    "If your mobile has built-in dark mode support then share to your " +
                    "friends let them enjoy dark mode\n" +
                    "App Available in Play Store\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" +
                    BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share to"));
        } catch (Exception e) {
            //e.toString();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myApp.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Status", "Paused");
    }


    @SuppressLint("SetTextI18n")
    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        @SuppressWarnings("deprecation") VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd();
                }
            });
        }
    }

    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(this, getResources().getString(R.string.native_id));

        builder.forUnifiedNativeAd(unifiedNativeAd -> {
            if (nativeAd != null) {
                nativeAd.destroy();
            }
            nativeAd = unifiedNativeAd;
            FrameLayout frameLayout =
                    findViewById(R.id.fl_adplaceholder);
            frameLayout.setVisibility(View.VISIBLE);
            @SuppressLint("InflateParams")
            UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                    .inflate(R.layout.ad_unified, null);
            populateUnifiedNativeAdView(unifiedNativeAd, adView);
            frameLayout.removeAllViews();
            frameLayout.addView(adView);
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null)
            nativeAd.destroy();
        super.onDestroy();
        clearReference();
        Log.d("Status", "Destroy");
    }

    private void updateRadioGroup() {
        switch (uiModeManager.getNightMode()) {
            case UiModeManager.MODE_NIGHT_NO:
                radioGroup.check(R.id.radioDay);
                break;
            case UiModeManager.MODE_NIGHT_YES:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ||
                        uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
                    radioGroup.check(R.id.radioNight);
                } else {
                    radioGroup.check(R.id.radioDay);
                }
                break;
            case UiModeManager.MODE_NIGHT_AUTO:
                radioGroup.check(R.id.radioAuto);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        int random = ThreadLocalRandom.current().nextInt(0, 3);
        if (myApp.adLoaded()) {
            myApp.showAd();
        } else if (sharedPreferences.getBoolean("rate", false)) {
            super.onBackPressed();
        } else if (random == 1){
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.rate_dialog);
            dialog.setCancelable(false);
            Window window = dialog.getWindow();
            assert window != null;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.DialogAnimation);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Button later = dialog.findViewById(R.id.button_later);
            Button rate = dialog.findViewById(R.id.rate_button);
            RatingBar ratingBar = dialog.findViewById(R.id.rating);
            later.setOnClickListener(v -> {
                dialog.cancel();
                super.onBackPressed();
            });
            rate.setOnClickListener(v -> {
                rating();
                dialog.cancel();
            });

            ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
                rating();
                dialog.cancel();
            });
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    private void rating() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        SharedPreferences.Editor editor;
        try {
            startActivity(goToMarket);
            editor = sharedPreferences.edit();
            editor.putBoolean("rate", true);
            editor.apply();
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            editor = sharedPreferences.edit();
            editor.putBoolean("rate", true);
            editor.apply();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.radioDay:
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    uiModeManager.disableCarMode(0);
                break;
            case R.id.radioNight:

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    uiModeManager.enableCarMode(0);
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                break;
            case R.id.radioAuto:
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    uiModeManager.disableCarMode(0);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateRadioGroup();
        recreate();
    }

    private void clearReference() {
        Activity activity = myApp.getCurrentActivity();
        if (this.equals(activity)) {
            myApp.setCurrentActivity(this);
        }
    }
}
