package com.keesing.kvsclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.keesing.kvsclient.rfid.Logger;
import com.keesing.kvsclient.rfid.PassportReader;
import com.secunet.epassportapi.Framework;
import com.secunet.epassportapi.Image;
import com.secunet.epassportapi.ImageList;
import com.secunet.epassportapi.Passport;

public class RfidActivity extends Activity {

    private PassportReader reader;
    private Framework framework;
    private EditText mrzEdit;
    private TextView log;
    private ImageView imgChip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_rfid_reading);

        findViewById(R.id.btnMrzRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RfidActivity.this, MRZCaptureActivity.class ));
            }
        });

        log = findViewById(R.id.txtOut);
        mrzEdit = (EditText)findViewById(R.id.mrz);
        mrzEdit.setText("P<HUNKARPATI<<VIKTORIA<<<<<<<<<<<<<<<<<<<<<<\nHU12345600HUN9202287F1501010123456782<<<<<04");
        imgChip = findViewById(R.id.imgChip);

        reader = new PassportReader();
        Logger logger = new Logger();
        framework = Framework.create(getString(R.string.epassport_test_licence), logger, reader, null, Framework.LogEverything);

    }



    @Override
    protected void onResume() {
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        IntentFilter[] writeTagFilters = new IntentFilter[]{tagDetected};
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
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
                if(tag != null) {
                    clearlog();
                    log("ISO tag found...");
                    log("Extended Length APDU support: " + tag.isExtendedLengthApduSupported());
                    log("Max. APDU size: " + tag.getMaxTransceiveLength());
                    log("setting APDU timeout...");
                    tag.setTimeout(5000);
                    final String mrz = mrzEdit.getText().toString().replace("\n", "").replace("\r", "").replace(" ", "");
                    // todo: cancel possibly running thread; ePassportAPI MUST NOT be used reentrant
                    new Thread(new Runnable() { public void run() { readPassport(tag, mrz); } }).start();
                }
            }
        }
        catch (Exception e)
        {
            log("Error: " + e.getMessage());
        }
    }

    private void readPassport(IsoDep tag, String mrz)
    {
        reader.setTag(tag);

        try {
            Passport p = new Passport(framework);
            try {
                log("perform BAC...");
                p.performBAC(mrz);
                if (p.getChipAuthenticationVersion() == 1) {
                    log("perform chip authentication...");
                    p.performChipAuthentication();
                } else if (p.supportsActiveAuthentication()) {
                    log("perform active authentication...");
                    p.performActiveAuthentication();
                }
                log("read datagroup 1...");
                byte[] dg1 = p.getDatagroup(1);

                log("MRZ (chip): " + framework.getMRZ(dg1));
                log("read datagroup 2...");
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
                log("reading finished");
            }
            catch (Exception e){
                Log.e("READING", e.getMessage());
                log(e.getMessage());
            } finally{
                p.delete();
            }
        }
        catch(Exception ex) {
            log("reading failed: " + ex.getMessage());
        }
    }

    private void clearlog()
    {
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

    private void log(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.append("\n");
                log.append(message);
            }
        });
    }
}
