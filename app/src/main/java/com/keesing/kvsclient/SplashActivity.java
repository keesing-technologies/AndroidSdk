package com.keesing.kvsclient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jenid.mobile.capture.nativecode.DocFinder;

import java.util.ArrayList;

public class SplashActivity extends Activity {

    private final static String TAG = "SplashActivity";
    private static int SPLASH_TIME_OUT = 200;

    private final int writeExternal = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case writeExternal :
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // camera/storage-related task you need to do.

                    goOn();
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory( Intent.CATEGORY_HOME );
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        ArrayList<String> permissions = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (permissions.size() > 0)
        {
            String[] perms = new String[permissions.size()];
            perms = permissions.toArray(perms);
            ActivityCompat.requestPermissions(this, perms, writeExternal);
        }
        else
        {
            goOn();
        }
    }

    private void goOn()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                {
                    // IMPORTANT initialization !!!
                    // initialize static content...
                    DocFinder docFinder = new DocFinder(SplashActivity.this);
                    docFinder.init();

                    // This method will be executed once the timer is over
                    Intent i = new Intent( SplashActivity.this, ExperimentalActivity.class );
                    startActivity(i);

                    // close this activity
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
