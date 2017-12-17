package com.example.godfathr.tlstest;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by godfathr on 12/17/2017.
 */

public class WebServiceFactory {
    //private String URL = "https://192.168.0.16/Timestamp/api/DateTimeRecords";

    public Response MakeRequest(OkHttpClient client, Request request){
        try {
            Response result = client.newCall(request).execute();
            return result;
        } catch (IOException e) {
            Log.e("Unable to make request", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
