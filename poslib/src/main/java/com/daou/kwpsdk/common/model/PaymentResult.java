package com.daou.kwpsdk.common.model;

/**
 * 결제 결과 모델
 */
public class PaymentResult {

    /** 결과 코드 (0000: 정상) */
    private String resultCode;

    /** 결과 메시지 */
    private String resultMessage;

    /** 승인 여부 */
    private boolean isApproved;

    /** 승인번호 */
    private String approvalNumber;

    /** 승인일시 (yyyyMMddHHmmss) */
    private String approvalDateTime;

    /** 카드사명 */
    private String issuerName;

    /** 매입사명 */
    private String acquirerName;

    /** 가맹점 번호 */
    private String merchantId;

    /** 거래 금액 */
    private long amount;

    /** 거래 유형 */
    private PaymentRequest.TransactionType transactionType;

    public PaymentResult() {
    }

    // Getters and Setters

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getApprovalNumber() {
        return approvalNumber;
    }

    public void setApprovalNumber(String approvalNumber) {
        this.approvalNumber = approvalNumber;
    }

    public String getApprovalDateTime() {
        return approvalDateTime;
    }

    public void setApprovalDateTime(String approvalDateTime) {
        this.approvalDateTime = approvalDateTime;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getAcquirerName() {
        return acquirerName;
    }

    public void setAcquirerName(String acquirerName) {
        this.acquirerName = acquirerName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public PaymentRequest.TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(PaymentRequest.TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "resultCode='" + resultCode + '\'' +
                ", resultMessage='" + resultMessage + '\'' +
                ", isApproved=" + isApproved +
                ", approvalNumber='" + approvalNumber + '\'' +
                ", approvalDateTime='" + approvalDateTime + '\'' +
                ", amount=" + amount +
                '}';
    }
}

