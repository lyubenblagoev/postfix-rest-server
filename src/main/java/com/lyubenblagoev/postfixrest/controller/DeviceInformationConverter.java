package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.service.model.DeviceInformation;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Component
public class DeviceInformationConverter {

    private final Parser parser;

    public DeviceInformationConverter() {
        this.parser = new Parser();
    }

    public DeviceInformation convert(HttpServletRequest request) {
        Objects.nonNull(request);
        return new DeviceInformation(getClientIpAddr(request), getClientBrowser(request), getClientOS(request));
    }

    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getClientOS(HttpServletRequest request) {
        Client client = getDeviceDetails(getUserAgent(request));
        if (client != null) {
            return client.os.family;
        }
        return "Unknown";
    }

    private String getClientBrowser(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        Client client = getDeviceDetails(userAgent);
        if (client != null) {
            if (client.userAgent.family.equals("Other")) {
                return userAgent;
            }
            return client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor;
        }
        return "Unknown";
    }

    private Client getDeviceDetails(String userAgent) {
        return parser.parse(userAgent);
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
