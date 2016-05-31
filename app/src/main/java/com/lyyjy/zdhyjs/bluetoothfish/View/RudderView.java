package com.lyyjy.zdhyjs.bluetoothfish.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2016/4/5.
 */
public class RudderView extends View {
    private String TAG="RudderView";

    public static final int FISH_LEFT=1;
    public static final int FISH_UP=2;
    public static final int FISH_RIGHT=3;
    public static final int FISH_STOP=4;

    private  int FISH_DIRECTION=FISH_UP;   //游动方向

    private float mSpeed=3; //旋转速度

    private float mTargetRotation=180; //目标角度

    public RudderView(Context context) {
        super(context);
    }

    public RudderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RudderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getFishDirection() {
        return getFishDirection(mTargetRotation);
    }

//    public void setTargetLocation(float x, float y){
//        mTargetRotation=getRotation(x,y);
//    }

    public void setTargetRotation(float rotation){
        mTargetRotation=rotation;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:case MotionEvent.ACTION_MOVE:{
                float centerX=getX()+getWidth()/2;
                float centerY=getY()+getHeight()/2;
                if (Math.pow(event.getRawX()-centerX,2)+Math.pow(event.getRawY()-centerY,2)>Math.pow(getWidth()/8,2)+Math.pow(getHeight()/8,2)){
                    mTargetRotation=getRotation(event.getRawX(),event.getRawY());
                }

            }break;
            case MotionEvent.ACTION_UP:{
                mTargetRotation=180;
            }break;
        }
        return true;
    }

    public void updateState(){
        setNextRotation(mTargetRotation);
    }

    private void setNextRotation(float targetRotation){
        float currentRotation=getRotation();
        float nextRotation=0;

        float interval=adjustAngle(mTargetRotation-currentRotation);
        if(interval>0){
            nextRotation=currentRotation+mSpeed;
            if (nextRotation>targetRotation){
                nextRotation=targetRotation;
            }
        } else{
            nextRotation=currentRotation-mSpeed;
            if (nextRotation<targetRotation){
                nextRotation=targetRotation;
            }
        }

        this.setRotation(nextRotation);
    }

    private float adjustAngle(float oldAngle){
        //间隔调至-180与180之间
        while (oldAngle>180){
            oldAngle-=360;
        }
        while (oldAngle<-180){
            oldAngle+=360;
        }

        return oldAngle;
    }

    private int getFishDirection(float rotation){
        if (rotation>210 && rotation<330){
            return FISH_LEFT;
        }else if (rotation>30 && rotation<150){
            return FISH_RIGHT;
        }else if (rotation<=30 && rotation>=0 || rotation>=330 && rotation<=360) {
            return FISH_UP;
        }else{
            return FISH_STOP;
        }
    }

    private float getRotation(float x,float y){
        float centerX=getX()+getWidth()/2;
        float centerY=getY()+getHeight()/2;

        /*获取旋转角度*/
        float targetRoation= (float) (90+Math.atan2(y-centerY,x-centerX)*180/Math.PI);

//        targetRoation=targetRoation>180?(targetRoation-360):targetRoation;
        targetRoation=targetRoation<0?(targetRoation+360):targetRoation;
        targetRoation=targetRoation>360?(targetRoation-360):targetRoation;
//        targetRoation=targetRoation>90?90:targetRoation;
//        targetRoation=targetRoation<-90?-90:targetRoation;

        return targetRoation;
    }
}
