package com.keesing.kvsclient;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keesing.kvsclient.types.LoginCredentials;
import com.keesing.kvsclient.utils.LoginOperationsListener;
import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class LoginDialog extends Dialog implements View.OnClickListener, WebServicePostOperation {

    private final LoginCredentials loginCredentials;
    private final LoginOperationsListener loginListener;

    public LoginDialog(Context context, LoginCredentials loginCredentials, LoginOperationsListener listener) {
        super(context);
        this.loginCredentials = loginCredentials;
        loginListener = listener;
    }

    private String username, hashedPasswd, account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_login);
        this.findViewById(R.id.btnLogin).setOnClickListener(this);

        if(this.loginCredentials != null){
            ((EditText) this.findViewById(R.id.txtLoginAccount)).setText(this.loginCredentials.getAccount());
            ((EditText) this.findViewById(R.id.txtLoginUsername)).setText(this.loginCredentials.getUsername());
        }

        this.setCancelable(false);
    }

    @Override
    public void onClick(View v) {

        username = ((EditText) this.findViewById(R.id.txtLoginUsername)).getText().toString().trim();
        hashedPasswd = md5(((EditText) this.findViewById(R.id.txtLoginPassword)).getText().toString().trim());
        account = ((EditText) this.findViewById(R.id.txtLoginAccount)).getText().toString().trim();

        innerCreds = new LoginCredentials(account, username, hashedPasswd);
        // send to the end point
        WebServiceHelper web = new WebServiceHelper(this);
        web.execute("POST", "", innerCreds);
        onFinish("", 200);
    }

    public static String md5(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")), 0, s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            Log.e("LOGING", e.getMessage());
            return "";
        }
    }

    private LoginCredentials innerCreds;
    @Override
    public void onFinish(String output, int statusCode) {

        if (statusCode == 200) {
            innerCreds.setLastLoginTime(new Date().getTime());
            this.loginListener.onSucceed(innerCreds);
        } else {
            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(output).getAsJsonObject();
            String tmp = json.get("error").getAsString();
            ((EditText) this.findViewById(R.id.txtLoginUsername)).setText(tmp);
            innerCreds.setPassword("");

            ((TextView) this.findViewById(R.id.txtLogingMessage)).setText(tmp);

            this.loginListener.onFailed(tmp, innerCreds);
        }
    }
}
