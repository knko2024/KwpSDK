package com.daou.kwpsdk.reader.usb;


import static com.daou.kwpsdk.reader.usb.CardReaderConstants.CERTIFYSWNUM;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.JavaScriptFS;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.MODELVERSION;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.PROCSTEP_DONE;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.ThPayStepIDLE;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.ThReaderIntergrity;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.ThTranstionLaunch;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.ThTranstionRunPayment;
import static com.daou.kwpsdk.reader.usb.CardReaderConstants.writeLogData;
import static com.daou.kwpsdk.reader.usb.KwpSdk.ThProcStep;

import android.content.Context;
import android.util.Log;

import com.daou.kwpsdk.common.util.DaouDataUtil;
import com.daou.kwpsdk.common.util.ReaderLogUtil;
import com.daou.kwpsdk.common.util.UtlTime;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReaderComm {
    String TAG = "DDRdrLibrary";
    D2xxManager ftdid2xx;
    FT_Device ftDevice;
    Context mContext;
    int openIndex = 0;
    int devCount = 0;
    byte[] RcvBytes = new byte[4096];
    byte aByte;
    byte[] finalData;

    CardReaderConstants.CardReadListener mCardRdrListener;
    DaouReaderPacket mDaouRdrPkt = new DaouReaderPacket();

    byte tempDataBuffer[] = new byte[500];

    ReaderComm(Context mmContext, CardReaderConstants.CardReadListener mListener) {
        mContext = mmContext;
        mCardRdrListener = mListener;
    }

    // 230517 kkn
    ReaderComm(Context mmContext) {
        mContext = mmContext;
    }

    public void ReaderClear() {
        ftdid2xx = null;
        ftDevice = null;
    }

    public boolean ReaderOpen() {
        D2xxManager.DriverParameters params = new D2xxManager.DriverParameters();
        params.setReadTimeout(6000);

        //TODO [USB 리더기 장치 연결확인 및 리더기 연결]
        devCount = ftdid2xx.createDeviceInfoList(mContext);
        //writeLogData("[장치인식 갯수]:" + devCount);
        //Log.d("KiwoomOrder", "[createDeviceInfoList]" + devCount);

        if (devCount <= 0) {
           //  Toast.makeText(mContext, "리더기 연결 확인 요망(1)", Toast.LENGTH_LONG).show();
            return false;
        } else {
            try {
                if (ftDevice == null) {
                    ftDevice = ftdid2xx.openByIndex(mContext, 0);
                    Log.d(TAG, "ftDevice = " + ftDevice);

                    if (ftDevice == null) {
                     //   Toast.makeText(mContext, "리더기 연결 확인 요망(2)", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    if (ftDevice.isOpen() == false) {
                        Log.d(TAG, "Device_number =" + Integer.toString(devCount));
                        ftDevice = ftdid2xx.openByIndex(mContext, openIndex);

                        Log.d(TAG, "openIndex =" + openIndex);
                        Log.d(TAG, "ftDevice  =" + ftDevice);
                        if (ftDevice == null) {
                          //  Toast.makeText(mContext, "리더기 연결 확인 요망(3)", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } else {
                        Log.d(TAG, "ALREADY OPEN");
                    }
                }

                if (ftDevice.isOpen() == true) {
                    // writeLogData("[리더기 OPEN]:TRUE");
                    ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
                    ftDevice.setBaudRate(115200);
                    ftDevice.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
                    ftDevice.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x00, (byte) 0x00);
                    ftDevice.setLatencyTimer((byte) 16);
                    ftDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                    Log.d(TAG, "devCount:" + devCount + " open index:" + openIndex);

                    return true;
                } else {
                    Log.d(TAG, "ftDev not OPEN");
                    Log.d(TAG, "Need to get permission!");
                    return false;
                }
            } catch (Exception e) {
                // RdrConnectCnt = 3;
                Log.d(TAG, "EXEPTION_ReaderOpen");
                // writeLogData("[ERROR]" + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }


    int getDevCount() {
        devCount = ftdid2xx.createDeviceInfoList(mContext);
        return devCount;
    }

    void ReaderClose() {
        ftDevice.close();
    }

    public void ReaderInitial() {
        try {
            ftdid2xx = D2xxManager.getInstance(mContext);
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }
    }

    int ThreadTransactionProc(int TransReqType) {
        int ProcStep = 0;
        //TransReqType = ThTranstionRunPayment;
        switch (ThProcStep) {
            case ThPayStepIDLE:
                Log.d(TAG, "ThPayStepIDLE");
                break;

            case ThTranstionLaunch:
                Log.d(TAG, "ThTranstionLaunch");
                TransactionLaunch(TransReqType);
                ThProcStep = ThPayStepIDLE;
                break;

            case ThTranstionRunPayment:
                Log.d(TAG, "ThTranstionRunPayment");
                TransactionCredit(TransReqType);
                ThProcStep = ThPayStepIDLE;
                break;


            case ThReaderIntergrity:
                Log.d(TAG, "ThReaderIntergrity");
                TransactionReaderIntergrity(TransReqType);
                ThProcStep = ThPayStepIDLE;
                break;
        }
        return ProcStep;
    }

    void TransactionLaunch(int TransType) {
        int ret = ReaderInterChangePacket(0, mDaouRdrPkt, 30000);
        TransType = PROCSTEP_DONE;

        if (ret != COMCHK_OK) {
            Log.d(TAG, "TransactionCredit_RdrComErrorCode");
            String retval = RdrComErrorCode(mDaouRdrPkt.gethPacID());
            mCardRdrListener.onCardReaderResult(retval);
        }
    }

    void TransactionCredit(int TransType) {
         int ret = ReaderInterChangePacket(0, mDaouRdrPkt, 30000);
        TransType = PROCSTEP_DONE;

        if (ret != COMCHK_OK) {
            Log.d(TAG, "TransactionCredit_RdrComErrorCode");
            String retval = RdrComErrorCode(mDaouRdrPkt.gethPacID());
            mCardRdrListener.onCardReaderResult(retval);
        }
    }

    void TransactionReaderIntergrity(int TransType) {
       int ret = ReaderInterChangePacket(0, mDaouRdrPkt, 30000);
        TransType = PROCSTEP_DONE;
        if (ret != COMCHK_OK) {
            Log.d(TAG, "TransactionCredit_RdrComErrorCode");
            String retval = RdrComErrorCode(mDaouRdrPkt.gethPacID());

            byte[] retvalBytes = retval.getBytes();
            ReaderLogUtil.data("RECV",retvalBytes,retval.length());

            mCardRdrListener.onCardReaderResult(retval);
        }
    }

    int ReaderInterChangePacket(int TransType, DaouReaderPacket RdrPkt, int Timeout) {
        int bStatus = 0;
        int iSep;
        boolean isRdrOpen = false;



        iSep = COMSTEP_MAKE_JOBCODE;

        while (iSep != COMSTEP_DONE) {
            switch (iSep) {
                case COMSTEP_COMP:
                    Log.d(TAG, "#######COMSTEP_COMP#######");
                    iSep = COMSTEP_DONE;
                    break;

                case COMSTEP_ERROR:     // Error
                    Log.d(TAG, "#######COMSTEP_ERROR#######");
                    // 에러코드를 읽어온다.
                    if (RdrPkt.hPacID == RID_K980_CAN_TRN) {
                        Log.d(TAG, "#######K980_CANCEL#######");
                    } else {
                        Log.d(TAG, "#######COMM ERROR#######");
                        //   Toast.makeText(mContext,"리더기 연결 확인 요망(3)",Toast.LENGTH_LONG).show();
                        //   String retval = RdrComErrorCode(mDaouRdrPkt.gethPacID());
                        //   mCardRdrListener.onCardReaderResult(retval);
                        Arrays.fill(tempDataBuffer, (byte) 0);
                    }
                    iSep = COMSTEP_DONE;
                    break;

                case COMSTEP_CANCEL:
                    Log.d(TAG, "#######COMSTEP_CANCEL#######");
                    ReaderClose();
                    mDaouRdrPkt.sethPacID(RID_K980_CAN_TRN);
                    iSep = COMSTEP_MAKE_JOBCODE;
                    break;

                case COMSTEP_TIMEOUT:
                    Log.d(TAG, "#######COMSTEP_TIMEOUT#######");
                    //  mDaouRdrPkt.sethPacID(RID_K980_CAN_TRN);

                    ReaderClose();
                    isRdrOpen = ReaderOpen();   // 포트 오픈
                    if (isRdrOpen == false) {      // 포트 오픈 실패
                        writeLogData("[리더기 연결 실패]");
                        bStatus = COMCHK_ERR_PORT_OPEN_FAIL;
                        iSep = COMSTEP_ERROR;
                        break;
                    }
                    ReaderSendPacketK980(RdrPkt);
                    iSep = COMSTEP_ERROR;
                    break;

                case COMSTEP_MAKE_JOBCODE:
                    Log.d(TAG, "#######COMSTEP_MAKE_JOBCODE#######");
                    isRdrOpen = ReaderOpen();               // 포트 오픈
                    if (isRdrOpen == false) {               // 포트 오픈 실패
                        writeLogData("[리더기 연결 실패]");
                        bStatus = COMCHK_ERR_PORT_OPEN_FAIL;
                        iSep = COMSTEP_ERROR;
                        break;
                    }
                    ReaderMakeSendPacket(RdrPkt);
                    iSep = COMSTEP_SEND_PACKET;

                    break;

                case COMSTEP_SEND_PACKET:
                    Log.d(TAG, "#######COMSTEP_SEND_PACKET#######");
                    ReaderLogUtil.data("SEND",finalData, finalData.length);

                    // 전문 송신
                    boolean ret = ReaderWritePacket(finalData, finalData.length);
                    if (!ret) {
                        //return COMCHK_ERR_PORT_SEND;
                        iSep = COMSTEP_ERROR;
                    } else {
                        iSep = COMSTEP_RCV_ACK;
                    }
                    break;

                case COMSTEP_RCV_ACK: // ACK 수신
                    Log.d(TAG, "#######COMSTEP_RCV_ACK#######");
                    bStatus = RdrPortReceive(DaouDataUtil.VAL_ACK, 3000);
                    if (bStatus == COMCHK_OK) {
                        Log.d(TAG, "#######bStatus_COMCHK_OK#######");
                        iSep = COMSTEP_RCV_PACKET;
                    } else if (bStatus == COMCHK_CANCEL_EVENT) {
                        Log.d(TAG, "#######COMCHK_CANCEL_EVENT#######");
                        iSep = COMSTEP_CANCEL;        // 거래취소
                    } else if (bStatus == COMCHK_TIMEOUT) {
                        Log.d(TAG, "#######COMCHK_TIMEOUT#######");
                        iSep = COMSTEP_TIMEOUT;        // 거래취소
                    } else {
                        Log.d(TAG, "#######COMSTEP_RCV_PACKET#######");
                        iSep = COMSTEP_ERROR;            // 에러
                    }
                    break;

                case COMSTEP_RCV_PACKET:
                    writeLogData("#######COMSTEP_RCV_PACKET#######");
                    bStatus = RdrPortReceive((byte) 0x00, 1000 * 90);
                    //
                    Log.d(TAG, "#######COMSTEP_RCV_PACKET_RETURN#######" + bStatus);
                    if (bStatus == COMCHK_OK)
                        iSep = COMSTEP_DIV_PACKET;
                    else if (bStatus == COMCHK_CANCEL_EVENT) {
                        iSep = COMSTEP_CANCEL;                                // 거래취소
                    } else if (bStatus == COMCHK_TIMEOUT) {
                        iSep = COMSTEP_TIMEOUT;
                    } else {
                       /* if (bStatus == COMCHK_TIMEOUT) {
                            ReqReaderK980A();
                            iSep = COMSTEP_SEND_PACKET;
                        }
                        else{*/
                        iSep = COMSTEP_ERROR;                                // 에러
                        // }

                    }
                    break;
                case COMSTEP_DIV_PACKET:                                // 수신전문 파싱
                    Log.d(TAG, "#######COMSTEP_DIV_PACKET#######");

                     String retval = RdrRcvPktDiv(RdrPkt);
                    //Log.d(TAG,"#retval:" + retval);
                    mCardRdrListener.onCardReaderResult(retval);
                    Arrays.fill(tempDataBuffer, (byte) 0);

                    iSep = COMSTEP_DONE;
                    break;
            }
        }
        ReaderClose();
        return bStatus;
    }
    String RdrComErrorCode(int value) {
        String retval = "";
        byte[] buffer = new byte[200];
        byte[] src_data;
        byte[] finalData;

        int src_len;
        int pos = 0;

        switch (value) {
            case RID_K100_INP_CARD:
                pos += DaouDataUtil.makePacket(buffer, pos, "K110".getBytes());
                break;
            case RID_K800_CER_SECURITY:
                pos += DaouDataUtil.makePacket(buffer, pos, "K810".getBytes());
                break;
            case RID_K820_CER_SESSION:
                pos += DaouDataUtil.makePacket(buffer, pos, "K830".getBytes());
                break;
            case RID_K840_KEY_DOWN:
                pos += DaouDataUtil.makePacket(buffer, pos, "K850".getBytes());
                break;
            case RID_K900_FRM_STATUS:
                pos += DaouDataUtil.makePacket(buffer, pos, "K910".getBytes());
                break;
            case RID_K920_CHK_INTEGRITY:
                pos += DaouDataUtil.makePacket(buffer, pos, "K930".getBytes());
                break;
            case RID_K980_CAN_TRN:
                pos += DaouDataUtil.makePacket(buffer, pos, "K990".getBytes());
                break;
        }

        //   pos +=  DaouDataUtil.makePacket(buffer, pos, "K110".getBytes());
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, "K000".getBytes());
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
        pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);

        Log.i("KiwoomOrder", "RdrComErrorCode:" + pos);

        byte[] copy = new byte[pos];
        System.arraycopy(buffer, 0, copy, 0, pos);
        // Log.d(TAG,"after_copy:" + new String(copy));

        retval = new String(copy);

        return retval;
    }
    boolean ReaderMakeSendPacket(DaouReaderPacket RdrPkt) {
    //  DaouReaderComm mDaouRdrComm  = new DaouReaderComm();
    //  DaouReaderPacket mDaouRdrPkt = new DaouReaderPacket();
        Log.d(TAG, "ReaderMakeSendPacket:" + RdrPkt.gethPacID());

        byte[] buffer = new byte[700];
        byte[] src_data;
        int src_len;
        int pos = 0;

        switch (RdrPkt.gethPacID()) {
            case RID_K100_INP_CARD:
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                        // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_CARD_INPUT_REQ.getBytes());            // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                        // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                // pos +=  DaouDataUtil.makePacket(buffer, pos, mmTerminalInfo.getTerNumber().getBytes());              // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, "99999900".getBytes(StandardCharsets.UTF_8));      // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                // 거래금액
                //pos +=  DaouDataUtil.makePacket(buffer, pos, "1004".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getSaleAmount().getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                // 입력요청구분
                pos += DaouDataUtil.makePacket(buffer, pos, "1".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);


                break;

            case RID_K800_CER_SECURITY:
                // Log.d(TAG,"RID_K800_CER_SECURITY_mDaouRdrPkt.getTrmlid:" + mDaouRdrPkt.getTrmlid());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                         // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_CERTIFY_SECURITY.getBytes());          // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                        // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));                  // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);
                //ReqReaderK800A(mDaouRdrPkt.getTrmlid());
                break;

            case RID_K820_CER_SESSION:
                // ReqReaderK820A(mDaouRdrPkt.getTrmlid(),RdrPkt.getServRandomeKey());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                     // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_CERTIFY_SESSIONKEY.getBytes());    // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                    // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));              // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getServRandomeKey().getBytes(StandardCharsets.UTF_8));                                 // 서버 Random Key
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);

                break;

            case RID_K840_KEY_DOWN:
                //ReqReaderK840A(mDaouRdrPkt.getTrmlid(),RdrPkt.getSecurityKey(),RdrPkt.getSecurityMac());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                     // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_SECURITYKEY_DOWN.getBytes());    // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                    // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));              // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getSecurityKey().getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getSecurityMac().getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);
                break;

            case RID_K900_FRM_STATUS:
                //ReqReaderK900A();
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                 // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_STATUST_INFORM.getBytes());    // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                //pos +=  DaouDataUtil.makePacket(buffer, pos, "99999900".getBytes());                            // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);

                break;

            case RID_K920_CHK_INTEGRITY:
                //ReqReaderK920A();
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                 // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_INTERGRITY.getBytes());    // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));   // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);
                break;

            case RID_K980_CAN_TRN:
                //ReqReaderK980A();
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
                pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                 // 전문길이
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_TRANS_CANCEL.getBytes());    // 전문번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                // 거래일시
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));   // 단말기번호
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
                pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);
                break;
        }

        finalData = new byte[pos + 4];
        finalData = MakeCRC(buffer, pos);

        return true;
    }


    boolean ReaderSendPacketK980(DaouReaderPacket RdrPkt) {
        //   DaouReaderComm      mDaouRdrComm    = new DaouReaderComm();
        //   DaouReaderPacket    mDaouRdrPkt     = new DaouReaderPacket();

        byte[] buffer = new byte[700];
        byte[] src_data;
        int src_len;
        int pos = 0;

        //ReqReaderK980A();
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_STX);
        pos += DaouDataUtil.makePacket(buffer, pos, "    ".getBytes());                                 // 전문길이
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.READER_TRANS_CANCEL.getBytes());    // 전문번호
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.NowTime().getBytes());                // 거래일시
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, RdrPkt.getTrmlid().getBytes(StandardCharsets.UTF_8));   // 단말기번호
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_FS);
        pos += DaouDataUtil.makePacket(buffer, pos, DaouDataUtil.VAL_ETX);

        finalData = new byte[pos + 4];
        finalData = MakeCRC(buffer, pos);

        ReaderWritePacket(finalData, finalData.length);

        //Arrays.fill(finalData,(byte)0x00);   // 230428 kkn
        return true;
    }
    boolean ReaderWritePacket(byte[] bBytes, int iLength) {
        if (ftDevice == null) {
            writeLogData("! ftDevice = null");
            return false;
        }

        if (ftDevice.isOpen() == false) {
            Log.d("KiwoomOrder", "getDevCount():" + getDevCount() +"\t" + "ftDevice.isOpen():" + ftDevice.isOpen());
            return false;
        }
        ReaderLogUtil.data("SEND",bBytes, iLength);
        ReaderLogUtil.d("SEND", new String(bBytes));

        ftDevice.purge((byte) 0x03);                                 // 230428 kkn    buffer flush
        ftDevice.write(bBytes, iLength);
        //writeLogData("SEND :" + new String(bBytes));
        return true;
    }

    int RdrPortReceive(byte ucMode, int iTimeout) {
       // Log.d("KiwoomOrder", "RdrPortReceive()" + "Mode:" + ucMode);
       // final String METHOD_NAME = "RdrPortReceive";

        Log.i(TAG, "Waiting to receive data. Mode: " + ucMode + ", Timeout: " + iTimeout);
        byte aByte;

        int iCmpCount = 0;
        int iNakCount = 0;
        int rcv_stx = 0;
        int rcvlen = 0;
        int usMakCrc = 0;
        int usRcvCrc = 0;
        int bStatus = 0;
        int iQueueLen = 0;

        byte temp[] = new byte[4];
        float timerValue = 100;
        int iLoopDone = 0;

        DaouReaderComm daouRdComm = new DaouReaderComm();
        daouRdComm.m_iRcvPacLen = 0;
        iCmpCount = 0;

        int iTimerId = 0;
        iTimerId = UtlTime.TimerStart(iTimeout);


        if (iTimeout > 0) {
            //     startTimer();
            //    timeTimer.start();

            iTimerId = UtlTime.TimerStart(iTimeout);
        }
        EvtTimeout = false;

        while (true) {
            if (iTimeout != 0) {
                /*
                if (EvtTimeout == true) {
                    writeLogData("[COMCHK_TIMEOUT EVENT]");
                    bStatus = COMCHK_TIMEOUT;
                    EvtTimeout = false;
                    iLoopDone = 1;
                    break;
                }

                 */

                if (UtlTime.TimerCheck(iTimerId) <= 0) {
                    writeLogData("[COMCHK_TIMEOUT EVENT]");
                    bStatus = COMCHK_TIMEOUT;
                    EvtTimeout = false;
                    iLoopDone = 1;
                    break;
                }
            }

            if (EvtCancel == true) {
                writeLogData("[CANCEL EVENT]");
                bStatus = COMCHK_CANCEL_EVENT;
                EvtCancel = false;
                iLoopDone = 1;
                break;
            }

            iQueueLen = ftDevice.getQueueStatus();
             if (iQueueLen <= 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            } else {
                Arrays.fill(RcvBytes, (byte) 0);
                int ret = 0;
                if (ucMode != 0) {  // ACK MODE
                    ret = ftDevice.read(RcvBytes, 1, 50);
                   // Log.d("KiwoomOrder", "> Recv" + "+ Data"+ "[" + RcvBytes.length + "] = " + DaouDataUtil.toHexString(RcvBytes[0]));

                    if (ret <= 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        continue;
                    }
                    //Log.d("KiwoomOrder", "VAL_ACK:" + iCmpCount);
                    //Log.d("KiwoomOrder", "Rcv Data = " + RcvBytes[0]);

                    if (RcvBytes[0] == DaouDataUtil.VAL_ACK) {
                        iCmpCount++;
                        if (iCmpCount >= 3) {
                            //Log.d("KiwoomOrder", "VAL_ACK:" + iCmpCount);
                            bStatus = COMCHK_OK;
                            iLoopDone = 1;
                            break;
                        }
                    } else if (RcvBytes[0] == DaouDataUtil.VAL_NAK) {
                        iNakCount++;
                        if (iNakCount >= 3) {
                            Log.d("KiwoomOrder", "VAL_NAK:" + iNakCount);
                            bStatus = COMCHK_ERR_NAK_RCV;
                            iLoopDone = 1;
                            break;
                        }
                    }
                    continue;
                } else {
                    byte[] QueueBuffer = new byte[iQueueLen];
                    ret = ftDevice.read(RcvBytes, iQueueLen, 50);
                    System.arraycopy(RcvBytes, 0, QueueBuffer, 0, QueueBuffer.length);
                   // Log.d("KiwoomOrder", "> Rcv Data" + "[" + QueueBuffer.length + "] = " + DaouDataUtil.toHexString(QueueBuffer));
                    ReaderLogUtil.data("RECV", QueueBuffer, QueueBuffer.length);
                    ReaderLogUtil.d("RECV", new String(QueueBuffer));
                    if (ret <= 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        continue;
                    }
                }
            }


            for (int bufcnt = 0; bufcnt < iQueueLen; bufcnt++) {
                aByte = RcvBytes[bufcnt];
                //   Log.d(TAG,"aByte:" +  aByte);
                //   Log.d(TAG,"rcv_stx:" +rcv_stx);
                if (rcv_stx == 0) {
                    if (aByte == DaouDataUtil.VAL_STX) {    // ACK (0x06) 5번 입력받은후 STX 입력들어옴.
                        //Log.d("KiwoomOrder", "ENTER_STX");
                        // writeLogData("ENTER_STX");
                        rcv_stx = 1;
                        rcvlen = 0;
                        daouRdComm.m_iRcvPacLen = 0;
                        tempDataBuffer[0] = aByte;
                        // memset(stRedCom.m_ucRcvPacBuf, 0x00, sizeof(stRedCom.m_ucRcvPacBuf));
                    }
                }
                if (rcv_stx == 1) {
                    daouRdComm.mucRcvPacBuf.put(aByte);
                    daouRdComm.m_iRcvPacLen++;
                    // Log.d("KiwoomOrder", "daouRdComm.m_iRcvPacLen:" + daouRdComm.m_iRcvPacLen);
                    if (daouRdComm.m_iRcvPacLen == 5) {
                        // receive lenth save..
                        for (int i = 0; i < 4; i++) {
                            tempDataBuffer[i + 1] = temp[i] = daouRdComm.mucRcvPacBuf.get(i + 1);
                        }
        /*
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf.temp:" +   String.valueOf(temp[0]));
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf.temp:" +   String.valueOf(temp[1]));
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf.temp:" +   String.valueOf(temp[2]));
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf.temp:" +   String.valueOf(temp[3]));
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf.String:" +   new String(temp, StandardCharsets.UTF_8));
        */
                        rcvlen = Integer.parseInt(new String(temp, StandardCharsets.UTF_8));                                           // 응답전문 길이
                        //Log.d("KiwoomOrder", "tempDataBuffer:" + new String (tempDataBuffer));

                    } else if ((daouRdComm.m_iRcvPacLen > 5 && rcvlen == daouRdComm.m_iRcvPacLen)) {
            /*
                    Log.d("KiwoomOrder", "m_iRcvPacLen:" + daouRdComm.m_iRcvPacLen);
                    Log.d("KiwoomOrder", "tempDataBuffer[0]"+ tempDataBuffer[0] );
                    Log.d("KiwoomOrder", "tempDataBuffer[1]"+ tempDataBuffer[1] );
                    Log.d("KiwoomOrder", "tempDataBuffer[2]"+ tempDataBuffer[2] );
                    Log.d("KiwoomOrder", "tempDataBuffer[3]"+ tempDataBuffer[3]);
                    Log.d("KiwoomOrder", "tempDataBuffer[4]"+ tempDataBuffer[4]);
           */
                        // 리더기에서 읽은 데이터를 CRC 생성
                        for (int i = 5; i < daouRdComm.m_iRcvPacLen - 4; i++) {
                            //Log.d("KiwoomOrder", "tempDataBuffer"+ "[" + i + "]" + daouRdComm.mucRcvPacBuf.get(i) );
                            tempDataBuffer[i] = daouRdComm.mucRcvPacBuf.get(i);
                        }

                        int crc = DaouDataUtil.calCRC(tempDataBuffer, rcvlen - 4);
                        byte[] src_data;
                        src_data = DaouDataUtil.toByteArray((short) crc);
                        String rcvCRC = DaouDataUtil.toHexString(src_data);
                        //Log.d("KiwoomOrder", "daouRdComm.crc_str:" +   rcvCRC);

                        //리더기에서 읽은 데이트의 마지막 4자리 CRC 값
                    /*
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf:" +   daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 4) );
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf:" +   daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 3) );
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf:" +   daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 2) );
                    Log.d("KiwoomOrder", "daouRdComm.mucRcvPacBuf:" +   daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 1) );
                    */
                        temp[0] = daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 4);
                        temp[1] = daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 3);
                        temp[2] = daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 2);
                        temp[3] = daouRdComm.mucRcvPacBuf.get(daouRdComm.m_iRcvPacLen - 1);
                        //  Log.d("KiwoomOrder", "markCRC:" +   new String(temp, StandardCharsets.UTF_8));

                        if (new String(temp, StandardCharsets.UTF_8).equals(rcvCRC)) {    //crc error
                            bStatus = COMCHK_OK;
                            Log.d("KiwoomOrder", "[CRC-OK]");
                        } else {
                            bStatus = COMCHK_ERR_DATA_RCV;
                            Log.d("KiwoomOrder", "[CRC-ERROR]");
                        }
                        iLoopDone = 1;
                        break;
                    }
                }
            }

            if (iLoopDone == 1) {
                Log.d("KiwoomOrder", "==============iLoopDone==========");
                break;
            }
        }

        if (iTimeout > 0) {
            //  timeTimer.cancel();
            //  stopTimer();
            UtlTime.TimerStop(iTimerId);
        }
        //Log.d("KiwoomOrder", "> Rcv bStatus=" + bStatus);
        return bStatus;
    }

    int RdrIniPacket(int bCodeIndex, String var1, String var2, String var3) {
        mDaouRdrPkt.sethPacID(bCodeIndex);

        switch (mDaouRdrPkt.gethPacID()) {
            case RID_K100_INP_CARD:
                mDaouRdrPkt.setTrmlid(var1);
                mDaouRdrPkt.setSaleAmount(var2);
                mDaouRdrPkt.setInputType(var3);
                break;
        }
        //Log.d(TAG, "bCodeIndex_gethPacID:" + mDaouRdrPkt.gethPacID());
        //Log.d(TAG, "bCodeIndex_getTrmlid:" + mDaouRdrPkt.getTrmlid());

        return COMCHK_OK;
    }

    int RdrIniPacket(int bCodeIndex, String var) {
        mDaouRdrPkt.sethPacID(bCodeIndex);
        switch (mDaouRdrPkt.gethPacID()) {
            case RID_K100_INP_CARD:
                mDaouRdrPkt.setSaleAmount(var);
                break;

            case RID_K900_FRM_STATUS:
                mDaouRdrPkt.setTrmlid(var);
                break;

            case RID_K800_CER_SECURITY:
                Log.d(TAG, "RID_K800_CER_SECURITY:" + var);
                mDaouRdrPkt.setTrmlid(var);
                break;

            case RID_K820_CER_SESSION:
                Log.d(TAG, "RID_K820_CER_SESSION:" + var);
                mDaouRdrPkt.setServRandomeKey(var);
                break;

            case RID_K840_KEY_DOWN:
                Log.d(TAG, "RID_K840_DOW_SKEY:" + var);
                // mDaouRdrPkt.setSecurityKey();
                // mDaouRdrPkt.setSecurityMac();
                break;

            case RID_K920_CHK_INTEGRITY:
                Log.d(TAG, "RID_K920_CHK_INTEGRITY:" + var);
                mDaouRdrPkt.setTrmlid(var);
                break;

            case RID_K980_CAN_TRN:
                Log.d(TAG, "RID_K980_CAN_TRN:" + var);
                mDaouRdrPkt.setTrmlid(var);
                break;
        }

        Log.d(TAG, "bCodeIndex_gethPacID:" + mDaouRdrPkt.gethPacID());
        Log.d(TAG, "bCodeIndex_getTrmlid:" + mDaouRdrPkt.getTrmlid());

        return COMCHK_OK;
    }


    String RdrRcvPktDiv(DaouReaderPacket RdrPkt) {
        int iPLen = 0;
        int pos = 0;
        String retval = "";

        byte[] buffer = new byte[1024];
        byte[] src_data;
        byte[] finalData;

        String SplitText[] = {""};
        String value = "";
        String[] SplitETX = new String[1];

        iPLen += CopyVarpFS(mDaouRdrPkt.rLength, tempDataBuffer, new String(RdrPkt.rLength).length(), 0);
        iPLen = CopyVarpFS(mDaouRdrPkt.rTeleNo, tempDataBuffer, new String(RdrPkt.rTeleNo).length(), iPLen);

        /*Log.i("KiwoomOrder","tempDataBuffer:" +   new String(tempDataBuffer));
        Log.i("KiwoomOrder","iPLen" + iPLen + "   "  +"!RdrRcvPktDiv_rLength:" +new String(mDaouRdrPkt.rLength ));
        Log.i("KiwoomOrder","iPLen" + iPLen + "   " +"!RdrRcvPktDiv_rTeleNo:"+new String(mDaouRdrPkt.rTeleNo));
        Log.i("KiwoomOrder","mDaouRdrPkt.hPacID:" +  mDaouRdrPkt.gethPacID());*/

        switch (RdrPkt.hPacID) {
            case RID_K100_INP_CARD:
            case RID_K180_INP_CARD:
                Log.i("KiwoomOrder", "RID_K100_Rdr");
                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = value.split(Character.toString((char) DaouDataUtil.VAL_FS));
                for (int i = 0; i <= SplitText.length - 1; i++) {
                    if (i == 0) {
                        //  Log.i("KiwoomOrder","SplitText"+"[" + i + "]" + SplitText[0].substring(1,5));
                        //  Log.i("KiwoomOrder","SplitText"+"[" + i + "]" + SplitText[0].substring(5,9));
                      /*
                            mDaouRdrPkt.rLength = SplitText[0].substring(1,5).getBytes(StandardCharsets.UTF_8);
                          mDaouRdrPkt.rTeleNo = SplitText[0].substring(5,9).getBytes(StandardCharsets.UTF_8);
                       */
                    }

                    // Log.i("KiwoomOrder","SplitText"+"[" + i + "]" + SplitText[i]);
                    // mDaouRdrPkt.rResCode = SplitText[i].getBytes(StandardCharsets.UTF_8);

                    if (i + 1 == SplitText.length) {
                        SplitETX = SplitText[i].split(Character.toString((char) DaouDataUtil.VAL_ETX));
                        // Log.i("KiwoomOrder","endSplit_0:" + SplitETX[0] );
                    }
                }

                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rCardType = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rCardData = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rChipData = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rCvmPin = SplitText[5].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[6].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rServiceCode = SplitText[7].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rMaskCardNo = SplitText[8].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rAIDCountryCode = SplitETX[0].getBytes(StandardCharsets.UTF_8);


                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceInfo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rCardType);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rCardData);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rChipData);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rServiceCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rMaskCardNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rAIDCountryCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);

                //Log.i("KiwoomOrder","RID_K110_:" + pos);
                byte[] copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                //Log.d(TAG,"after_copy:" + new String(copy));

                retval = new String(copy);
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                break;

            case RID_K900_FRM_STATUS:                                                                // 리더기 상태 확인
                Log.i("KiwoomOrder", "RID_K900_FRM_STATUS");
                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = RcvPktSplit(value);
                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[5].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[6].getBytes(StandardCharsets.UTF_8);
