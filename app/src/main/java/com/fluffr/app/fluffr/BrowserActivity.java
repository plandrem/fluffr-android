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

import com.parse.Parse;
import com.parse.ParseAnalytics;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class BrowserActivity extends ActionBarActivity {

    /*
    This is the generic scrolling browser view to be used for the Home, Favorites, and Inbox screens.
    The primary element is a custom ListView widget, in which each row contains the image and
    buttons for user interaction.
    */

    private ListView listView;

    // STANDARD CLASS METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        // create dummy array for testing listview
        final ArrayList<Item> list = getDevelopmentItems();
        Log.d("Dev Data", list.get(0).title);
        Log.d("Dev Data", list.get(1).title);

        // instantiate adapter for communicating between data and listview
        final CustomAdapter adapter = new CustomAdapter(this,list);


        // configure listview widget
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Item item = list.get(position);
                Log.d("OnItemClick", String.format("title: %s, position %d, id %d", item.title, position, id));
            }
        });

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

    private ArrayList<Item> getDevelopmentItems() {

        final ArrayList<Item> list = new ArrayList<Item>();


        for (int i=1; i<=100; i++) {

            Item item = new Item();
            item.title = String.format("Item %d", i);
            item.subtitle = Integer.toString(i);
            item.image = getResources().getDrawable(R.drawable.pandafail);

            list.add(item);
        }

        return list;

    }

}
