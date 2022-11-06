package com.jalexcode.apklis4devsmanager.util;

public class Util {
    public static double getDiscount(double money){
        double sellerTax = 30;
        double onatTax = 3.5;
        double totalTax = sellerTax + onatTax;
        double discount = (money * totalTax) / 100;
        return money - discount;
    }
}
