package com.joe.bibi.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Joe on 2016/1/27.
 */
public class ToastUtils {
    public static void make(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
}
