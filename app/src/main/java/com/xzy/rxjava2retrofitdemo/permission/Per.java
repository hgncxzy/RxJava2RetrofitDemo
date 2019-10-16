package com.xzy.rxjava2retrofitdemo.permission;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class Per {
    public static void main(String[] args) {
        System.out.println('I' + 'T');
    }


    public static boolean isGrantExternalInternet(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE
            }, 1);

            return false;
        }

        return true;
    }
}
