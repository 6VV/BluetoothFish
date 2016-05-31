package com.lyyjy.zdhyjs.bluetoothfish;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/3/29.
 */
public class WaveControler {
    static {
        System.loadLibrary("FishNDK");
    }

    private String TAG="WaveControler";

    private int mCClassAddress;
    private Bitmap mBitmap;

    private boolean mIsRippling=false;

    public WaveControler(Bitmap bitmap){
        mBitmap=bitmap;
        mCClassAddress=createCClass(bitmap);
    }

    public WaveControler(Bitmap bitmap,float scale){
        mBitmap=bitmap;
        mCClassAddress=createCClassWithScale(bitmap, scale);
    }

    //销毁时调用
    protected  void finalize(){
        try {
            deleteCClass(mCClassAddress);
            mCClassAddress=0;
        }finally {
            try {
                super.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public void startRipple(){
        mIsRippling=true;
        new Thread(mRannableRippling).start();
    }

    public void stopRipple(){
        mIsRippling=false;
    }

    private Runnable mRannableRippling=new Runnable() {
        @Override
        public void run() {
            while (mIsRippling){
                rippleBitmap();
            }
        }
    };

    public void rippleBitmap(){
        rippleBitmap(mBitmap,mCClassAddress);
    }

    public void setSourcePower(float x,float y){
        setSourcePower(x, y, mCClassAddress);
    }

    public void setSourcePowerSize(int radius,int depth){
        setSourcePowerSize(radius, depth, mCClassAddress);
    }

    public void moveLine(float startX,float startY,float endX,float endY){
        moveLine(startX, startY, endX, endY, mCClassAddress);
    }

    private native int createCClass(Bitmap bitmap); //创建C++类
    private native int createCClassWithScale(Bitmap bitmap,float scale);   //以一定缩放比例创建C++类
    private native void deleteCClass(int cClassAddress);    //销毁C++类
    private native void rippleBitmap(Bitmap bitmap,int cClassAddress);  //渲染
    private native void setSourcePower(float x,float y,int cClassAddress);  //设置波源
    private native void setSourcePowerSize(int radius,int depth,int cClassAddress);   //初始化波源大小
    private native void moveLine(float startX,float startY,float endX,float endY,int cClassAddress);  //滑动
}
