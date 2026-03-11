package com.daou.kwpsdk.reader.usb;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.ThReaderIntergrity;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.ThTranstionRunPayment;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;
import com.daou.kwpsdk.common.util.ReaderLogUtil;
import com.ftdi.j2xx.D2xxManager;
public class KwpSdk {
    static String TAG = "KWPSDK";
    ReaderComm mReaderComm;
    Thread serialThread;
    DaouReaderPacket mDaouRdrPkt = null;
    public static int ThProcStep = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Flutter/Host callback
    private ResultCallback resultCallback;
    public interface ResultCallback {
        void onResult(String result);
    }
    public void setResultCallback(ResultCallback callback) {
        this.resultCallback = callback;
    }

    /** 카드 입력 요청 시 UI(팝업) 표시/숨김을 앱에 위임하는 핸들러. */
    private CardInputUiHandler cardInputUiHandler;
    public interface CardInputUiHandler {
        void onShowCardInput(Context context);
        void onHideCardInput();
    }
    public void setCardInputUiHandler(CardInputUiHandler handler) {
        this.cardInputUiHandler = handler;
    }
    public boolean initCardReader() {
        Log.d("KiwoomOrder", "[USB 리더기 초기설정]");
        if (mReaderComm == null) {
            Log.e(TAG, "mReaderComm is not initialized");
            return false;
        }
        boolean ret = mReaderComm.ReaderOpen();
        if (!ret) {
            Log.d("KiwoomOrder", "[리더기 오픈 실패]");
            return false;
        } else {
            Log.d("KiwoomOrder", "[리더기 오픈 완료]");
        }
        return true;
    }
    public void k100(Context mContext, final String trmlid, final String Amount, final String InputType) {
        ReaderLogUtil.d(TAG, "#######################################################################");
        ReaderLogUtil.d(TAG, "                      카드 입력 요청  K100/K180                          ");
        ReaderLogUtil.d(TAG, "#######################################################################");
        ReaderLogUtil.d(TAG, "trmlid:" + trmlid);
        ReaderLogUtil.d(TAG, "Amount:" + Amount);
        ReaderLogUtil.d(TAG, "InputType:" + InputType);
        prepareReader(mContext);
        if (!initCardReader()) {
            dispatchResult("ERROR:READER_INIT_FAILED");
            return;
        }
        mReaderComm.RdrIniPacket(mReaderComm.RID_K100_INP_CARD, trmlid);
        mDaouRdrPkt.tInputType[0] = ReaderComm.CARD_INPUT_TYPE_NOMAL;
        ThProcStep = ThTranstionRunPayment;
        ThreadTransactionProc(mContext, ThProcStep);
    }


    // Pure connectivity check only.
    public Boolean isUsbConnected(Context context) {
        try {
            D2xxManager d2xxManager = D2xxManager.getInstance(context);
            int deviceCount = d2xxManager.createDeviceInfoList(context);
            return deviceCount > 0;
        } catch (D2xxManager.D2xxException e) {
            Log.e(TAG, "USB 연결 확인 실패: " + e.getMessage());
            return false;
        }
    }

    // One-shot K900 request: receives K910 via callback.
    public void reqK100(Context context ) {
        if (cardInputUiHandler != null) {
            cardInputUiHandler.onShowCardInput(context);
        }
        prepareReader(context);
        if (!initCardReader()) {
            if (cardInputUiHandler != null) {
                cardInputUiHandler.onHideCardInput();
            }
            dispatchResult("ERROR:READER_INIT_FAILED");
            return;
        }
        mReaderComm.RdrIniPacket(mReaderComm.RID_K100_INP_CARD, "1004");
        mDaouRdrPkt.tInputType[0] = ReaderComm.CARD_INPUT_TYPE_NOMAL;
        ThProcStep = ThTranstionRunPayment;
        runSingleTransaction(ThProcStep);
    }

    public void setChkConnectUsb(Context context) {
        prepareReader(context);
        if (!initCardReader()) {
            return;
        }
        mReaderComm.RdrIniPacket(mReaderComm.RID_K900_FRM_STATUS, "99999987");
        mDaouRdrPkt.tInputType[0] = ReaderComm.CARD_INPUT_TYPE_NOMAL;
        ThProcStep = ThReaderIntergrity;
        startTransactionLoop(ThReaderIntergrity);
    }


    public void ThreadTransactionProc(Context context, int ThProcStep) {
        startTransactionLoop(ThProcStep);
    }
    public void stopTransactionLoop() {
        if (serialThread != null && serialThread.isAlive()) {
            serialThread.interrupt();
            try {
                serialThread.join(1000); // 스레드 종료 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "스레드 종료 대기 중 인터럽트 발생");
            }
            serialThread = null;
        }
    }
    private void prepareReader(Context context) {
        stopTransactionLoop();
        mReaderComm = new ReaderComm(context, mCardReaderListener);
        mReaderComm.ReaderInitial();
        mDaouRdrPkt = new DaouReaderPacket();
        Log.d(TAG, "getDevCount : " + mReaderComm.getDevCount());
    }
    private void startTransactionLoop(final int procStep) {
        stopTransactionLoop();
        serialThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (mDaouRdrPkt != null) {
                        try {
                            mReaderComm.ThreadTransactionProc(procStep);
                        } catch (Exception ex) {
                            Log.e(TAG, "트랜잭션 처리 중 오류: " + ex.getMessage(), ex);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "KwpSdk-Loop");
        serialThread.setDaemon(true);
        serialThread.start();
    }
    private void runSingleTransaction(final int procStep) {
        stopTransactionLoop();
        serialThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mDaouRdrPkt != null) {
                    try {
                        mReaderComm.ThreadTransactionProc(procStep);
                    } catch (Exception ex) {
                        Log.e(TAG, "K900 상태 조회 중 오류: " + ex.getMessage(), ex);
                    }
                }
            }
        }, "KwpSdk-K900-OneShot");
        serialThread.setDaemon(true);
        serialThread.start();
    }
    private void dispatchResult(final String result) {
        if (resultCallback == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (cardInputUiHandler != null) {
                    cardInputUiHandler.onHideCardInput();
                }
                resultCallback.onResult(result);
            }
        });
    }
    CardReaderConstants.CardReadListener mCardReaderListener = new CardReaderConstants.CardReadListener() {
        @Override
        public void onCardReaderResult(String result) {
            Log.i(TAG, "onCardReaderResult:" + result);
            dispatchResult(result);
        }
    };
}