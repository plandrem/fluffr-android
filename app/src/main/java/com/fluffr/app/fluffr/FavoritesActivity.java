package com.fluffr.app.fluffr;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class FavoritesActivity extends BrowserActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new loadInitialFluffs().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorites, menu);
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

    private class loadInitialFluffs extends AsyncTask<Void, Void, ArrayList<Item>> {

        protected void onPreExecute() {
            // activate any kind of loading spinners here.
            FavoritesActivity.this.spinner.show();
        }

        protected ArrayList<Item> doInBackground(Void... params) {

            ArrayList<Item> fluffs = new ArrayList<Item>();

            // get data from Parse
            ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");
            query.whereEqualTo("title","fluff_2");

            try {
                List<ParseObject> parseObjects = query.find();

                if (parseObjects.size() == 0) {
                    Log.e("loadInitialFluffs", "Error: no parse objects found.");
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
                adapter = new BrowserActivity.CustomAdapter(FavoritesActivity.this,list);

                // configure listview widget
                listView.setAdapter(adapter);

                // disable loading spinner
                spinner.dismiss();

            }
        }
    }

}
