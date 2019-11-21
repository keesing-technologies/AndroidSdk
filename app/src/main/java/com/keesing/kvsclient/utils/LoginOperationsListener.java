package com.keesing.kvsclient.utils;

public interface LoginOperationsListener {

    void onSucceed(String account, String username, String password);

    void onFailed(String error);

}
