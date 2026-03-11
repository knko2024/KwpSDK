package com.daou.kwpsdk.reader.usb;

import android.util.Log;

public class CardReaderConstants {
    static String CERTIFYSWNUM = "##DAOU-ORDER1000";
    static String MODELVERSION = "106";

   // public static int ThProcStep = 0;
    public static final int ThPayStepIDLE = 0;
    public static final int ThTranstionLaunch = 1;
    public static final int ThTranstionRunPayment = 2;
    public static final int ThReaderIntergrity = 3;
    public static final int PROCSTEP_DONE = 0;
    public static final int PROCSTEP_COMPLETE = 1;
    public static final int PROCSTEP_ERROR_READER = 2;
    public static final int PROCSTEP_INPUT_CARD_READER = 10;
   // public static final String JavaScriptFS = "!@#$";

    public static final byte JavaScriptFS = 0x1c;


    public interface CardReadListener{
        void onCardReaderResult(String result);
    }

    public static void writeLogData(String logData) {
        Log.d("KiwoomOrder", logData);
    /*  String foldername   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/LOG_ORDER";
        String filename     = "/KiwoomOrder100.txt";
        String contents     = logData;
        String timeStamp    = currentDateTimeFully();

        logData = timeStamp + ":"+ contents;
        File dir            = new File (foldername);

        //디렉토리 폴더가 없으면 생성함
        if(!dir.exists()){
             dir.mkdir();
        }
        FileWriter fileWritter = null;
        try {
            fileWritter = new FileWriter(foldername + filename  ,false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(logData+"\n");
            bufferWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
