package com.daou.kwpsdk.reader.usb;

import java.nio.ByteBuffer;

public class DaouReaderComm {
    int  m_iRdrPortNo;
    int  m_bReaderRun;
    int  m_iSndPacLen;
    int  m_iRcvPacLen;
    byte [] m_ucSndPacBuf;
    byte [] m_ucRcvPacBuf;
    ByteBuffer mucRcvPacBuf = ByteBuffer.allocate(500);


}


