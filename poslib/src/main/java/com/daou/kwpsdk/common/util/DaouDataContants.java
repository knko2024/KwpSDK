package com.daou.kwpsdk.common.util;

public class DaouDataContants {
	public static final String CREDIT_REQ 									="0200";
	public static final String CREDIT_RES 									="0210";
	public static final String CREDIT_EMV_REQ							  	="0100";
	public static final String CREDIT_EMV_RES							  	="0110";
	public static final String CREDIT_CANCEL_REQ 							="0420";
	public static final String CREDIT_CANCEL_RES 							="0430";
	public static final String CREDIT_EMV_CANCEL_REQ 						="0400";
	public static final String CREDIT_EMV_CANCEL_RES 						="0410";
	public static final String TASK_CREDIT									="10";

	public static final String KAKAO_ACCEPT_CERTIFY_REQ 					="0200";
	public static final String KAKAO_CANCEL_CERTIFY_REQ 					="0420";
	public static final String KAKAO_TASK				 					="18";



	public static final String SEND_TC_ISSUER_SCRIPT_RESULT_REQ="0100";
	public static final String SEND_TC_ISSUER_SCRIPT_RESULT_RES="0110";
	public static final String TASK_SEND_TC_ISSUER_SCRIPT_RESULT ="78";

	public static final String CASH_RECEIPT_REQ="0200";
	public static final String CASH_RECEIPT_RES="0210";
	public static final String CASH_RECEIPT_CANCEL_REQ="0420";
	public static final String CASH_RECEIPT_CANCEL_RES="0430";
	
	public static final String TASK_CASH_RECEIPT ="40";
	public static final String TASK_NO_EOT ="99";

	// 단말기 개시거래
	public static final String DEVICE_OPENING_TRANSACTION_REQ		="0200";
    public static final String TASK_DEVICE_OPENING_TRANSACTION 		="90";
	public static final String TASK_DEVICE_PG_GETSTOREINFO 			="9E";
	public static final String RENEWAL_OF_DEVICE_ENCRYPT_KEY_REQ	="0200";
	public static final String RENEWAL_OF_DEVICE_ENCRYPT_KEY_RES	="0210";
	public static final String TASK_RENEWAL_OF_DEVICE_ENCRYPT_KEY 	="91";
	public static final String TERMINAL_SECURITY_CERTIFICATION_REQ	="0200";
	public static final String TERMINAL_SECURITY_CERTIFICATION_RES	="0210";
	public static final String TASK_TERMINAL_SECURITY_CERTIFICATION ="9C";

	// 단말기 보안인증
	public static final String TERMINAL_SECURITY_KEY_DOWNLOAD_REQ="0200";		//요청전문
	public static final String TERMINAL_SECURITY_KEY_DOWNLOAD_RES="0210";		//응답전문
	public static final String TASK_TERMINAL_SECURITY_KEY_DOWNLOAD ="9D";

	public static final String INCOMPLETE_TRANSACTION_REQ="0420";
	public static final String INCOMPLETE_TRANSACTION_RES="0430";
	public static final String TASK_INCOMPLETE_TRANSACTION ="99";

	public static byte VAL_FS = 0x1c;
	public static final byte 	VAL_ETX = 0x03;
	static final byte VAL_STX = 0x02;
	static final byte VAL_EOT = 0x04;
	static final byte VAL_ENQ = 0x05;
	static final byte VAL_ACK = 0x06;
	static final byte VAL_NAK = 0x15;
	static final byte VAL_DLE = 0x10;

	public static final String VAL_WCC_KEYIN ="K";
	public static final String VAL_WCC_BARCODE ="B";
	public static final String VAL_WCC_ZEROCANCEL ="Z";
	public static final String VAL_WCC_KAKAO_BARCODE ="KB";
	public static final String VAL_WCC_EMVQR ="i";
    public static final String VAL_WCC_TAB ="N";  // NFC

	public static final String VAL_WCC_SWIPE ="S";
	public static final String VAL_WCC_IC ="I";
	static final String VAL_VERSION_DIVISION_V2 ="V2";
	public static final String VAL_TERMINAL_NUMBER ="";
	static final String VAL_MODULE_ID ="1000000011";

  	public static final String VAL_PRODUCTION_SERIAL_NUMBER ="DA";
	public static final String VAL_COMPANY_NUMBER ="";
	public static final String VAL_HW_CERT_DEFAULT ="";
	public static final String VAL_SW_CERT_DEFAULT ="PAYJOA-M10001000";

