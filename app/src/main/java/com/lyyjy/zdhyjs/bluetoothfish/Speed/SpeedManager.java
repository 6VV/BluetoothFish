package com.lyyjy.zdhyjs.bluetoothfish.Speed;

import android.content.Context;
import android.widget.ImageButton;

import com.lyyjy.zdhyjs.bluetoothfish.CommandCode;

/**
 * Created by Administrator on 2016/5/5.
 */
public class SpeedManager {
    private Context mContext;
    private static final int FISH_MAX_SPEED=0x03;    //最大速度
    private static final int FISH_MIN_SPEED=0x00;    //最小速度

    public static final int PRESS_MIN_TIME_TO_CHANGE=500;   //按下最短时常
    public static final int SPEED_MAX_DIF=FISH_MAX_SPEED-FISH_MIN_SPEED+1;

    private SpeedView mSpeedView;
    private ImageButton mBtnIncreaseSpeed;
    private ImageButton mBtnDecreaseSpeed;

    private int mFishSpeed;

    public SpeedManager(Context context,SpeedView speedView,ImageButton btnIncreaseSpeed,ImageButton btnDecreaseSpeed){
        mContext=context;
        mSpeedView=speedView;
        mBtnDecreaseSpeed=btnDecreaseSpeed;
        mBtnIncreaseSpeed=btnIncreaseSpeed;

        refreshSpeedCommand();
    }


    public void startWave(){
        mSpeedView.startWave();
    }

    public void stopWave(){
        mSpeedView.stopWave();
    }

    /*鱼加速*/
    public void fishAccelerate()
    {
        new Thread(runnableFishAccelerate).start();
    }

    /*鱼减速*/
    public void fishDeaccelerate()
    {
        new Thread(runnableFishDeaccelerate).start();
    }

    private long m_timeLastAccelerate=0;     //上次鱼加速的时间
    private Runnable runnableFishAccelerate =new Runnable() {
        @Override
        public void run() {
            /*加速键按下时*/
//            Log.e(TAG, String.valueOf(mSpeedManager.getBtnIncrease().isPressed()));
            while(mBtnIncreaseSpeed.isPressed())
            {
            /*距离上一次加速键按下超过500ms*/
                if (System.currentTimeMillis()-m_timeLastAccelerate>PRESS_MIN_TIME_TO_CHANGE){
                    if (mFishSpeed!=FISH_MAX_SPEED){
                        mFishSpeed++;
                    }
                    refreshSpeedCommand();
                    m_timeLastAccelerate=System.currentTimeMillis();
                }
            }
            m_timeLastAccelerate=0;
        }
    };

    private long m_timeLastDeaccelerate=0;   //上次鱼减速的时间
    private Runnable runnableFishDeaccelerate =new Runnable() {
        @Override
        public void run() {
              /*加速键按下时*/
            while(mBtnDecreaseSpeed.isPressed())
            {
            /*距离上一次加速键按下超过500ms*/
                if (System.currentTimeMillis()-m_timeLastDeaccelerate>PRESS_MIN_TIME_TO_CHANGE){
//                    Log.e(TAG,"btnFishDeaccelerate>500");
                    if (mFishSpeed!=FISH_MIN_SPEED){
                        mFishSpeed--;
                    }
                    refreshSpeedCommand();
                    m_timeLastDeaccelerate=System.currentTimeMillis();
                }
            }
            m_timeLastDeaccelerate=0;
        }
    };

    private void refreshSpeedCommand() {
        CommandCode.setSpeed((byte)mFishSpeed);

        //更新速度槽
        mSpeedView.setWaterLevel((mFishSpeed+1) * 1.0f / SPEED_MAX_DIF);
    }

}
