package com.keesing.kvsclient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.jenid.genuineidmobilemrz.controller.GenuineIDMRZActivity;
import com.jenid.genuineidmobilemrz.controller.GenuineIDMRZCallback;

public class MRZCaptureActivity  extends GenuineIDMRZActivity implements GenuineIDMRZCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MRZCaptureActivity", "Creating this view");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mrz_capture);
    }

    @Override
    public void doAfterMRZDetection(String line1, String line2, String line3, boolean checksum, Bitmap bitmap, double processingTime) {
        Intent intent = new Intent(this, RfidActivity.class);
        intent.putExtra("line1", line1);
        intent.putExtra("line2", line2);
        intent.putExtra("line3", line3);
        intent.putExtra("chcksm", checksum);
        intent.putExtra("proctime", String.format("%1$,.2f", new Object[] { Double.valueOf(processingTime) }));
        startActivity(intent);
        // finish();
    }

    @Override
    public void back() {

    }
}
