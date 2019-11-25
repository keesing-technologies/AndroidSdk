package com.keesing.kvsclient;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keesing.kvsclient.types.LoginCredentials;
import com.keesing.kvsclient.utils.Hashing;
import com.keesing.kvsclient.utils.LoginOperationsListener;
import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;

import java.util.Date;

public final class LoginDialog extends Dialog implements View.OnClickListener, WebServicePostOperation {

    private final LoginCredentials loginCredentials;
    private final LoginOperationsListener loginListener;

    public LoginDialog(Context context, LoginCredentials loginCredentials, LoginOperationsListener listener) {
        super(context);
        this.loginCredentials = loginCredentials;
        loginListener = listener;
    }

    private LoginCredentials innerCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_login);
        this.findViewById(R.id.btnLogin).setOnClickListener(this);

        if (this.loginCredentials != null) {
            ((EditText) this.findViewById(R.id.txtLoginAccount)).setText(this.loginCredentials.getAccount());
            ((EditText) this.findViewById(R.id.txtLoginUsername)).setText(this.loginCredentials.getUsername());
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT ;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        this.getWindow().setAttributes(lp);
        this.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        try {
            innerCredentials = new LoginCredentials();
            innerCredentials.setAccount(((EditText) this.findViewById(R.id.txtLoginAccount)).getText().toString().trim());
            innerCredentials.setUsername(((EditText) this.findViewById(R.id.txtLoginUsername)).getText().toString().trim());
            innerCredentials.setPassword(Hashing.pdkdf2(((EditText) this.findViewById(R.id.txtLoginPassword)).getText().toString().trim()));
            // send to the end point
            WebServiceHelper web = new WebServiceHelper(this);
            // web.execute("POST", "", innerCreds);
            onFinish("", 200);
        } catch (Exception e) {
            Log.e("LOGIN", e.getMessage(), e);
        }
    }

    @Override
    public void onFinish(String output, int statusCode) {

        if (statusCode == 200) {
            innerCredentials.setLastLoginTime(new Date().getTime());
            this.hide();
            this.loginListener.onSucceed(innerCredentials);
        } else {
            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(output).getAsJsonObject();
            String tmp = json.get("error").getAsString();
            ((EditText) this.findViewById(R.id.txtLoginUsername)).setText(tmp);
            innerCredentials.setPassword("");

            ((TextView) this.findViewById(R.id.txtLogingMessage)).setText(tmp);

            this.loginListener.onFailed(tmp, innerCredentials);
        }
    }
}
