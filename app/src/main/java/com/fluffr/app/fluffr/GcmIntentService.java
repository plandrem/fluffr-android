package com.fluffr.app.fluffr;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by Patrick on 12/1/14.
 */
public class GcmIntentService extends IntentService {
    private final String TAG = "GcmIntentService";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

//                // This loop represents the service doing some work.
//                for (int i=0; i<5; i++) {
//                    Log.i(TAG, "Working... " + (i+1)
//                            + "/5 @ " + SystemClock.elapsedRealtime());
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                    }
//                }
//                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                // Post notification of received message.
//                sendNotification("Received: " + extras.toString());

                Log.i(TAG, "Received: " + extras.toString());
                processGcmMessage(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, BrowserActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void processGcmMessage(Bundle extras) {
        Log.d("processGcmMessage",extras.toString());

        // all of our GCM messages should contain a type.
        if (extras.keySet().contains("type")) {
            String type = extras.getString("type");

            if (type.equals("receivedFluff")) {
                // user has been sent a fluff from another user
                String msg = extras.getString("msg");
                String fluffId = extras.getString("fluffId");
                String sender = extras.getString("sender");

                // build notification
                mNotificationManager = (NotificationManager)
                        this.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent startupIntent = new Intent(this, BrowserActivity.class);
                startupIntent.putExtra("startupMode","newFluff");
                startupIntent.putExtra("fluffId",fluffId);

                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                        startupIntent, 0);

                // set vibration pattern
                long[] vib = new long[2];
                vib[0] = 0;
                vib[1] = 400;

                // load icon for pulldown menu
                Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.fluffricon);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setAutoCancel(true)
                                .setVibrate(vib)
                                .setSmallIcon(R.drawable.fluffr_notification_icon)
                                .setLargeIcon(icon)
                                .setContentTitle("You've received a Fluff!")
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(msg))
                                .setContentText(msg);

                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

            }

        } else {
            // is some unknown kind of message...
        }

    }
}