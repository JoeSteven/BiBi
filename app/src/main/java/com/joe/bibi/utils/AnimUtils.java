package com.joe.bibi.utils;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by Joe on 2016/2/26.
 */
public class AnimUtils {

    public static TranslateAnimation TaAnimSelf(float fromX,float toX,float fromY,float toY,long duration,boolean fillafter){
        TranslateAnimation ta=new TranslateAnimation(Animation.RELATIVE_TO_SELF,fromX,Animation.RELATIVE_TO_SELF,toX,
                Animation.RELATIVE_TO_SELF,fromY,Animation.RELATIVE_TO_SELF,toY);
        if(duration!=0){
            ta.setDuration(duration);
        }else{
            ta.setDuration(500);
        }
        ta.setFillAfter(fillafter);
        return ta;
    }

    public static RotateAnimation RoAnimSelf(float fromDegree,float toDegree,long duration,boolean fillafter){
        RotateAnimation sa=new RotateAnimation(fromDegree,toDegree,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        if(duration!=0){
            sa.setDuration(duration);
        }else{
            sa.setDuration(500);
        }
        sa.setFillAfter(fillafter);
        return sa;
    }
}
