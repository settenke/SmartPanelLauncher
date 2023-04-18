package com.example.smartpanellauncher;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler mHandler;
    private final long TIMEOUT = 1000 * 60;
    private ContentResolver cResolver;
    // Window object, that will store a reference to the current window
    private Window window;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            reactToIdleState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, TIMEOUT);
        myCustomOnCreate(savedInstanceState);
    }

    private boolean checkSystemWritePermission() {
        if(Settings.System.canWrite(this)) {
            return true;
        } else {
            openAndroidPermissionsMenu();
        }

        return false;
    }

    private void openAndroidPermissionsMenu() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        this.startActivity(intent);
    }

    protected void myCustomOnCreate(Bundle savedInstanceState) {
        // Get the content resolver
        cResolver = getContentResolver();

        // Get the current window
        window = getWindow();

        if (checkSystemWritePermission()) {
            // To handle the auto
            Settings.System.putInt(
                    cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            );
        }
    }

    protected void reactToIdleState() {
        if (checkSystemWritePermission()) {
            WindowManager.LayoutParams layoutpars = window.getAttributes();
            layoutpars.screenBrightness = 0.0f;
            window.setAttributes(layoutpars);
        }
    }

    protected void myCustomOnClick(View view) {
        if (checkSystemWritePermission()) {
            WindowManager.LayoutParams layoutpars = window.getAttributes();
            layoutpars.screenBrightness = 1.0f;
            window.setAttributes(layoutpars);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resetHandler();
        return false;
    }

    @Override
    public void onClick(View view) {
        resetHandler();
        myCustomOnClick(view);
    }

    private void resetHandler() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, TIMEOUT);
    }
}