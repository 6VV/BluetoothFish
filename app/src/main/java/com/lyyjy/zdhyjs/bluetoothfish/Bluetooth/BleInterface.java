package com.lyyjy.zdhyjs.bluetoothfish.Bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2016/3/24.
 */
public interface BleInterface {
    void onBleOpen();
    void onConnect();
    void onBleScan(BluetoothDevice device);
    void onDisconnect();
    void onChangeName();
    void onChangePower(int power);
}
