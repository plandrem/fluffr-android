package com.fluffr.app.fluffr;

import android.os.AsyncTask;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 11/9/14.
 *
 * Performs asynchronous loading of data from Parse.
 *
 * Inputs:
 * String mode: ['init','favorites'] - selects which data to be retrieved
 */
public class LoadFluffs extends AsyncTask<Void, Void, ArrayList<Fluff>> {

    private BrowserActivity parentActivity;
    private String mode;

    public LoadFluffs(BrowserActivity a, String mode) {
        super();
        this.parentActivity = a;
        this.mode = mode;
    }

    protected void onPreExecute() {
        // activate any kind of loading spinners here.
        parentActivity.spinner.show();
    }

    protected ArrayList<Fluff> doInBackground(Void... params) {

        ArrayList<Fluff> fluffs = new ArrayList<Fluff>();

        // get data from Parse
        ParseQuery<ParseObject> query;

        Log.d("LoadFluffs","Running " + mode + " query");

        if (mode.equals("init")) {
            query = ParseQuery.getQuery("fluff");

        } else if (mode.equals("favorites")) {
            query = ParseQuery.getQuery("fluff");
            query.whereEqualTo("title","fluff_2");

        } else {
            query = ParseQuery.getQuery("fluff");
        }


        try {
            List<ParseObject> parseObjects = query.find();
            ArrayList<String> favorites = (ArrayList) ParseUser.getCurrentUser().get("favorites");

            if (parseObjects.size() == 0) {
                Log.e("LoadFluffs", "Error: no parse objects found.");
            } else {

                for (ParseObject object : parseObjects) {

                    Fluff fluff = new Fluff();
                    fluff.title = (String) object.get("title");
                    fluff.subtitle = "subtitle";
                    fluff.id = object.getObjectId();
                    fluff.parseFile = object.getParseFile("image");

                    if (fluff.parseFile != null) {
                        ImageLoader.getInstance().loadImageSync(fluff.parseFile.getUrl());
                    } else {
                        Log.e("LoadFluffs",String.format("Error: no ParseFile found for item with objectId %s", fluff.id));
                    }

                    if (favorites.contains(fluff.id)) {
                        fluff.favorited = true;
                    }

                    Log.d("LoadFluffs", "objectId: " + object.getObjectId());

                    fluffs.add(fluff);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return fluffs;

    }

    protected void onPostExecute(ArrayList<Fluff> fluffs) {

        // disable any loading spinners and announce to the listview that data is ready.

        if (fluffs == null) {
            Log.e("LoadFluffs","Error: fluffs array returned null.");
        } else {

            // replace browser's existing data with new list
            parentActivity.list.clear();

            for (Fluff fluff : fluffs) {
                parentActivity.list.add(fluff);
            }

            parentActivity.adapter.notifyDataSetChanged();

            // disable loading spinner
            parentActivity.spinner.dismiss();

        }
    }

}