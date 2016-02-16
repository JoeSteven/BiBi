package com.joe.bibi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Joe on 2016/2/7.
 */
public class NetUtils {
    public static boolean isInternetAvilable(Context context) {
        Boolean isOn=false;
        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo net=cm.getActiveNetworkInfo();
        if(net!=null){
            isOn=cm.getActiveNetworkInfo().isAvailable();
        }
        return isOn;
    }
}
