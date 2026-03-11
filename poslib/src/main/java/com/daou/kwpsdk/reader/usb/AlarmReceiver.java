package com.daou.kwpsdk.reader.usb;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();
        if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            Log.d("KiwoomOrder", "onReceive:" + "ACTION_USB_DEVICE_ATTACHED");

            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            device.getVersion();

            Log.d("KiwoomOrder", "onReceive:" + device.getVersion());
            Log.d("KiwoomOrder", "onReceive:" + device.getConfiguration(0));
        }
    }
}