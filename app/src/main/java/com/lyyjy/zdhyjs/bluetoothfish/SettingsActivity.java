package com.lyyjy.zdhyjs.bluetoothfish;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BleInterface;
import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BluetoothBleManager;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener,BleInterface {
    private final String TAG="SettingsActivity";

    private Button btnModifyName;   //修改名字
//    private EditText etNewName;     //新名字
    private CheckBox cbCloseBluetooth;  //关闭蓝牙
    private CheckBox cbAutoReconnect;   //自动重连
    private CheckBox cbSaveControlersLocation;  //保存控件位置
    private CheckBox cbOpenWaterWave;      //开启水波效果

    private BluetoothBleManager mBluetoothBleManager;   //蓝牙管理器

    private PersistentDataManager mPersistentDataManager;

    private String mNewName;

    private SettingHandler mHandler;
    private final int HANDLER_RESET_DEVICE=0;   //重置设备
    private final int HANDLER_CONNECT_STATE=1;  //连接状态改变

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mBluetoothBleManager=BluetoothBleManager.GetInstance(this);

        btnModifyName = (Button) findViewById(R.id.btnModifyName);
//        etNewName= (EditText) findViewById(R.id.etNewName);
        cbAutoReconnect = (CheckBox) findViewById(R.id.cbAutoReconnect);
        cbCloseBluetooth = (CheckBox) findViewById(R.id.cbCloseBluetooth);
        cbSaveControlersLocation= (CheckBox) findViewById(R.id.cbSaveControlersLocation);
        cbOpenWaterWave= (CheckBox) findViewById(R.id.cbOpenWaterWave);


        btnModifyName.setOnClickListener(this);
        cbAutoReconnect.setOnClickListener(this);
        cbCloseBluetooth.setOnClickListener(this);
        cbSaveControlersLocation.setOnClickListener(this);
        cbOpenWaterWave.setOnClickListener(this);

        //ActionBar
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mHandler=new SettingHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothBleManager.setBleInterface(this);
        mBluetoothBleManager.setCurrentContext(this);

        mPersistentDataManager=PersistentDataManager.getInstance(this);

        //获取设置相关信息
        if (mPersistentDataManager.mIsCloseBluetooth){
            cbCloseBluetooth.setChecked(true);
        }
        else {
            cbCloseBluetooth.setChecked(false);
        }

        if (mPersistentDataManager.mIsAutoReconnect){
            cbAutoReconnect.setChecked(true);
        }
        else {
            cbAutoReconnect.setChecked(false);
        }

        if (mPersistentDataManager.mIsSaveControlerLocation){
            cbSaveControlersLocation.setChecked(true);
        }
        else {
            cbSaveControlersLocation.setChecked(false);
        }

        if (mPersistentDataManager.mIsOpenWaterWave){
            cbOpenWaterWave.setChecked(true);
        }
        else {
            cbOpenWaterWave.setChecked(false);
        }
//        if (BluetoothBleManager.mConnectionState==BluetoothBleManager.STATE_DISCONNECT){
//            return;
//        }
//        else {
//            //获取连接设备相关信息
//            Map<String, String> mapDevice = dataManager.selectDevice(BluetoothBleManager.mBluetoothDevice.getAddress());
//
//            if (mapDevice.size()==0) {
//                return;
//            }
//        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnModifyName:{
                if (!checkConnect()){
                    return;
                }
                modifyName();
            }break;
            case R.id.cbCloseBluetooth:{
                closeBluetoothSetting();
            }break;
            case R.id.cbAutoReconnect:{
                reconnectSetting();
            }break;
            case R.id.cbSaveControlersLocation:{
                saveControlerLocation();
            }break;
            case R.id.cbOpenWaterWave:{
                openWaterWave();
            }break;
        }
    }

    private void openWaterWave() {
        if (cbOpenWaterWave.isChecked()){
            mPersistentDataManager.updateOpenWaterWave(PersistentDataManager.CHECK);
            mPersistentDataManager.mIsOpenWaterWave=true;
        }
        else {
            mPersistentDataManager.updateOpenWaterWave(PersistentDataManager.UNCHECK);
            mPersistentDataManager.mIsOpenWaterWave=false;
        }
    }

    private void saveControlerLocation() {
        if (cbSaveControlersLocation.isChecked()){
            mPersistentDataManager.updateSaveControlerLocation(PersistentDataManager.CHECK);
            mPersistentDataManager.mIsSaveControlerLocation=true;
        }
        else {
            mPersistentDataManager.updateSaveControlerLocation(PersistentDataManager.UNCHECK);
            mPersistentDataManager.mIsSaveControlerLocation=false;
        }
    }

    private void reconnectSetting() {
        if (cbAutoReconnect.isChecked()){
            mPersistentDataManager.updateSettingsReconnect(PersistentDataManager.CHECK);
            mPersistentDataManager.mIsAutoReconnect=true;
        }
        else {
            mPersistentDataManager.updateSettingsReconnect(PersistentDataManager.UNCHECK);
            mPersistentDataManager.mIsAutoReconnect=false;
        }
    }

    private void closeBluetoothSetting() {
        if (cbCloseBluetooth.isChecked()){
            mPersistentDataManager.updateSettingsCloseBluetooth(PersistentDataManager.CHECK);
            mPersistentDataManager.mIsCloseBluetooth=true;
        }
        else {
            mPersistentDataManager.updateSettingsCloseBluetooth(PersistentDataManager.UNCHECK);
            mPersistentDataManager.mIsCloseBluetooth=false;
        }
    }

    private void modifyName(){
        Log.e(TAG,"modify name");
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("填写新名字");
        dialog.setMessage("修改名字后会自动重启仿生鱼,新名字在设备重启后生效");
        final EditText editText=new EditText(this);
        dialog.setView(editText);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNewName=editText.getText().toString();
                setNewName(mNewName);
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    private void setNewName(final String strNewName){
//        AlertDialog.Builder dialog=new AlertDialog.Builder(SettingsActivity.this);
//        dialog.setTitle("是否修改为新名字");
//        dialog.setMessage("新名字在重启设备后生效");
//        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
                byte[] bytesNewName = strNewName.getBytes();
                int nameLength = bytesNewName.length;

                if (nameLength == 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                    alertDialog.setTitle("名字不能为空");
                    alertDialog.setPositiveButton("确定", null);
                    alertDialog.show();
                } else if (nameLength > 12) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                    alertDialog.setTitle("名字长度不能大于12个字符");
                    alertDialog.setPositiveButton("确定", null);
                    alertDialog.show();
                } else {
                    mBluetoothBleManager.writeDataToDevice(CommandCode.getRenameCommand(bytesNewName));
                }
//            }
//        });
//        dialog.setNegativeButton("取消",null);
//
//        dialog.show();
    }
