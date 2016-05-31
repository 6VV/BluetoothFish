package com.lyyjy.zdhyjs.bluetoothfish.LightColor;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/5.
 */
public class LightColor {
    public static final int COLOR_WHITE=0xFFFFFFFF;
    public static final int COLOR_YELLOW=0xFFFFFF00;
    public static final int COLOR_VIOLET=0xFFFF00FF;
    public static final int COLOR_RED=0xFFFF0000;
    public static final int COLOR_CYAN=0xFF00FFFF;
    public static final int COLOR_GREEN=0xFF00FF00;
    public static final int COLOR_BLUE=0xFF0000FF;
    public static final int COLOR_BLACK=0xFF000000;

    public static final int[] COLOR_ARRAY={COLOR_WHITE,COLOR_YELLOW,COLOR_VIOLET,
        COLOR_RED,COLOR_CYAN,COLOR_GREEN,COLOR_BLUE,COLOR_BLACK};

    private int mColor;

    public LightColor(int color){
        mColor=color;
    }

    public void setColor(int color){
        mColor=color;
    }

    public int getColor(){
        return mColor;
    }

    public byte getSimpleColor(){
        byte result=0;
        if ((mColor&0x000000FF)!=0){
            result+=1;
        }
        if ((mColor&0x0000FF00)!=0){
            result+=2;
        }
        if ((mColor&0x00FF0000)!=0){
            result+=4;
        }

        return result;
    }
}
