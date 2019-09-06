package com.keesing.kvsclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keesing.kvsclient.rfid.Logger;
import com.keesing.kvsclient.rfid.PassportReader;
import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;
import com.secunet.epassportapi.Framework;
import com.secunet.epassportapi.Image;
import com.secunet.epassportapi.ImageList;
import com.secunet.epassportapi.Passport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class RfidActivity extends Activity {

    private PassportReader reader;
    private Framework framework;
    private EditText mrzEdit;
    private TextView log;
    private ImageView imgChip;
    private int REQUEST_TAKE_PHOTO = 125630;
    private int REQUEST_CROP_PHOTO = 234560;
    private String currentPhotoPath;
    private File photoFile = null;
    // private MRZReader mrzReader = null;
    private String mrzString = "";
    private String capturingJson = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_rfid_reading);

        findViewById(R.id.btnMrzRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Regular.ttf");

        ((Button) findViewById(R.id.btnMrzRead)).setTypeface(typeFace);

        log = findViewById(R.id.txtOut);
        log.setTypeface(typeFace);


        imgChip = findViewById(R.id.imgChip);

        reader = new PassportReader();
        Logger logger = new Logger();
        framework = Framework.create(getString(R.string.epassport_test_licence), logger, reader, null, Framework.LogEverything);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            mrzString = extras.getString("mrz_string");
            Log.d("RFIDActivity", mrzString);
            ((TextView) findViewById(R.id.rfidPageTitle)).setTypeface(typeFace);
            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(mrzString).getAsJsonObject();

            mrzEdit = (EditText) findViewById(R.id.mrz);
            mrzEdit.setTypeface(typeFace);
            String mrz = json.get("MRZ").getAsString();
            mrz = mrz.replace("\n\f", "").replace(" ", "");
            mrzEdit.setText(mrz);
            // mrzEdit.setText("P<HUNKARPATI<<VIKTORIA<<<<<<<<<<<<<<<<<<<<<<\nHU12345600HUN9202287F1501010123456782<<<<<04");
            // mrzEdit.setText("P<NLDGROOS<<HERMAN<<<<<<<<<<<<<<<<<<<<<<<<<<\nNRL8B30500NLD7207222M2403133167605811<<<<<96");
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createTemporaryImage();
            } catch (IOException ex) {
                Log.e("KEESING.TAKE_PICTURE", ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.keesing.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createTemporaryImage() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            // check if there is any data provided (thumbnail from the camera app)
            if (data != null) {
                Bundle extras = data.getExtras();
                final Bitmap imageBitmap = (Bitmap) extras.get("data");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgChip.setImageBitmap(imageBitmap);
                    }
                });
            }

            // here we can send this picture to MRZ reader
            Uri picUri = FileProvider.getUriForFile(this, "com.keesing.fileprovider", photoFile);
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            //cropIntent.putExtra("aspectX", 1);
            //cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            cropIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(cropIntent, REQUEST_CROP_PHOTO);


            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, options);
                    byte[] buffer = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
                    MRZReaderResult result = mrzReader.processFrame(buffer, bitmap.getWidth(), bitmap.getHeight());
                    Log.i("KEESING.MRZ READER", result.toString());

                    imgChip.setImageURI(FileProvider.getUriForFile(RfidActivity.this,
                            "com.keesing.fileprovider",
                            new File(currentPhotoPath)));
                }
            });
