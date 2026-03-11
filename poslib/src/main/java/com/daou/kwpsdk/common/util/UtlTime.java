package com.daou.kwpsdk.common.util;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtlTime {
    public static String GetSystemTime(String strTimeFormat)
    {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat(strTimeFormat);
        String strGetTime = sdf.format(date);

        return strGetTime;
    }

    public static byte[] GetSystemTimeToBa(String strTimeFormat)
    {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat(strTimeFormat);
        String strGetTime = sdf.format(date);

        return strGetTime.getBytes();
    }
    public static void SetSystemTime(String strTime)
    {
        /*
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat(strTimeFormat);
        String strGetTime = sdf.format(date);

        return strGetTime;
        */

    }


    static final int TIMER_COUNT = 100;
    static long[] hTimerUser = new long[TIMER_COUNT];
    public static int TimerStart(long loSetTime)
    {
        int iTimerNo = 0;

     //   UtlLog.printz("! Timer Set [%d] : %d\r\n", iTimerNo, loSetTime);

        for (iTimerNo = 0; iTimerNo < TIMER_COUNT; iTimerNo++)
        {
            if (hTimerUser[iTimerNo] <= 0L) {
                hTimerUser[iTimerNo] = System.currentTimeMillis() + loSetTime;
                return iTimerNo;
            }
        }

    //    printz("! Timer ID full [%d] : %d\r\n", iTimerNo, loSetTime);
        return -1;
    }


    public static Long TimerCheck(int iTimerNo)
    {
        if(iTimerNo < 0) return -1L;
        if(iTimerNo >= TIMER_COUNT) return -1L;

        long loCurrentMsTime = System.currentTimeMillis();  // 현재시간

        if(hTimerUser[iTimerNo] > loCurrentMsTime){ // 저장시간이 현재시간 보다 크면

            return hTimerUser[iTimerNo] - loCurrentMsTime;
        }

        // 타이머 만료
        hTimerUser[iTimerNo] = 0L;
        return 0L;
    }

    public static int TimerStop(int iTimerNo) {
        if(iTimerNo < 0) return -1;
        if(iTimerNo >= TIMER_COUNT) return -1;

        hTimerUser[iTimerNo] = 0L;
        return 0;
    }
}
