package com.lyubenblagoev.postfixrest.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_devices")
public class Device extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date refreshTokenExpirationDate;

    private String remoteAddress;

    private String type;

    private String os;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getRefreshTokenExpirationDate() {
        return refreshTokenExpirationDate;
    }

    public void setRefreshTokenExpirationDate(Date refreshTokenExpirationDate) {
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
}
