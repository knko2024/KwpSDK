package com.daou.kwpsdk.common.util;

import android.util.Log;

import java.util.Arrays;

public class ReaderLogUtil {
    private static final String TAG = "KWPSDK";

    // 일반적인 작업 로그
    public static void i(String method, String message) {
        Log.i(TAG, String.format("[%s] %s", method, message));
    }

    // 디버그 정보 로그
    public static void d(String method, String message) {
        Log.d(TAG, String.format("[%s] %s", method, message));
    }

    // 에러 로그
    public static void e(String method, String message, Throwable tr) {
        Log.e(TAG, String.format("[%s] %s", method, message), tr);
    }

    public static void e(String method, String message) {
        Log.e(TAG, String.format("[%s] %s", method, message));
    }

    // 데이터 송수신 로그
    public static void data(String direction, byte[] data, int length) {
        String hexData = DaouDataUtil.toHexString(Arrays.copyOf(data, length));
        Log.d(TAG, String.format("[%s] Length=%d, Data=%s", direction, length, hexData));
    }
}
