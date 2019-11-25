package com.keesing.kvsclient.types;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class LoginCredentials {

    public static final String STORE_KEY = "credentials";
    public static final int EXPIRATION_TIMEOUT = 60 * 60 * 1000;

    @Expose
    private String account;

    @Expose
    private String username;

    @Expose
    private String password;

    private long lastLoginTime;

    public LoginCredentials(String account, String username, String password) {
        this.account = account;
        this.username = username;
        this.password = password;
    }

    public LoginCredentials() {
        this.account = "";
        this.username = "";
        this.password = "";
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }



    public boolean isExpired(){
        return this.lastLoginTime < new Date().getTime() - EXPIRATION_TIMEOUT;
    }
}
