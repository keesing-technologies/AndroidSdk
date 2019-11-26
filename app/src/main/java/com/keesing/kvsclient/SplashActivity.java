package com.keesing.kvsclient;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jenid.mobile.capture.nativecode.DocFinder;
import com.keesing.kvsclient.types.LoginCredentials;
import com.keesing.kvsclient.utils.LoginOperationsListener;
import com.keesing.kvsclient.utils.Store;

import java.util.ArrayList;

public class SplashActivity extends Activity {

    private final static String TAG = "SplashActivity";
    private static int SPLASH_TIME_OUT = 200;

    private final int writeExternal = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case writeExternal: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // camera/storage-related task you need to do.

                    // login();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                    // close the app
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        ArrayList<String> permissions = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (permissions.size() > 0) {
            String[] perms = new String[permissions.size()];
            perms = permissions.toArray(perms);
            ActivityCompat.requestPermissions(this, perms, writeExternal);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        login();
    }

    private void login() {

        goOn();
        if(true) return;

        LoginCredentials loginCredentials = Store.Retreive(SplashActivity.this, LoginCredentials.STORE_KEY, LoginCredentials.class);

        if (loginCredentials == null || loginCredentials.isExpired()) {
            LoginDialog dialog = new LoginDialog(this, loginCredentials, new LoginOperationsListener() {
                @Override
                public void onSucceed(LoginCredentials data) {
                    // save data into private store and continue
                    Store.Save(SplashActivity.this, LoginCredentials.STORE_KEY, data);
                    goOn();
                }

                @Override
                public void onFailed(String error, LoginCredentials data) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    builder
                            .setTitle("Login Failed")
                            .setMessage(error)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    System.exit(0);
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
            dialog.show();
        } else {
            goOn();
        }
    }

    private void goOn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                {
                    // IMPORTANT initialization !!!
                    // initialize static content...
                    DocFinder docFinder = new DocFinder(SplashActivity.this);
                    docFinder.init();

                    // This method will be executed once the timer is over
                    Intent i = new Intent(SplashActivity.this, DocumentCapturingActivity.class);
                    startActivity(i);

                    // close this activity
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        /*
        new version (19172) doesn't have R.id.action_settings.
        if (id == R.id.action_settings)
        {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
