package com.daou.kwpsdk.common.callback;

import com.daou.kwpsdk.common.model.PaymentResult;

/**
 * 결제 결과 리스너
 */
public interface OnPaymentResultListener {

    /**
     * 결제 성공
     * @param result 결제 결과
     */
    void onPaymentSuccess(PaymentResult result);

    /**
     * 결제 실패
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    void onPaymentFailure(int errorCode, String errorMessage);

    /**
     * 결제 진행 중 상태 업데이트
     * @param status 현재 상태 메시지
     */
    void onPaymentProgress(String status);
}

