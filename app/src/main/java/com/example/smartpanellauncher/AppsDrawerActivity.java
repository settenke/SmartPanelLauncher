package com.example.smartpanellauncher;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class AppsDrawerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_drawer);

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = findViewById(R.id.my_linear_layout);
        PackageManager pm = getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        for (ApplicationInfo appInfo : apps) {
            Intent intent = pm.getLaunchIntentForPackage(appInfo.packageName);
            if (intent != null) {

                String appName = appInfo.loadLabel(pm).toString();
                Drawable icon = appInfo.loadIcon(pm);

                @SuppressLint("InflateParams") LinearLayout itemLayout = (LinearLayout) inflater.inflate(R.layout.app_item, null);

                ImageView imageView = itemLayout.findViewById(R.id.icon);
                TextView textView = itemLayout.findViewById(R.id.name);

                imageView.setImageDrawable(icon);
                textView.setText(appName);

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(intent);
                    }
                };

                View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Intent appInfoIntent = new Intent();
                        appInfoIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", appInfo.packageName, null);
                        appInfoIntent.setData(uri);
                        startActivity(appInfoIntent);

                        return false;
                    }
                };

                itemLayout.setOnClickListener(clickListener);
                itemLayout.setOnLongClickListener(longClickListener);

                layout.addView(itemLayout);
            }
        }
    }
}