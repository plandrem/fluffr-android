package com.fluffr.app.fluffr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Patrick on 11/2/14.
 */
public class PhoneContact {
    public String number;
    public String name;
    public long id;
    public Bitmap photo;
    public Uri photoUri;

    public void setPhotoFromString(String photoString) {
        if (photoString == null) return;

        Log.d("PhoneContact","converting photo string...");
        byte[] bytes = photoString.getBytes();
        photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void setPhotoFromUri(Uri uri) {

    }
}
