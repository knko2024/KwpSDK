package com.daou.kwpsdk.van.daou;

import android.util.Log;

import com.daou.kwpsdk.common.callback.OnOpeningTransactionListener;
import com.daou.kwpsdk.common.callback.OnPaymentResultListener;
import com.daou.kwpsdk.common.model.OpeningTransactionResult;
import com.daou.kwpsdk.common.model.PaymentRequest;
import com.daou.kwpsdk.common.model.ReaderIntegrityResult;
import com.daou.kwpsdk.van.BaseVanClient;
import com.daou.kwpsdk.van.VanConfig;

import java.util.concurrent.Executor;

/**
 * DAOU VAN client implementation.
 */
public class DaouVanClient extends BaseVanClient {

    private static final String TAG = "DAOUVanClient";
    private static final String DEFAULT_DOWNLOAD_SERVER_IP = "222.106.99.137";
    private static final int DEFAULT_DOWNLOAD_SERVER_PORT = 20071;
    private static final String DEFAULT_MODEL_CODE = "PK1B";
    private static final String DEFAULT_SOFTWARE_CERT = "###DAOU-MPOS1000";
    private static final String DEFAULT_COMPANY_NUMBER = "1242137263";
    private static final String ERROR_NOT_INITIALIZED = "2001";
    private static final String ERROR_INVALID_REQUEST = "2002";

    private final Executor openingExecutor;
    private final DaouOpeningTransactionRequestBuilder openingRequestBuilder;
    private final DaouOpeningTransactionResponseParser openingResponseParser;
    private final DaouVanCommFsm vanCommFsm;

    private volatile DaouVanSession session = DaouVanSession.empty();

    public DaouVanClient() {
        this(new Executor() {
            @Override
            public void execute(Runnable command) {
                Thread thread = new Thread(command, "DaouVanClient-Opening");
                thread.start();
            }
        }, new DaouOpeningTransactionRequestBuilder(),
            new DaouOpeningTransactionResponseParser(), new DaouVanCommFsm());
    }

    DaouVanClient(Executor openingExecutor,
                  DaouOpeningTransactionRequestBuilder openingRequestBuilder,
                  DaouOpeningTransactionResponseParser openingResponseParser,
                  DaouVanCommFsm vanCommFsm) {
        this.openingExecutor = openingExecutor;
        this.openingRequestBuilder = openingRequestBuilder;
        this.openingResponseParser = openingResponseParser;
        this.vanCommFsm = vanCommFsm;
    }

    @Override
    protected void doInitialize(VanConfig config) {
        if (isBlank(config.getDownloadServerIp())) {
            if (isBlank(config.getServerIp())) {
                config.setDownloadServerIp(DEFAULT_DOWNLOAD_SERVER_IP);
            } else {
                config.setDownloadServerIp(config.getServerIp());
            }
        }
        if (config.getDownloadServerPort() <= 0) {
            if (config.getServerPort() <= 0) {
                config.setDownloadServerPort(DEFAULT_DOWNLOAD_SERVER_PORT);
            } else {
                config.setDownloadServerPort(config.getServerPort());
            }
        }
        if (isBlank(config.getServerIp())) {
            config.setServerIp(config.getDownloadServerIp());
        }
        if (config.getServerPort() <= 0) {
            config.setServerPort(config.getDownloadServerPort());
        }
        if (isBlank(config.getModelCode())) {
            config.setModelCode(DEFAULT_MODEL_CODE);
        }
        if (isBlank(config.getSoftwareCert())) {
            config.setSoftwareCert(DEFAULT_SOFTWARE_CERT);
        }
        if (isBlank(config.getCompanyNumber())) {
            config.setCompanyNumber(DEFAULT_COMPANY_NUMBER);
        }
        Log.d(TAG, "DAOU VAN initialized: "
            + config.getDownloadServerIp() + ":" + config.getDownloadServerPort());
    }

