package com.lyubenblagoev.postfixrest.service.model;

public class DeviceInformation {

    private String ipAddress;

    private String browser;

    private String os;

    public DeviceInformation(String ipAddress, String browser, String os) {
        this.ipAddress = ipAddress;
        this.browser = browser;
        this.os = os;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOs() {
        return os;
    }
}
