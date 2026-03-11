package com.daou.kwpsdk.reader.usb;

public class DaouReaderPacket {
    //--------------------------------------------------------------------
    // 리더기 설정 정보 가져오기 (from WEB)
    //--------------------------------------------------------------------
    public String trmlid;
    public String getTrmlid() {
        return trmlid;
    }
    public void setTrmlid(String trmlid) {
        this.trmlid = trmlid;
    }

    public String biznbr;
    public void setBiznbr(String biznbr) {
        this.biznbr = biznbr;
    }
    public String getBiznbr() {
        return biznbr;
    }
    public String getInputType() {
        return InputType;
    }

    public void setInputType(String inputType) {
        InputType = inputType;
    }
    public String InputType;
    public String getSaleAmount() {
        return SaleAmount;
    }
    public void setSaleAmount(String saleAmount) {
        SaleAmount = saleAmount;
    }
    public String SaleAmount;

    public String getServRandomeKey() {
        return ServRandomeKey;
    }

    public void setServRandomeKey(String servRandomeKey) {
        this.ServRandomeKey = servRandomeKey;
    }

    public String ServRandomeKey;
   // byte [] tServRandomKey= new byte[428];		// 	// 4. 서버 Random Key		428

    public String getSecurityKey() {
        return SecurityKey;
    }

    public void setSecurityKey(String securityKey) {
        SecurityKey = securityKey;
    }

    public String SecurityKey;

    public String getSecurityMac() {
        return SecurityMac;
    }

    public void setSecurityMac(String securityMac) {
        SecurityMac = securityMac;
    }

    public String SecurityMac;

    //--------------------------------------------------------------------
    // 헤더
    //--------------------------------------------------------------------
    int hPacID = 0 ;
    String AndroidId = "";
    public String getAndroidId() {
        return AndroidId;
    }

    public void setAndroidId(String androidId) {
        AndroidId = androidId;
    }


    public int gethPacID() {
        return hPacID;
    }
    public void sethPacID(int hPacID) {
        this.hPacID = hPacID;
    }

    byte[] TransPck = new byte[500];
    public byte[] getTransPck() {
        return TransPck;
    }
    public void setTransPck(byte[] TransPck){ this.TransPck = TransPck; }

    // byte [] m_ucSndPacBuf;
    byte [] tLength = new byte[4 + 1];				// 1. 전문길이				4
    byte [] tTeleNo = new byte[4 + 1];				// 1. 전문번호				4
    byte [] tDateTime;				                // 2. 거래일시				14
    byte [] tTID;					                // 3. 단말기번호				8
    byte [] tSaleAmount;			                // 4. 거래금액				V12
    byte [] tRandomNo;				                // 5. 난수					32

    byte [] rLength     = new byte[4];		        // 1. 전문길이				4
    byte [] rTeleNo     = new byte[4];		        // 1. 전문번호				4
    byte [] rResCode    = new byte[4];			    // 2. 응답코드				4
    byte [] rDateTime   = new byte[14];		    	// 2. 거래일시				14
    byte [] rTID        = new byte[8];		        // 3. 단말기번호				8

    public byte[] getrTID() {
        return rTID;
    }
    public void setrTID(byte[] rTID) {
        this.rTID = rTID;
    }
    byte [] rCvmPin             = new byte[1];			        // 6. CVM PIN & 서명처리구분
    byte [] rDeviceInfo         = new byte[42];			        // 7. 디바이스 정보			42
    byte [] rDeviceCheckTime    = new byte[14];			        // 7. 검사시간			14
    byte [] rRFU                = new byte[1];		            //    예비

    byte [] rCardType           = new byte[1];
    byte [] rCardData           = new byte[512];
    byte [] rChipData           = new byte[512];
    byte [] rPinInputType       = new byte[512];

    byte [] rServiceCode    = new byte[3];
    byte [] rMaskCardNo     = new byte[20];
    byte [] rAIDCountryCode = new byte[50];

    //--------------------------------------------------------------------
    // 데이터
    //--------------------------------------------------------------------
    // 2.2	카드 입력 요청전문
//	byte [] tSaleAmount;			// 4. 거래금액				V12
    byte [] tInputType = new byte[]{(byte)0x0, (byte)0x0};		        // 5. 입력 요청구분			1
    byte [] tUnionPayMode;		    // 5. 입력 요청구분			1

    // 2.3	카드 입력 응답전문
    /*byte [] rCardType;				// 3. 카드구분				1
    byte [] rCardData;			    // 4. 카드데이터				120
    byte [] rChipData;			    // 5. Chip Data				V512
    byte [] rPinInputType;		    // 6. PIN 입력 구분			1*/

    // 7. 디바이스 정보			    42
   /* byte [] rServiceCode;			// 8. 서비스 코드				3
    byte [] rMaskCardNo;			// 9. 카드번호				V20
    byte [] rAIDCountryCode;	    // 10. AID + 국가코드	    V50*/
    //--------------------------------------------------------------------
    // 2.4	IC카드 거래완료 요청전문
    byte [] tEmvData;				// 5. EMV 데이터				V512
    // 2.5	IC카드 거래완료 응답전문
    byte [] rTcIsr;				// 5. TC & ISR				V512
    //--------------------------------------------------------------------
    // 2.7	PIN 입력 응답전문
//	uint8 tSaleAmount[12 + 1];			// 4. 거래금액				12
    byte [] tPinType;				// 5. 핀구분					1
    byte [] tPIN;					// 6. PIN					24
    // 2.6	PIN 입력 요청전문
    byte [] rPinType;				// 6. PIN 요청 구분			1
    //--------------------------------------------------------------------
    // 2.9	AID 선택 응답전문
    byte [] tAidIndex[];				// 4. AID 인덱스				1
    // 2.8	AID 선택 요청전문
    byte [] rAidCount[];				// 3. AID 개수				2
    byte [] rAidLabel[];			// 4. AID Label 내역			V512
    //--------------------------------------------------------------------




    //--------------------------------------------------------------------
    // 2.22	보안인증 요청전문
    // 2.23	보안인증 응답전문


    byte [] rPkVersion  = new byte[2];		// 3. Public Key 버전		2
    byte [] tServRandomKey= new byte[344];		// 	// 4. 서버 Random Key		428


    byte [] tSecKey = new byte[88];				// 3. 보안키 정보				88
    byte [] tMac =  new byte[12];     			// 4. MAC					12


}
