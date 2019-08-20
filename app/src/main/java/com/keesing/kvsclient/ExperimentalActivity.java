package com.keesing.kvsclient;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.jenid.mobile.capture.configuration.DeviceSupport;
import com.jenid.mobile.capture.controller.GenuineIDActivity;
import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;

public class ExperimentalActivity extends GenuineIDActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.setPrimaryColor(getResources().getColor(R.color.keesingBlue));
        this.setTextFont("fonts/Ubuntu-Regular.ttf");
        this.setSecondaryTextFont("fonts/Ubuntu-Italic.ttf");

        super.onCreate(savedInstanceState);

        super.setTakePhotoTimeOut(15000);
        super.setEnableFaceDetection(true);

        new WebServiceHelper(new WebServicePostOperation() {
            @Override
            public void onFinish(String output, int statusCode) {
                if (statusCode != 200) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ExperimentalActivity.this);
                    builder
                            .setTitle(R.string.communication_problem_title)
                            .setMessage(R.string.webservice_connection_problem)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(true)
                            .show();
                }
            }
        }).execute("get", "");
    }

    @Override
    public void doAfterDocumentFound(
            Bitmap frontImage,
            String encodedFrontImage,
            Bitmap backImage,
            String encodedBackImage,
            Bitmap faceImage,
            String encodedFaceImage,
            final String completeJsonPayload) {
        Log.i(TAG, completeJsonPayload);

        new WebServiceHelper(new WebServicePostOperation() {
            @Override
            public void onFinish(String output, int statusCode) {
                // show message to user...
                if (statusCode == 200) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ExperimentalActivity.this);
                    builder
                            .setTitle("Upload")
                            .setMessage(R.string.doc_submitted)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    navigateBackHere();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_info)
                            .show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExperimentalActivity.this);
                    builder
                            .setTitle(R.string.communication_problem_title)
                            .setMessage(R.string.communication_problem_desc)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, completeJsonPayload);
                                    navigateBackHere();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

            }
        }).execute("post", "", completeJsonPayload);
    }

    private void navigateBackHere() {
        Intent i = new Intent(ExperimentalActivity.this, ExperimentalActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void doAfterFail(
            Bitmap frontImage,
            String encodedFrontImage,
            Bitmap backImage,
            String encodedBackImage,
            String completePayload) {
        ExperimentalActivity.this.navigateBackHere();
    }

    @Override
    public void renderButton(Button button) {
        super.renderButton(button);
        button.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Italic.ttf"));
        button.setBackgroundColor(getResources().getColor(R.color.keesingBlue));
    }

    @Override
    public void renderSecondaryButton(Button button) {
        super.renderSecondaryButton(button);
        button.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Italic.ttf"));
        button.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void renderPhotoButton(Button button) {
        super.renderPhotoButton(button);
    }


    @Override
    public void backPressed() {
        System.exit(0);
    }

    @Override
    public void doAfterDeviceIsNotSupported(DeviceSupport deviceSupport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ExperimentalActivity.this);
        builder
                .setTitle("Device not supported")
                .setMessage("Problem happened running application <Cause: " + deviceSupport.toString() + ">")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


}
