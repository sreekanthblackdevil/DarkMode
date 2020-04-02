package com.nightcoder.darkmode;

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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class BaseActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup radioGroup;
    private UiModeManager uiModeManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiModeManager = ContextCompat.getSystemService(this, UiModeManager.class);
        radioGroup = findViewById(R.id.radioGroup);
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
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setTitle(R.string.alert_title)
                    .setMessage(R.string.msg_android_version_compatible)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.close), ((dialog, which) -> super.onBackPressed()))
                    .setPositiveButton(R.string.uninstall, (dialog, which) -> {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.setData(Uri.fromParts("package",
                                BaseActivity.this.getPackageName(), null));
                        startActivity(i);
                        BaseActivity.this.finish();
                    })
                    .show();
        }

        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
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
        if (sharedPreferences.getBoolean("rate", false)) {
            super.onBackPressed();
        } else {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.rate_dialog);
            Window window = dialog.getWindow();
            assert window != null;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.DialogAnimation);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Button later = dialog.findViewById(R.id.button_later);
            Button rate = dialog.findViewById(R.id.rate_button);
            later.setOnClickListener(v -> super.onBackPressed());
            rate.setOnClickListener(v -> {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("rate", true);
                    editor.apply();
                    dialog.cancel();
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    editor = sharedPreferences.edit();
                    editor.putBoolean("rate", true);
                    editor.apply();
                    dialog.cancel();
                }
            });
            dialog.show();
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
}
