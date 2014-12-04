package com.fluffr.app.fluffr;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;


public class BrowserActivity extends ActionBarActivity
        implements ButtonInterface, MeteorCallback, ResultListener {

    /*
    This is the generic scrolling browser view to be used for the Home, Favorites, and Inbox screens.
    The primary element is a custom ListView widget, in which each row contains the image and
    buttons for user interaction.
    */

    //UI Stuff
    private static String currentState = "Browse";
    private static int currentBrowseIndex = 0;
    private static int currentFavoritesIndex = 0;

    // ListView and Data Stuff
    public ListView listView;
    public ArrayList<Fluff> list = new ArrayList<Fluff>();
    public ArrayList<Fluff> favorites = new ArrayList<Fluff>();
    public CustomAdapter adapter;
    public LoadingSpinner spinner = new LoadingSpinner();
    public boolean downloading = false;
    public String userPhoneNumber = "";

    // Nav Drawer Stuff
    private ArrayList<NavItem> pages = new ArrayList<NavItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    //GCM stuff
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    public String SENDER_ID = "380230415302";
    public String regid;
    public Context context;
    public GoogleCloudMessaging gcm;
    public SharedPreferences prefs;

    //Meteor Stuff
    private Meteor meteor;


    // STANDARD CLASS METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        // assign views
        listView = (ListView) findViewById(R.id.listview);

        // setup Navigation Drawer
        pages.add(new NavItem("Browse"));
        pages.add(new NavItem("Favorites"));
        pages.add(new NavItem("Inbox"));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new NavAdapter(this, pages));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                updateActionBar();
                invalidateOptionsMenu(); // forces redraw of options menu
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

        };

        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Store user's phone number for referencing account
        setUserNumber();

        //Handle Parse User Account
        setParseUser();

        context = getApplicationContext();

        // Check device for Play Services APK.
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.

            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            Log.d("gcm","regid: " + regid);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i("onCreate", "No valid Google Play Services APK found.");

        }

        //Connect to Meteor Server
        meteor = new Meteor("ws://www.fluffr.co/websocket");
        meteor.setCallback(this);

        //Configure Adapter; dataset will be empty.
        adapter = new CustomAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new FluffScrollListener(this));

        //Load initial data
        new LoadFluffs(this, "init").execute();

        //Finalize UI
        updateActionBar();

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        meteor.disconnect();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        // change visibility of action bar stuff depending on drawer state
        return super.onPrepareOptionsMenu(menu);
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

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    // HELPER CLASSES FOR LISTVIEW

    public class CustomAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<Fluff> fluffs;
        private LayoutInflater inflater;

        // constructor for class
        CustomAdapter(Context context, ArrayList<Fluff> list) {
            this.context = context;
            this.fluffs = list;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return fluffs.size();
        }

        @Override
        public Fluff getItem(int position) {
            return fluffs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            FluffView fluffView = (FluffView) convertView;

            if (fluffView == null) {
                // add custom row layout into parent viewgroup (the row)
                fluffView = FluffView.inflate(parent);
            }

            fluffView.setItem(getItem(position), BrowserActivity.this);

            return fluffView;

        }

        public void addFluffs(ArrayList<Fluff> newFluffs) {
            this.fluffs.addAll(newFluffs);
            notifyDataSetChanged();

        }
    }

    public class NavAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<NavItem> items;
        private LayoutInflater inflater;

        // constructor for class
        NavAdapter(Context context, ArrayList<NavItem> list) {
            this.context = context;
            this.items = list;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public NavItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            NavItemView navItemView = (NavItemView) convertView;

            if (navItemView == null) {
                // add custom row layout into parent viewgroup (the row)
                navItemView = NavItemView.inflate(parent);
            }

            navItemView.setItem(getItem(position));

            return navItemView;

        }
    }

    public class LoadingSpinner {

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

    // HELPER CLASSES FOR NAV DRAWER

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectNavigationItem(position);
        }
    }

    private void selectNavigationItem(int position) {

        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);

        if (pages.get(position).text.equals("Browse")) {
            // replace contents of browser's array
            new LoadFluffs(this,"init").execute();
            currentState = "Browse";

        } else if (pages.get(position).text.equals("Favorites")) {
            // replace contents of browser's array
//            new LoadFluffs(this, "favorites").execute();
            currentState = "Favorites";

            // replace browser's existing data with new list
            this.list.clear();

            for (Fluff fluff : favorites) {
                this.list.add(fluff);
            }

            this.adapter.notifyDataSetChanged();

        }

