package com.jalexcode.apklis4devsmanager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.jalexcode.apklis4devsmanager.R;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cu.kareldv.apklis.api2.Session;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences preferences;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        // progress tint
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.textColor),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        //
        preferences = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        String username = preferences.getString("username", "");
        if (username.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    startActivity(new Intent(SplashActivity.this, StartUpActivity.class));
                    finish();
                }
            }.start();
        } else {
            new startSessionThread().execute();
        }
    }

    private class startSessionThread extends AsyncTask<Void, Void, Session> { //AsyncTask<File, Void, TreeNode<DataNode>>
        private String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Session session2) {
            super.onPostExecute(session2);
            if (session2 != null) {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("session", session2);
                i.putExtras(b);
                startActivity(i);
                finish();
            } else {
                showErrorDialog();
            }
        }

        public Session doInBackground(Void... voids) {
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");
            Session s = null;
            try {
                s = new Session(username, password);
                s.updateCache();
            } catch (Throwable e) {
                e.printStackTrace();
                error = e.getMessage();
            }
            return s;
        }
    }

    public void showErrorDialog(){
        progressBar.setVisibility(View.GONE);
        //
        SweetAlertDialog sd = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sd.setTitleText("Error");
        sd.setContentText("No tienes conexión a Internet");
        sd.setCanceledOnTouchOutside(false);
        sd.setConfirmText("Reintentar");
        sd.setCancelText("Cerrar");
        sd.setConfirmClickListener(sweetAlertDialog -> {
            new startSessionThread().execute();
            sd.dismiss();
        });
        sd.setCancelClickListener(sweetAlertDialog -> finish());
        sd.show();
    }

}