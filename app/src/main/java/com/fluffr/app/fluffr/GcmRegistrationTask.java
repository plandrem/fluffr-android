package com.fluffr.app.fluffr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Patrick on 12/1/14.
 */
public class GcmRegistrationTask extends AsyncTask<Void,Void,String> {

    private BrowserActivity parent;

    public GcmRegistrationTask(BrowserActivity p) {
        this.parent = p;
    }

    @Override
    protected String doInBackground(Void... params) {
        String msg = "";
        try {
            if (parent.gcm == null) {
                parent.gcm = GoogleCloudMessaging.getInstance(parent.context);
            }
            parent.regid = parent.gcm.register(parent.SENDER_ID);
            msg = "Device registered, registration ID=" + parent.regid;

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            sendRegistrationIdToBackend();

            // For this demo: we don't need to send it because the device
            // will send upstream messages to a server that echo back the
            // message using the 'from' address in the message.

            // Persist the regID - no need to register again.
            parent.storeRegistrationId(parent.context, parent.regid);
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        Log.d("GcmRegistrationTask",msg);
    }

    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }



}
