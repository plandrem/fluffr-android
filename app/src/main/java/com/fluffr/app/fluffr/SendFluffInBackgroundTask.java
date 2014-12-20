package com.fluffr.app.fluffr;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.Meteor;

/**
 * Created by Patrick on 12/16/14.
 */
public class SendFluffInBackgroundTask extends AsyncTask<PhoneContact,Void,Boolean>{

    private Context context;
    private BrowserActivity browserActivity;
    private Meteor meteor;
    private Fluff fluff;

    private enum accountType {
        HAS_ACCOUNT,
        PENDING_ACCOUNT,
        NO_ACCOUNT,
        ACCOUNT_ERROR
    }

    public SendFluffInBackgroundTask(Context context, BrowserActivity ba, Fluff f) {
        this.context = context;
        this.browserActivity = ba;
        this.meteor = ba.meteor;
        this.fluff = f;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Sending Fluff...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(PhoneContact... params) {

        PhoneContact recipient = params[0];
        Boolean success = false;

        //TODO - change to recipient.number
        String patricksNumber = "+16518155005";

        try {

            switch (checkAccountType(patricksNumber)) {

                case HAS_ACCOUNT:
                    updateRecipientInbox(patricksNumber);
                    sendPushNotification(patricksNumber);

                    success = true;
                    break;

                case PENDING_ACCOUNT:
                    updatePendingUserInbox(recipient.getNumber());
                    sendSMS(patricksNumber);

                    success = true;
                    break;

                case NO_ACCOUNT:
                    createPendingAccount(recipient.getNumber());
                    updatePendingUserInbox(recipient.getNumber());
                    sendSMS(patricksNumber);

                    success = true;
                    break;
            }

            // Update sending user's "sent" array
            ParseUser sendingUser = ParseUser.getCurrentUser();
            sendingUser.addUnique("sent", fluff.id);
            sendingUser.saveInBackground();

            // Update fluff's times sent
            ParseQuery fluffQuery = ParseQuery.getQuery("fluff");
            ParseObject f = fluffQuery.get(fluff.id);
            f.increment("timesSent");
            f.saveInBackground();

            // add selected contact to recent contacts list
            ArrayList recents = new ArrayList(8);

            if (sendingUser.getList("recentRecipients") != null) {
                recents.addAll(sendingUser.getList("recentRecipients"));

                if (recents.size() >= 8) {
                    // pop oldest
                    recents.remove(7);
                }

                // make sure there aren't any duplicates in recent recipients
                for (int i=0; i < recents.size(); i++) {
                    if (recipient.id == (Integer) recents.get(i)) {
                        recents.remove(i);
                    }
                }

                sendingUser.remove("recentRecipients");

            } else {
                // first recipient saved

            }

            // push this current contact
            recents.add(0, recipient.id);
            for (Object id : recents) {
                sendingUser.add("recentRecipients", id);
            }

            sendingUser.save();


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        if (success) {
            Toast.makeText(context, "Fluff Sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Fluff Sending Failed :(", Toast.LENGTH_SHORT).show();
        }
    }

    private accountType checkAccountType(String number) {

        accountType returnVal = accountType.ACCOUNT_ERROR;

        ParseQuery userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username",number);
        try {

            if (userQuery.count() == 0) {

                // account does not exist; check pending accounts

                ParseQuery pendingQuery = ParseQuery.getQuery("pendingUser");
                pendingQuery.whereEqualTo("number",number);

                if (pendingQuery.count() > 0) {
                    returnVal = accountType.PENDING_ACCOUNT;
                } else {
                    returnVal = accountType.NO_ACCOUNT;
                }

            } else {
                // account exists
                returnVal = accountType.HAS_ACCOUNT;

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return returnVal;
    }

    private void sendSMS(String number) throws ParseException {

        String TAG = "sendSMS";
        Log.d(TAG, "sending SMS to: " + number);

        SmsManager smsManager = SmsManager.getDefault();
        String ownerName = "";
        String message = "";

        // get name of user for personalized message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Cursor c = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
            int count = c.getCount();
            String[] columnNames = {ContactsContract.Profile.DISPLAY_NAME};
            boolean b = c.moveToFirst();
            int p = c.getPosition();
            if (count == 1 && p == 0) {
                for (int j = 0; j < columnNames.length; j++) {
                    String columnName = columnNames[j];
                    String columnValue = c.getString(c.getColumnIndex(columnName));
                    Log.d(TAG, columnName + " - " + columnValue);

                    if (columnName != null) {
                        if (columnName.equals(ContactsContract.Profile.DISPLAY_NAME)) {
                            ownerName = columnValue.split(" ")[0];
                        }
                    }

                }
            }
            c.close();

            // Send SMS message
            message = context.getResources().getString(R.string.non_user_sms);
            smsManager.sendTextMessage(number, null, ownerName + " " + message, null, null);


        } else {
            // Pre-Ice-Cream-Sandwich
            message = context.getResources().getString(R.string.old_non_user_sms);
            smsManager.sendTextMessage(number, null, message, null, null);

        }

    }

    private void sendPushNotification(String number) throws ParseException {

        String TAG = "SendPushNotification";

        // get details for recipient
        ParseQuery userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username", number);
        ParseUser recipientUser = (ParseUser) userQuery.getFirst();

        Log.d(TAG, "to: " + number);
        Log.d(TAG, recipientUser.toString());

        String platform = recipientUser.getString("platform");
        String deviceId = "";



        // Get device id for recipient
        if (platform.equals("android")) {
            deviceId = recipientUser.getString("androidGcmRegistrationId");

        } else if (platform.equals("iOS")) {
            // handle iOS push notifications
        }

        // Send data to Meteor server, which will push to the recipient
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("sender", browserActivity.userPhoneNumber);
        payload.put("recipient", number);
        payload.put("targetDevice", deviceId);
        payload.put("fluffId", fluff.id);
        payload.put("date", new Date().getTime());
        payload.put("platform", platform);

        Object[] data = {payload};
        meteor.call("sendFluff", data, browserActivity);

    }

    private void updateRecipientInbox(String number) throws ParseException{

        // get details for recipient
        ParseQuery userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username", number);
        ParseUser recipientUser = (ParseUser) userQuery.getFirst();

        JSONObject obj = getObjectForInbox();

        recipientUser.add("inbox", obj);
        recipientUser.put("hasUnseenFluffs", "true");
        recipientUser.save();

    }

    private void createPendingAccount(String number) throws ParseException {

        //TODO -- normalize phone numbers; can use PhoneNumberUtils class

        ParseObject newUser = new ParseObject("pendingUser");
        newUser.put("number",number);
        newUser.save();


        }

    private void updatePendingUserInbox(String number) throws ParseException {
        ParseQuery query = ParseQuery.getQuery("pendingUser");
        query.whereEqualTo("number",number);
        ParseObject pendingUser = query.getFirst();

        JSONObject obj = getObjectForInbox();

        pendingUser.add("inbox", obj);
        pendingUser.save();

    }

    private JSONObject getObjectForInbox() {
        // Update receiving user's inbox
        JSONObject obj = new JSONObject();
        try {
            obj.put("fluffId", fluff.id);
            obj.put("from", browserActivity.userPhoneNumber);
            obj.put("date", new Date().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;

    }
}
