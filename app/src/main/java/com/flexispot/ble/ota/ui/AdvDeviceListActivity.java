package com.flexispot.ble.ota.ui;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flexispot.ble.R;
import com.flexispot.ble.ota.adapter.DeviceListAdapter;
import com.flexispot.ble.ota.ble.AdvDevice;
import com.flexispot.ble.ota.util.Arrays;
import com.flexispot.ble.ota.util.TelinkLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 广播设备列表页面
 * Created by Administrator on 2017/2/20.
 */
public class AdvDeviceListActivity extends BaseActivity {

    private ListView lv_devices;
    private DeviceListAdapter mListAdapter;
    private List<AdvDevice> mDeviceList = new ArrayList<>();
    private final Handler mScanHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter;
    private final static long SCAN_PERIOD = 10 * 1000;
    private boolean mScanning = false;
    private static final int REQUEST_DETAIL = 1;
    private ImageView iv_back;

    private TextView menu_scan;
    private TextView menu_stop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adv_device_list);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("AdvDevices");
        }

        menu_scan=findViewById(R.id.menu_scan);
        menu_stop=findViewById(R.id.menu_stop);
        iv_back=findViewById(R.id.iv_back);
        lv_devices = (ListView) findViewById(R.id.lv_devices);
        mListAdapter = new DeviceListAdapter(this, mDeviceList);
        lv_devices.setAdapter(mListAdapter);
        lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivityForResult(new Intent(AdvDeviceListActivity.this, DeviceDetailActivity.class)
                        .putExtra("device", mDeviceList.get(position)), REQUEST_DETAIL);
                if (mScanning) {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(scanCallback);
                }
            }
        });

        if (!isSupport(getApplicationContext())) {
            Toast.makeText(this, "ble not support", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
//        scanToggle(true);

        Click();
        itemClean();
        isStoragePermissionGranted();
    }
    private void Click(){
        menu_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanToggle(false);
                itemClean();
            }
        });
        menu_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanToggle(true);
                itemClean();
            }
        });
     ;
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              finish();
            }
        });
    }

    private void itemClean(){

        if (!mScanning) {
            menu_stop.setVisibility(View.GONE);
            menu_scan.setVisibility(View.VISIBLE);

        } else {
            menu_stop.setVisibility(View.VISIBLE);
            menu_scan.setVisibility(View.GONE);


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DETAIL && resultCode == RESULT_OK){
            scanToggle(true);
        }
    }

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            TelinkLog.w("scan:" + device.getName() + " mac:" + device.getAddress() + " rssi:" + rssi + " record:  " + Arrays.bytesToHexString(scanRecord, ":"));
            for (final AdvDevice advDevice : mDeviceList) {
                if (device.getAddress().equals(advDevice.device.getAddress())) {
                    if (advDevice.rssi != rssi) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                advDevice.rssi = rssi;
                                mListAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                    return;
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceList.add(new AdvDevice(device, rssi, scanRecord));
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        checkBleState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.stopLeScan(scanCallback);
        }
    }

    private void checkBleState() {
        if (this.mBluetoothAdapter != null
                && !this.mBluetoothAdapter.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("开启蓝牙，体验智能灯!");
            builder.setCancelable(false);
            builder.setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enable(getApplicationContext());
                }
            });
            builder.show();
        }
    }

    public boolean isSupport(Context context) {
        return this.getAdapter(context) != null;
    }

    public boolean enable(Context context) {
        BluetoothAdapter mAdapter = getAdapter(context);
        if (mAdapter == null)
            return false;
        if (mAdapter.isEnabled())
            return true;
        return mBluetoothAdapter.enable();
    }

    public BluetoothAdapter getAdapter(Context context) {
        synchronized (this) {
            if (mBluetoothAdapter == null) {
                BluetoothManager manager = (BluetoothManager) context
                        .getSystemService(Context.BLUETOOTH_SERVICE);
                this.mBluetoothAdapter = manager.getAdapter();
            }
        }

        return this.mBluetoothAdapter;
    }

    private Runnable scanTask = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                TelinkLog.i("ADV#stopScan");
                mScanning = false;
                mBluetoothAdapter.stopLeScan(scanCallback);
                invalidateOptionsMenu();
            }
        }
    };

    private int scanDelay = 0 * 1000;

    private void scanToggle(final boolean enable) {
        mScanHandler.removeCallbacks(scanTask);
        if (enable) {
            TelinkLog.i("ADV#startScan");
            mScanning = true;
            mDeviceList.clear();
            mListAdapter.notifyDataSetChanged();
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.startLeScan(scanCallback);
                    mScanHandler.postDelayed(scanTask, SCAN_PERIOD);
                }
            }, scanDelay);
        } else {
            TelinkLog.i("ADV#scanToggle#stopScan");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(scanCallback);
        }
        invalidateOptionsMenu();
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {

            int readPermissionCheck = ContextCompat.checkSelfPermission(AdvDeviceListActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermissionCheck = ContextCompat.checkSelfPermission(AdvDeviceListActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (readPermissionCheck == PackageManager.PERMISSION_GRANTED
                    && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                Log.v("juno", "Permission is granted");
                return true;
            } else {
                Log.v("juno", "Permission is revoked");
                ActivityCompat.requestPermissions(AdvDeviceListActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }


    public void refresh(View view) {
//        scanToggle(true);
        BluetoothManager manager = (BluetoothManager) this
                .getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
        Toast.makeText(this, "当前连接设备个数" + devices.size(), Toast.LENGTH_SHORT).show();

    }
}
