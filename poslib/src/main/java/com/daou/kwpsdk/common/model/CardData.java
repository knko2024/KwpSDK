package com.daou.kwpsdk.common.model;

/**
 * 카드 데이터 모델
 * 카드리더기에서 읽은 카드 정보를 담는 클래스
 */
public class CardData {

    /** 카드번호 (마스킹 처리된) */
    private String cardNumber;

    /** 카드 유효기간 (YYMM) */
    private String expireDate;

    /** Track2 데이터 */
    private String track2Data;

    /** IC 카드 여부 */
    private boolean isIcCard;

    /** 암호화된 카드 데이터 */
    private String encryptedData;

    /** 읽기 방식 (SWIPE, IC, NFC, MANUAL) */
    private ReadType readType;

    public enum ReadType {
        SWIPE,      // 마그네틱 스와이프
        IC,         // IC칩 삽입
        NFC,        // 비접촉(NFC)
        MANUAL      // 수기 입력
    }

    public CardData() {
    }

    public CardData(String cardNumber, String expireDate, String track2Data,
                    boolean isIcCard, String encryptedData, ReadType readType) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.track2Data = track2Data;
        this.isIcCard = isIcCard;
        this.encryptedData = encryptedData;
        this.readType = readType;
    }

    // Getters and Setters

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public boolean isIcCard() {
        return isIcCard;
    }

    public void setIcCard(boolean icCard) {
        isIcCard = icCard;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public ReadType getReadType() {
        return readType;
    }

    public void setReadType(ReadType readType) {
        this.readType = readType;
    }

    @Override
    public String toString() {
        return "CardData{" +
                "cardNumber='" + cardNumber + '\'' +
                ", expireDate='" + expireDate + '\'' +
                ", isIcCard=" + isIcCard +
                ", readType=" + readType +
                '}';
    }
}