    public void requestOpeningTransaction(final ReaderIntegrityResult integrityResult,
                                          final OnOpeningTransactionListener listener) {
        if (listener == null) {
            return;
        }
        if (!initialized || config == null) {
            listener.onOpeningFailure(
                ERROR_NOT_INITIALIZED,
                "DAOU VAN is not initialized.",
                buildFailureResult(ERROR_NOT_INITIALIZED, "DAOU VAN is not initialized.")
            );
            return;
        }
        if (integrityResult == null || !integrityResult.isSuccess()) {
            String code = integrityResult != null ? integrityResult.getResponseCode() : ERROR_INVALID_REQUEST;
            String message = integrityResult != null
                ? integrityResult.getResponseMessage()
                : "Opening transaction requires a successful K930 result.";
            listener.onOpeningFailure(
                code,
                message,
                buildFailureResult(code, message)
            );
            return;
        }

        openingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                OpeningTransactionResult result;
                try {
                    byte[] requestFrame = openingRequestBuilder.build(config, integrityResult);
                    DaouVanCommFsm.Result commResult = vanCommFsm.execute(
                        requestFrame,
                        config.getDownloadServerIp(),
                        config.getDownloadServerPort()
                    );
                    result = openingResponseParser.parse(commResult.getResponseFrame());
                    result.setNetworkStatus(commResult.getNetworkStatus().name());
                    if (!commResult.isSuccessfulTransport()) {
                        result.setResponseMessage(resolveTransportMessage(commResult.getNetworkStatus()));
                    }
                } catch (IllegalArgumentException ex) {
                    Log.e(TAG, "Opening transaction request is invalid: " + ex.getMessage(), ex);
                    result = buildFailureResult(ERROR_INVALID_REQUEST, ex.getMessage());
                } catch (Exception ex) {
                    Log.e(TAG, "Opening transaction failed: " + ex.getMessage(), ex);
                    result = buildFailureResult("5000", "Opening transaction failed: " + ex.getMessage());
                }

                if (result.isSuccess()) {
                    session = DaouVanSession.fromOpeningTransactionResult(result);
                    listener.onOpeningSuccess(result);
                } else {
                    listener.onOpeningFailure(result.getFailureCode(), result.getResponseMessage(), result);
                }
            }
        });
    }

    public DaouVanSession getSession() {
        return session;
    }

    @Override
    protected void doRequestPayment(PaymentRequest request, OnPaymentResultListener listener) {
        if (listener != null) {
            listener.onPaymentFailure(9000, "DAOU payment approval is not implemented yet.");
        }
    }

    @Override
    protected void doRequestCancel(PaymentRequest request, OnPaymentResultListener listener) {
        if (listener != null) {
            listener.onPaymentFailure(9001, "DAOU payment cancel is not implemented yet.");
        }
    }

    @Override
    protected void doRelease() {
        session = DaouVanSession.empty();
        Log.d(TAG, "DAOU VAN released");
    }

    @Override
    public VanType getVanType() {
        return VanType.DAOU;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private OpeningTransactionResult buildFailureResult(String code, String message) {
        OpeningTransactionResult result = new OpeningTransactionResult();
        result.setResponseCode(code == null ? "" : code);
        result.setResponseMessage(message == null ? "" : message);
        return result;
    }

    private String resolveTransportMessage(DaouVanCommFsm.NetworkStatus status) {
        switch (status) {
            case SOCKET_ERROR:
                return "Opening transaction socket connection failed.";
            case ACK_TIMEOUT:
                return "Opening transaction ACK wait timed out.";
            case RESPONSE_TIMEOUT:
                return "Opening transaction response wait timed out.";
            case NO_EOT:
                return "Opening transaction finished without EOT.";
            case DLE:
                return "Opening transaction ended with DLE.";
            case NAK:
                return "Opening transaction request was rejected with NAK.";
            case INVALID_SERVER_MESSAGE:
                return "Opening transaction received an invalid server message.";
            case IO_ERROR:
                return "Opening transaction I/O failed.";
            case SUCCESS:
            default:
                return "Opening transaction completed successfully.";
        }
    }
}
