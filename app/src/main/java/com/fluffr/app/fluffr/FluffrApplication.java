package com.fluffr.app.fluffr;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.text.ParseException;

/**
 * Created by Patrick on 10/23/14.
 */
public class FluffrApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // Instantiate Universal Image Loader (https://github.com/nostra13/Android-Universal-Image-Loader)
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // display options go here
//                .showImageOnLoading(R.drawable.spinner)
//                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                // configuration options go here
                .defaultDisplayImageOptions(options)
//                .memoryCache(new LruMemoryCache(4 * 1024 * 1024))
                .build();


        ImageLoader.getInstance().init(config);


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

    public static Context getContext() {
        return context;
    }

}
