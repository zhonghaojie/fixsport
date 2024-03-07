package com.flexispot.ble.gui.view;

import android.bluetooth.BluetoothAdapter;

public class NewBluetooth  {




    /**
     * 当前 Android 设备是否支持 Bluetooth
     *
     * @return true：支持 Bluetooth false：不支持 Bluetooth
     */
    public boolean isBluetoothSupported() {

            return  BluetoothAdapter.getDefaultAdapter() != null;

    }
    /**
     * 当前 Android 设备的 bluetooth 是否已经开启
     *
     * @return true：Bluetooth 已经开启 false：Bluetooth 未开启
     */
    public boolean isBluetoothEnabled(){
        BluetoothAdapter   bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        return bluetoothAdapter.isEnabled();

    }


    /**
     * 强制开启当前 Android 设备的 Bluetooth
     *
     * @return true：强制打开 Bluetooth　成功　false：强制打开 Bluetooth 失败
     */

    public boolean turnOnBluetooth(){
        BluetoothAdapter  bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        return bluetoothAdapter.enable();
    }



}
