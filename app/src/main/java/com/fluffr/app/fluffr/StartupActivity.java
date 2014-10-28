package com.fluffr.app.fluffr;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class StartupActivity extends ActionBarActivity {

    private final ArrayList<Item> list = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // Load useful things for the application here:
        Log.d("Startup","Setting up Image loader...");
        setupUniversalImageLoader();

        Log.d("Startup","Precaching initial images...");
        getStartingFluffs();

        Log.d("Startup","Launching Home activity...");
        launchHomeActivity();

    }

    private void launchHomeActivity() {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("list",list);
        startActivity(intent);
    }

    private void setupUniversalImageLoader() {
        // Instantiate Universal Image Loader (https://github.com/nostra13/Android-Universal-Image-Loader)
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // display options go here
                .showImageOnLoading(R.drawable.pandafail)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                // configuration options go here
                .defaultDisplayImageOptions(options)
                .memoryCache(new LruMemoryCache(4 * 1024 * 1024))
                .build();


        ImageLoader.getInstance().init(config);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.startup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getStartingFluffs() {

        // get data from Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");

        List<ParseObject> parseObjects = null;

        // synchronous query for parse data - we will show a spinner while the data loads
        try {
            parseObjects = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (parseObjects == null) return;

        for (ParseObject object : parseObjects) {

            Item item = new Item();
            item.title = (String) object.get("title");
            item.subtitle = "subtitle";
            item.id = object.getObjectId();
            item.parseFile = object.getParseFile("image");

            Log.d("getDevelopmentItems", "objectId: " + object.getObjectId());

            this.list.add(item);

        }


    }

}
