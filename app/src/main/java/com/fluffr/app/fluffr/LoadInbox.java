package com.fluffr.app.fluffr;

import android.os.AsyncTask;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Patrick on 12/4/14.
 */
public class LoadInbox extends AsyncTask<Void,Void,ArrayList<Fluff>> {


    private BrowserActivity parentActivity;
    private String mode;
    private int startIndex;

    //TODO -- implement query limit for inbox
    private int QUERY_LIMIT = 20;

    public LoadInbox(BrowserActivity a, String mode) {
        this(a,mode,0);
    }

    public LoadInbox(BrowserActivity a, String mode, int startIndex) {
        super();
        this.parentActivity = a;
        this.mode = mode;
        this.startIndex = startIndex;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        parentActivity.downloadsInProgress += 1;
        parentActivity.spinner.show();
    }

    @Override
    protected ArrayList<Fluff> doInBackground(Void... params) {

        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<HashMap<String,String>> inbox = (ArrayList) user.get("inbox");
        ArrayList<String> favorites = (ArrayList) user.get("favorites");

        ArrayList<Fluff> fluffs = new ArrayList<Fluff>();

        if (inbox == null) {
            Log.d("LoadInbox","Inbox empty.");
            return null;
        }

        Log.d("LoadInbox","inbox: " + inbox.toString());

        if (inbox.size() == 0) {
            Log.d("LoadInbox","Inbox empty.");
            return null;
        }

        // get image data from Parse
        for (HashMap<String,String> hm : inbox) {

            Fluff f = new Fluff(hm.get("fluffId"));
            f.sender = hm.get("from");
            f.sendDate = hm.get("date");

            if (favorites.contains(f.id)) {
                f.favorited = true;
            }

            fluffs.add(f);

        }

        Collections.reverse(fluffs);

        return fluffs;
    }

    @Override
    protected void onPostExecute(ArrayList<Fluff> fluffs) {
        super.onPostExecute(fluffs);
        parentActivity.inbox = fluffs;

        if (parentActivity.getCurrentState().equals("Inbox")) {
            parentActivity.adapter.clear();
            parentActivity.adapter.addFluffs(fluffs);
            parentActivity.spinner.dismiss();
        }

        parentActivity.downloadsInProgress -= 1;
        if (parentActivity.downloadsInProgress == 0) parentActivity.spinner.dismiss();


    }
}
