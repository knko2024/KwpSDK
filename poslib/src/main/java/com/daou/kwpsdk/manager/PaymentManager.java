package com.daou.kwpsdk.manager;

import android.content.Context;
import android.util.Log;

import com.daou.kwpsdk.common.callback.OnCardReadListener;
import com.daou.kwpsdk.common.callback.OnPaymentResultListener;
import com.daou.kwpsdk.common.model.CardData;
import com.daou.kwpsdk.common.model.PaymentRequest;
import com.daou.kwpsdk.common.model.PaymentResult;
import com.daou.kwpsdk.reader.CardReader;
import com.daou.kwpsdk.van.VanClient;
import com.daou.kwpsdk.van.VanConfig;

/**
 * 결제 관리자 (Payment Manager)
 * 카드리더기와 VAN 통신을 조율하여 결제 프로세스 전체를 관리합니다.
 * Flutter에서 MethodChannel을 통해 이 클래스를 호출합니다.
 */
public class PaymentManager {

    private static final String TAG = "PaymentManager";

    private static PaymentManager instance;

    private Context context;
    private CardReader cardReader;
    private VanClient vanClient;
    private VanConfig vanConfig;
    private boolean isProcessing = false;

    private PaymentManager() {
    }

    /**
     * 싱글턴 인스턴스 반환
     */
    public static synchronized PaymentManager getInstance() {
        if (instance == null) {
            instance = new PaymentManager();
        }
        return instance;
    }

    /**
     * PaymentManager 초기화
     * @param context Android Context
     * @param cardReader 카드 리더기 구현체
     * @param vanClient VAN 클라이언트 구현체
     * @param vanConfig VAN 설정 정보
     */
    public void initialize(Context context, CardReader cardReader,
                           VanClient vanClient, VanConfig vanConfig) {
        this.context = context;
        this.cardReader = cardReader;
        this.vanClient = vanClient;
        this.vanConfig = vanConfig;
        Log.d(TAG, "PaymentManager 초기화 완료 - Reader: " + cardReader.getDeviceName()
                + ", VAN: " + vanClient.getVanType());
    }

    /**
     * 리더기 연결
     */
    public void connectReader() throws Exception {
        if (cardReader == null) {
            throw new IllegalStateException("CardReader가 설정되지 않았습니다.");
        }
        cardReader.connect(context);
    }

    /**
     * VAN 초기화
     */
    public void initializeVan() throws Exception {
        if (vanClient == null || vanConfig == null) {
            throw new IllegalStateException("VanClient 또는 VanConfig가 설정되지 않았습니다.");
        }
        vanClient.initialize(vanConfig);
    }

