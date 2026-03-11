package com.daou.kwpsdk.common.util;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DaouDataUtil {
    public static final byte VAL_STX = 0x02;
    public static final byte VAL_ETX = 0x03;
    public static final byte VAL_EOT = 0x04;
    public static final byte VAL_ENQ = 0x05;
    public static final byte VAL_ACK = 0x06;
    public static final byte VAL_NAK = 0x15;
    public static final byte VAL_DLE = 0x10;
    public static final byte VAL_FS = 0x1c;
    public static final String READER_INFO_SEND            =   "K000";
    public static final String READER_CARD_INPUT_REQ            =   "K100";
    public static final String READER_CERTIFY_SECURITY          =   "K800";
    public static final String READER_CERTIFY_SESSIONKEY        =   "K820";
    public static final String READER_SECURITYKEY_DOWN          =   "K840";
    public static final String READER_STATUST_INFORM            =   "K900";
    public static final String READER_INTERGRITY                =   "K920";
    public static final String READER_TRANS_CANCEL              =   "K980";
    Timer timer;
    int   nCnt;
    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static int makePacket(byte[]dst, int dst_pos, byte src) {
        dst[dst_pos] = src;
        return 1;
    }

    public static int makePacket(byte[]dst, int dst_pos, byte[] src) {
        System.arraycopy(src, 0, dst, dst_pos, src.length);
        return src.length;
    }

    public static int calCRC(byte[] data, int len) {
        int crc = 0;
        int pos = 0;

        while (--len >= 0) {
            crc = crc ^ (int) data[pos++] << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) > 0)
                    crc = crc << 1 ^ 0x1021;
                else
                    crc = crc << 1;
            }
        }
        return crc & 0xFFFF;
    }

    public static byte[] INTtoASCII0Lead(int nValue){
        byte[] pAscii = new byte[4];
        int      nDigit, nCnt;
        byte   cShare;

        nCnt = 0;

        if (nValue < 0) {
            nValue *= -1;

            pAscii[nCnt++] = '-';
        }

        nDigit = 4;

        if (nDigit >= 4) {
            cShare = (byte)(nValue / 1000);
            nValue = nValue % 1000;
            pAscii[nCnt++] = (byte) (cShare + '0');
        }
        if (nDigit >= 3) {
            cShare = (byte)(nValue / 100);
            nValue = nValue % 100;
            pAscii[nCnt++] = (byte) (cShare + '0');
        }
        if (nDigit >= 2) {
            cShare = (byte)(nValue / 10);
            nValue = nValue % 10;
            pAscii[nCnt++] = (byte) (cShare + '0');
        }
        if (nDigit >= 1) {
            cShare = (byte)nValue;
            pAscii[nCnt++] =(byte) (cShare + '0');
        }

        return   pAscii;
    }

    public static String addCharBefore(String strInput, char c, int targetLen) {
        if (strInput == null) {
            strInput = "";
        }
        String ret = strInput;
        for (int j = ret.length(); j < targetLen; j++){
            ret = c + ret;
        }
        return ret;
    }

    public void ReaderTimer(int period){

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 반복실행할 구문
                Log.d("KiwoomOrder","AlarmReaderTimer:" + period);
                TimeCount();
                // timer 종료
          //      timer.cancel();

            }
        };
        // timer 실행
        timer.schedule(timerTask, 0, period);

    }
    private void TimeCount(){
       // Log.d(TAG,"Count:" + nCnt);
        if(nCnt >= 10){
            timer.cancel();
        }
        nCnt++;
    }

    public static String NowTime(){
        Date d = Calendar.getInstance().getTime(); // Current time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss"); // Set your date format
        String currentData = sdf.format(d); // Get Date String according to date format
        return currentData;
    }

    public static byte[] toByteArray(short i) {
        byte[] array = new byte[2];

        array[1] = (byte) (i & 0xFF);
        array[0] = (byte) ((i >> 8) & 0xFF);

        return array;
    }

    public static String toHexString(byte[] array, int offset, int length) {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(short i) {
        return toHexString( toByteArray(i) );
    }

    public static String toHexString(byte[] array) {
        return toHexString(array, 0, array.length);
    }


}