*/
        } else if (requestCode == REQUEST_CROP_PHOTO && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap thePic = extras.getParcelable("data");

            File directory = RfidActivity.this.getDir("imageDir", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, "profile.jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                thePic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

           /* new SurysRabbitMQPublisher(RfidActivity.this,mypath.getPath(), new DataReceiver<String>() {
                @Override
                public void run(String... params) {
                    Toast.makeText(RfidActivity.this, params[0], Toast.LENGTH_LONG).show();
                }
            }).execute("");*/
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        IntentFilter[] writeTagFilters = new IntentFilter[]{tagDetected};
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);


        //mrzReader = new MRZReader();
        //mrzReader.initReader(this);
    }

    @Override
    protected void onPause() {
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        framework.delete();
        framework = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                final Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                final IsoDep tag = IsoDep.get(tagFromIntent);
                if (tag != null) {
                    clearlog();
                    log("ISO tag found...");
                    log("Extended Length APDU support: " + tag.isExtendedLengthApduSupported());
                    log("Max. APDU size: " + tag.getMaxTransceiveLength());
                    log("Setting APDU timeout...");
                    tag.setTimeout(5000);
                    final String mrz = mrzEdit.getText().toString().replace("\n", "").replace("\r", "").replace(" ", "");
                    // todo: cancel possibly running thread; ePassportAPI MUST NOT be used reentrant
                    new Thread(new Runnable() {
                        public void run() {
                            readPassport(tag, mrz);
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }

    private void readPassport(IsoDep tag, String mrz) {
        reader.setTag(tag);

        try {
            Passport p = new Passport(framework);
            try {
                log("Perform BAC...");
                p.performBAC(mrz);

                //log("Perform PACE...");
                //p.performPACE(mrz);

                if (p.getChipAuthenticationVersion() == 1) {
                    log("Chip authentication...");
                    p.performChipAuthentication();
                } else if (p.supportsActiveAuthentication()) {
                    log("Active authentication...");
                    // simple way of checking of the chip structure
                    p.performActiveAuthentication();
                }
                log("Datagroup 1...");
                byte[] dg1 = p.getDatagroup(1);
                log("MRZ (chip): " + framework.getMRZ(dg1));

                byte[] certificate = p.getDocumentSignerCertificate(Passport.FileType.EfSod);
                log("Certificate data: " + String.valueOf(certificate));

                log("Datagroup 2...");
                byte[] dg2 = p.getDatagroup(2);
                ImageList images = framework.getImages(dg2);
                log("Chip contains " + images.getImageCount() + " Images");
                Image imageBmp = framework.convert(images.getImage(0), Image.Format.Bmp);

                final byte[] buffer = imageBmp.getData();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                        imgChip.setMinimumWidth(bm.getWidth());
                        imgChip.setMinimumHeight(bm.getHeight());
                        imgChip.setImageBitmap(bm);
                    }
                });


                log("Reading Finished");

                sendData();
            } catch (Exception e) {
                Log.e("READING", e.getMessage());
                log(e.getMessage());
            } finally {
                p.delete();
            }
        } catch (Exception ex) {
            log("Reading Failed: " + ex.getMessage());
        }
    }

    private void clearlog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.setText("");
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgChip.setImageBitmap(null);
            }
        });
    }

    private void log(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.append("\n");
                log.append(message);
            }
        });
    }


    private void sendData() {
        try {
            InputStreamReader inputStream =
                    new InputStreamReader(RfidActivity.this.openFileInput("output.json"));
            BufferedReader reader = new BufferedReader(inputStream);
            final String json = reader.readLine();
            reader.close();
            inputStream.close();

            new WebServiceHelper(new WebServicePostOperation() {
                @Override
                public void onFinish(String output, int statusCode) {
                    // show message to user...
                    if (statusCode == 200) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(RfidActivity.this);
                        builder
                                .setTitle("Upload")
                                .setMessage(R.string.doc_submitted)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        navigateBack();
                                    }
                                }).setIcon(android.R.drawable.ic_dialog_info)
                                .show();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RfidActivity.this);
                        builder
                                .setTitle(R.string.communication_problem_title)
                                .setMessage(R.string.communication_problem_desc)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Log.d(TAG, completeJsonPayload);
                                        navigateBack();
                                    }
                                }).setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }

                }
            }).execute("post", "", json);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateBack(){
        startActivity(new Intent(RfidActivity.this, DocumentCapturingActivity.class));
        finish();
    }

    // untested function
    private byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

}
