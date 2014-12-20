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
//        parentActivity.spinner.show();
        Log.d("LoadInbox","Running inbox query with startindex = " + Integer.toString(startIndex));
    }

    @Override
    protected ArrayList<Fluff> doInBackground(Void... params) {

        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<HashMap<String,Object>> hashmaps = (ArrayList) user.get("inbox");
        if (hashmaps == null) {
            Log.d("LoadInbox","Inbox empty.");
            return null;
        }


        ArrayList<InboxItem> inbox = new ArrayList<InboxItem>(hashmaps.size());

        for (HashMap hm : hashmaps) {
            inbox.add(new InboxItem(hm));
        }
        Collections.sort(inbox,new InboxItem.DateComparator());

        ArrayList<String> favorites;
        favorites = (ArrayList) user.get("favorites");
        if (favorites == null) favorites = new ArrayList<String>();
        Log.d("LoadInbox",String.format("favorites: %s",favorites));

        ArrayList<Fluff> fluffs = new ArrayList<Fluff>();


        Log.d("LoadInbox","inbox: " + inbox.toString());

        if (inbox.size() == 0) {
            Log.d("LoadInbox","Inbox empty.");
            return null;
        } else {
            Log.d("LoadInbox","Inbox size: " + Integer.toString(inbox.size()));
        }

        // get image data from Parse
        for (int i=startIndex; i<startIndex + QUERY_LIMIT; i++) {

            if (i >= inbox.size()) break;

            InboxItem item = inbox.get(i);

            Log.d("LoadInbox",String.format("Item - date: %d, sender: %s, fluffId: %s",item.date,item.from,item.fluffId));
            Fluff f = Fluff.fromString(item.fluffId);
            f.sender = (String) item.from;
            f.sendDate = (Long) item.date;

            Log.d("LoadInbox",String.format("Fluff - id: %s",f.id));
            Log.d("LoadInbox",String.format("favorites: %s",favorites.toString()));

            if (favorites.contains(f.id)) {
                f.favorited = true;
            }

            fluffs.add(f);

        }

        return fluffs;
    }

    @Override
    protected void onPostExecute(ArrayList<Fluff> fluffs) {
        super.onPostExecute(fluffs);

        if (fluffs != null) {
            parentActivity.inbox.addAll(fluffs);

            if (parentActivity.getCurrentState().equals("Inbox")) {
                parentActivity.adapter.addFluffs(fluffs);
                parentActivity.adapter.logFluffs();
            }

        }

        Log.e("LoadInbox","completed.");

        parentActivity.downloadsInProgress -= 1;
        if (parentActivity.downloadsInProgress == 0) parentActivity.spinner.dismiss();

    }
}
