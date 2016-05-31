package com.lyyjy.zdhyjs.bluetoothfish.SwimMode;

import com.lyyjy.zdhyjs.bluetoothfish.CommandCode;

/**
 * Created by Administrator on 2016/5/4.
 */
public class SwimMode {
    private int mStateNum=1;
    private int mSwimTime=30;
    private int mColorID=0;
    private int mDirectionID=0;

    public void setDirectionID(int id){
        mDirectionID=id;
    }

    public int getDirectionID(){
        return mDirectionID;
    }

    public byte getDirection(){
        byte data;
        switch (mDirectionID){
            case 0:{
                data = CommandCode.FISH_UP;
            }break;
            case 1:{
                data = CommandCode.FISH_LEFT;
            }break;
            case 2:{
                data = CommandCode.FISH_RIGHT;
            }break;
            case 3:{
                data = CommandCode.FISH_STOP;
            }break;
            default: data = CommandCode.FISH_STOP;
        }
        return data;
    }

    public void setColorID(int id){
        mColorID=id;
    }

    public int getColorID(){
        return mColorID;
    }
    public SwimMode(int swimTime,int stateNum){
        mSwimTime=swimTime;
        mStateNum=stateNum;
    }

    public int getSwimTime(){
        return mSwimTime;
    }

    public String getStateNum(){
        return "状态"+mStateNum+":";
    }

    public void setSwimTime(int time){
        mSwimTime=time;
    }
}
