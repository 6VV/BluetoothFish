package com.lyyjy.zdhyjs.bluetoothfish.Bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.lyyjy.zdhyjs.bluetoothfish.CommandCode;
import com.lyyjy.zdhyjs.bluetoothfish.PersistentDataManager;

import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2016/1/5.
 */
public class BluetoothBleManager {
    private final String TAG="BluetoothBleManager";

    /*获取唯一实例*/
    private volatile static BluetoothBleManager mInstance=null;
    public static BluetoothBleManager GetInstance(Context context){
        if (mInstance==null)
        {
            synchronized (BluetoothBleManager.class){
                if (mInstance==null){
                    mInstance=new BluetoothBleManager(context.getApplicationContext());
                }
            }
        }
        return  mInstance;
    }


    private BluetoothBleManager(Context context){
        mCurrentContext=context;
        isSupportBluetooth();
    }

    /*服务及特性UUID*/
    private static final UUID UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");   //读写对应的服务UUID
    private static final UUID UUID_CHARACTERISTIC_WRITE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");  //写入特性对应的UUID
    private static final UUID UUID_CHARACTERISTIC_READ = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");   //读取特性对应的UUID

    /*连接状态标志*/
    public static final int STATE_DISCONNECT = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING=2;

    public static int mConnectionState=STATE_DISCONNECT;    //当前连接状态

    private Context mCurrentContext;    //当前Activity

    //蓝牙相关
    public static BluetoothManager mBluetoothManager=null;  //蓝牙管理器
    public static BluetoothAdapter mBluetoothAdapter=null;   //蓝牙适配器
    public static BluetoothGatt mBluetoothGatt=null;     //蓝牙Gatt
    public static BluetoothDevice mBluetoothDevice=null; //当前连接的蓝牙设备
    public static String mDeviceName;      //当前连接的蓝牙设备名
    public static String mDeviceAddress;   //当前连接的蓝牙设备地址

    private BluetoothGattCharacteristic mBluetoothGattCharacteristicRead;   //读取用Characteristic
    private BluetoothGattCharacteristic mBluetoothGattCharacteristicWrite;  //写入用Characteristic

    private BleInterface mBleInterface;

    //消息指令
    public static final int HANDLER_CONNECT=1;
    public static final int HANDLER_NOT_FIND_SERVICE=2;
    public static final int HANDLER_NOT_FIND_CHARACTERISTIC=3;
    public static final int HANDLER_SET_PASSWORD=4;
    public static final int HANDLER_SET_NAME=5;
    public static final int HANDLER_DISCONNECT=7;
    public static final int HANDLER_DISCOVER_SERVICE=8;

    private Handler mMessageHandler=new MessageHandler();

    public void setCurrentContext(Context context){
        mCurrentContext=context;
    }

