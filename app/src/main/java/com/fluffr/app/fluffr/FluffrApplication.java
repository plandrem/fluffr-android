package com.fluffr.app.fluffr;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

/**
 * Created by Patrick on 10/23/14.
 */
public class FluffrApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String app_id = getResources().getString(R.string.parse_app_id);
        String client_key = getResources().getString(R.string.parse_client_key);

        Parse.initialize(this, app_id, client_key);

        Log.d("FluffrApplication","Parse Initialized.");

        uploadParseImages();

    }

    private byte[] convertDrawableResourceToBytes(int res) {
        Bitmap bm = ((BitmapDrawable) getResources().getDrawable(res)).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();

        return data;
    }

    private void uploadParseImages() {

        byte[] bytes = convertDrawableResourceToBytes(R.drawable.pandafail);
        ParseFile file = new ParseFile("fluff.jpg",bytes);

        file.saveInBackground();

        ParseObject newFluff = new ParseObject("fluff");

        newFluff.put("title","pandafail");
        newFluff.put("image",file);

        newFluff.saveInBackground();


    }


}
