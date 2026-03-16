package com.daou.kwpsdk.reader.usb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;

public class KwpSdk {
    static String TAG = "KWPSDK";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TransportController transportController = new TransportController(
        new TransportController.TransportEventListener() {
            @Override
            public void onResult(String result) {
                dispatchResult(result);
            }

            @Override
            public void onCardInputVisibilityChanged(boolean visible, Context context) {
                dispatchCardInputVisibility(visible, context);
            }
        }
    );

    private volatile boolean cardInputUiVisible = false;
    private ResultCallback resultCallback;
    private CardInputUiHandler cardInputUiHandler;

    public interface ResultCallback {
        void onResult(String result);
    }

    public interface CardInputUiHandler {
        void onShowCardInput(Context context);
        void onHideCardInput();
    }

    /**
     * Register a callback that receives reader results.
     *
     * @param callback callback invoked with the reader result string
     * @return none
     */
    public void setResultCallback(ResultCallback callback) {
        this.resultCallback = callback;
    }

    /**
     * Register a handler that shows or hides the card input UI.
     *
     * @param handler UI handler for card input state
     * @return none
     */
    public void setCardInputUiHandler(CardInputUiHandler handler) {
        this.cardInputUiHandler = handler;
    }

    /**
     * Check whether a USB card reader is currently connected.
     *
     * @param context Android context used to access the FTDI driver
     * @return true when at least one USB reader is connected
     */
    public Boolean isUsbConnected(Context context) {
        try {
            Context appContext = toApplicationContext(context);
            if (appContext == null) {
                return false;
            }
            D2xxManager d2xxManager = D2xxManager.getInstance(appContext);
            int deviceCount = d2xxManager.createDeviceInfoList(appContext);
            return deviceCount > 0;
        } catch (D2xxManager.D2xxException e) {
            Log.e(TAG, "USB connection check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Request a K100 card input transaction.
     *
     * @param context Android context for the transport worker
     * @return none
     */
    public void reqK100(Context context) {
        transportController.enqueueK100(toApplicationContext(context));
    }

    /**
     * Request a K800 security certification transaction.
     *
     * @param context Android context for the transport worker
     * @param trmlid terminal identifier used by the reader protocol
     * @return none
     */
    public void reqK800(Context context, String trmlid) {
        transportController.enqueueK800(toApplicationContext(context), trmlid);
    }

    /**
     * Request a K980 cancel transaction.
     *
     * @param context Android context for the transport worker
     * @return none
     */
    public void reqK980(Context context) {
        transportController.enqueueK980(toApplicationContext(context));
    }

    /**
     * Report whether a transaction is currently active.
     *
     * @return true when a reader transaction is in progress
     */
    public boolean isTransactionActive() {
        return transportController.isTransactionActive();
    }

    /**
     * Shut down the worker thread and close the reader.
     *
     * @return none
     */
    public void shutdown() {
        transportController.shutdown();
    }

    /**
     * Deliver a reader result on the main thread.
     *
     * @param result reader result string
     * @return none
     */
    private void dispatchResult(final String result) {
        if (resultCallback == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (resultCallback != null) {
                    resultCallback.onResult(result);
                }
            }
        });
    }

    /**
     * Update card input UI visibility on the main thread.
     *
     * @param visible true to show the card input UI
     * @param context Android context used when showing the UI
     * @return none
     */
    private void dispatchCardInputVisibility(final boolean visible, final Context context) {
        if (visible == cardInputUiVisible) {
            return;
        }
        cardInputUiVisible = visible;

        if (cardInputUiHandler == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (cardInputUiHandler == null) {
                    return;
                }
                if (visible) {
                    cardInputUiHandler.onShowCardInput(context);
                } else {
                    cardInputUiHandler.onHideCardInput();
                }
            }
        });
    }

    /**
     * Normalize the supplied context to an application context.
     *
     * @param context source Android context
     * @return application context or the original context
     */
    private Context toApplicationContext(Context context) {
        if (context == null) {
            return null;
        }
        Context applicationContext = context.getApplicationContext();
        return applicationContext != null ? applicationContext : context;
    }
}
