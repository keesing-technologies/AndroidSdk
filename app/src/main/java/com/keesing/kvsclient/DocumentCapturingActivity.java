package com.keesing.kvsclient;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jenid.mobile.capture.configuration.DeviceSupport;
import com.jenid.mobile.capture.controller.GenuineIDActivity;
import com.keesing.kvsclient.utils.DataReceiver;
import com.keesing.kvsclient.utils.SurysRabbitMQConsumer;
import com.keesing.kvsclient.utils.SurysRabbitMQPublisher;
import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class DocumentCapturingActivity extends GenuineIDActivity {

    private SurysRabbitMQConsumer consumer = null;

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

                    AlertDialog.Builder builder = new AlertDialog.Builder(DocumentCapturingActivity.this);
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

    private int numberOfImages = 1;
    private int mrzCallsCount = 0;
    private String mrz = "";

    @Override
    public void doAfterDocumentFound(
            Bitmap frontImage,
            final String encodedFrontImage,
            Bitmap backImage,
            String encodedBackImage,
            Bitmap faceImage,
            String encodedFaceImage,
            final String completeJsonPayload) {

        Log.i(TAG, completeJsonPayload);

        sendData(completeJsonPayload);
        // runSurysMrzExtraction(completeJsonPayload, encodedBackImage, encodedFrontImage);
    }

    private void navigateBackHere() {
        Intent i = new Intent(DocumentCapturingActivity.this, DocumentCapturingActivity.class);
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
        DocumentCapturingActivity.this.navigateBackHere();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentCapturingActivity.this);
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

    private void sendData(String json){
        new WebServiceHelper(new WebServicePostOperation() {
            @Override
            public void onFinish(String output, int statusCode) {
                // show message to user...
                if (statusCode == 200) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(DocumentCapturingActivity.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(DocumentCapturingActivity.this);
                    builder
                            .setTitle(R.string.communication_problem_title)
                            .setMessage(R.string.communication_problem_desc)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.d(TAG, completeJsonPayload);
                                    navigateBackHere();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

            }
        }).execute("post", "", json);
    }


    private void runSurysMrzExtraction(final String completeJsonPayload, String encodedBackImage, String encodedFrontImage) {
        this.consumer = new SurysRabbitMQConsumer(new DataReceiver<String>() {
            @Override
            public void run(String... params) {
                // Toast.makeText(DocumentCapturingActivity.this, params[0], Toast.LENGTH_LONG).show();
                // navigateBackHere();
                mrzCallsCount++;

                try {
                    OutputStreamWriter outputStreamWriter =
                            new OutputStreamWriter(DocumentCapturingActivity.this.openFileOutput("output.json", Context.MODE_PRIVATE));
                    outputStreamWriter.write(completeJsonPayload);
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JsonParser jsonParser = new JsonParser();
                JsonObject json = jsonParser.parse(params[0]).getAsJsonObject();
                String tmp = json.get("MRZ").getAsString();

                // check if mrz has been detected
                if(!tmp.contains("failed_to_locate_mrz")) {
                    mrz = tmp;
                }

                if(mrzCallsCount == numberOfImages) {
                    Intent intent = new Intent(DocumentCapturingActivity.this, RfidActivity.class);
                    Bundle b = new Bundle();
                    b.putString("capturing_json", "output.json");
                    b.putString("mrz_string", mrz);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            }
        });

        if (encodedBackImage!= null && encodedBackImage.length() != 0)
            numberOfImages = 2;

        new SurysRabbitMQPublisher(DocumentCapturingActivity.this, "",
                this.consumer).execute(encodedFrontImage, encodedBackImage);
    }


}
