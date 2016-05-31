package com.lyyjy.zdhyjs.bluetoothfish.View;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by Administrator on 2016/3/27.
 */
public class FishView extends ImageView {
    private String TAG="FishView";

    private Random mRandom=new Random();    //随机数
    private float mBaseSpeed =10;           //基础速度
    private float mSpeed= mBaseSpeed;       //当前速度
    private float mRotation=0;              //转角
    private float mBaseRotation =2;  //基础转角
    private int mSpeedUpTimes=5;    //加速倍数
    private int mRotationUpTimes=5; //转角倍数

    private int mScreenWidth;   //屏幕宽度
    private int mScreenHeight;  //屏幕高度

    private float mTargetLocationX=0; //目标地点
    private float mTargetLocationY=0;

    private long mLastSetLocationTime=0;    //上次设置目标地点的时间

    private float mScaleToSizeOfScreen;   //最大值相对于屏幕的比率

    public FishView(Context context) {
        super(context);
        Init();
    }

    public FishView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public FishView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }

    void Init(){
        //获取屏幕大小
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        //设置控件属性
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setScaleType(ScaleType.FIT_CENTER);

    }

    public void setTargetLocation(int x,int y){
        mLastSetLocationTime=System.currentTimeMillis();
        mTargetLocationX=x;
        mTargetLocationY=y;
    }


    private void getNewRotation(){
        if (mTargetLocationX<0.01 && mTargetLocationY<0.01){
            mSpeed= mBaseSpeed;
            mRotation=mRotation+ mRandom.nextFloat()*(mBaseRotation *2)- mBaseRotation;
        }
        else{
            //弧度
            double mRadian=mRotation*Math.PI/180;

            //鱼头部坐标
            float x=(float)(getX()+getWidth()/2+getHeight()/2*Math.sin(mRadian));
            float y=(float)(getY()+getHeight()/2*(1-Math.cos(mRadian)));

            //目标位置偏离角度
            float targetRotation=(float)(90-Math.atan2(y-mTargetLocationY,mTargetLocationX-x)*180/Math.PI);

            //调整速度
            getVelocity(x,y,mTargetLocationX,mTargetLocationY);

            //目标位置与当前位置的角度差值（角度）
            float diffRotation=mRotation-targetRotation;
            float increaseRotation=getIncreaseRotation(diffRotation);

            //若角度变化小于0.1且速度小于0.1
            if (increaseRotation<=0.2 && mSpeed<0.2){
                if (System.currentTimeMillis()-mLastSetLocationTime>1000){
                    mTargetLocationX=0;
                    mTargetLocationY=0;
                }
                increaseRotation=0;
                mSpeed=0;
            }
            mRotation+=increaseRotation;

//            Log.e(TAG,"diff rotation:"+diffRotation+","+"speed:"+mSpeed);
//            Log.e(TAG,"Current rotation:"+mRotation);
//            Log.e(TAG,"Target X:"+mTargetLocationX+","+"Target Y:"+mTargetLocationY);
//            Log.e(TAG,"Current X:"+x+","+"Current Y:"+y);
//            Log.e(TAG,"Real X:"+getX()+","+"Real Y:"+getY());
//            Log.e(TAG,"Width:"+getWidth()+","+"Height:"+getHeight());

        }

        if (mRotation>=360){
            mRotation-=360;
        }else if (mRotation<=0){
            mRotation+=360;
        }
    }

    private void getVelocity(float currentX,float currentY,float targetX,float targetY){
        double distance=Math.sqrt(Math.pow(mTargetLocationY-currentY,2)+Math.pow(mTargetLocationX-currentX,2));
        if (distance<50){
            mSpeed=0;
        }
        else{
            mSpeed=(float)(Math.sqrt(distance / mScreenWidth)* mBaseSpeed *mSpeedUpTimes);
        }
    }

    private float getIncreaseRotation(float diffRotation){
        if (diffRotation<0){
            while (diffRotation<0){
                diffRotation+=360;
            }
        }
        else if (diffRotation>360){
            while (diffRotation>360){
                diffRotation-=360;
            }
        }

        float increaseRotation=0;
        //若差值大于180度，则增大当前角度
        if (diffRotation>180){
            diffRotation=360-diffRotation;
            //若差值大于5度
            if (diffRotation>5){
                increaseRotation=(diffRotation)/180* mBaseRotation *mRotationUpTimes;
            }
        }
        else{
            if (diffRotation>5){
                increaseRotation=-1*(diffRotation)/180* mBaseRotation *mRotationUpTimes;
            }
        }

        return increaseRotation;
    }

    public int[] getNextPosition(){
        getNewRotation();
        this.setRotation(mRotation);
//        Log.e(TAG,"Speed:"+mSpeed);
        int nextX=(int)(getX()+mSpeed*Math.sin(mRotation*Math.PI/180));
        int nextY=(int)(getY()-mSpeed*Math.cos(mRotation*Math.PI/180));

        if (nextX>=mScreenWidth){
            nextX=-this.getWidth();
        }
        else if (nextX<-this.getWidth()){
            nextX=mScreenWidth;
        }

        if (nextY>=mScreenHeight){
            nextY=-this.getHeight();
        }
        else if (nextY<-this.getHeight()){
            nextY=mScreenHeight;
        }

        int[] vector={nextX,nextY};
        return vector;
    }

    public void setScaleToSizeOfScreen(float scale){
        mScaleToSizeOfScreen =scale;
    }

    public float getScaleToSizeOfScreen(){
        return mScaleToSizeOfScreen;
    }
}
