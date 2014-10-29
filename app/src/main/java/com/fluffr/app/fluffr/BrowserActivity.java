package com.fluffr.app.fluffr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class BrowserActivity extends ActionBarActivity {

    /*
    This is the generic scrolling browser view to be used for the Home, Favorites, and Inbox screens.
    The primary element is a custom ListView widget, in which each row contains the image and
    buttons for user interaction.
    */

    private ListView listView;
    private ArrayList<Item> list = new ArrayList<Item>();
    private CustomAdapter adapter;
//    private final LoadingDialogFragment spinner = new LoadingDialogFragment();
    private LoadingSpinner spinner = new LoadingSpinner();

    // STANDARD CLASS METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        // assign views
        listView = (ListView) findViewById(R.id.listview);

        // Load starting list of fluffs, so that the user doesn't see a bunch of blank items
        // on first load. More fluffs will get loaded upon scrolling events.
        new loadInitialFluffs().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browser, menu);
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

    // HELPER CLASSES FOR LISTVIEW

    private class CustomAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<Item> items;
        private LayoutInflater inflater;

        // constructor for class
        CustomAdapter(Context context, ArrayList<Item> list) {
            this.context = context;
            this.items = list;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ItemView itemView = (ItemView) convertView;

            if (itemView == null) {
                // add custom row layout into parent viewgroup (the row)
                itemView = ItemView.inflate(parent);
            }

            itemView.setItem(getItem(position));

            return itemView;

        }
    }

    private class loadInitialFluffs extends AsyncTask<Void, Void, ArrayList<Item>> {

        protected void onPreExecute() {
            // activate any kind of loading spinners here.
            spinner.show();
        }

        protected ArrayList<Item> doInBackground(Void... params) {

            ArrayList<Item> fluffs = new ArrayList<Item>();

            // get data from Parse
            ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");

            try {
                List<ParseObject> parseObjects = query.find();

                if (parseObjects.size() == 0) {
                    Log.e("loadInitialFluffs","Error: no parse objects found.");
                } else {

                    for (ParseObject object : parseObjects) {

                        Item item = new Item();
                        item.title = (String) object.get("title");
                        item.subtitle = "subtitle";
                        item.id = object.getObjectId();
                        item.parseFile = object.getParseFile("image");

                        if (item.parseFile != null) {
                            ImageLoader.getInstance().loadImageSync(item.parseFile.getUrl());
                        } else {
                            Log.e("loadInitialFluffs",String.format("Error: no ParseFile found for item with objectId %s",item.id));
                        }

                        Log.d("loadInitialFluffs", "objectId: " + object.getObjectId());

                        fluffs.add(item);
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return fluffs;

        }

        protected void onPostExecute(ArrayList<Item> fluffs) {

            // disable any loading spinners and announce to the listview that data is ready.

            if (fluffs == null) {
                Log.e("loadInitialFluffs","Error: fluffs array returned null.");
            } else {

                // instantiate adapter for communicating between data and listview
                list = fluffs;
                adapter = new CustomAdapter(BrowserActivity.this,list);

                // configure listview widget
                listView.setAdapter(adapter);

                // disable loading spinner
                spinner.dismiss();

            }
        }
    }

    private class LoadingSpinner {

        private Dialog dialog;

        public void show() {
            dialog = new Dialog(BrowserActivity.this, R.style.Theme_Transparent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_dialog);

            dialog.show();

        }

        public void dismiss() {
            dialog.dismiss();
        }
    }

}
