package com.lyyjy.zdhyjs.bluetoothfish;

/**
 * Created by Administrator on 2016/5/10.
 */
public class CommandCode {
    //控制小鱼指令
    private static final byte COMMAND_HEAD_1 = (byte) 0x55;
    private static final byte COMMAND_HEAD_2 = (byte) 0xAA;
    private static final byte COMMAND_HEAD_3 = (byte) 0x99;
    private static final byte COMMAND_HEAD_4 = (byte) 0x11;

    public static final byte FISH_UP =(byte)0x00;
    public static final byte FISH_RIGHT =(byte)0x02;
    public static final byte FISH_LEFT =(byte)0x03;
    public static final byte FISH_STOP =(byte)0x20;

    private static final byte MODE_MANUAL=(byte)0x00;
    private static final byte MODE_AUTO=(byte)0x01;

    private static final byte REQUEST_CONTROL = (byte) 0x00;  //控制指令
    private static final byte REQUEST_SET_NAME = (byte) 0x02; //设置名字
    private static final byte REQUEST_RESET = (byte) 0x04;      //重置
    private static final byte REQUEST_COLOR=(byte)0x05; //灯光颜色
    private static final byte REQUEST_MODE=(byte)0x06;  //游动模式
    private static final byte REQUEST_FINAL = (byte) 0xFF;    //数据尾

    private static byte[] COMMAND_SET_COLOR={COMMAND_HEAD_1, COMMAND_HEAD_2, COMMAND_HEAD_3,COMMAND_HEAD_4, REQUEST_COLOR,0x00,REQUEST_FINAL};
    private static byte[] COMMAND_SET_MODE={COMMAND_HEAD_1,COMMAND_HEAD_2,COMMAND_HEAD_3,COMMAND_HEAD_4,REQUEST_MODE,0x00,REQUEST_FINAL};

    public static final byte BACK_SUCCESS = (byte) 0x68;    //设置成功
    public static final byte BACK_GET_PASSWORD = (byte) 0x03;   //返回密码
    public static final byte BACK_HEART_HIT=(byte)0x04;     //心跳
    public static final byte BACK_POWER=(byte)0x05;     //电量
    public static final byte[] COMMAND_RESET_DEVICE={COMMAND_HEAD_1, COMMAND_HEAD_2, COMMAND_HEAD_3,COMMAND_HEAD_4, REQUEST_RESET, REQUEST_FINAL};

    public static byte[] FISH_COMMAND={
            COMMAND_HEAD_1, COMMAND_HEAD_2, COMMAND_HEAD_3, COMMAND_HEAD_4,
            REQUEST_CONTROL,
            0x00,
            (byte) 0x03,
            REQUEST_FINAL};

    public static void setSpeed(byte speed){
        FISH_COMMAND[6]=speed;
    }

    public static void setDirection(byte direction){
        FISH_COMMAND[5]=direction;
    }

    public static byte[] getRenameCommand(byte[] newName){
        int nameLength=newName.length;
        byte[] bytesSetName = new byte[7 + nameLength];
        bytesSetName[0] = COMMAND_HEAD_1;
        bytesSetName[1] = COMMAND_HEAD_2;
        bytesSetName[2] = COMMAND_HEAD_3;
        bytesSetName[3] = COMMAND_HEAD_4;
        bytesSetName[4] = REQUEST_SET_NAME;
        bytesSetName[5] = (byte) nameLength;
        for (int i = 0; i < nameLength; i++) {
            bytesSetName[6 + i] = newName[i];
        }
        bytesSetName[6 + nameLength] = REQUEST_FINAL;

        return bytesSetName;
    }

    public static byte[] getColorCommand(byte color){
        COMMAND_SET_COLOR[5]=color;
        return COMMAND_SET_COLOR;
    }

    public static byte[] getAutoModeCommand(){
        COMMAND_SET_MODE[5]=MODE_AUTO;
        return COMMAND_SET_MODE;
    }

    public static byte[] getManualModeCommand(){
        COMMAND_SET_MODE[5]=MODE_MANUAL;
        return COMMAND_SET_MODE;
    }
}
