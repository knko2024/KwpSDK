package com.daou.kwpsdk.reader.usb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.daou.kwpsdk.common.callback.OnOpeningTransactionListener;
import com.daou.kwpsdk.common.callback.OnReaderIntegrityListener;
import com.daou.kwpsdk.common.callback.OnReaderSecurityListener;
import com.daou.kwpsdk.common.model.OpeningTransactionResult;
import com.daou.kwpsdk.common.model.ReaderIntegrityResult;
import com.daou.kwpsdk.common.model.ReaderSecurityResult;
import com.daou.kwpsdk.van.VanConfig;
import com.daou.kwpsdk.van.daou.DaouVanClient;
import com.daou.kwpsdk.van.daou.DaouVanSession;
import com.ftdi.j2xx.D2xxManager;

public class KwpSdk {
    static String TAG = "KWPSDK";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final DaouVanClient daouVanClient = new DaouVanClient();
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

            @Override
            public void onIntegrityResult(ReaderIntegrityResult result) {
                dispatchIntegrityResult(result);
            }

            @Override
            public void onSecurityResult(ReaderSecurityResult result) {
                dispatchSecurityResult(result);
            }
        }
    );

    private volatile boolean cardInputUiVisible = false;
    private volatile boolean openingTransactionPending = false;
    private ResultCallback resultCallback;
    private CardInputUiHandler cardInputUiHandler;
    private OnReaderIntegrityListener readerIntegrityListener;
    private OnReaderSecurityListener readerSecurityListener;
    private OnOpeningTransactionListener openingTransactionListener;

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
     * Register a callback that receives parsed K930 integrity check results.
     *
     * @param listener callback invoked after a K920/K930 exchange completes
     * @return none
     */
    public void setReaderIntegrityListener(OnReaderIntegrityListener listener) {
        this.readerIntegrityListener = listener;
    }

    /**
     * Register a callback that receives parsed K810 security certification results.
     *
     * @param listener callback invoked after a K800/K810 exchange completes
     * @return none
     */
    public void setReaderSecurityListener(OnReaderSecurityListener listener) {
        this.readerSecurityListener = listener;
    }

    /**
     * Register a callback that receives opening transaction results.
     *
     * @param listener callback invoked after the 020090/021090 exchange completes
     * @return none
     */
    public void setOpeningTransactionListener(OnOpeningTransactionListener listener) {
        this.openingTransactionListener = listener;
    }

    /**
     * Initialize the DAOU opening transaction client.
     *
     * @param config VAN configuration for the download server
     * @return none
     * @throws Exception when the VAN client cannot be initialized
     */
    public void initializeOpeningTransaction(VanConfig config) throws Exception {
        daouVanClient.initialize(config);
    }

    /**
     * Release opening transaction resources.
     *
     * @return none
     */
    public void releaseOpeningTransaction() {
        clearOpeningTransactionPending();
        daouVanClient.release();
    }

    /**
     * Return the saved opening transaction session.
     *
     * @return opening transaction session values
     */
    public DaouVanSession getOpeningTransactionSession() {
        return daouVanClient.getSession();
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
     * Request a K920 integrity check transaction.
     *
     * @param context Android context for the transport worker
     * @param trmlid terminal identifier used by the reader protocol
     * @return none
     */
    public void reqK920(Context context, String trmlid) {
        transportController.enqueueK920(toApplicationContext(context), trmlid);
    }

    /**
     * Request a K920 integrity check transaction and deliver a parsed K930 result.
     *
     * @param context Android context for the transport worker
     * @param trmlid terminal identifier used by the reader protocol
     * @param listener callback invoked with success or failure
     * @return none
     */
    public void requestIntegrityCheck(Context context, String trmlid,
                                      OnReaderIntegrityListener listener) {
        setReaderIntegrityListener(listener);
        reqK920(context, trmlid);
    }

    /**
     * Run K920/K930 and automatically start the 020090 opening transaction on success.
     *
     * @param context Android context for the transport worker
     * @param trmlid terminal identifier used by the reader protocol
     * @param config VAN configuration for the opening transaction
     * @param listener callback invoked with the opening transaction result
     * @return none
     */
    public void requestIntegrityCheckAndOpeningTransaction(Context context, String trmlid,
                                                           VanConfig config,
                                                           OnOpeningTransactionListener listener) {
        setOpeningTransactionListener(listener);
        try {
            initializeOpeningTransaction(config);
        } catch (Exception ex) {
            Log.e(TAG, "Opening transaction init failed: " + ex.getMessage(), ex);
            dispatchOpeningResult(buildOpeningFailureResult(
                "2001",
                "Failed to initialize opening transaction: " + ex.getMessage()
            ));
            return;
        }
        openingTransactionPending = true;
        reqK920(context, trmlid);
    }

    /**
     * Request a K800 security certification transaction and deliver a parsed K810 result.
     *
     * @param context Android context for the transport worker
     * @param trmlid terminal identifier used by the reader protocol
     * @param listener callback invoked with success or failure
     * @return none
     */
    public void requestSecurityCertification(Context context, String trmlid,
                                             OnReaderSecurityListener listener) {
        setReaderSecurityListener(listener);
        reqK800(context, trmlid);
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
        clearOpeningTransactionPending();
        transportController.shutdown();
        daouVanClient.release();
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
     * Deliver a parsed K930 response on the main thread.
     *
     * @param result parsed reader integrity result
     * @return none
     */
    private void dispatchIntegrityResult(final ReaderIntegrityResult result) {
        if (openingTransactionPending) {
            if (result != null && result.isSuccess()) {
                openingTransactionPending = false;
                daouVanClient.requestOpeningTransaction(result, new OnOpeningTransactionListener() {
                    @Override
                    public void onOpeningSuccess(OpeningTransactionResult openingResult) {
                        dispatchOpeningResult(openingResult);
                    }

                    @Override
                    public void onOpeningFailure(String code, String message,
                                                 OpeningTransactionResult rawResult) {
                        dispatchOpeningResult(rawResult);
                    }
                });
            } else {
                openingTransactionPending = false;
                dispatchOpeningResult(buildOpeningFailureResult(result));
            }
        }

        if (readerIntegrityListener == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (readerIntegrityListener == null || result == null) {
                    return;
                }
                if (result.isSuccess()) {
                    readerIntegrityListener.onIntegritySuccess(result);
                } else {
                    readerIntegrityListener.onIntegrityFailure(
                        result.getResponseCode(),
                        result.getResponseMessage(),
                        result
                    );
                }
            }
        });
    }

    /**
     * Deliver a parsed K810 response on the main thread.
     *
     * @param result parsed reader security result
     * @return none
     */
    private void dispatchSecurityResult(final ReaderSecurityResult result) {
        if (readerSecurityListener == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (readerSecurityListener == null || result == null) {
                    return;
                }
                if (result.isSuccess()) {
                    readerSecurityListener.onSecuritySuccess(result);
                } else {
                    readerSecurityListener.onSecurityFailure(
                        result.getResponseCode(),
                        result.getResponseMessage(),
                        result
                    );
                }
            }
        });
    }

    /**
     * Deliver an opening transaction result on the main thread.
     *
     * @param result parsed opening transaction result
     * @return none
     */
    private void dispatchOpeningResult(final OpeningTransactionResult result) {
        if (openingTransactionListener == null || result == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (openingTransactionListener == null || result == null) {
                    return;
                }
                if (result.isSuccess()) {
                    openingTransactionListener.onOpeningSuccess(result);
                } else {
                    openingTransactionListener.onOpeningFailure(
                        result.getFailureCode(),
                        result.getResponseMessage(),
                        result
                    );
                }
            }
        });
    }

    private OpeningTransactionResult buildOpeningFailureResult(ReaderIntegrityResult integrityResult) {
        if (integrityResult == null) {
            return buildOpeningFailureResult("2002", "K930 result is missing.");
        }
        OpeningTransactionResult result = new OpeningTransactionResult();
        result.setResponseCode(integrityResult.getResponseCode());
        result.setResponseMessage(integrityResult.getResponseMessage());
        result.setRawResponse(integrityResult.getRawPayload());
        return result;
    }

    private OpeningTransactionResult buildOpeningFailureResult(String code, String message) {
        OpeningTransactionResult result = new OpeningTransactionResult();
        result.setResponseCode(code);
        result.setResponseMessage(message);
        return result;
    }

    private void clearOpeningTransactionPending() {
        openingTransactionPending = false;
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
