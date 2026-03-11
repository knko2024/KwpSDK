package com.daou.kwpsdk.reader;

import android.content.Context;

import com.daou.kwpsdk.common.callback.OnCardReadListener;

/**
 * 카드 리더기 공통 인터페이스
 * USB, Bluetooth 등 다양한 연결 방식의 리더기가 이 인터페이스를 구현합니다.
 */
public interface CardReader {

    /**
     * 리더기 연결 타입
     */
    enum ConnectionType {
        USB,
        BLUETOOTH,
        BLE,        // Bluetooth Low Energy
        SERIAL      // 시리얼 통신
    }

    /**
     * 리더기 초기화 및 연결
     * @param context Android Context
     * @throws Exception 연결 실패 시
     */
    void connect(Context context) throws Exception;

    /**
     * 리더기 연결 해제
     */
    void disconnect();

    /**
     * 리더기 연결 상태 확인
     * @return 연결 여부
     */
    boolean isConnected();

    /**
     * 카드 읽기 시작
     * @param listener 카드 읽기 결과 리스너
     */
    void readCard(OnCardReadListener listener);

    /**
     * 카드 읽기 중지
     */
    void cancelRead();

    /**
     * 리더기 연결 타입 반환
     * @return ConnectionType
     */
    ConnectionType getConnectionType();

    /**
     * 리더기 모델명 반환
     * @return 모델명
     */
    String getDeviceName();

    /**
     * 리더기 펌웨어 버전 반환
     * @return 펌웨어 버전
     */
    String getFirmwareVersion();
}