/*
                Log.d(TAG,"COMSTEP_DIV_PACKET:" + new String(mDaouRdrPkt.rResCode) + DaouDataUtil.VAL_FS + new String(mDaouRdrPkt.rDeviceInfo) +  DaouDataUtil.VAL_FS + androidId);
                retval =  new String(mDaouRdrPkt.rResCode) + JavaScriptFS + new String(mDaouRdrPkt.rDeviceInfo) +  JavaScriptFS + androidId + JavaScriptFS + CERTIFYSWNUM  + JavaScriptFS + POSID  + JavaScriptFS +  MODELVERSION;
*/
                Arrays.fill(buffer, (byte) 0);
            //  pos +=  DaouDataUtil.makePacket(buffer, pos, "K910".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceInfo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);

                copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                Log.d(TAG,"after_copy:" + new String(copy));
                retval = new String(copy);
                break;

            case RID_K920_CHK_INTEGRITY:

                Log.i("KiwoomOrder", "RID_K920_CHK_INTEGRITY");
                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = RcvPktSplit(value);

                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceCheckTime = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[5].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[6].getBytes(StandardCharsets.UTF_8);

                Arrays.fill(buffer, (byte) 0);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceInfo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceCheckTime);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);

                Log.i("KiwoomOrder", "RID_K920_CHK_INTEGRITY:" + pos);
                copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                 Log.d(TAG,"after_copy:" + new String(copy));

                retval = new String(copy);
                break;


            case RID_K800_CER_SECURITY:
                Log.i("KiwoomOrder", "RID_K800_CER_SECURITY");
                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = RcvPktSplit(value);

                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rPkVersion = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[5].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[6].getBytes(StandardCharsets.UTF_8);


                Arrays.fill(buffer, (byte) 0);
                //pos +=  DaouDataUtil.makePacket(buffer, pos, "K810".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceInfo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rPkVersion);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);

                Log.i("KiwoomOrder", "RID_K800_CER_SECURITY_len:" + pos);
                copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                // Log.d(TAG,"after_copy:" + new String(copy));
                retval = new String(copy);
                break;

            case RID_K820_CER_SESSION:
                Log.i("KiwoomOrder", "RID_K820_CER_SESSION");

                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = RcvPktSplit(value);

                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.tServRandomKey = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[5].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[6].getBytes(StandardCharsets.UTF_8);

                Arrays.fill(buffer, (byte) 0);
                //pos +=  DaouDataUtil.makePacket(buffer, pos, "K830".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceInfo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.tServRandomKey);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);


                copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                // Log.d(TAG,"after_retval:" + new String(copy));
                retval = new String(copy);
                break;

            case RID_K840_KEY_DOWN:
                //  Log.i("KiwoomOrder","RID_K840_KEYDOWN");
                writeLogData("RID_K840_KEYDOWN");

                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = RcvPktSplit(value);

                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[5].getBytes(StandardCharsets.UTF_8);

                Arrays.fill(buffer, (byte) 0);
                //pos +=  DaouDataUtil.makePacket(buffer, pos, "K850".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, CERTIFYSWNUM.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, MODELVERSION.getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rDeviceInfo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);

                copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                retval = new String(copy);
                break;

            case RID_K980_CAN_TRN:
                Log.i("KiwoomOrder", "RID_K980_CAN_TRN");
                value = "";
                try {
                    value = new String(tempDataBuffer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SplitText = RcvPktSplit(value);

                mDaouRdrPkt.rLength = SplitText[0].substring(1, 5).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rTeleNo = SplitText[0].substring(5, 9).getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rResCode = SplitText[1].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[2].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[3].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[4].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rRFU = SplitText[5].getBytes(StandardCharsets.UTF_8);
                mDaouRdrPkt.rDeviceInfo = SplitText[6].getBytes(StandardCharsets.UTF_8);

                Arrays.fill(buffer, (byte) 0);
                // pos +=  DaouDataUtil.makePacket(buffer, pos, "K990".getBytes());
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rTeleNo);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, mDaouRdrPkt.rResCode);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                pos += DaouDataUtil.makePacket(buffer, pos, JavaScriptFS);
                copy = new byte[pos];
                System.arraycopy(buffer, 0, copy, 0, pos);
                // Log.d(TAG,"after_retval:" + new String(copy));
                retval = new String(copy);
                EvtCancel = false;  //  230428 kkn
                break;
        }
        return retval;
    }


    String[] RcvPktSplit(String text) {
        String RcvText[];
        String[] SplitETX = new String[1];

        RcvText = text.split(Character.toString((char) DaouDataUtil.VAL_FS));
        for (int i = 0; i <= RcvText.length - 1; i++) {
            if (i == 0) {
                //  writeLogData("RcvText"+"[" + i + "]"+ RcvText[0].substring(1,5) );
                //  writeLogData("RcvText"+"[" + i + "]"+ RcvText[0].substring(5,9));
                //  Log.i("KiwoomOrder","RcvText"+"[" + i + "]" + RcvText[0].substring(1,5));
                //  Log.i("KiwoomOrder","RcvText"+"[" + i + "]" + RcvText[0].substring(5,9));
            }
            // Log.i("KiwoomOrder","RcvText"+"[" + i + "]" + RcvText[i]);
            // writeLogData("RcvText"+"[" + i + "]"+RcvText[i]);
            if (i + 1 == RcvText.length) {
                SplitETX = RcvText[i].split(Character.toString((char) DaouDataUtil.VAL_ETX));
                //Log.i("KiwoomOrder","endSplit_0:" + SplitETX[0] );
                //writeLogData("EndETX:" + SplitETX[0] );
            }
        }
        return RcvText;
    }

    int CopyVarpFS(byte[] DataDst, byte[] DataSrc, int Size, int len) {
        // Log.i("KiwoomOrder","!DataDst:"+ new String(DataDst));
        int i;
        int iLen = 1;
        if (len != 0)
            len -= 1;

        iLen += len;
        for (i = 0; i < Size; i++) {
            if (DataSrc[iLen] == DaouDataUtil.VAL_FS || DataSrc[iLen] == DaouDataUtil.VAL_ETX) {    // FS check(종료로 인식.. 포인트 증가)
                iLen++;
                return iLen;
            }
            DataDst[i] = DataSrc[iLen++];
        }

        if (DataSrc[iLen] == DaouDataUtil.VAL_FS || DataSrc[iLen] == DaouDataUtil.VAL_ETX) {        // iSize 까지 다 채우고 나온뒤..
            iLen++;
        }
        //Log.i("KiwoomOrder","!iLen:"+ iLen);
        return iLen;
    }

    byte[] MakeCRC(byte[] buffer, int pos) {
        byte[] returnCRC = new byte[pos + 4];

        byte[] src_data;
        int src_len;

        returnCRC = new byte[pos + 4];
        System.arraycopy(buffer, 0, returnCRC, 0, pos);

        String data_len = "" + (pos + 4);
        data_len = DaouDataUtil.addCharBefore(data_len, '0', 4);

        src_data = data_len.getBytes();
        src_len = src_data.length;
        System.arraycopy(src_data, 0, returnCRC, 1, src_len);
        //Log.d(TAG,"crc_finalData:" + new String(returnCRC) + "__pos:" + pos);

        int crc = DaouDataUtil.calCRC(returnCRC, pos);
        src_data = DaouDataUtil.toByteArray((short) crc);
        String crc_str = DaouDataUtil.toHexString(src_data);
        src_data = crc_str.getBytes();
        src_len = src_data.length;
        System.arraycopy(src_data, 0, returnCRC, pos, src_len);
        pos += src_len;
        buffer = null;

        return returnCRC;
    }


    private final int COMSTEP_DONE = 0;
    private final int COMSTEP_COMP = 1;
    private final int COMSTEP_ERROR = 2;
    private final int COMSTEP_CANCEL = 3;
    private final int COMSTEP_MAKE_JOBCODE = 4;
    private final int COMSTEP_MAKE_PACKET = 5;
    private final int COMSTEP_SEND_PACKET = 6;
    private final int COMSTEP_RCV_ACK = 7;
    private final int COMSTEP_RCV_PACKET = 8;
    private final int COMSTEP_DIV_PACKET = 9;
    private final int COMSTEP_TIMEOUT = 10;
    private final int COMCHK_OK = 0;
    private final int COMCHK_CANCEL_EVENT = 1;
    private final int COMCHK_TIMEOUT = 2;
    private final int COMCHK_ERR_PORT_OPEN = 3;
    private final int COMCHK_ERR_PORT_ALREADY = 4;
    private final int COMCHK_ERR_PORT_SEND = 5;
    private final int COMCHK_ERR_PORT_OPEN_FAIL = 6;
    private final int COMCHK_ERR_DATA_RCV = 7;
    private final int COMCHK_ERR_NAK_RCV = 8;
    public final int RID_K980_CAN_TRN = 20;        // 거래 취소

    public final int RID_K100_INP_CARD = 1;        // 카드입력요청
    public final int RID_K180_INP_CARD = 2;        // 카드입력요청 (카드제거 없이 거래 가능)	//

    public final int RID_K300_ENC_DATA = 9;        // 데이터 암호화		//
    public final int RID_K400_CHK_ICCARD_TYPE = 10;        // 신용/현금 IC Check	//

    public final int RID_K700_TRS_PARAMETER = 13;        // 파라미터 전송
    public final int RID_K800_CER_SECURITY = 14;        // 보안인증
    public final int RID_K820_CER_SESSION = 15;        // 세션키 생성
    public final int RID_K840_KEY_DOWN = 16;        // 보안키 다운로드
    public final int RID_K900_FRM_STATUS = 17;        // 리더기 상태 확인
    public final int RID_K920_CHK_INTEGRITY = 18;        // 무결성 검사


    public static boolean EvtCancel = false;
    public static boolean EvtTimeout = false;


    public static final byte CARD_INPUT_TYPE_NOMAL = 0x01;    // 기본 입력 요청, 리더기에서 입력되는 대로 리턴, KEYIN 버튼생성
    public static final byte CARD_INPUT_TYPE_EVENT = 0x02;    // 이벤트 발생으로 입력된 카드를 읽기 위한 입력 요청		(사용안함)
    public static final byte CARD_INPUT_TYPE_IC = 0x03;    // MS카드 리딩으로 발생된 이벤트에 대해 IC카드 입력이 필요한 경우	(사용안함)
    public static final byte CARD_INPUT_TYPE_FALLBACK = 0x04;    // fall - back 거래 입력 요청
    public static final byte CARD_INPUT_TYPE_MS = 0x05;    // MS 입력 요청
    public static final byte CARD_INPUT_TYPE_ENC_KIN = 0x06;    // 암호화 된 KEYIN 입력 요청	//(멀티패드용)



}