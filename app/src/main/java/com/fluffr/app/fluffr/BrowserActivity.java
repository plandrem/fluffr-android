package com.fluffr.app.fluffr;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

    // STANDARD CLASS METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        // instantiate adapter for communicating between data and listview
        this.adapter = new CustomAdapter(this,list);

        // configure listview widget
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        // create dummy array for testing listview - notifies listview to update when finished.
        getDevelopmentItems();

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
//
//                Item item = list.get(position);
//                Log.d("OnItemClick", String.format("title: %s, position %d, id %d", item.title, position, id));
//            }
//        });

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

    private class MyCallback extends FindCallback<ParseObject> {

        private ArrayList<Item> list;

        public MyCallback(ArrayList<Item> list) {
            super();
            this.list = list;
        }

        @Override
        public void done(List<ParseObject> parseObjects, ParseException e) {
            if (e == null) {

                for (ParseObject object : parseObjects) {

                    Item item = new Item();
                    item.title = (String) object.get("title");
                    item.subtitle = "subtitle";
                    item.id = (String) object.get("objectId");

                    this.list.add(item);

                }
            } else {

                Log.d("getDevelopmentItems", "Parse Error: " + e.getMessage());

            }
        }
    }


    private void getDevelopmentItems() {

        // get data from Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");
//        query.findInBackground(MyCallback(this.list));

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {

                    for (ParseObject object : parseObjects) {

                        Item item = new Item();
                        item.title = (String) object.get("title");
                        item.subtitle = "subtitle";
                        item.id = object.getObjectId();

                        Log.d("getDevelopmentItems","objectId: " + object.getObjectId());

                        BrowserActivity.this.list.add(item);
                        BrowserActivity.this.adapter.notifyDataSetChanged();

                    }


                } else {
                    Log.d("getDevelopmentItems","Parse Error: " + e.getMessage());
                }
            }
        });

        // static list of panda images

//        for (int i=1; i<=100; i++) {
//
//            Item item = new Item();
//            item.title = String.format("Item %d", i);
//            item.subtitle = Integer.toString(i);
//            item.image = getResources().getDrawable(R.drawable.pandafail);
//
//            list.add(item);
//        }

//        this.list = list;
//        this.adapter.notifyDataSetChanged();

    }

}
