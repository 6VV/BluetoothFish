package com.lyyjy.zdhyjs.bluetoothfish;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Administrator on 2016/1/6.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    private final String TAG = "DatabaseManager";

    private static final String DATABASE_NAME = "BluetoothDevice";
    private static final int DATABASE_VERSION = 2;

    //设备表格
    private static final String DEVICE_TABLE_NAME = "DeviceTable";
    private static final String TABLE_DEVICE_ADDRESS = PersistentDataManager.TABLE_DEVICE_ADDRESS;
    private static final String TABLE_DEVICE_NAME = PersistentDataManager.TABLE_DEVICE_NAME;
    private static final String TABLE_DEVICE_PASSWORD = PersistentDataManager.TABLE_DEVICE_PASSWORD;
    private static final String TABLE_DEVICE_SAVE_PASSWORD =PersistentDataManager.TABLE_DEVICE_SAVE_PASSWORD;
    private static final String TABLE_DEVICE_AUTO_CONNECT =PersistentDataManager.TABLE_DEVICE_AUTO_CONNECT;

    //设置表格
    private static final String SETTINGS_TABLE_NAME="SettingsTable";
    private static final String TABLE_SETTINGS_CLOSE_BLUETOOTH=PersistentDataManager.TABLE_SETTINGS_CLOSE_BLUETOOTH;
    private static final String TABLE_SETTINGS_AUTO_RECONNECT=PersistentDataManager.TABLE_SETTINGS_AUTO_RECONNECT;
    private static final String TABLE_SETTINGS_SAVE_CONTROLER_LOCATION=PersistentDataManager.TABLE_SETTINGS_SAVE_CONTROLER_LOCATION;
    private static final String TABLE_SETTINGS_OPEN_WATER_WAVE=PersistentDataManager.TABLE_SETTINGS_OPEN_WATER_WAVE;

    //控件位置表格
    private static final String LOCATION_TABLE_NAME="LocationTable";
    private static final String TABLE_LOCATION_ID=PersistentDataManager.TABLE_LOCATION_ID;
    private static final String TABLE_LOCATION_LEFT=PersistentDataManager.TABLE_LOCATION_LEFT;
    private static final String TABLE_LOCATION_TOP=PersistentDataManager.TABLE_LOCATION_TOP;
    private static final String TABLE_LOCATION_RIGHT=PersistentDataManager.TABLE_LOCATION_RIGHT;
    private static final String TABLE_LOCATION_BOTTOM=PersistentDataManager.TABLE_LOCATION_BOTTOM;

    private final String CREATE_DEVICE_TABLE = "create table " + DEVICE_TABLE_NAME + "(" +
            TABLE_DEVICE_ADDRESS + " text," +
            TABLE_DEVICE_NAME + " text," +
            TABLE_DEVICE_PASSWORD + " text," +
            TABLE_DEVICE_SAVE_PASSWORD +" text," +
            TABLE_DEVICE_AUTO_CONNECT +" text)";

    private final String CREATE_SETTINGS_TABLE="create table "+SETTINGS_TABLE_NAME+"("+
            TABLE_SETTINGS_CLOSE_BLUETOOTH+" text," +
            TABLE_SETTINGS_AUTO_RECONNECT+" text," +
            TABLE_SETTINGS_SAVE_CONTROLER_LOCATION+" text," +
            TABLE_SETTINGS_OPEN_WATER_WAVE+" text)";

    private final String CREATE_LOCATION_TABLE="create table "+LOCATION_TABLE_NAME+"("+
            TABLE_LOCATION_ID+" integer,"+
            TABLE_LOCATION_LEFT+" integer,"+
            TABLE_LOCATION_TOP+" integer,"+
            TABLE_LOCATION_RIGHT+" integer,"+
            TABLE_LOCATION_BOTTOM+" integer)";

    public static DatabaseManager getInstance(Context context) {
        return new DatabaseManager(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DEVICE_TABLE);
        db.execSQL(CREATE_SETTINGS_TABLE);
        db.execSQL(CREATE_LOCATION_TABLE);
        Log.e(TAG, "create");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+DEVICE_TABLE_NAME);
        db.execSQL("drop table if exists "+SETTINGS_TABLE_NAME);
        db.execSQL("drop table if exists "+LOCATION_TABLE_NAME);
        onCreate(db);
    }

    //插入设置数据
    public void insertUpdateSettings(String closeBluetooth,String autoReconnect,String saveControlerLocation,String openWaterWave){
        SQLiteDatabase db=getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(TABLE_SETTINGS_CLOSE_BLUETOOTH,closeBluetooth);
        values.put(TABLE_SETTINGS_AUTO_RECONNECT,autoReconnect);
        values.put(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION,saveControlerLocation);
        values.put(TABLE_SETTINGS_OPEN_WATER_WAVE,openWaterWave);

        Cursor cursor = db.rawQuery("select * from " + SETTINGS_TABLE_NAME,null);
        //若已存在
        if (cursor.getCount() > 0) {
            db.update(SETTINGS_TABLE_NAME, values, null,null);
        } else {
            db.insert(SETTINGS_TABLE_NAME, null, values);
        }

        cursor.close();
        db.close();
    }

    //获取设置数据
    public Map<String,String> getSettings(){
        SQLiteDatabase db = getWritableDatabase();

        Map<String,String> settings=new HashMap<>();
        Cursor cursor = db.query(SETTINGS_TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                settings.put(TABLE_SETTINGS_CLOSE_BLUETOOTH, cursor.getString(cursor.getColumnIndex(TABLE_SETTINGS_CLOSE_BLUETOOTH)));
                settings.put(TABLE_SETTINGS_AUTO_RECONNECT, cursor.getString(cursor.getColumnIndex(TABLE_SETTINGS_AUTO_RECONNECT)));
                settings.put(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION, cursor.getString(cursor.getColumnIndex(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION)));
                settings.put(TABLE_SETTINGS_OPEN_WATER_WAVE, cursor.getString(cursor.getColumnIndex(TABLE_SETTINGS_OPEN_WATER_WAVE)));
            }
        }
        else {
            ContentValues values=new ContentValues();
            values.put(TABLE_SETTINGS_CLOSE_BLUETOOTH,PersistentDataManager.UNCHECK);
            values.put(TABLE_SETTINGS_AUTO_RECONNECT,PersistentDataManager.CHECK);
            values.put(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION,PersistentDataManager.UNCHECK);
            values.put(TABLE_SETTINGS_OPEN_WATER_WAVE,PersistentDataManager.UNCHECK);

            db.insert(SETTINGS_TABLE_NAME, null, values);

            settings.put(TABLE_SETTINGS_CLOSE_BLUETOOTH, PersistentDataManager.UNCHECK);
            settings.put(TABLE_SETTINGS_AUTO_RECONNECT, PersistentDataManager.CHECK);   //默认为自动连接
            settings.put(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION, PersistentDataManager.UNCHECK);
            settings.put(TABLE_SETTINGS_OPEN_WATER_WAVE, PersistentDataManager.UNCHECK);
        }

        cursor.close();
        db.close();

        return settings;
    }

    public void insertUpdateDevice(String address, String name, String password,String savePassword,String autoConnect) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABLE_DEVICE_ADDRESS, address);
        values.put(TABLE_DEVICE_NAME, name);
        values.put(TABLE_DEVICE_PASSWORD, password);
        values.put(TABLE_DEVICE_SAVE_PASSWORD,savePassword);
        values.put(TABLE_DEVICE_AUTO_CONNECT,autoConnect);

        Cursor cursor = db.rawQuery("select * from " + DEVICE_TABLE_NAME + " where " + TABLE_DEVICE_ADDRESS + " = ?", new String[]{address});
        //若已存在
        if (cursor.getCount() > 0) {
            db.update(DEVICE_TABLE_NAME, values, TABLE_DEVICE_ADDRESS + " = ?", new String[]{address});
        } else {
            db.insert(DEVICE_TABLE_NAME, null, values);
        }

        cursor.close();
        db.close();
    }

    //测试用，显示所有存储的设备信息
    public void selectDevices() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(DEVICE_TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String address = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_ADDRESS));
                String name = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_NAME));
                String passwrod = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_PASSWORD));
                String saved = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_SAVE_PASSWORD));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public Map<String,String> selectDevice(String address) {
        Map<String,String> deviceInformation = new HashMap<>();

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from " + DEVICE_TABLE_NAME + " where " + TABLE_DEVICE_ADDRESS + " = ?", new String[]{address});
        //若查询结果大于0
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_NAME));
                String passwrod = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_PASSWORD));
                String savePassword = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_SAVE_PASSWORD));
                String autoConnect = cursor.getString(cursor.getColumnIndex(TABLE_DEVICE_AUTO_CONNECT));
                deviceInformation.put(TABLE_DEVICE_ADDRESS, address);
                deviceInformation.put(TABLE_DEVICE_NAME, name);
                deviceInformation.put(TABLE_DEVICE_PASSWORD, passwrod);
                deviceInformation.put(TABLE_DEVICE_SAVE_PASSWORD, savePassword);
                deviceInformation.put(TABLE_DEVICE_AUTO_CONNECT, autoConnect);
            }
        }

        cursor.close();
        db.close();

        return deviceInformation;
    }

    public void deleteDevice(String address) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + DEVICE_TABLE_NAME + " where " + TABLE_DEVICE_ADDRESS + " = " + address);
        db.close();
    }

    public void updateDeviceSavePassword(String address, String savePassword){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(TABLE_DEVICE_SAVE_PASSWORD, savePassword);
        db.update(DEVICE_TABLE_NAME, values, TABLE_DEVICE_ADDRESS + " = ?", new String[]{address});
        db.close();
    }

    public void updateDeviceAutoConnect(String address,String autoConnect){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(TABLE_DEVICE_AUTO_CONNECT, autoConnect);
        db.update(DEVICE_TABLE_NAME, values, TABLE_DEVICE_ADDRESS + " = ?", new String[]{address});
        db.close();
    }

    public void updateSettingsCloseBluetooth(String closeBluetooth){
        SQLiteDatabase db=getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from " + SETTINGS_TABLE_NAME, null);
        ContentValues values=new ContentValues();
        values.put(TABLE_SETTINGS_CLOSE_BLUETOOTH, closeBluetooth);

        if (cursor.getCount()>0){
            db.update(SETTINGS_TABLE_NAME,values,null,null);
        }
        else {
//            values.put(TABLE_SETTINGS_AUTO_RECONNECT,PersistentDataManager.UNCHECK);
            db.insert(SETTINGS_TABLE_NAME, null, values);
        }

        db.close();
    }

    public void updateSettingsAutoReconnect(String autoReconnect){
        SQLiteDatabase db=getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from " + SETTINGS_TABLE_NAME, null);
        ContentValues values=new ContentValues();
        values.put(TABLE_SETTINGS_AUTO_RECONNECT, autoReconnect);

        if (cursor.getCount()>0){
            Log.e(TAG,"reconnect >0");
            db.update(SETTINGS_TABLE_NAME,values,null,null);
        }
        else {
            Log.e(TAG,"reconnect");
//            values.put(TABLE_SETTINGS_CLOSE_BLUETOOTH,PersistentDataManager.UNCHECK);
            db.insert(SETTINGS_TABLE_NAME, null, values);
        }

        db.close();
    }

    public void updateSaveControlerLocation(String saveLocation){
        SQLiteDatabase db=getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from " + SETTINGS_TABLE_NAME, null);
        ContentValues values=new ContentValues();
        values.put(TABLE_SETTINGS_SAVE_CONTROLER_LOCATION, saveLocation);

        if (cursor.getCount()>0){
            db.update(SETTINGS_TABLE_NAME,values,null,null);
        }
        else {
            db.insert(SETTINGS_TABLE_NAME, null, values);
        }

        db.close();
    }

    public void updateOpenWaterWave(String openWaterWave){
        SQLiteDatabase db=getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from " + SETTINGS_TABLE_NAME, null);
        ContentValues values=new ContentValues();
        values.put(TABLE_SETTINGS_OPEN_WATER_WAVE, openWaterWave);

        if (cursor.getCount()>0){
            db.update(SETTINGS_TABLE_NAME,values,null,null);
        }
        else {
            db.insert(SETTINGS_TABLE_NAME, null, values);
        }

        db.close();
    }


    public void insertUpdateLocation(int id,int left,int top,int right,int bottom){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABLE_LOCATION_ID, id);
        values.put(TABLE_LOCATION_LEFT, left);
        values.put(TABLE_LOCATION_TOP, top);
        values.put(TABLE_LOCATION_RIGHT,right);
        values.put(TABLE_LOCATION_BOTTOM,bottom);

        Cursor cursor = db.rawQuery("select * from " + LOCATION_TABLE_NAME + " where " + TABLE_LOCATION_ID + " = ?", new String[]{String.valueOf(id)});
        //若已存在
        if (cursor.getCount() > 0) {
            db.update(LOCATION_TABLE_NAME, values, TABLE_LOCATION_ID + " = ?", new String[]{String.valueOf(id)});
        } else {
            db.insert(LOCATION_TABLE_NAME, null, values);
        }

        cursor.close();
        db.close();
    }

    public Map<Integer,Vector<Integer>> selectLocation(){
        Map<Integer,Vector<Integer>> controlerLocation = new HashMap<>();

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from " + LOCATION_TABLE_NAME, null);
        //若查询结果大于0
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    Integer id = cursor.getInt(cursor.getColumnIndex(TABLE_LOCATION_ID));
                    Integer left = cursor.getInt(cursor.getColumnIndex(TABLE_LOCATION_LEFT));
                    Integer top = cursor.getInt(cursor.getColumnIndex(TABLE_LOCATION_TOP));
                    Integer right = cursor.getInt(cursor.getColumnIndex(TABLE_LOCATION_RIGHT));
                    Integer bottom = cursor.getInt(cursor.getColumnIndex(TABLE_LOCATION_BOTTOM));

                    Vector<Integer> location = new Vector<Integer>();
                    location.add(left);
                    location.add(top);
                    location.add(right);
                    location.add(bottom);

                    controlerLocation.put(id, location);
                }while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();

        return controlerLocation;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.e(TAG, "destroy");
        this.close();
        super.finalize();
    }
}
