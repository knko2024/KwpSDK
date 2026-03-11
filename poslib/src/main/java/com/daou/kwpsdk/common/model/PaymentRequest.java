package com.daou.kwpsdk.common.model;

/**
 * 결제 요청 모델
 */
public class PaymentRequest {

    /** 거래 유형 */
    private TransactionType transactionType;

    /** 결제 금액 */
    private long amount;

    /** 할부 개월수 (0: 일시불) */
    private int installmentMonths;

    /** 카드 데이터 */
    private CardData cardData;

    /** 세금 */
    private long tax;

    /** 봉사료 */
    private long serviceFee;

    /** 가맹점 번호 */
    private String merchantId;

    /** 원거래 승인번호 (취소 시 사용) */
    private String originalApprovalNumber;

    /** 원거래 승인일자 (취소 시 사용) */
    private String originalApprovalDate;

    public enum TransactionType {
        APPROVAL,       // 승인
        CANCEL,         // 취소
        PARTIAL_CANCEL  // 부분취소
    }

    public PaymentRequest() {
    }

    // Getters and Setters

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getInstallmentMonths() {
        return installmentMonths;
    }

    public void setInstallmentMonths(int installmentMonths) {
        this.installmentMonths = installmentMonths;
    }

    public CardData getCardData() {
        return cardData;
    }

    public void setCardData(CardData cardData) {
        this.cardData = cardData;
    }

    public long getTax() {
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public long getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(long serviceFee) {
        this.serviceFee = serviceFee;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getOriginalApprovalNumber() {
        return originalApprovalNumber;
    }

    public void setOriginalApprovalNumber(String originalApprovalNumber) {
        this.originalApprovalNumber = originalApprovalNumber;
    }

    public String getOriginalApprovalDate() {
        return originalApprovalDate;
    }

    public void setOriginalApprovalDate(String originalApprovalDate) {
        this.originalApprovalDate = originalApprovalDate;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "transactionType=" + transactionType +
                ", amount=" + amount +
                ", installmentMonths=" + installmentMonths +
                ", merchantId='" + merchantId + '\'' +
                '}';
    }
}

