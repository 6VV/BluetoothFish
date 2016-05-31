package com.lyyjy.zdhyjs.bluetoothfish;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BleInterface;
import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BleSearchInterface;
import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BluetoothBleManager;
import com.lyyjy.zdhyjs.bluetoothfish.Device.Device;
import com.lyyjy.zdhyjs.bluetoothfish.Device.DeviceAdapter;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,BleInterface,BleSearchInterface {
    private final String TAG = "SearchActivity";

    private final int REQUEST_FINE_LOCATION=0;

    private ListView lvDevices;

    private ArrayList<BluetoothDevice> mDeviceSearched; //查询到的设备
    private ArrayList<Device> mDevicesInfo;              //查询到设备信息
    private boolean mScaning = false;                //是否正在查询
    private final int CONNECT_TIMEOUT=5000;     //连接超时时间
//    private static final int SCAN_PERIOD = 15000;   //最大查询时间（ms）
//    private static final int PERIOD_GET_PASSWORD=3000;  //获取密码最大允许时间

    private BluetoothBleManager mBluetoothBleManager;

    private SearchHandler mHandler;   //用于控制查询过程事件
    private final int HANDLER_CONNECTED=1;

    private BluetoothDevice mCurrentDevice; //当前选择的设备

    private MenuItem mMenuItemLoading;
    private MenuItem mMenuItemScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mHandler = new SearchHandler();

        mBluetoothBleManager = BluetoothBleManager.GetInstance(this);

        lvDevices = (ListView) findViewById(R.id.lvDevices);
        lvDevices.setOnItemClickListener(this);

        //ActionBar
        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothBleManager.setBleInterface(this);
        mBluetoothBleManager.setCurrentContext(this);

        //若已打开蓝牙
        if (mBluetoothBleManager.isBleEnabled()){
            InitState();
        }
        //若未打开蓝牙
        else{
            openBluetooth();
        }
    }

    public void openBluetooth() {
        //判断蓝牙是否打开
        if (!mBluetoothBleManager.isBleEnabled()) {
            AlertDialog.Builder bluetoothEnableDialog = new AlertDialog.Builder(this);
            bluetoothEnableDialog.setTitle("是否开启蓝牙");
            bluetoothEnableDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            bluetoothEnableDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBluetoothBleManager.openBluetooth();
                    onBleOpen();
                }
            });
            bluetoothEnableDialog.show();
        }
    }

    //初始化状态
    public void InitState(){
        mBluetoothBleManager.setCurrentContext(this);   //设置当前Acitivity为本Activity

        //搜索设备信息
        mDeviceSearched = new ArrayList<BluetoothDevice>(); //清空已搜索到的设备列表
        mDevicesInfo = new ArrayList<Device>();     //清空设备信息列表
        DeviceAdapter arrayAdapter = new DeviceAdapter(SearchActivity.this, R.layout.adapter_device, mDevicesInfo);
        lvDevices.setAdapter(arrayAdapter);
        scanLeDevice(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        mMenuItemLoading = menu.findItem(R.id.action_loading);
        mMenuItemScanning = menu.findItem(R.id.action_search);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
            }break;
            case R.id.action_search:{
                scanLeDevice(!mScaning);
            }break;
            case R.id.action_refresh:{
                scanLeDevice(false);
                clearDevice();
                scanLeDevice(true);
            }break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearDevice() {
        mDeviceSearched.clear();
        mDevicesInfo.clear();
        DeviceAdapter arrayAdapter = new DeviceAdapter(SearchActivity.this, R.layout.adapter_device, mDevicesInfo);
        lvDevices.setAdapter(arrayAdapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mScaning) {
            mMenuItemScanning.setTitle("停止");
        }else {
            mMenuItemScanning.setTitle("搜索");
        }
        setLoadingState(mScaning);
        return super.onPrepareOptionsMenu(menu);
    }

    private void scanLeDevice(final boolean enable) {

        //若为开始搜索且sdk>=23
        if (enable && Build.VERSION.SDK_INT >= 23) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                    //判断是否需要 向用户解释，为什么要申请该权限
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        Toast.makeText(this, "Android6.0以上版本需要开启定位才能搜索蓝牙设备", Toast.LENGTH_LONG).show();
                    }
                    ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE_LOCATION);
                    return;
                }
        }

        mScaning=enable;
        mBluetoothBleManager.scanLeDevice(enable);
        invalidateOptionsMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The requested permission is granted.
                    mScaning=true;
                    mBluetoothBleManager.scanLeDevice(true);
                    invalidateOptionsMenu();
                }
                break;
        }
    }
    @Override
    public void onBleScan(final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mDeviceSearched.contains(device)) {
                    mDeviceSearched.add(device);
                    String strName = device.getName();
                    byte[] byteName = strName.getBytes();

                    if(byteName.length<2){
                        return;
                    }
                    //若前两个字节正确
                    if (byteName[0] == 0x01 && byteName[1] == 0x02) {
                        String strNewName = strName.substring(2);
                        Device newDevice = new Device(strNewName, R.drawable.logo,device.getAddress());
                        mDevicesInfo.add(newDevice);
                        DeviceAdapter arrayAdapter = new DeviceAdapter(SearchActivity.this, R.layout.adapter_device, mDevicesInfo);
                        lvDevices.setAdapter(arrayAdapter);
                    }
                }
            }
        });
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onChangeName() {

    }

    @Override
    public void onChangePower(int power) {

    }

    @Override
    public void onBleOpen() {
        InitState();
    }

    @Override
    public void onConnect() {
        Message msg=new Message();
        msg.what=HANDLER_CONNECTED;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        scanLeDevice(false);
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = mDeviceSearched.get(position);
        if (device == null) {
            Toast.makeText(SearchActivity.this, "未找到该设备", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchActivity.this);
        alertDialog.setTitle("是否连接该设备");
        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectDevice(device);
//                mHandler.postDelayed(runnableConnectTimeout, CONNECT_TIMEOUT);
            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialog.show();
    }

    private Runnable runnableConnectTimeout=new Runnable() {
        @Override
        public void run() {
            mBluetoothBleManager.destory();
            AlertDialog.Builder bluetoothEnableDialog = new AlertDialog.Builder(SearchActivity.this);
            bluetoothEnableDialog.setTitle("连接超时");
            bluetoothEnableDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearDevice();
                }
            });
            bluetoothEnableDialog.show();
        }
    };

    //连接设备
    private void connectDevice(BluetoothDevice device){
        mCurrentDevice=device;
        Toast.makeText(this, "正在连接设备", Toast.LENGTH_SHORT).show();
        mBluetoothBleManager.connect(device);
        mBluetoothBleManager.mDeviceName=device.getName();
        mBluetoothBleManager.mDeviceAddress=device.getAddress();
    }

    private void changeToMainActivity() {
        Log.e(TAG,"change to main activity");
        scanLeDevice(false);

        //切换到MainActivity
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(intent);
        Log.e(TAG, "change succeed");
        finish();
    }

    private void setLoadingState(boolean loading){
        if (mMenuItemLoading!=null){
            if (loading) {
                mMenuItemLoading.setActionView(R.layout.actionbar_search_loading);
                mMenuItemLoading.setVisible(true);
            }
            else {
                mMenuItemLoading.setVisible(false);
                mMenuItemLoading.setActionView(null);
            }
        }
    }

    private class SearchHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case HANDLER_CONNECTED:
                {
                    changeToMainActivity();
//                    mHandler.removeCallbacks(runnableConnectTimeout);
                }break;
            }

        }
    }
}
