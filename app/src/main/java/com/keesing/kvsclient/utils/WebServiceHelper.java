package com.keesing.kvsclient.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebServiceHelper extends AsyncTask<Object, Integer, String> {

    public static final String TAG = "Keesing:WebService";
    private WebServicePostOperation _callback;
    private int statusCode;

    private final String BASE_ADDRESS = "https://acc-sdkapi.keesingtechnologies.com";
    // "https://devdocker01.ktech.local:8889/sdkapi";
    //private final String CERTIFICATE_HOSTNAME = "devws01.ktech.local";

    public WebServiceHelper(WebServicePostOperation callback) {
        _callback = callback;
    }

    @Override
    protected void onPostExecute(String s) {
        this._callback.onFinish(s, this.statusCode);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            StringBuilder returnedData = new StringBuilder();
            // expecting to receive 3 params,
            // first: method like GET, POST, ...
            // second: api to call
            // third: the JSON data to post, in case of GET the third parameter won't be checked.
            assert params == null || params.length < 2;

            String method = params.length >= 1 ? String.valueOf(params[0]) : "GET";
            String endpoint = params.length >= 2 ? BASE_ADDRESS + "/" + params[1] : BASE_ADDRESS;

            URL url = new URL(endpoint);
            //unsafe for production app, for now we use it since the app is just sdk trial.
            disableSSLCertificateChecking();
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setHostnameVerifier(getHostnameVerifier());
            urlConnection.setRequestMethod(method.toUpperCase());

            if (method.equalsIgnoreCase("post")) {

                Gson gson = new Gson();
                String json = "";

                if (params[2] instanceof String) {
                    json = (String) params[2];
                } else {
                    json = gson.toJson(params[2]);
                }

                urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(json.length()));
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write((json).getBytes("UTF-8"));
                outputStream.close();
            }

            this.statusCode = urlConnection.getResponseCode();
            Log.i(TAG, method + " " + endpoint + " returned " + this.statusCode );
            if (this.statusCode == 200) {
                InputStream in = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
                String line = null;

                while ((line = br.readLine()) != null) {
                    returnedData.append(line);
                }
                br.close();

                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            urlConnection.disconnect();
            return returnedData.toString();
        } catch (IOException e) {
            Log.e(TAG, "Unknown issue", e);
        } finally {
            return "";
        }
    }

    private HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                // HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                // return hv.verify(WebServiceHelper.this.CERTIFICATE_HOSTNAME, session);
                return true;
            }
        };
        return hostnameVerifier;
    }

    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, null); // new java.security.SecureRandom()

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
