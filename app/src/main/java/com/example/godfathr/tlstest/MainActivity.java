package com.example.godfathr.tlstest;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.VolleyLog;


public class MainActivity extends AppCompatActivity {

    //the loading Dialog
    ProgressDialog pDialog;
    // Textview to show data
    TextView tlsversion, yearId, stamp, recordId, yearNumber;
    // JSON object that contains weather information
    JSONObject jsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // link the XML layout to this JAVA class
        setContentView(R.layout.activity_main);

        //link graphical items to variables
        recordId = (TextView) findViewById(R.id.recordId);
        yearId = (TextView) findViewById(R.id.yearId);
        stamp = (TextView) findViewById(R.id.stamp);
        yearNumber = (TextView) findViewById(R.id.yearNumber);
        tlsversion = (TextView) findViewById(R.id.tlsversion);


        // prepare the loading Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait ...");
        pDialog.setCancelable(false);
    }

    private static OkHttpClient getNewHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS);

        return enableTls12OnPreLollipop(client).build();
    }

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
                //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());


                String baseUrl = "https://192.168.0.16/Timestamp/api/DateTimeRecords";
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                };
                // Install the all-trusting trust manager
                try {
                    //SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (GeneralSecurityException e) {
                }
                // Now we can access an https URL without having the certificate in the truststore
                try {
                    String host;
                    //split the base url and just get the host portion for each possible endpoint
                    String hostUrl[] = baseUrl.split("16/");
                    host = hostUrl[0] + "16/";
                    URL url = new URL(host);
                } catch (MalformedURLException e) {
                }
/**************************************************************************************************************/

                //https://stackoverflow.com/questions/32425547/okhttp-unable-to-find-acceptable-protocolsandroid 
                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2) //this is where we explicitly set the version of TLS
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
                        )
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                //specs.add(ConnectionSpec.COMPATIBLE_TLS); //fallback to TLS 1.0...need to remove this. just playing around with it
                //specs.add(ConnectionSpec.CLEARTEXT);
                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }

    public void makeRequest(View view) {
        // Check if Internet is working
        if (!isNetworkAvailable(this)) {
            // Show a message to the user to check his Internet
            Toast.makeText(this, "Please check your Internet connection", Toast.LENGTH_LONG).show();
        } else {
            new DownloadTimeStamp().execute("https://192.168.0.16/Timestamp/api/DateTimeRecords");
        }
    }

    ////////////////////make request in the background
    private static class DownloadTimeStamp extends com.example.godfathr.tlstest.DownloadTimeStamp {
        @Override
        protected Response doInBackground(String... strings) {
            int count = strings.length;
            //long totalSize = 0;
            for (int i = 0; i < count; i++) {
                OkHttpClient _client = getNewHttpClient();
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        //...
                        return true;
                    }
                };
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                Log.e("::::SockFac", _client.socketFactory().toString());
                Log.e("::::protocols", _client.protocols().toString());
                //Log.e("::::ciphers", HttpsURLConnection.getDefaultSSLSocketFactory().getDefaultCipherSuites().toString());


                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });


                HttpUrl.Builder urlBuilder = HttpUrl.parse(strings[i].toString()).newBuilder();

                String url = urlBuilder.build().toString();

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response result = _client.newCall(request).execute();
                    Log.e(":::Resp", result.toString());
                    return result;
                } catch (IOException e) {
                    Log.e("Unable to make request", e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
    }

    ////////////////////check internet connection
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
