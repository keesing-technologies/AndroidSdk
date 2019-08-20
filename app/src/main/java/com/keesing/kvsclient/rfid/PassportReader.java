package com.keesing.kvsclient.rfid;
import android.nfc.tech.IsoDep;
import android.util.Log;
import com.secunet.epassportapi.RfidReaderBase;

import java.io.IOException;

public class PassportReader extends RfidReaderBase {


    private static final String TAG = "KEESING.PassportReader";
    private volatile IsoDep tag;
    public void setTag(IsoDep tag)
    {
        this.tag = tag;
    }
    @Override
    public void connect()
    {
        Log.v(TAG, "connect chip");
        if(tag == null)
            throw new RuntimeException("No chip found");
        try {
            tag.connect();
        }
        catch(IOException ex) {
            // cannot throw checked exceptions, only runtime exceptions
            throw new RuntimeException("Connect failed", ex);
        }
    }
    @Override
    public byte[] send(byte[] command)
    {
        try {
            byte[] response = tag.transceive(command);
            return response;
        }
        catch(IOException ex) {
            // cannot throw checked exceptions, only runtime exceptions
            throw new RuntimeException("Send APDU failed", ex);
        }
    }
    @Override
    public void disconnect()
    {
        if(tag != null)
        {
            try {
                tag.close();
            }
            catch(IOException ex) {
                // cannot throw checked exceptions, only runtime exceptions
                throw new RuntimeException("Disconnect failed", ex);
            }
            finally {
                tag = null;
            }
        }
    }

}