	public static final String VAL_TERMINAL_DIVISION_GENERAL 						="0";			 //일반
	public static final String VAL_TERMINAL_DIVISION_GAS 							="1";
	public static final String VAL_TERMINAL_DIVISION_CHARGING 						="2";
	public static final String VAL_TERMINAL_DIVISION_DUTY_FREE_OIL 					="3";
	public static final String VAL_TERMINAL_DIVISION_TAXABLE_TAXFREE 				="4";		//과세/비과세
	public static final String VAL_TERMINAL_DIVISION_PAYBACK 						="P";
	public static final String VAL_TERMINAL_DIVISION_KING_POINT_TRANS				="W";
	public static final String VAL_TERMINAL_DIVISION_LOCAL_CURR_PAY 				="D";
	public static final String VAL_TERMINAL_DIVISION_TAX_REFUND 					="T";
	public static final String VAL_TERMINAL_DIVISION_CASH_IC 						="K";
	public static final String VAL_TERMINAL_DIVISION_OFFLINE_PG 					="G";			//오프라인 PG
	public static final String VAL_TERMINAL_DIVISION_MULTI_VENDOR 					="M";			//다중사업자
	public static final String VAL_TERMINAL_DIVISION_TAXABLE_TAXFREE_MULTI_VENDOR 	="Y";		//다중사업자 & 과세비과세 사용

	public static final String VAL_DIVISION_DONGLE 									="W";
	
	public static final String VAL_ELECTRONIC_SIGN 									="S";
	public static final String VAL_ELECTRONIC_OTHERS 								="A";
	public static final String VAL_RF_CARD_VISA_WAVE 								="W";
	public static final String VAL_NO_SLIP_DIVISION 								="  ";
	public static final String VAL_PC_POS 											="Y";
	public static final String VAL_PC_POS_OTHER 									=" ";
	
	public static final String VAL_KEY_DIVISION_DUKPT 								="D";
	public static final String VAL_KEY_DIVISION_NIPP 								="N";
	
	public static final String VAL_MODEL_CODE 										="PK1B";   				// C1IT_BT         PK1B
	public static final String VAL_CARD_TYPE_DEFAULT								="J";				// card:: J/Smart
	public static final String VAL_RESP_CODE_SUCCESS 								= "0000";
	public static final String VAL_RESP_NEED_KEY_UPDATE 							= "9027";		// 보안키 업데이트 요망

	public static String SWModelName												="PAYJOA-M1000";
	public static String SWModelNo 													="1000";
	public static final String VAL_FALL_BACK_REASON_NO_RESPONSE="01";
	public static final String VAL_FALL_BACK_REASON_NO_SUPPORT_APPICATION="02";
	public static final String VAL_FALL_BACK_REASON_NOT_ICC="03";
	public static final String VAL_FALL_BACK_REASON_UNDER_COVERAGE_MANDATORY_DATA="04";
	public static final String VAL_FALL_BACK_REASON_FAIL_CVM_CMD_RESPONSE="05";
	public static final String VAL_FALL_BACK_REASON_SET_EMV_CMD_INCORRECT="06";
	public static final String VAL_FALL_BACK_REASON_TERMINAL_MAL_FUNCTION="07";
	public static final String VAL_INCOMPLETE_REASON_WRITE_BACK_DECLINE="2";
	public static final String VAL_INCOMPLETE_REASON_WRITE_BACK_IC_CARD_REMOVED="3";
	public static final String VAL_INCOMPLETE_REASON_WRITE_BACK_OTHERS="4";

	public static final int		MIN_NUMBER_CMD_RESP		= 3;		// see **나) @Page6
	public static final int		DEFAULT_NUMBER_CMD_RESP	= 5;
	public static final int		MAX_RETRANSMIT_COUNT		= 3;



	String vanName				= "DaouData";
  	String VanTestServerIP		= "222.106.99.137";
	int    VanTestServerPort    = 20071;



	interface TimeoutValue{				//	see **가) @Page6
		int CONNECTION_TIMEOUT		= 1000;		// 1 Sec
		int ENQ_TIMEOUT				= 10000;	// 10 Sec
		int TRANSACTION_TIMEOUT		= 50000;	// 30 Sec ==> 50 Sec
		int ACK_TIMEOUT				= TRANSACTION_TIMEOUT; 	// see **라.2)@Page6. 3Sec
		int EOT_TIMEOUT				= 3000;		// 3 Sec
		int SERVER_RETRY_TIMEOUT    = 7000;		// 7 Sec	EOT못받았을때 ACK전송후 EOT까지.
	}

	interface InternalErrorCode{
		byte	ERR_RX_MESSAGE_LENGTH			= (byte)0xFF;		// -1: minimum length error
		byte	ERR_RX_MESSAGE_UNKNOWN			= (byte)0xFE;		// -2: unknown cmd/response
		byte	ERR_RX_MESSAGE_DISTORTION		= (byte)0xFD;		// -3: distortion cmd/response
	}

}
