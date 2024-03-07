package com.flexispot.ble.gui.view;


public interface OnBluethTableListener {

    void ConnectionState(boolean connection);

    void EquipmentState(int height, int alert, int remindmin, int sit, int station);

    void GetDeviceInfo(int maxheight, int minheight, int unit);

    void RemindParam(int sitmin, int standmin, int sitremindopen, int standremindopen);

    void ServiceNotFound();
}