    /**
     * 결제 시작 (카드 읽기 → VAN 승인 요청)
     * @param amount 결제 금액
     * @param installmentMonths 할부 개월수 (0: 일시불)
     * @param listener 결제 결과 리스너
     */
    public void startPayment(long amount, int installmentMonths,
                             OnPaymentResultListener listener) {
        if (isProcessing) {
            listener.onPaymentFailure(3001, "이미 결제가 진행 중입니다.");
            return;
        }

        if (cardReader == null || !cardReader.isConnected()) {
            listener.onPaymentFailure(1001, "카드 리더기가 연결되지 않았습니다.");
            return;
        }

        if (vanClient == null || !vanClient.isAvailable()) {
            listener.onPaymentFailure(2001, "VAN 서버가 초기화되지 않았습니다.");
            return;
        }

        isProcessing = true;
        listener.onPaymentProgress("카드를 읽어주세요...");

        // Step 1: 카드 읽기
        cardReader.readCard(new OnCardReadListener() {
            @Override
            public void onCardReadSuccess(CardData cardData) {
                Log.d(TAG, "카드 읽기 성공: " + cardData);
                listener.onPaymentProgress("카드 읽기 완료. VAN 서버에 승인 요청 중...");

                // Step 2: VAN 승인 요청
                PaymentRequest request = new PaymentRequest();
                request.setTransactionType(PaymentRequest.TransactionType.APPROVAL);
                request.setAmount(amount);
                request.setInstallmentMonths(installmentMonths);
                request.setCardData(cardData);
                request.setMerchantId(vanConfig.getTerminalId());

                vanClient.requestPayment(request, new OnPaymentResultListener() {
                    @Override
                    public void onPaymentSuccess(PaymentResult result) {
                        isProcessing = false;
                        Log.d(TAG, "결제 성공: " + result);
                        listener.onPaymentSuccess(result);
                    }

                    @Override
                    public void onPaymentFailure(int errorCode, String errorMessage) {
                        isProcessing = false;
                        Log.e(TAG, "결제 실패: " + errorCode + " - " + errorMessage);
                        listener.onPaymentFailure(errorCode, errorMessage);
                    }

                    @Override
                    public void onPaymentProgress(String status) {
                        listener.onPaymentProgress(status);
                    }
                });
            }

            @Override
            public void onCardReadFailure(int errorCode, String errorMessage) {
                isProcessing = false;
                Log.e(TAG, "카드 읽기 실패: " + errorCode + " - " + errorMessage);
                listener.onPaymentFailure(errorCode, errorMessage);
            }

            @Override
            public void onWaitingForCard(String message) {
                listener.onPaymentProgress(message);
            }
        });
    }

    /**
     * 결제 취소
     * @param amount 취소 금액
     * @param originalApprovalNumber 원거래 승인번호
     * @param originalApprovalDate 원거래 승인일자
     * @param listener 결제 결과 리스너
     */
    public void cancelPayment(long amount, String originalApprovalNumber,
                              String originalApprovalDate,
                              OnPaymentResultListener listener) {
        if (isProcessing) {
            listener.onPaymentFailure(3001, "이미 처리가 진행 중입니다.");
            return;
        }

        if (vanClient == null || !vanClient.isAvailable()) {
            listener.onPaymentFailure(2001, "VAN 서버가 초기화되지 않았습니다.");
            return;
        }

        isProcessing = true;
        listener.onPaymentProgress("결제 취소 요청 중...");

        PaymentRequest request = new PaymentRequest();
        request.setTransactionType(PaymentRequest.TransactionType.CANCEL);
        request.setAmount(amount);
        request.setOriginalApprovalNumber(originalApprovalNumber);
        request.setOriginalApprovalDate(originalApprovalDate);
        request.setMerchantId(vanConfig.getTerminalId());

        vanClient.requestCancel(request, new OnPaymentResultListener() {
            @Override
            public void onPaymentSuccess(PaymentResult result) {
                isProcessing = false;
                listener.onPaymentSuccess(result);
            }

            @Override
            public void onPaymentFailure(int errorCode, String errorMessage) {
                isProcessing = false;
                listener.onPaymentFailure(errorCode, errorMessage);
            }

            @Override
            public void onPaymentProgress(String status) {
                listener.onPaymentProgress(status);
            }
        });
    }

    /**
     * 결제 진행 중 취소
     */
    public void abortPayment() {
        if (isProcessing && cardReader != null) {
            cardReader.cancelRead();
            isProcessing = false;
            Log.d(TAG, "결제 진행 중 취소됨");
        }
    }

    /**
     * 리소스 해제
     */
    public void release() {
        if (cardReader != null) {
            cardReader.disconnect();
        }
        if (vanClient != null) {
            vanClient.release();
        }
        isProcessing = false;
        Log.d(TAG, "PaymentManager 리소스 해제 완료");
    }

    // Getters

    public CardReader getCardReader() {
        return cardReader;
    }

    public void setCardReader(CardReader cardReader) {
        this.cardReader = cardReader;
    }

    public VanClient getVanClient() {
        return vanClient;
    }

    public void setVanClient(VanClient vanClient) {
        this.vanClient = vanClient;
    }

    public boolean isProcessing() {
        return isProcessing;
    }
}

