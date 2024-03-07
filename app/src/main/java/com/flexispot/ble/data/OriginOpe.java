package com.flexispot.ble.data;

import com.flexispot.ble.gui.view.OnBluethTableListener;

public class OriginOpe {

    private OnBluethTableListener listener;

    public OriginOpe(OnBluethTableListener listener) {
        this.listener = listener;
    }

    public void opeData(byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(
                    data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format(
                        "%02X ", byteChar));
            }
            System.out.println("----------------------->" + stringBuilder.toString());
        }
//            Log.e("broadcastUpdate", "data=" + data[0]);
        if (data != null && data.length == 20) {
            if (data[0] == 0x00 && data[19] == ~data[0]) {
                //设备回复实时数据
                int height = (int) (((data[1] & 0xff) << 8) | (data[2] & 0xff));
                int alert = (int) (((data[3] & 0xff) << 8) | (data[4] & 0xff));
                int remindmin = (int) data[5];
                int sit = (int) (((data[6] & 0xff) << 8) | (data[7] & 0xff));
                int station = (int) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
                if (listener != null)
                    listener.EquipmentState(height, alert, remindmin, sit, station);
            } else if (data[0] == 0x01 && data[19] == ~data[0]) {
                //设备信息
                int maxheight = (int) (((data[1] & 0xff) << 8) | (data[2] & 0xff));
                int minheight = (int) (((data[3] & 0xff) << 8) | (data[4] & 0xff));
                int unit = (int) data[5];
                if (listener != null)
                    listener.GetDeviceInfo(maxheight, minheight, unit);
            } else if (data[0] == 0x04 && data[19] == ~data[0]) {
                //获取设备坐站设置
                int sitmin = (int) (((data[1] & 0xff) << 8) | (data[2] & 0xff));
                int standmin = (int) (((data[3] & 0xff) << 8) | (data[4] & 0xff));
                int sitremindopen = (((data[8] & 0xff) >> 0) & 0xff);
                int standremindopen = (((data[8] & 0xff) >> 1) & 0xff);
                if (listener != null)
                    listener.RemindParam(sitmin, standmin, sitremindopen, standremindopen);
            }
        }
    }

}
