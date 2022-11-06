package com.jalexcode.apklis4devsmanager.apklis;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jalexcode.apklis4devsmanager.R;
import com.jalexcode.apklis4devsmanager.activity.LoginActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import cu.kareldv.apklis.api2.Session;

public class AppStatsJobService extends JobService {

    private Handler AppStatsServiceHandler;

    @Override
    public void onCreate(){
        super.onCreate();

        AppStatsServiceHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        boolean update_exist = msg.getData().getBoolean("update_exist",false);
                        String version_name =  msg.getData().getString("version_name","");
                        JobParameters jobParameters = msg.getData().getParcelable("params");

                        boolean AppActive = false;
                        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH){

                            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = am.getRunningAppProcesses();
                            for(ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList){
                                if(processInfo.importance==ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                                    for(String activeProcess : processInfo.pkgList){
                                        if(activeProcess.equals(getApplicationContext().getPackageName())){
                                            AppActive = true;
                                        }
                                    }
                                }
                            }
                        }

                        if(update_exist) {
                            if (!AppActive) {

                                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                                    NotificationChannel chanel = new NotificationChannel("chanel","AppStats",NotificationManager.IMPORTANCE_DEFAULT);
                                    notificationManager.createNotificationChannel(chanel);
                                }

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"chanel")
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle(getApplicationContext().getApplicationInfo().loadLabel(getApplicationContext().getPackageManager()).toString())
                                        .setContentText("Nueva Versión v"+version_name+" Disponible En Apklis")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                                notificationManagerCompat.notify(0, builder.build());
                            }
                            else {
                                Intent intent = new Intent("apklis_update");
                                intent.putExtra("version_name",version_name);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            }
                        }

                        jobFinished(jobParameters,false);


                    default:super.handleMessage(msg);
                }

            }
        };
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        NetWorkThread netWorkThread = new NetWorkThread(params);
        new Thread(netWorkThread).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


class NetWorkThread extends Thread {

    JobParameters jobParameters;

    NetWorkThread(JobParameters jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    public void run() {

        String api_apklis_json = "";
        boolean update_exist = false;
        int apklis_version_code = 0;
        String apklis_version_name = "";

        PackageInfo pinfo = null;
        try {
            pinfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int this_version_code = pinfo.versionCode;

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        Session session;
        try {
            session = new Session(username, password);
            session.updateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message message = new Message();

        message.what = 1;
        Bundle bundle = new Bundle();

        bundle.putParcelable("params", jobParameters);
        bundle.putSerializable("app_sale", update_exist);
        bundle.putString("version_name", apklis_version_name);
        message.setData(bundle);
        AppStatsServiceHandler.sendMessage(message);

    }
}
}
