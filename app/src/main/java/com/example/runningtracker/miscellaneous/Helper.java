package com.example.runningtracker.miscellaneous;

public class Helper {
    public static double mToKmString(double m){
        double km = m / 1000;
        double roundTo2Dp = Math.round(km * 100.0) / 100.0;

        return roundTo2Dp;
    }

    public static String secToHourMinSecString(int secs){
        int hours = (int) Math.floor(secs / 3600);
        int minutes = (int) Math.floor((secs % 3600) / 60);

        String hoursTxt = "";
        if (hours < 10) {
            hoursTxt = "" + 0 + hours;
        } else {
            hoursTxt = String.valueOf(hours);
        }

        String minsTxt = "";
        if (hours < 10) {
            minsTxt = "" + 0 + minutes;
        } else {
            minsTxt = String.valueOf(minutes);
        }

        String secsTxt = "";

        if ((secs % 60) < 10) {
            secsTxt = "" + 0 + (secs % 60);
        } else {
            secsTxt = String.valueOf(secs % 60);
        }

        return "" + hoursTxt + ":" + minsTxt + ":" + secsTxt;
    }
}
