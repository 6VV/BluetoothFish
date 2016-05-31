package com.lyyjy.zdhyjs.bluetoothfish.SwimMode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.lyyjy.zdhyjs.bluetoothfish.PersistentDataManager;
import com.lyyjy.zdhyjs.bluetoothfish.R;
import com.lyyjy.zdhyjs.bluetoothfish.View.LoadBitmap;
import com.lyyjy.zdhyjs.bluetoothfish.WaveControler;

import java.util.Random;

/**
 * Created by Administrator on 2016/1/5.
 */
public class ViewBackground extends View {
    private String TAG="ViewBackground";
    private WaveControler mWaveControler;
    private float mScale=1;
    private Random mRandom=new Random();
    private PersistentDataManager persistentDataManager;

    private Context mContext;

//    int mScreenWidth;   //屏幕宽度
//    int mScreenHeight;  //屏幕高度
    RectF mRectFScreen; //屏幕区域
    Bitmap mBitmapBackground;   //背景图

    public ViewBackground(Context context) {
        super(context);
        mContext=context;
        init();
    }

    public ViewBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        init();
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        canvas.drawBitmap(mBitmapBackground,null,mRectFScreen,null);
//    }


    void init(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        mRectFScreen=new RectF(0,0,screenWidth,screenHeight);

        LoadBitmap loadBitmap=new LoadBitmap(getResources(), R.mipmap.background_main,-1,500000,screenWidth,screenHeight);
        mBitmapBackground=loadBitmap.getBitmap();
        mScale=(float)screenWidth/mBitmapBackground.getWidth();
        mWaveControler =new WaveControler(mBitmapBackground,mScale);

        mWaveControler.setSourcePowerSize(mBitmapBackground.getHeight()/20,1000);

        persistentDataManager=PersistentDataManager.getInstance(mContext);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmapBackground,null,mRectFScreen,null);
    }

    private boolean mIsRippling=false;

    public void startRipple(){
        mIsRippling=true;
        new Thread(mRannableRippling).start();
    }

    public void stopRipple(){
        mIsRippling=false;
        mIsRaining=false;
    }


    private long time=System.currentTimeMillis();
    private Runnable mRannableRippling=new Runnable() {
        @Override
        public void run() {
            while (mIsRippling && persistentDataManager.mIsOpenWaterWave){
                mWaveControler.rippleBitmap();
                postInvalidate();
            }

            //等待时间
            long currTime=System.currentTimeMillis();
            if (currTime-time<50){
                try {
                    Thread.sleep(50-currTime+time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            time=System.currentTimeMillis();
        }
    };

    public void setSourcePower(float x,float y){
        mWaveControler.setSourcePower(x, y);
    }

    public void setSourcePowerSize(int radius, int depth)
    {
        mWaveControler.setSourcePowerSize(radius, depth);
    }

    public void moveLine(float startX,float startY,float endX,float endY){
        mWaveControler.moveLine(startX, startY, endX, endY);
    }

    float lastX=0;
    float lastY=0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //若未开启水波效果
        if (!persistentDataManager.mIsOpenWaterWave){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                lastX=event.getX();
                lastY=event.getY();
                this.setSourcePower(lastX, lastY);
//                Log.e(TAG, "startX:" + lastX + "," + "startY:"+lastY);
            }break;
            case MotionEvent.ACTION_MOVE:{
//                Log.e(TAG, "startX:" + lastX + "," + "startY:" + lastY + "," + "endX:" + event.getX() + "," + "endY:" + event.getY());
                this.moveLine( lastX,  lastY, event.getX(), event.getY());
                lastX=event.getX();
                lastY=event.getY();
            }break;
        }
        return false;
    }



    private boolean mIsRaining=false;
    public void startRain(){
        mIsRaining=true;
        new Thread(mRunnableRaining).start();
    }
    public void stopRain(){
        mIsRaining=false;
    }

    public boolean isRaining(){
        return mIsRaining;
    }

    private Runnable mRunnableRaining=new Runnable() {
        @Override
        public void run() {
            while (mIsRaining && persistentDataManager.mIsOpenWaterWave){
                int times=mRandom.nextInt(3);
                for (int i=0;i<times;++i){
                    int x=mRandom.nextInt((int)mRectFScreen.width());
                    int y=mRandom.nextInt((int)mRectFScreen.height());
                    setSourcePower(x,y);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
