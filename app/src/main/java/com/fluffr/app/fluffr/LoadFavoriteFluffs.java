package com.fluffr.app.fluffr;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 11/9/14.
 */
public class LoadFavoriteFluffs extends AsyncTask<Void, Void, ArrayList<Item>> {

    private BrowserActivity parentActivity;

    public LoadFavoriteFluffs(BrowserActivity a) {
        super();
        parentActivity = a;
    }

    protected void onPreExecute() {
        // activate any kind of loading spinners here.
        parentActivity.spinner.show();
    }

    protected ArrayList<Item> doInBackground(Void... params) {

        ArrayList<Item> fluffs = new ArrayList<Item>();

        // get data from Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");
        query.whereEqualTo("title","fluff_2");

        try {
            List<ParseObject> parseObjects = query.find();

            if (parseObjects.size() == 0) {
                Log.e("LoadFavoriteFluffs", "Error: no parse objects found.");
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
                        Log.e("LoadFavoriteFluffs",String.format("Error: no ParseFile found for item with objectId %s",item.id));
                    }

                    Log.d("LoadFavoriteFluffs", "objectId: " + object.getObjectId());

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
            Log.e("LoadFavoriteFluffs","Error: fluffs array returned null.");
        } else {

            // replace browser's existing data with new list
            parentActivity.list.clear();

            for (Item fluff : fluffs) {
                parentActivity.list.add(fluff);
            }

            parentActivity.adapter.notifyDataSetChanged();

            // disable loading spinner
            parentActivity.spinner.dismiss();

        }
    }
}