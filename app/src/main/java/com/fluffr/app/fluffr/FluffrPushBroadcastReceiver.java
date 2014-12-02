package com.fluffr.app.fluffr;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Patrick on 11/25/14.
 */
public class FluffrPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
//        super.onPushOpen(context, intent);

//        Toast.makeText()
        Log.d("onPushOpen", "Intent: " + intent.toString());
        Intent i = new Intent(context,BrowserActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }
}
