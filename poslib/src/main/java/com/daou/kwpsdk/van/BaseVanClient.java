package com.daou.kwpsdk.van;

import android.util.Log;

import com.daou.kwpsdk.common.callback.OnPaymentResultListener;
import com.daou.kwpsdk.common.model.PaymentRequest;

/**
 * VAN 클라이언트 기본 추상 클래스
 * 공통 로직을 여기에 구현하고, 각 VAN사별 클래스가 상속합니다.
 */
public abstract class BaseVanClient implements VanClient {

    private static final String TAG = "KWPSDK";

    protected VanConfig config;
    protected boolean initialized = false;

    @Override
    public void initialize(VanConfig config) throws Exception {
        this.config = config;
        Log.d(TAG, "VAN 초기화: " + config);
        doInitialize(config);
        initialized = true;
    }

    @Override
    public void requestPayment(PaymentRequest request, OnPaymentResultListener listener) {
        if (!initialized) {
            listener.onPaymentFailure(2001, "VAN이 초기화되지 않았습니다.");
            return;
        }
        listener.onPaymentProgress("VAN 서버에 승인 요청 중...");
        doRequestPayment(request, listener);
    }

    @Override
    public void requestCancel(PaymentRequest request, OnPaymentResultListener listener) {
        if (!initialized) {
            listener.onPaymentFailure(2001, "VAN이 초기화되지 않았습니다.");
            return;
        }
        listener.onPaymentProgress("VAN 서버에 취소 요청 중...");
        doRequestCancel(request, listener);
    }

    @Override
    public void release() {
        Log.d(TAG, "VAN 리소스 해제");
        doRelease();
        initialized = false;
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    /**
     * 실제 초기화 처리 (하위 클래스에서 구현)
     */
    protected abstract void doInitialize(VanConfig config) throws Exception;

    /**
     * 실제 결제 승인 요청 처리 (하위 클래스에서 구현)
     */
    protected abstract void doRequestPayment(PaymentRequest request, OnPaymentResultListener listener);

    /**
     * 실제 결제 취소 요청 처리 (하위 클래스에서 구현)
     */
    protected abstract void doRequestCancel(PaymentRequest request, OnPaymentResultListener listener);

    /**
     * 실제 리소스 해제 처리 (하위 클래스에서 구현)
     */
    protected abstract void doRelease();
}

