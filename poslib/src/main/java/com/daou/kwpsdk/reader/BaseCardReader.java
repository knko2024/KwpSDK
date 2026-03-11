package com.daou.kwpsdk.reader;

import android.content.Context;
import android.util.Log;

import com.daou.kwpsdk.common.callback.OnCardReadListener;

/**
 * 카드 리더기 기본 추상 클래스
 * 공통 로직을 여기에 구현하고, 각 연결방식별 클래스가 상속합니다.
 */
public abstract class BaseCardReader implements CardReader {

    private static final String TAG = "BaseCardReader";

    protected Context context;
    protected boolean connected = false;
    protected OnCardReadListener currentListener;

    @Override
    public void connect(Context context) throws Exception {
        this.context = context;
        Log.d(TAG, "Connecting " + getConnectionType() + " reader: " + getDeviceName());
        doConnect(context);
        connected = true;
        Log.d(TAG, "Connected successfully: " + getDeviceName());
    }

    @Override
    public void disconnect() {
        Log.d(TAG, "Disconnecting reader: " + getDeviceName());
        doDisconnect();
        connected = false;
        currentListener = null;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void readCard(OnCardReadListener listener) {
        if (!isConnected()) {
            listener.onCardReadFailure(1001, "리더기가 연결되지 않았습니다.");
            return;
        }
        this.currentListener = listener;
        listener.onWaitingForCard("카드를 읽어주세요.");
        doReadCard(listener);
    }

    @Override
    public void cancelRead() {
        Log.d(TAG, "Card read cancelled");
        doCancelRead();
        currentListener = null;
    }

    /**
     * 실제 연결 처리 (하위 클래스에서 구현)
     */
    protected abstract void doConnect(Context context) throws Exception;

    /**
     * 실제 연결 해제 처리 (하위 클래스에서 구현)
     */
    protected abstract void doDisconnect();

    /**
     * 실제 카드 읽기 처리 (하위 클래스에서 구현)
     */
    protected abstract void doReadCard(OnCardReadListener listener);

    /**
     * 실제 카드 읽기 취소 처리 (하위 클래스에서 구현)
     */
    protected abstract void doCancelRead();
}

