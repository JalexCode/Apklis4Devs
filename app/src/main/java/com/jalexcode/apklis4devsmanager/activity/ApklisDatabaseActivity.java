package com.jalexcode.apklis4devsmanager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jalexcode.apklis4devsmanager.R;

import cu.kareldv.apklis.api2.Session;
import cu.kareldv.apklis.api2.model.DashboardStats;

public class ApklisDatabaseActivity extends AppCompatActivity {
    private Session session;

    private ImageView backButton;
    private TextView usersCount, devsCount,appsCount,cubanAppsCount,payAppsCount,downloadsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apklis_database);
        //
        setUpUI();
        //
        Intent currentSession = getIntent();
        if (currentSession != null){
            if (currentSession.hasExtra("session")) {
                Log.e("Getting intent", "Starting by previus session");
                session = (Session) currentSession.getSerializableExtra("session");
                //
                getData();
            }
        }

    }

    private void setUpUI(){
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
        //
        usersCount = findViewById(R.id.usersCount);
        devsCount = findViewById(R.id.devsCount);
        appsCount = findViewById(R.id.appsCount);
        cubanAppsCount = findViewById(R.id.cubanAppsCount);
        payAppsCount = findViewById(R.id.payAppsCount);
        downloadsCount = findViewById(R.id.downloadsCount);
    }

    private void getData(){
        DashboardStats stats = session.getDashboardStats();
        //
        usersCount.setText(String.valueOf(stats.getUsers()));
        devsCount.setText(String.valueOf(stats.getDevelopers()));
        appsCount.setText(String.valueOf(stats.getApp()));
        cubanAppsCount.setText(String.valueOf(stats.getCuban_apps()));
        payAppsCount.setText(String.valueOf(stats.getPaid_apps()));
//        downloadsCount.setText(String.valueOf(stats.get));
    }
}