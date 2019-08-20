package com.keesing.kvsclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        findViewById(R.id.btnRfid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // work with framework here
                startActivity(new Intent(MainActivity.this, RfidActivity.class));

            }
        });

        findViewById(R.id.btnCapturing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ExperimentalActivity.class));
            }
        });
    }
}