//    private void modifyPassword(){
//        AlertDialog.Builder dialog=new AlertDialog.Builder(SettingsActivity.this);
//        dialog.setTitle("是否修改为新密码");
//        dialog.setNegativeButton("取消", null);
//        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String strNewPassword = etNewPassword.getText().toString();
//                byte[] bytesNewPassword = strNewPassword.getBytes();
//                int passwordLength = bytesNewPassword.length;
//
//                if (passwordLength == 0) {
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
//                    alertDialog.setTitle("密码不能为空");
//                    alertDialog.setPositiveButton("确定", null);
//                    alertDialog.show();
//                } else if (passwordLength > 6) {
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
//                    alertDialog.setTitle("密码长度不能大于6个字符");
//                    alertDialog.setPositiveButton("确定", null);
//                    alertDialog.show();
//                } else {
//
//                }
//            }
//        });
//        dialog.show();
//    }

    private boolean checkConnect(){
        if (BluetoothBleManager.mConnectionState==BluetoothBleManager.STATE_DISCONNECT){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
            alertDialog.setTitle("请先连接一个设备");
            alertDialog.setPositiveButton("确定", null);
            alertDialog.show();
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onChangeName() {
        Message msg=new Message();
        msg.what=HANDLER_RESET_DEVICE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onChangePower(int power) {

    }

    private class SettingHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_RESET_DEVICE:
                {
                    Toast.makeText(SettingsActivity.this,"名字修改成功，等待设备重启",Toast.LENGTH_LONG).show();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBluetoothBleManager.writeDataToDevice(CommandCode.COMMAND_RESET_DEVICE);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mBluetoothBleManager.connect(mBluetoothBleManager.mBluetoothDevice);
                    mBluetoothBleManager.mDeviceName=mNewName;
                }break;
                case HANDLER_CONNECT_STATE:{
                    if (mBluetoothBleManager.mConnectionState==mBluetoothBleManager.STATE_CONNECTED){
                        Toast.makeText(SettingsActivity.this,"连接成功",Toast.LENGTH_LONG).show();
                    }else if(mBluetoothBleManager.mConnectionState==mBluetoothBleManager.STATE_DISCONNECT){
                        Toast.makeText(SettingsActivity.this,"断开连接",Toast.LENGTH_LONG).show();
                    }
                }break;
            }
        }
    }

    @Override
    public void onBleOpen() {

    }

    @Override
    public void onConnect() {
        Message msg=new Message();
        msg.what=HANDLER_CONNECT_STATE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onBleScan(BluetoothDevice device) {

    }

    @Override
    public void onDisconnect() {

    }
}
