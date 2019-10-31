package com.keesing.kvsclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);


        // System.loadLibrary("libopencv_java3");
        // System.loadLibrary("everything");
        // System.loadLibrary("genuineidapimrzlib");
        System.loadLibrary("jenidcore");

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Regular.ttf");

        ((Button)findViewById(R.id.btnRfid)).setTypeface(typeFace);
        ((Button)findViewById(R.id.btnCapturing)).setTypeface(typeFace);
        ((TextView)findViewById(R.id.txt001)).setTypeface(typeFace);
        ((TextView)findViewById(R.id.txt002)).setTypeface(typeFace);

        findViewById(R.id.btnRfid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // work with framework here
                Intent i = new Intent(MainActivity.this, RfidActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.btnCapturing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DocumentCapturingActivity.class));
            }
        });
    }
}
