package com.daou.kwpsdk.reader.usb;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
    //private static final String ACTION_USB_PERMISSION = "com.daoudata.kiwoompay.order";

    private static final String ACTION_USB_PERMISSION =     "android.hardware.usb.action.USB_DEVICE_ATTACHED";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        //   UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        //call method to set up device communication
                        Log.i("KiwoomOrder", "EXTRA_PERMISSION_GRANTED:");
                    }
                } else {
                    Log.i("KiwoomOrder", "EXTRA_PERMISSION_GRANTED");

                }
            }
        }


    }
}