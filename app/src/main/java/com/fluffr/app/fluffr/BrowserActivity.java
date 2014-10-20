package com.fluffr.app.fluffr;

import android.content.Context;
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
        final ArrayList<String> list = new ArrayList<String>();

        for (int i=1; i<=100; i++) {
            list.add(Integer.toString(i));
        }

        // instantiate adapter for communicating between data and listview
        final CustomAdapter adapter = new CustomAdapter(this,list);


        // configure listview widget
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                String value = list.get(position);
                Log.d("OnItemClick", String.format("value: %s, position %d, id %d", value, position, id));
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
        private final ArrayList<String> values;
        private LayoutInflater inflater;

        // constructor for class
        CustomAdapter(Context context, ArrayList<String> values) {
            this.context = context;
            this.values = values;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public Object getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView title;
            TextView subtitle;

            View rowView = convertView;

            if (rowView == null) {
                // add custom row layout into parent viewgroup (the row)
                rowView = inflater.inflate(R.layout.row_layout, parent, false);

                // create references to views defined in custom row layout
                title = (TextView) rowView.findViewById(R.id.title);
                subtitle = (TextView) rowView.findViewById(R.id.subtitle);

                // configure views based on source data
                title.setText(values.get(position));
                subtitle.setText(Integer.toString(position));

            }

            return rowView;

        }
    }

}
