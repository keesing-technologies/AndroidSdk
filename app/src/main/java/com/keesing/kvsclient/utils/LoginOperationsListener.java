package com.keesing.kvsclient.utils;

import com.keesing.kvsclient.types.LoginCredentials;

public interface LoginOperationsListener {

    void onSucceed(LoginCredentials data);

    void onFailed(String error, LoginCredentials data);

}
