package com.nightcoder.darkmode;

import android.annotation.TargetApi;
import android.app.UiModeManager;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class QuickTileServices extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final Intent landingPageIntent = new Intent(this, BaseActivity.class);
            landingPageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAndCollapse(landingPageIntent);
        } else {
            toggleSetting();
            syncTile();
        }
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        syncTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        syncTile();
    }

    private boolean isNightModeOn(UiModeManager uiModeManager) {
        return uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    private void toggleSetting() {
        final UiModeManager uiModeManager = getSystemService(UiModeManager.class);
        assert uiModeManager != null;
        if (isNightModeOn(uiModeManager)) {
            //switch off the night mode
            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
        } else {
            // enable night mode
            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
        }
    }

    private void syncTile() {
        final UiModeManager uiModeManager = getSystemService(UiModeManager.class);
        final Tile tile = getQsTile();
        assert uiModeManager != null;
        tile.setState(isNightModeOn(uiModeManager) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }


}
