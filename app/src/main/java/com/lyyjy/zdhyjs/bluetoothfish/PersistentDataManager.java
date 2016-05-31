package com.lyyjy.zdhyjs.bluetoothfish;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Administrator on 2016/1/7.
 */
public class PersistentDataManager {
    private DatabaseManager mDataManager;

    public static final String CHECK ="1";
    public static final String UNCHECK ="0";

    public static final String TABLE_DEVICE_ADDRESS = "Address";
    public static final String TABLE_DEVICE_NAME = "Name";
    public static final String TABLE_DEVICE_PASSWORD = "Password";
    public static final String TABLE_DEVICE_SAVE_PASSWORD ="SavePassword";
    public static final String TABLE_DEVICE_AUTO_CONNECT ="AutoConnect";

    public static final String TABLE_SETTINGS_CLOSE_BLUETOOTH="CloseBluetooth";
    public static final String TABLE_SETTINGS_AUTO_RECONNECT = "AutoReconnect";
    public static final String TABLE_SETTINGS_SAVE_CONTROLER_LOCATION="SaveControlerLocation";
    public static final String TABLE_SETTINGS_OPEN_WATER_WAVE="OpenWaterWave";

    public static final String TABLE_LOCATION_ID="ID";
    public static final String TABLE_LOCATION_LEFT="left";
    public static final String TABLE_LOCATION_TOP="top";
    public static final String TABLE_LOCATION_RIGHT="right";
    public static final String TABLE_LOCATION_BOTTOM="bottom";

    private volatile static PersistentDataManager mInstance=null;

    public static PersistentDataManager getInstance(Context context){
        if (mInstance==null)
        {
            synchronized (PersistentDataManager.class){
                if (mInstance==null){
                    mInstance=new PersistentDataManager(context.getApplicationContext());
                }
            }
        }
        return  mInstance;
    }

    public boolean mIsCloseBluetooth =false; //是否设置为关闭蓝牙
    public boolean mIsAutoReconnect=false;   //是否设置为自动重连
    public boolean mIsSaveControlerLocation=false;   //是否保证控件位置
    public boolean mIsOpenWaterWave=false;   //是否开启水波效果

    private PersistentDataManager(Context context){
        mDataManager=DatabaseManager.getInstance(context);
        init();
    }

    void init(){
        //获取设置相关信息
        Map<String,String> mapSettings=mDataManager.getSettings();
        if (mapSettings.get(TABLE_SETTINGS_CLOSE_BLUETOOTH).equals(CHECK)){
            mIsCloseBluetooth=true;
        }
        else {
            mIsCloseBluetooth=false;
        }

        if (mapSettings.get(TABLE_SETTINGS_AUTO_RECONNECT).equals(CHECK)){
            mIsAutoReconnect=true;
        }
        else {
            mIsAutoReconnect=false;
        }

        if (mapSettings.get(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION).equals(CHECK)){
            mIsSaveControlerLocation=true;
        }
        else {
            mIsSaveControlerLocation=false;
        }

        if (mapSettings.get(TABLE_SETTINGS_OPEN_WATER_WAVE).equals(CHECK)){
            mIsOpenWaterWave=true;
        }
        else {
            mIsOpenWaterWave=false;
        }
    }

    public Map<String,String> selectDevice(String address){
        return mDataManager.selectDevice(address);
    }

    public void insertUpdateDevice(String address,String name,String password,String savePassword,String autoConnect){
        mDataManager.insertUpdateDevice(address, name, password, savePassword, autoConnect);
    }

    public void deleteDevice(String address){
        mDataManager.deleteDevice(address);
    }

    public void updateDeviceSavePasswrod(String address, String savePassword){
        mDataManager.updateDeviceSavePassword(address, savePassword);
    }

    public void updateDeviceAutoConnect(String address, String autoConnect) {
        mDataManager.updateDeviceAutoConnect(address, autoConnect);
    }

    public void insertUpdateSettings(String closeBluetooth,String autoReconnect,String saveLocation,String openWaterWave){
        mDataManager.insertUpdateSettings(closeBluetooth, autoReconnect, saveLocation, openWaterWave);
    }

    public Map<String,String> getSettings(){
        return mDataManager.getSettings();
    }

    public void updateSettingsCloseBluetooth(String closeBluetooth){
        mDataManager.updateSettingsCloseBluetooth(closeBluetooth);
    }

    public void updateSettingsReconnect(String autoReconnect){
        mDataManager.updateSettingsAutoReconnect(autoReconnect);
    }

    public void updateSaveControlerLocation(String saveLocation){
        mDataManager.updateSaveControlerLocation(saveLocation);
    }

    public void updateOpenWaterWave(String openWaterWave){
        mDataManager.updateOpenWaterWave(openWaterWave);
    }

    public void insertUpdateLocation(int id,int left,int top,int right,int bottom){
        mDataManager.insertUpdateLocation(id, left, top, right, bottom);
    }

    public Map<Integer,Vector<Integer>> selectLocation(){
        return mDataManager.selectLocation();
    }
    //测试用，显示所有设备信息
    public void selectDevices(){
        mDataManager.selectDevices();
    }
}
