package com.fluffr.app.fluffr;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private int startIndex;
    private boolean inBackground = false;

    private int prependedFluffs = 0;
    private int QUERY_LIMIT = 20;

    public LoadFluffs(BrowserActivity a, String mode) {
        this(a,mode,false,0);
    }

    public LoadFluffs(BrowserActivity a, String mode, boolean inBackground) {
        this(a,mode,inBackground,0);
    }

    public LoadFluffs(BrowserActivity a, String mode, boolean inBackground, int startIndex) {
        super();
        this.parentActivity = a;
        this.mode = mode;
        this.startIndex = startIndex;
        this.inBackground = inBackground;
    }

    protected void onPreExecute() {

        if (!inBackground) {
            // activate any kind of loading spinners here.
            parentActivity.spinner.show();
        }

        parentActivity.downloadsInProgress += 1;
    }

    protected ArrayList<Fluff> doInBackground(Void... params) {

        ArrayList<Fluff> fluffs = new ArrayList<Fluff>();

        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<String> favorites = (ArrayList) user.get("favorites");
        ArrayList<String> dislikes  = (ArrayList) user.get("dislikes");

        // get data from Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");
        query.setLimit(QUERY_LIMIT);

        query.addAscendingOrder("index");
        query.whereNotEqualTo("deletedByAdmin",true);
        if (dislikes != null) query.whereNotContainedIn("objectId",dislikes);

        // if we need to load fluffs before the starting index, use a prepend query
        // otherwise leave null.
        ParseQuery<ParseObject> prependQuery = null;


        Log.d("LoadFluffs", "Running " + mode + " query with start index: " + Integer.toString(startIndex));

        //TODO -- update favorites and inbox as user scrolls

        if (mode.equals("init")) {
            if (startIndex > 0) {

                query.whereGreaterThanOrEqualTo("index", startIndex);

                prependQuery = ParseQuery.getQuery("fluff");
                prependQuery.addDescendingOrder("index");
                prependQuery.whereNotEqualTo("deletedByAdmin",true);
                if (dislikes != null) prependQuery.whereNotContainedIn("objectId",dislikes);

                prependQuery.whereLessThan("index",startIndex);
                prependQuery.setLimit(QUERY_LIMIT);

            }

        } else if (mode.equals("favorites")) {
            query.whereContainedIn("objectId", favorites);

        } else if (mode.equals("more_browse")) {
            query.whereGreaterThanOrEqualTo("index",startIndex);

        } else if (mode.equals("more_browse_up")) {
            query = null;

            prependQuery = ParseQuery.getQuery("fluff");
            prependQuery.addDescendingOrder("index");
            prependQuery.whereNotEqualTo("deletedByAdmin",true);
            if (dislikes != null) prependQuery.whereNotContainedIn("objectId",dislikes);

            prependQuery.whereLessThan("index",startIndex);
            prependQuery.setLimit(QUERY_LIMIT);

        } else {

        }


        try {
            if (query != null) {
                List<ParseObject> parseObjects = query.find();

                if (parseObjects.size() == 0) {
                    Log.e("LoadFluffs", "Error: no parse objects found.");
                    return null;

                } else {

                    for (ParseObject object : parseObjects) {

                        Fluff fluff = new Fluff(object);

                        if (favorites.contains(fluff.id)) {
                            fluff.favorited = true;
                        }

//                        Log.d("LoadFluffs", "objectId: " + object.getObjectId());

                        fluffs.add(fluff);
                    }
                }
            }

            // prepend with indices before startIndex
            if (prependQuery != null) {
                ArrayList<Fluff> prependFluffs = new ArrayList<Fluff>(QUERY_LIMIT);
                List<ParseObject> prependParseObjects = prependQuery.find();

                if (prependParseObjects.size() == 0) {
                    Log.e("LoadFluffs", "Error: no prependable parse objects found.");
                    return null;

                } else {

                    for (ParseObject object : prependParseObjects) {

                        Fluff fluff = new Fluff(object);

                        if (favorites.contains(fluff.id)) {
                            fluff.favorited = true;
                        }

//                        Log.d("LoadFluffs", "objectId: " + object.getObjectId());

                        prependFluffs.add(fluff);
                    }

                    fluffs.addAll(0, prependFluffs);
                    prependedFluffs = prependFluffs.size();
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

            if (mode.equals("init")) {

                // replace browser's existing data with new list
                parentActivity.list.clear();

                for (Fluff fluff : fluffs) {
                    parentActivity.list.add(fluff);
                }

                parentActivity.adapter.addFluffs(fluffs);

                if (startIndex > 0) {
                    // loading from saved state -- set listview to proper position
//                    parentActivity.listView.setSelection(prependedFluffs);
                    parentActivity.listView.setSelectionFromTop(prependedFluffs, parentActivity.listOffset);

                    //TODO - adjust listview position to match the scroll position when app was closed.

                } else {
                    // load from scratch

                }


                // now that we've loaded initial data, handle any
                // further instructions such as navigation to other pages
                parentActivity.checkStartupInstructions();

//                parentActivity.spinner.dismiss();

            } else if (mode.equals("favorites")) {
                parentActivity.favorites.clear();

                for (Fluff fluff : fluffs) {
                    parentActivity.favorites.add(fluff);
                }

                if (parentActivity.getCurrentState().equals("Favorites")) {
                    parentActivity.adapter.clear();
                    parentActivity.adapter.addFluffs(parentActivity.favorites);
                    parentActivity.spinner.dismiss();
                }

            } else if (mode.equals("more_browse")) {
                parentActivity.list.addAll(fluffs);
                parentActivity.adapter.addFluffs(fluffs);

            } else if (mode.equals("more_browse_up")) {
                Collections.reverse(fluffs);
                parentActivity.list.addAll(0, fluffs);
                parentActivity.adapter.prependFluffs(fluffs);

            }

            parentActivity.downloadsInProgress -= 1;
            if (!inBackground) {
                if (parentActivity.downloadsInProgress == 0) parentActivity.spinner.dismiss();
            }

        }
    }

}