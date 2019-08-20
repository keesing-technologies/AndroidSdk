package com.keesing.kvsclient.rfid;

import android.util.Log;

import com.secunet.epassportapi.LoggerBase;
public class Logger extends LoggerBase {
    private static final String TAG = "KEESING.ePassportAPI";
    @Override
    public void log(String message) {
        Log.d(TAG, message);
    }
}