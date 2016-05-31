package com.lyyjy.zdhyjs.bluetoothfish.Device;

/**
 * Created by Administrator on 2016/1/15.
 */
public class Device {
    private String mDeviceName;
    private String mDeviceAddress;
    private int mDeviceIcon;

    public Device(String deviceName,int deviceIcon,String deviceAddress){
        mDeviceName=deviceName;
        mDeviceIcon=deviceIcon;
        mDeviceAddress=deviceAddress;
    }

    public String getName(){
        return mDeviceName;
    }

    public int getIcon(){
        return mDeviceIcon;
    }

    public String getAddress(){
        return mDeviceAddress;
    }
}
