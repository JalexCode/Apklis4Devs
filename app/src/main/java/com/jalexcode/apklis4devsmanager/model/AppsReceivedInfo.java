package com.jalexcode.apklis4devsmanager.model;

import android.app.Application;

import java.util.List;

import cu.kareldv.apklis.api2.Session;

public class AppsReceivedInfo {
    private int appCountValue = 0;
    private int appDownloadsValue = 0;
    private int appSalesCountValue = 0;
    private double totalMoneyValue = 0;
    private Session session;
    public AppsReceivedInfo(){}

    public AppsReceivedInfo(Session session) {
        this.session = session;
    }

    public int getAppCountValue() {
        return appCountValue;
    }

    public void setAppCountValue(int appCountValue) {
        this.appCountValue = appCountValue;
    }

    public int getAppDownloadsValue() {
        return appDownloadsValue;
    }

    public void setAppDownloadsValue(int appDownloadsValue) {
        this.appDownloadsValue = appDownloadsValue;
    }

    public int getAppSalesCountValue() {
        return appSalesCountValue;
    }

    public void setAppSalesCountValue(int appSalesCountValue) {
        this.appSalesCountValue = appSalesCountValue;
    }

    public double getTotalMoneyValue() {
        return totalMoneyValue;
    }

    public void setTotalMoneyValue(double totalMoneyValue) {
        this.totalMoneyValue = totalMoneyValue;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
