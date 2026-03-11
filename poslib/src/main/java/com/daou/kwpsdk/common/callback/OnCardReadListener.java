package com.daou.kwpsdk.common.callback;

import com.daou.kwpsdk.common.model.CardData;

/**
 * 카드 읽기 결과 리스너
 */
public interface OnCardReadListener {

    /**
     * 카드 읽기 성공
     * @param cardData 읽은 카드 데이터
     */
    void onCardReadSuccess(CardData cardData);

    /**
     * 카드 읽기 실패
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    void onCardReadFailure(int errorCode, String errorMessage);

    /**
     * 카드 읽기 대기 중 (사용자에게 카드 삽입/접촉 안내)
     * @param message 안내 메시지
     */
    void onWaitingForCard(String message);
}

