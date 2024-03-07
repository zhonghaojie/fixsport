package com.koylo.ble.flexispot;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParsedAd implements Serializable {
    public byte flags;
    public List<UUID> uuids = new ArrayList<>();
    public String localName;
    public short manufacturer;
    public byte[] custom;
    public String dec;
    public BluetoothDevice device;

    public boolean isBuleTable() {
        if (manufacturer != 0) {
            return false;
        }
        if (custom == null || custom.length != 2)
            return false;
        if (custom[0] != 0 || custom[1] != 1)
            return false;
        return true;
    }
}