//        updateActionBar();
    }

    private void updateActionBar() {

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (currentState.equals("Browse")) {
            actionBar.setTitle("Browse");
        } else if (currentState.equals("Favorites")) {
            actionBar.setTitle("Favorites");
        }
    }

    @Override
    public void FavoritesButtonPressed(Fluff fluff) {

        // check if current fluff is already in user's favorites
        if (fluff.favorited) {
            //already favorited - remove from favorites
            fluff.favorited = false;

            //remove from current application favorites list
            int i = 0;
            for (Fluff f : favorites) {
                if (f.id.equals(fluff.id)) {
                    favorites.remove(i);
                    break;
                }
                i++;
            }

            //remove from Parse Database

            List<String> toRemove = new ArrayList<String>(1);
            toRemove.add(fluff.id);

            ParseUser user = ParseUser.getCurrentUser();
            user.removeAll("favorites", toRemove);
            user.saveInBackground();


        } else {
            fluff.favorited = true;

            // Add to current application favorites list
            this.favorites.add(fluff);

            // Add to Parse Database
            ParseUser user = ParseUser.getCurrentUser();
            user.addUnique("favorites", fluff.id);
            user.saveInBackground();

            Log.d("FavoritesButtonInterface", "Item added!");
        }
    }

    @Override
    public void DeleteButtonPressed(Fluff fluff) {
        new HttpTestTask().execute();
    }

    private void setUserNumber() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        userPhoneNumber = tMgr.getLine1Number();
    }

    private void setParseUser() {

        ParseUser user = ParseUser.getCurrentUser();

        if (user == null) {
            // user has not been registered - create new account

//            spinner.show();

            // check if user exists in database
            ParseQuery query = ParseUser.getQuery();
            query.whereEqualTo("username",userPhoneNumber);

            ParseUser existingUser = null;

            try {
                existingUser = (ParseUser) query.getFirst();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (existingUser != null) {
                Log.e("setParseUser","Logging in user: " + userPhoneNumber);
                try {
                    ParseUser.logIn(userPhoneNumber, "password");
                    user = existingUser;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {

                Log.d("setParseUser", "New account for user: " + userPhoneNumber);

                user = new ParseUser();
                user.setUsername(userPhoneNumber);
                user.setPassword("password");
                try {
                    user.signUp();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //            spinner.dismiss();

                //TODO - display tutorial modal screen
            }


        } else {
            // user already registered

            Log.d("setParseUser", "Resuming session for this user.");

            // get favorites list
            new LoadFluffs(this,"favorites").execute();


        }


        if (user == null) {
            Log.e("setParseUser","user setup failed.");
            return;
        }

        // either way, update the installation info for Parse Push

        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        String id = parseInstallation.getInstallationId();

        Log.d("Send installation id","user: " + user.toString() + "\ninstallationId: " + id);

        user.put("installationId",id);
        user.saveInBackground();


    }

    public static String getCurrentState() {
        return currentState;
    }

    public static void increaseBrowseIndex(int count) {
        currentBrowseIndex += count;
        Log.d("increaseBrowseIndex", "New Browse Index: " + Integer.toString(currentBrowseIndex));
    }

    public static void increaseFavoritesIndex(int count) {
        currentFavoritesIndex += count;
    }

    public static int getCurrentBrowseIndex() {
        return currentBrowseIndex;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("checkPlayServices", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {

        String TAG = "getRegistrationId";

        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(BrowserActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new GcmRegistrationTask(this).execute();
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    public void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("storeRegistrationId", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /*************************************************/
    // Meteor Interface Methods
    /*************************************************/

    //MeteorCallback
    @Override
    public void onConnect() {
        Log.d("Meteor Interface","onConnect");

        Map<String,Object> payload = new HashMap<String, Object>();
        payload.put("foo","bar");

        Object[] data = {payload};
        meteor.call("test",data,this);
    }

    @Override
    public void onDisconnect(int i, String s) {
        Log.d("Meteor Interface","onDisconnect");

    }

    @Override
    public void onDataAdded(String s, String s2, String s3) {

    }

    @Override
    public void onDataChanged(String s, String s2, String s3, String s4) {

    }

    @Override
    public void onDataRemoved(String s, String s2) {

    }

    @Override
    public void onException(Exception e) {

    }

    //ResultListener
    @Override
    public void onSuccess(String s) {
        Log.d("Meteor Interface","onSuccess: " + s);

    }

    @Override
    public void onError(String s, String s2, String s3) {
        Log.e("Meteor Interface","onError:");
        Log.e("Meteor Interface","error:" + s);
        Log.e("Meteor Interface","reason:" + s2);
        Log.e("Meteor Interface","details:" + s3);
    }

    public void sendFluff(String recipient, final String fluffId) throws ParseException {
        // responds when the user selects a contact from the ContactsDialog. Pushes a notification
        // to the recipient's phone with a data payload containing the sender and new Fluff id.
        // Each users' DB entry is updated to note that the Fluff was sent, and the Fluff itself
        // records that it has been sent another time.

        //TODO - determine the recipient's platform type
        String platform = "android";

        // Send data to Meteor server, which will push to the recipient
        Map<String,Object> payload = new HashMap<String, Object>();
        payload.put("sender",userPhoneNumber);
        payload.put("recipient",recipient);
        payload.put("fluffId",fluffId);
        payload.put("platform",platform);

        Object[] data = {payload};
        meteor.call("sendFluff",data,this);

        // Update sending user's "sent" array
        ParseUser sendingUser = ParseUser.getCurrentUser();
        sendingUser.addUnique("sent",fluffId);
        sendingUser.saveEventually();

        // Update receiving user's inbox
        ParseQuery userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username",recipient);
        userQuery.getFirstInBackground(new updateInboxCallback(userPhoneNumber, fluffId) {
        });

        // Update fluff's times sent
        ParseQuery fluffQuery = ParseQuery.getQuery("fluff");
        ParseObject fluff = fluffQuery.get(fluffId);
        fluff.increment("timesSent");
        fluff.saveEventually();



    }

    private class updateInboxCallback extends GetCallback {
        String sender = "";
        String fluffId = "";
        String date = new Date().toString();

        private updateInboxCallback(String sender, String fluffId) {
            super();
            this.sender = sender;
            this.fluffId = fluffId;
        }

        @Override
        public void done(ParseObject parseObject, ParseException e) {

            if (e != null) {
                e.printStackTrace();

            } else {
                // insert an object into the inbox array, eg
                // {"fluffId":"skh5215f","from":"16518155005","date":"2014-12-03 17:18:00"}

                ParseUser recipientUser = (ParseUser) parseObject;
                recipientUser.add("inbox","{\"fluffId\":\"" + fluffId + "\",\"from\":\"" + sender + "\",\"date\":\"" + date + "\"}");
                recipientUser.saveEventually();

            }
        }

    }
}
