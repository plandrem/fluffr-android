package com.fluffr.app.fluffr;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by Patrick on 12/2/14.
 */
public class HttpTestTask extends AsyncTask<Void,Void,Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet("http://www.fluffr.co/test");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String response = "";

        try {
            response = client.execute(get, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("HTTP response", response);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
