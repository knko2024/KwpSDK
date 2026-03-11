package com.daou.kwpsdk.van;

/**
 * VAN 설정 정보
 * VAN 서버 접속에 필요한 설정값을 담는 클래스
 */
public class VanConfig {

    /** VAN사 타입 */
    private VanClient.VanType vanType;

    /** VAN 서버 IP (주소) */
    private String serverIp;

    /** VAN 서버 포트 */
    private int serverPort;

    /** 가맹점 번호 (TID) */
    private String terminalId;

    /** 통신 타임아웃 (밀리초) */
    private int timeout = 30000;

    /** SSL 사용 여부 */
    private boolean useSsl = false;

    public VanConfig() {
    }

    public VanConfig(VanClient.VanType vanType, String serverIp, int serverPort,
                     String terminalId) {
        this.vanType = vanType;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.terminalId = terminalId;
    }

    // Getters and Setters

    public VanClient.VanType getVanType() {
        return vanType;
    }

    public void setVanType(VanClient.VanType vanType) {
        this.vanType = vanType;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    @Override
    public String toString() {
        return "VanConfig{" +
                "vanType=" + vanType +
                ", serverIp='" + serverIp + '\'' +
                ", serverPort=" + serverPort +
                ", terminalId='" + terminalId + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}

