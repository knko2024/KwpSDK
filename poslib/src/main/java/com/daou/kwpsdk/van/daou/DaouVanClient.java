package com.daou.kwpsdk.van.daou;
import android.util.Log;
import com.daou.kwpsdk.common.callback.OnPaymentResultListener;
import com.daou.kwpsdk.common.model.PaymentRequest;
import com.daou.kwpsdk.van.BaseVanClient;
import com.daou.kwpsdk.van.VanConfig;

/**
 * DAOU VAN 클라이언트 구현체
 */
public class DaouVanClient extends BaseVanClient {

    private static final String TAG = "DAOUVanClient";

    @Override
    protected void doInitialize(VanConfig config) throws Exception {
        Log.d(TAG, "DAOU VAN 초기화: " + config.getServerIp() + ":" + config.getServerPort());
        // TODO: DAOU VAN 서버 연결 초기화
    }

    @Override
    protected void doRequestPayment(PaymentRequest request, OnPaymentResultListener listener) {
        Log.d(TAG, "DAOU 결제 승인 요청: " + request);
        // TODO: DAOU VAN 프로토콜에 맞는 전문 생성 및 송수신 구현
    }

    @Override
    protected void doRequestCancel(PaymentRequest request, OnPaymentResultListener listener) {
        Log.d(TAG, "DAOU 결제 취소 요청: " + request);
        // TODO: DAOU VAN 취소 전문 생성 및 송수신 구현
    }

    @Override
    protected void doRelease() {
        Log.d(TAG, "DAOU VAN 리소스 해제");
        // TODO: 소켓 닫기
    }

    @Override
    public VanType getVanType() {
        return VanType.DAOU;
    }
}

