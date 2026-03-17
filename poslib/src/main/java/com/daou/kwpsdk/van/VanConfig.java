package com.daou.kwpsdk.van;

/**
 * VAN configuration values.
 */
public class VanConfig {

    private static final String DEFAULT_DOWNLOAD_SERVER_IP = "222.106.99.137";
    private static final int DEFAULT_DOWNLOAD_SERVER_PORT = 20071;
    private static final String DEFAULT_MODEL_CODE = "PK1B";
    private static final String DEFAULT_SOFTWARE_CERT = "###DAOU-MPOS1000";
    private static final String DEFAULT_COMPANY_NUMBER = "1242137263";

    private VanClient.VanType vanType;
    private String serverIp;
    private int serverPort;
    private String terminalId;
    private int timeout = 30000;
    private boolean useSsl = false;
    private String downloadServerIp = DEFAULT_DOWNLOAD_SERVER_IP;
    private int downloadServerPort = DEFAULT_DOWNLOAD_SERVER_PORT;
    private String modelCode = DEFAULT_MODEL_CODE;
    private String softwareCert = DEFAULT_SOFTWARE_CERT;
    private String companyNumber = DEFAULT_COMPANY_NUMBER;

    public VanConfig() {
    }

    public VanConfig(VanClient.VanType vanType, String serverIp, int serverPort,
                     String terminalId) {
        this.vanType = vanType;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.terminalId = terminalId;
        this.downloadServerIp = serverIp;
        this.downloadServerPort = serverPort;
    }

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

    public String getDownloadServerIp() {
        return downloadServerIp;
    }

    public void setDownloadServerIp(String downloadServerIp) {
        this.downloadServerIp = downloadServerIp;
    }

    public int getDownloadServerPort() {
        return downloadServerPort;
    }

    public void setDownloadServerPort(int downloadServerPort) {
        this.downloadServerPort = downloadServerPort;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getSoftwareCert() {
        return softwareCert;
    }

    public void setSoftwareCert(String softwareCert) {
        this.softwareCert = softwareCert;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Override
    public String toString() {
        return "VanConfig{"
            + "vanType=" + vanType
            + ", serverIp='" + serverIp + '\''
            + ", serverPort=" + serverPort
            + ", terminalId='" + terminalId + '\''
            + ", downloadServerIp='" + downloadServerIp + '\''
            + ", downloadServerPort=" + downloadServerPort
            + ", modelCode='" + modelCode + '\''
            + ", companyNumber='" + companyNumber + '\''
            + ", timeout=" + timeout
            + '}';
    }
}
