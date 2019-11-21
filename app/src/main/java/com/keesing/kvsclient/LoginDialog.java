package com.keesing.kvsclient;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keesing.kvsclient.utils.LoginOperationsListener;
import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginDialog extends Dialog implements View.OnClickListener, WebServicePostOperation {

    private final LoginOperationsListener loginListener;

    public LoginDialog(Context context, LoginOperationsListener listener){
        super(context);
        loginListener = listener;
    }

    private String username, hashedPasswd, account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_login);
        this.findViewById(R.id.btnLogin).setOnClickListener(this);
        this.setCancelable(false);
    }

    @Override
    public void onClick(View v) {

        username = ((EditText)this.findViewById(R.id.txtLoginUsername)).getText().toString().trim();
        hashedPasswd =  md5(((EditText)this.findViewById(R.id.txtLoginPassword)).getText().toString().trim());
        account = ((EditText)this.findViewById(R.id.txtLoginAccount)).getText().toString().trim();

        // send to the end point
        WebServiceHelper web = new WebServiceHelper(this);
        JsonObject jobj = new JsonObject();
        jobj.addProperty("username", username);
        jobj.addProperty("password", hashedPasswd);
        // web.execute("POST", "", jobj.toString());
        onFinish("", 200);
    }

    public static String md5(String s)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")),0,s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e("LOGING", e.getMessage());
            return "";
        }
    }

    @Override
    public void onFinish(String output, int statusCode) {
        if(statusCode == 200) {
            this.loginListener.onSucceed(account, username, hashedPasswd);
            this.hide();
        } else {
            // show a message
            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(output).getAsJsonObject();
            String tmp = json.get("error").getAsString();
            ((EditText)this.findViewById(R.id.txtLoginUsername)).setText(tmp);
        }
    }
}
