package com.daou.kwpsdk.van;

import com.daou.kwpsdk.common.callback.OnPaymentResultListener;
import com.daou.kwpsdk.common.model.PaymentRequest;

/**
 * VAN 클라이언트 공통 인터페이스
 * 각 VAN사별 구현체가 이 인터페이스를 구현합니다.
 */
public interface VanClient {

    /**
     * VAN사 식별자
     */
    enum VanType {
        DAOU,       // DAOU

    }

    /**
     * VAN 서버 연결 초기화
     * @param config VAN 설정 정보
     * @throws Exception 초기화 실패 시
     */
    void initialize(VanConfig config) throws Exception;

    /**
     * 결제 승인 요청
     * @param request 결제 요청 데이터
     * @param listener 결제 결과 리스너
     */
    void requestPayment(PaymentRequest request, OnPaymentResultListener listener);

    /**
     * 결제 취소 요청
     * @param request 취소 요청 데이터 (원거래 정보 포함)
     * @param listener 결제 결과 리스너
     */
    void requestCancel(PaymentRequest request, OnPaymentResultListener listener);

    /**
     * VAN 서버 연결 해제
     */
    void release();

    /**
     * VAN 타입 반환
     * @return VanType
     */
    VanType getVanType();

    /**
     * VAN 서버 접속 가능 여부 확인
     * @return 접속 가능 여부
     */
    boolean isAvailable();
}