    private void isSupportBluetooth() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mCurrentContext);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        mBluetoothManager = (BluetoothManager) mCurrentContext.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            dialog.setTitle("不支持蓝牙设备");
            dialog.show();
        }

        if (!mCurrentContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            dialog.setTitle("不支持蓝牙4.0");
            dialog.show();
        }
    }

    //返回蓝牙打开状态
    public boolean isBleEnabled(){
        return mBluetoothAdapter.isEnabled();
    }

    public void openBluetooth(){
        mBluetoothAdapter.enable();
    }

    public BluetoothAdapter getAdapter(){
        return mBluetoothAdapter;
    }

    public void setBleInterface(BleInterface bleInterface){
        Log.e(TAG,"set interface");
        mBleInterface=bleInterface;
    }
    //搜索蓝牙设备
    boolean mScaning=false;
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            if (mScaning){
                return;
            }

            mScaning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScaning = false;
        }
    }

    //查询回调函数
    private BluetoothAdapter.LeScanCallback mLeScanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mBleInterface.onBleScan(device);
        }
    };

    /*连接蓝牙*/
    public void connect(BluetoothDevice device) {

        if(!mBluetoothAdapter.isEnabled()){
            Toast.makeText(mCurrentContext, "请先开启蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        if(device==null){
            Toast.makeText(mCurrentContext,"请选择一个设备",Toast.LENGTH_SHORT).show();
            return;
        }

        mBluetoothDevice=device;

        //移除心跳
        handlerHearHitStop.removeCallbacks(runnableHearHitStop);

        //销毁原有gatt
        destory();

        mConnectionState=STATE_CONNECTING;
        mBluetoothGatt = device.connectGatt(mCurrentContext.getApplicationContext(), false, mGattCallback);
    }

    //建立连接时的调用的函数
    private void connectFunction(){
        BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(UUID_SERVICE);    //获取特定服务

        //若未找到相关服务，则断开连接
        if (bluetoothGattService == null) {
            Message msg=new Message();
            msg.what=HANDLER_NOT_FIND_SERVICE;
            mMessageHandler.sendMessage(msg);
            this.destory();
            return;
        }

        mBluetoothGattCharacteristicRead = bluetoothGattService.getCharacteristic(UUID_CHARACTERISTIC_READ);    //获取读特性
        mBluetoothGattCharacteristicWrite=bluetoothGattService.getCharacteristic(UUID_CHARACTERISTIC_WRITE);    //获取写特性

        //若未找到相关特性
        if (mBluetoothGattCharacteristicRead == null||mBluetoothGattCharacteristicWrite==null) {
            Message msg=new Message();
            msg.what=HANDLER_NOT_FIND_CHARACTERISTIC;
            mMessageHandler.sendMessage(msg);
            this.destory();
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristicRead, true);   //监听读事件

        mBleInterface.onConnect();
    }

    //写入数据
    public boolean writeDataToDevice(byte[] data) {
        if (mBluetoothGatt == null) {
            return false;
        }

            if (mConnectionState==STATE_DISCONNECT){
                return  false;
            }

        mBluetoothGattCharacteristicWrite.setValue(data);
        mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristicWrite);

        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Message msg=new Message();
                msg.what=HANDLER_CONNECT;
                mMessageHandler.sendMessage(msg);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Message msg=new Message();
                msg.what=HANDLER_DISCONNECT;
                mMessageHandler.sendMessage(msg);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Message msg=new Message();
            msg.what=HANDLER_DISCOVER_SERVICE;
            mMessageHandler.sendMessage(msg);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] data=characteristic.getValue();

            onGetData(data);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            if (status==BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "rssi:" + Byte.toString((byte)rssi));
            }
        }
    };

    boolean mIsToGetFirstHitData=false;
    List<Integer> mPowerList;

    private void onGetData(byte[] data){
//        Log.e(TAG,""+String.valueOf(data[0]));
        if (data.length<2){
            return;
        }
        switch (data[0]){
            case CommandCode.BACK_SUCCESS:
            {
                if (data[1]==(byte)0x01) {
                    Message msg=new Message();
                    msg.what=HANDLER_SET_PASSWORD;
                    mMessageHandler.sendMessage(msg);
                }
                //若为获取密码
                else if (data[1]==(byte)0x02){
                    mBleInterface.onChangeName();
                }
            }break;
            case CommandCode.BACK_GET_PASSWORD:{
                byte[] bytesPassword=new byte[data.length-1];
                for (int i=0;i<bytesPassword.length;i++){
                    bytesPassword[i]=data[i+1];
                }
            }break;
            case CommandCode.BACK_HEART_HIT:{
                Log.e(TAG, "P123:");

                int powerArg=0;
                if (data.length<6){
                    return;
                }
                int power=0x000000ff & data[5];
//                if (mPowerList.size()>10){
//                    mPowerList.remove(0);
//                    mPowerList.add(new Integer(power));
//                    for (Integer p:
//                         mPowerList) {
//                        powerArg+=p;
//                    }
//                    powerArg=powerArg/mPowerList.size();
//                }else{
//                    mPowerList.add(new Integer(power));
//                }
//
                mBleInterface.onChangePower(power);
                mIsToGetFirstHitData=false;

                Log.e(TAG, "POWRE:" + power);

                handlerHearHitStop.removeCallbacks(runnableHearHitStop);
                handlerHearHitStop.postDelayed(runnableHearHitStop, hearHitDelayTime);
            }break;
            case CommandCode.BACK_POWER:{
                int power=0x000000ff & data[1];
                mBleInterface.onChangePower(power);
            }break;
        }
//        if (data[0]== CommandCode.BACK_SUCCESS){
//            if (data[1]==(byte)0x01) {
//                Message msg=new Message();
//                msg.what=HANDLER_SET_PASSWORD;
//                mMessageHandler.sendMessage(msg);
//            }
//            //若为获取密码
//            else if (data[1]==(byte)0x02){
//                mBleInterface.onChangeName();
//            }
//        }else if (data[0]== CommandCode.BACK_GET_PASSWORD){
//            //若为获取密码
//            byte[] bytesPassword=new byte[data.length-1];
//            for (int i=0;i<bytesPassword.length;i++){
//                bytesPassword[i]=data[i+1];
//            }
//        }
//        //若返回的是心跳
//        else if (data[0]== CommandCode.BACK_HEART_HIT)
//        {
//            handlerHearHitStop.removeCallbacks(runnableHearHitStop);
//            handlerHearHitStop.postDelayed(runnableHearHitStop, hearHitDelayTime);
//        }
//        else if (data[0]== CommandCode.BACK_POWER){
//            int power=0x000000ff & data[1];
//            mBleInterface.onChangePower(power);
//        }
    }


    int hearHitDelayTime=2000;      //2000ms内未接收到心跳，则断开连接
    Handler handlerHearHitStop =new Handler();   //心跳断开时
    Runnable runnableHearHitStop =new Runnable() {
        @Override
        public void run() {
            Log.e("Manager","lose hear hit");
            disconnect();
        }
    };

    public void disconnect(){
        if (mBluetoothGatt==null){
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void closeBluetooth(){
        mBluetoothAdapter.disable();
    }

    public void destory(){
        mConnectionState = STATE_DISCONNECT;

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public int getConnectState(){
        return mConnectionState;
    }

    private class MessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            String message=new String();
            switch (msg.what){
                case HANDLER_CONNECT:{
                    Log.e(TAG,"connected");
                    mIsToGetFirstHitData=true;
                    mConnectionState = STATE_CONNECTED; //连接状态
                    mBluetoothGatt.discoverServices();  //获取所有服务

                }break;
                case HANDLER_DISCONNECT:{
                    Log.e(TAG,"disconnected");
                    mConnectionState = STATE_DISCONNECT;
                    mBleInterface.onDisconnect();

                    //若设置为自动重连
                    if (PersistentDataManager.getInstance(mCurrentContext).mIsAutoReconnect){
                        mBluetoothGatt.connect();
                    }
                }break;
                case HANDLER_DISCOVER_SERVICE:
                {
                    Log.e(TAG, "services discovered");
                    connectFunction();
                }break;
                case HANDLER_NOT_FIND_SERVICE:{
                    message="未找到相关服务";
                    Toast.makeText(mCurrentContext, message, Toast.LENGTH_SHORT).show();
                }break;
                case HANDLER_NOT_FIND_CHARACTERISTIC:{
                    message="未找到相关特性";
                    Toast.makeText(mCurrentContext, message, Toast.LENGTH_SHORT).show();
                }break;
            }
        }
    }
}
