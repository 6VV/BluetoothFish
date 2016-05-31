package com.lyyjy.zdhyjs.bluetoothfish;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BleInterface;
import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BluetoothBleManager;
import com.lyyjy.zdhyjs.bluetoothfish.LightColor.LightColorManager;
import com.lyyjy.zdhyjs.bluetoothfish.Speed.SpeedManager;
import com.lyyjy.zdhyjs.bluetoothfish.Speed.SpeedView;
import com.lyyjy.zdhyjs.bluetoothfish.SwimMode.SwimModeManager;
import com.lyyjy.zdhyjs.bluetoothfish.SwimMode.ViewBackground;
import com.lyyjy.zdhyjs.bluetoothfish.View.BatteryView;
import com.lyyjy.zdhyjs.bluetoothfish.View.FishView;
import com.lyyjy.zdhyjs.bluetoothfish.View.RudderView;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, BleInterface {
    private final String TAG = "MainActivity";

    //蓝牙相关
    private BluetoothBleManager mBluetoothBleManager;   //蓝牙管理器

    //蓝牙相关事件
    public MyHandler mHandler;
    public final int HANDLER_CONNECT_STATE = 1;    //蓝牙连接状态改变
    private final int HANDLER_FISH_SWIM = 2; //小鱼游动
    private final int HANDLER_INIT_STATE = 3; //初始化控件状态
    private final int HANDLER_CHANGE_POWER=4;   //刷新电量

    //布局
    private ViewBackground mViewBackground; //背景
    private FrameLayout mFishBackground;    //鱼背景
    private FrameLayout mFrameLayout;   //前景
    private RelativeLayout mLayoutMain; //主布局

    //Actionbar控件
    private MenuItem mMenuItemBattery;
    private MenuItem mMenuItemBluetooth;
    private MenuItem mMenuItemConnect;
    private MenuItem mMenuItemSensor;
    private MenuItem mMenuItemMoveWidget;

    //控制按钮
    private Vector<Integer> mVecControlerID = new Vector<Integer>();
    private SpeedManager mSpeedManager;

    private RudderView viewRubber;
    private int mFishDirection;

    //屏幕大小
    private int mScreenWidth;
    private int mScreenHeight;

    //按下时是否为控制小鱼
    private boolean mIsToMoveFish = true;

    private boolean mIsSensor = false;

    //控件上一个位置
    private int mActionBarHeight;
    private int mLastX;
    private int mLastY;

    //广播接收器
    private IntentFilter mIntentFilter;
    private BluetoothChangeReceiver mBluetoothChangeReceiver;

    //鱼动画
    private Vector<FishView> mFishs = new Vector<FishView>();
    private Vector<AnimationDrawable> mFishAnimations = new Vector<AnimationDrawable>();

    private Random mRandom = new Random();

    //电量
    private BatteryView mBatteryView;

    //游动控制
    private SwimModeManager mSwimModeManager;

    //发送控制
    private boolean mBeginSendMessage = false;

    //传感器管理器
    private SensorManager m_sensorManager;
    private MySensorEventListener m_senserListener = new MySensorEventListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        //背景
        mViewBackground = new ViewBackground(this);
        mViewBackground = new ViewBackground(this);
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.addView(mViewBackground);

        //鱼背景
        mFishBackground = new FrameLayout(this);
        mFrameLayout.addView(mFishBackground);

        //控件布局
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutMain = (RelativeLayout) inflater.inflate(R.layout.activity_main, null);
        mLayoutMain.post(runableLayout);
        mFrameLayout.addView(mLayoutMain, layoutParams);
        setContentView(mFrameLayout);
        mFrameLayout.setSystemUiVisibility(1);

        viewRubber = (RudderView) findViewById(R.id.viewRudder);

        initSpeedManager();

        viewRubber.setOnTouchListener(this);

        mVecControlerID.add(R.id.viewRudder);
        mVecControlerID.add(R.id.viewFishSpeed);
        mVecControlerID.add(R.id.btnFishAccelerate);
        mVecControlerID.add(R.id.btnFishDecelerate);

        mBatteryView = new BatteryView(this);
        //设置BluetoothBleManager相关参数
        mBluetoothBleManager = BluetoothBleManager.GetInstance(this);

        mHandler = new MyHandler();

        //初始化广播接收器
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBluetoothChangeReceiver = new BluetoothChangeReceiver();

        //方向传感器
        m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //获取屏幕大小
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        //初始化小鱼
        InitFishs();
    }

    private void initSpeedManager() {
        SpeedView viewFishSpeed = (SpeedView) findViewById(R.id.viewFishSpeed);
        ImageButton btnIncreaseSpeed = (ImageButton) findViewById(R.id.btnFishAccelerate);
        ImageButton btnDecreaseSpeed = (ImageButton) findViewById(R.id.btnFishDecelerate);

        viewFishSpeed.setOnTouchListener(this);
        btnIncreaseSpeed.setOnTouchListener(this);
        btnDecreaseSpeed.setOnTouchListener(this);

        mSpeedManager = new SpeedManager(this, viewFishSpeed, btnIncreaseSpeed, btnDecreaseSpeed);
    }

    private Runnable runableLayout = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = HANDLER_INIT_STATE;
            mHandler.sendMessage(msg);
        }
    };

    private void InitFishs() {
        //添加小鱼
        for (int i = 0; i < 2; ++i) {
            FishView ivFish = new FishView(this);
            ivFish.setImageResource(R.drawable.yellow_fish);
            ivFish.setScaleToSizeOfScreen(4);
            AnimationDrawable fishAnimation = (AnimationDrawable) ivFish.getDrawable();
            mFishs.add(ivFish);
            mFishAnimations.add(fishAnimation);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mFishBackground.addView(ivFish, layoutParams);
        }

        for (int i = 0; i < 3; ++i) {
            FishView ivFish = new FishView(this);
            ivFish.setImageResource(R.drawable.ink_fish);
            ivFish.setScaleToSizeOfScreen(8);
            AnimationDrawable fishAnimation = (AnimationDrawable) ivFish.getDrawable();
            mFishs.add(ivFish);
            mFishAnimations.add(fishAnimation);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mFishBackground.addView(ivFish, layoutParams);
        }
        mFishs.get(0).post(runnableResizeFish);
    }

    Runnable runnableResizeFish = new Runnable() {
        @Override
        public void run() {
            for (FishView fish :
                    mFishs) {
                float scale = (float) mScreenHeight / fish.getHeight() / fish.getScaleToSizeOfScreen();
                fish.setScaleX(scale);
                fish.setScaleY(scale);
            }
        }
    };

    private void InitFishState(FishView fish) {
        int x = mRandom.nextInt(mScreenWidth);
        int y = mRandom.nextInt(mScreenHeight);
        fish.layout(x, y, fish.getWidth() + x, fish.getHeight() + y);
        fish.setRotation(mRandom.nextFloat() * 180);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setFishTargetLocation(event.getX(), event.getY());
        mViewBackground.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void setFishTargetLocation(float x, float y) {
        for (FishView fish :
                mFishs) {
            fish.setTargetLocation((int) x, (int) y);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //注册蓝牙状态监听器
        registerReceiver(mBluetoothChangeReceiver, mIntentFilter);

        //注册重力传感器
        Sensor accelerometerSensor = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sensorManager.registerListener(m_senserListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        beginSwim();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothBleManager.setBleInterface(this);
        mBluetoothBleManager.setCurrentContext(this);
        invalidateOptionsMenu();

        mBeginSendMessage = true;
        new Thread(runnableFishControl).start();

        //获取actionbar高度
        TypedArray actionbarSizeTypedArray = this.obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        mActionBarHeight = (int) actionbarSizeTypedArray.getDimension(0, 0);

        //开启水波效果
        mViewBackground.startRipple();
        mSpeedManager.startWave();
    }

    @Override
    protected void onPause() {
        mBeginSendMessage = false;

        //停止水波效果
        mViewBackground.stopRipple();
        mSpeedManager.stopWave();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        m_sensorManager.unregisterListener(m_senserListener);
        unregisterReceiver(mBluetoothChangeReceiver);

        stopSwim();
    }

    private void saveControlerLocation() {
        PersistentDataManager persistentDataManager = PersistentDataManager.getInstance(this);
        for (Integer id :
                mVecControlerID) {
            View view = findViewById(id);
            persistentDataManager.insertUpdateLocation(id, view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
    }

    private void loadControlerLacation() {
        Map<Integer, Vector<Integer>> map = PersistentDataManager.getInstance(this).selectLocation();
        if (map.size() == 0) {
            return;
        }
        for (Integer id :
                mVecControlerID) {
            Vector<Integer> vec = map.get(id);
            findViewById(id).layout(vec.get(0), vec.get(1), vec.get(2), vec.get(3));
        }
    }

    @Override
    protected void onDestroy() {
//        mViewBackground.stop();
        mBluetoothBleManager.destory();
        //若设置为退出时关闭蓝牙
        if (PersistentDataManager.getInstance(this).mIsCloseBluetooth) {
            mBluetoothBleManager.closeBluetooth();
        }
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuItemBattery = menu.findItem(R.id.action_battery);
        mMenuItemBluetooth = menu.findItem(R.id.action_bluetooth);
        mMenuItemConnect = menu.findItem(R.id.action_connect);
        mMenuItemSensor = menu.findItem(R.id.action_sensor);
        mMenuItemMoveWidget = menu.findItem(R.id.action_moveWidget);
        initBatteryView();

        //游动控制
        mSwimModeManager = new SwimModeManager(this, menu.findItem(R.id.action_swim_mode));
        return true;
    }

    private void initBatteryView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.actionbar_battery, null);
        mBatteryView = (BatteryView) layout.findViewById(R.id.viewBattery);
        mBatteryView.resizeWidth(144);
        mMenuItemBattery.setActionView(layout);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //检查蓝牙状态
        if (mBluetoothBleManager.isBleEnabled()) {
            mMenuItemBluetooth.setIcon(R.mipmap.bluetooth_enable);
        } else {
            mMenuItemBluetooth.setIcon(R.mipmap.bluetooth_disabled);
        }

        //检查是否连接
        if (BluetoothBleManager.mConnectionState == BluetoothBleManager.STATE_CONNECTED) {
            mMenuItemConnect.setIcon(R.mipmap.connected);
        } else {
            mMenuItemConnect.setIcon(R.mipmap.disconnected);
        }

        //检查操作方式
        if (mIsSensor) {
            mMenuItemSensor.setIcon(R.mipmap.sensor);
        } else {
            mMenuItemSensor.setIcon(R.mipmap.manual);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void startSearch() {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth: {
                controlBluetooth();
            }
            break;
            case R.id.action_connect: {
                controlConnect();
            }
            break;
            case R.id.action_search: {
                startSearch();
            }
            break;
            case R.id.action_setting: {
                changeSetting();
            }
            break;
            case R.id.action_information: {
                showInformation();
            }
            break;
            case R.id.action_sensor: {
                changeControlMethod();
            }
            break;
            case R.id.action_moveWidget: {
                changeMoveSymbol();
            }
            break;
            case R.id.action_startRaining: {
                if (mViewBackground.isRaining()) {
                    mViewBackground.stopRain();
                } else {
                    mViewBackground.startRain();
                }
            }
            break;
            case R.id.action_swim_mode: {
                mSwimModeManager.changeSwimMode();
            }
            break;
            case R.id.action_select_light: {
                new LightColorManager(this).selectDialog();
            }
            break;
            case android.R.id.home: {
                exit();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    void changeControlMethod() {
        //若之前为重力感应
        if (mIsSensor) {
            mMenuItemSensor.setIcon(R.mipmap.manual);
            viewRubber.setVisibility(View.VISIBLE);
            mIsSensor = false;
        } else {
            mMenuItemSensor.setIcon(R.mipmap.sensor);
            viewRubber.setVisibility(View.INVISIBLE);
            mIsSensor = true;
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mFishUpTouchDown = false;
        mFishLeftTouchDown = false;
        mFishRightTouchDown = false;
        touchDown = false;
    }

    void changeMoveSymbol() {
        if (mIsToMoveFish) {
            mMenuItemMoveWidget.setTitle("锁定控件");
        } else {
            mMenuItemMoveWidget.setTitle("拖动控件");
        }
        mIsToMoveFish = !mIsToMoveFish;
    }

    //overflow中显示图片
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    private void changeSetting() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showInformation() {
        //若当前未连接任何设备
        if (BluetoothBleManager.mConnectionState == BluetoothBleManager.STATE_DISCONNECT) {
            Toast.makeText(this, "未连接设备", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("设备信息");
            Log.e(TAG, "device name:" + BluetoothBleManager.mDeviceName);
            alertDialog.setMessage("设备名称：" + BluetoothBleManager.mDeviceName + '\n' + "设备地址：" + BluetoothBleManager.mDeviceAddress);
            alertDialog.setPositiveButton("确定", null);
            alertDialog.show();
        }
    }

    private void controlConnect() {
        if (mBluetoothBleManager.mBluetoothDevice == null) {
            Toast.makeText(this, "请先选择一个设备", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            if (mBluetoothBleManager.mConnectionState == mBluetoothBleManager.STATE_CONNECTED) {
                alertDialog.setTitle("是否断开连接");
                alertDialog.setMessage("手动断开连接后，不会自动重连");
                alertDialog.setNegativeButton("取消", null);
                alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothBleManager.destory();
                        mMenuItemConnect.setIcon(R.mipmap.disconnected);
                    }
                });
                alertDialog.show();
            } else {
                alertDialog.setTitle("是否连接");
                alertDialog.setNegativeButton("取消", null);
                alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothBleManager.connect(mBluetoothBleManager.mBluetoothDevice);
                    }
                });
                alertDialog.show();
            }
        }
    }

    private void controlBluetooth() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        if (mBluetoothBleManager.isBleEnabled()) {
            alertDialog.setTitle("是否关闭蓝牙");
            alertDialog.setNegativeButton("取消", null);
            alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBluetoothBleManager.closeBluetooth();
                }
            });
            alertDialog.show();
        } else {
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

    private boolean touchDown = false;  //是否有按键按下
    private final int mPressInterval = 100; //按键按下事件间隔

    private boolean mFishUpTouchDown = false; //前进按钮按下
    private boolean mFishLeftTouchDown = false;   //左移按钮按下
    private boolean mFishRightTouchDown = false;  //右移按钮按下

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //若按键时为控制小鱼
        if (mIsToMoveFish) {
            onTouchToControlFish(v, event);
        }
        //若按键时为拖动控件
        else {
            onTouchToMoveControlers(v, event);
        }
        return false;
    }

    private void onTouchToControlFish(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.viewRudder: {
                if (mIsSensor) {
                    return;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE: {
                        fishControl();
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        touchDown = false;
                    }
                    break;
                }
            }
            break;
            case R.id.btnFishAccelerate: {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mSpeedManager.fishAccelerate();
                }
            }
            break;
            case R.id.btnFishDecelerate: {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mSpeedManager.fishDeaccelerate();
                }
            }
            break;
            default:
                break;
        }
    }

    private void onTouchToMoveControlers(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                int dx = (int) event.getRawX() - mLastX;
                int dy = (int) event.getRawY() - mLastY;

                int left = v.getLeft() + dx;
                int top = v.getTop() + dy;
                int right = v.getRight() + dx;
                int bottom = v.getBottom() + dy;

                if (left < 0) {
                    left = 0;
                    right = left + v.getWidth();
                }
                if (right > mScreenWidth) {
                    right = mScreenWidth;
                    left = right - v.getWidth();
                }
                if (top < mActionBarHeight) {
                    top = mActionBarHeight;
                    bottom = top + v.getHeight();
                }
                if (bottom > mScreenHeight) {
                    bottom = mScreenHeight;
                    top = bottom - v.getHeight();
                }
                v.layout(left, top, right, bottom);
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (PersistentDataManager.getInstance(this).mIsSaveControlerLocation) {
                    saveControlerLocation();
                }
            }
            break;
        }
    }

    private void fishControl() {
        if (BluetoothBleManager.mConnectionState != BluetoothBleManager.STATE_CONNECTED) {
            touchDown = false;
            return;
        }

        touchDown = true;

        if (!mIsSensor) {
            mFishDirection = viewRubber.getFishDirection();
        }

        switch (mFishDirection) {
            case RudderView.FISH_UP: {
                CommandCode.setDirection(CommandCode.FISH_UP);
            }
            break;
            case RudderView.FISH_LEFT: {
                CommandCode.setDirection(CommandCode.FISH_LEFT);
            }
            break;
            case RudderView.FISH_RIGHT: {
                CommandCode.setDirection(CommandCode.FISH_RIGHT);
            }
            break;
            case RudderView.FISH_STOP: {
                CommandCode.setDirection(CommandCode.FISH_STOP);
            }
            break;
        }
    }

    private Runnable runnableFishControl = new Runnable() {
        @Override
        public void run() {
            while (mBeginSendMessage) {
                handlerFishControl.sendEmptyMessage(0);
                try {
                    Thread.sleep(mPressInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler handlerFishControl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (touchDown) {
                Log.e(TAG, String.valueOf(mFishDirection));
                mBluetoothBleManager.writeDataToDevice(CommandCode.FISH_COMMAND);
            }
        }
    };

    @Override
    public void onBleOpen() {
    }

    @Override
    public void onConnect() {
        Log.e(TAG, "connected");
        Message msg = new Message();
        msg.what = HANDLER_CONNECT_STATE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onBleScan(BluetoothDevice device) {
    }

    @Override
    public void onDisconnect() {
//        Log.e(TAG,"disconnect");
        Message msg = new Message();
        msg.what = HANDLER_CONNECT_STATE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onChangeName() {

    }

    @Override
    public void onChangePower(int power) {
        Message msg=new Message();
        msg.what=HANDLER_CHANGE_POWER;
        msg.arg1=power;
        mHandler.sendMessage(msg);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_CONNECT_STATE: {
                    if (BluetoothBleManager.mConnectionState == BluetoothBleManager.STATE_CONNECTED) {
                        mMenuItemConnect.setIcon(R.mipmap.connected);
                    } else {
                        mMenuItemConnect.setIcon(R.mipmap.disconnected);
                    }
                }
                break;
                case HANDLER_FISH_SWIM: {
                    for (FishView fish :
                            mFishs) {
                        int[] vec = fish.getNextPosition();
                        int nextX = vec[0];
                        int nextY = vec[1];
                        fish.layout(nextX, nextY, nextX + fish.getWidth(), nextY + fish.getHeight());
                        viewRubber.updateState();
                    }
                }
                break;
                case HANDLER_INIT_STATE: {
                    //初始化控件位置
                    loadControlerLacation();

                    //初始化小鱼状态
                    for (FishView fish :
                            mFishs) {
                        InitFishState(fish);
                    }
                }
                break;
                case HANDLER_CHANGE_POWER:{
                    mBatteryView.setPower(msg.arg1);
                }break;
            }
            super.handleMessage(msg);
        }
    }

    private class BluetoothChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBluetoothBleManager.isBleEnabled()) {
                mMenuItemBluetooth.setIcon(R.mipmap.bluetooth_enable);
            } else {
                mMenuItemBluetooth.setIcon(R.mipmap.bluetooth_disabled);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long mClickTime = 0;  //上次按下返回键的时间
    private final int CLICK_INTERVAL = 3000;  //按键间隔（ms）

    private void exit() {
        if (System.currentTimeMillis() - mClickTime > CLICK_INTERVAL) {
            Toast.makeText(MainActivity.this, "再次按下后退键后退出程序", Toast.LENGTH_SHORT).show();
            mClickTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private boolean mIsSwiming = false;

    private void beginSwim() {
        for (AnimationDrawable animation :
                mFishAnimations) {
            animation.start();
        }

        mIsSwiming = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsSwiming) {
                    Message msg = new Message();
                    msg.what = HANDLER_FISH_SWIM;
                    mHandler.sendMessage(msg);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void stopSwim() {
        mIsSwiming = false;
        for (AnimationDrawable animation :
                mFishAnimations) {
            animation.stop();
        }
    }

    final int X_START_VALUE = 3;
    final int Y_START_VALUE = 3;

    //传感器监听器
    private final class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //若为按键控制
            if (!mIsSensor) {
                return;
            }
            //可以得到传感器实时测量出来的变化值
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];

                //若手机Y轴向下
                if (y < -Y_START_VALUE) {
                    mFishDirection = RudderView.FISH_LEFT;
                    fishControl();
                } else if (y > Y_START_VALUE) {
                    mFishDirection = RudderView.FISH_RIGHT;
                    fishControl();
                } else if (x < -X_START_VALUE) {
                    mFishDirection = RudderView.FISH_UP;
                    fishControl();
                } else {
                    touchDown = false;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
