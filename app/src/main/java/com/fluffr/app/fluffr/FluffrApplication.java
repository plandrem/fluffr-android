package com.fluffr.app.fluffr;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

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

//        uploadParseImage("fluff_0", R.drawable.imgres_0);
//        uploadParseImage("fluff_1",R.drawable.imgres_1);
//        uploadParseImage("fluff_2",R.drawable.imgres_2);
//        uploadParseImage("fluff_3",R.drawable.imgres_3);
//        uploadParseImage("fluff_4",R.drawable.imgres_4);

    }

    private byte[] convertDrawableResourceToBytes(int res) {
        Bitmap bm = ((BitmapDrawable) getResources().getDrawable(res)).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();

        return data;
    }

    private void uploadParseImage(String title, int res) {

        byte[] bytes = convertDrawableResourceToBytes(res);
        ParseFile file = new ParseFile("fluff.jpg",bytes);

        file.saveInBackground();

        ParseObject newFluff = new ParseObject("fluff");

        newFluff.put("title",title);
        newFluff.put("image",file);

        newFluff.saveInBackground();


    }


}
