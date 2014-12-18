package com.fluffr.app.fluffr;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private static boolean hasUnseenFluffs = false;

    // ListView and Data Stuff
    public ListView listView;
    public ArrayList<Fluff> list = new ArrayList<Fluff>();
    public ArrayList<Fluff> favorites = new ArrayList<Fluff>();
    public ArrayList<Fluff> inbox = new ArrayList<Fluff>();
    public CustomAdapter adapter;
    public LoadingSpinner spinner = new LoadingSpinner();
    public int downloadsInProgress = 0;
    public String userPhoneNumber = "";
    public int listOffset = 0;
    public int listPosition = 0;

    // Nav Drawer Stuff
    private ArrayList<NavItem> pages = new ArrayList<NavItem>();
    private DrawerLayout drawerLayout;
    private LinearLayout drawerLinearLayout;
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

    public static final String RECEIVE_FLUFF = "com.fluffr.action.receive_fluff";
    private BroadcastReceiver FluffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("FluffReceiver","received broadcast.");

            // add new fluff to inbox
            Toast.makeText(BrowserActivity.this,"Fluff Received.",Toast.LENGTH_SHORT).show();

            Bundle extras = intent.getExtras();

            Fluff newFluff = Fluff.fromString(extras.getString("fluffId"));
            newFluff.sender = extras.getString("sender");
            newFluff.sendDate = extras.getLong("date");

            inbox.add(0, newFluff);

            if (currentState.equals("Inbox")) {
                ArrayList<Fluff> fluffArrayList = new ArrayList<Fluff>(1);
                fluffArrayList.add(newFluff);
                adapter.prependFluffs(fluffArrayList);
                scrollToTop();
            } else {
                hasUnseenFluffs = true;
                updateActionBar();
            }

        }
    };


    //Meteor Stuff
    public Meteor meteor;


    // STANDARD CLASS METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        if (savedInstanceState != null) {
            Log.d("onCreate","found SavedInstanceState bundle");
//            currentState = savedInstanceState.getString("currentState");
        }

        spinner.show();

        // assign views
        listView = (ListView) findViewById(R.id.listview);

        // setup Navigation Drawer
        pages.add(new NavItem("Browse"));
        pages.add(new NavItem("Favorites"));
        pages.add(new NavItem("Inbox"));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLinearLayout = (LinearLayout) findViewById(R.id.left_drawer);
        drawerList = (ListView) findViewById(R.id.left_drawer_list);
        drawerList.setAdapter(new NavAdapter(this, pages));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Store user's phone number for referencing account
        setUserNumber();

        //Handle Parse User Account
        // must happen before checking GCM registration for push notifications
        // regid is stored in parse user account
        setParseUser();

        Log.d("onCreate","getting context...");
        context = getApplicationContext();

        Log.d("onCreate","checking Play service...");
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
        Log.d("onCreate","connecting to Meteor...");
        meteor = new Meteor("ws://www.fluffr.co/websocket");
        meteor.setCallback(this);

        //Configure Adapter; dataset will be empty.
        Log.d("onCreate","configuring Fluff adapter...");
        adapter = new CustomAdapter(this, new ArrayList<Fluff>());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new FluffScrollListener(this));

        // Check for saved data, or start up with initial parameters
        LoadState();

        // Register receiver for incoming messages
        Log.d("onCreate","registering Fluff receiver...");
        IntentFilter intentFilter = new IntentFilter(RECEIVE_FLUFF);
        this.registerReceiver(FluffReceiver,intentFilter);


        //Finalize UI
        Log.d("onCreate","finalize UI...");
        updateActionBar();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "received new intent.");

        Bundle extras = intent.getExtras();

        if (extras.containsKey("startupMode")) {
            String mode = extras.getString("startupMode");

            if (mode.equals("newFluff")) {
                goToInbox();
                scrollToTop();
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d("onResume","onResume");
        super.onResume();
        checkPlayServices();
        checkStartupInstructions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        meteor.disconnect();
        this.unregisterReceiver(FluffReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("onSaveInstanceState","saving state...");
        super.onSaveInstanceState(outState);

        Parcelable listState = listView.onSaveInstanceState();
        outState.putParcelable("listState",listState);
        outState.putString("currentState",currentState);
        outState.putInt("currentBrowseIndex", currentBrowseIndex);

    }

    private void SaveState(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentState",currentState);
        editor.putInt("currentBrowseIndex",currentBrowseIndex);

        if (getCurrentState().equals("Browse")) savePosition();

        // get currently visible Fluff index
        Fluff f = list.get(listPosition);
        int index = f.index;

        // get list offset from top
        Log.d("SaveState","position: " + Integer.toString(listPosition));

        editor.putInt("listOffset",listOffset);
        editor.putInt("listPosition",listPosition);

        Log.d("SaveState", "saving...");
        String logStr = "";
        logStr += String.format("currentState: %s, ", currentState);
        logStr += String.format("currentBrowseIndex: %d, ", currentBrowseIndex);
        logStr += String.format("index: %d, ", index);
        logStr += String.format("offset: %d, ", listOffset);
        Log.d("SaveState", logStr);

        editor.putInt("fluffIndex",index);

        editor.commit();
    }

    private void LoadState(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        currentState = sharedPreferences.getString("currentState","Browse");
        currentBrowseIndex = sharedPreferences.getInt("currentBrowseIndex",0);
        int index = sharedPreferences.getInt("fluffIndex", 0);
        listOffset = sharedPreferences.getInt("listOffset",0);
        listPosition = sharedPreferences.getInt("listPosition",0);

//        currentBrowseIndex = 0;
//        index = 0;

        String logStr = "";
        logStr += String.format("currentState: %s, ", currentState);
        logStr += String.format("currentBrowseIndex: %d, ", currentBrowseIndex);
        logStr += String.format("index: %d, ", index);
        logStr += String.format("offset: %d, ", listOffset);
        Log.d("LoadState", logStr);

        //Load initial data
        Log.d("onCreate","Executing initial LoadFluff...");
        new LoadFluffs(this, "init", false, index).execute();

        // get favorites list
        new LoadFluffs(this,"favorites").execute();

        // get inbox list
        new LoadInbox(this,"inbox").execute();

    }

    @Override
    protected void onStop() {
        super.onStop();
        SaveState();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerLinearLayout);
        // change visibility of action bar stuff depending on drawer state
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browser, menu);

//        Animation blink = AnimationUtils.loadAnimation(BrowserActivity.this,R.anim.glow_blink);
//        ImageButton inboxButton = (ImageButton) menu.findItem(R.id.action_inbox).getActionView();
////        inboxButton.clearAnimation();
////        inboxButton.setAnimation(blink);
//        inboxButton.setImageResource(R.drawable.button_glow);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_inbox) {
//            goToInbox();
//            return true;
//        }

//        if (drawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }

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

            fluffView.setItem(getItem(position), BrowserActivity.this, position);

            return fluffView;

        }

        public void addFluffs(ArrayList<Fluff> newFluffs) {
            this.fluffs.addAll(newFluffs);
            notifyDataSetChanged();

        }

        public void prependFluffs(ArrayList<Fluff> newFluffs) {

            int index = listView.getFirstVisiblePosition() + newFluffs.size();
            View v = listView.getChildAt(listView.getHeaderViewsCount());
            int top = (v == null) ? 0 : v.getTop();

            this.fluffs.addAll(0,newFluffs);
            notifyDataSetChanged();

            listView.setSelectionFromTop(index, top);

        }

        public void logFluffs() {

            String str = "Current Fluffs in Adapter: ";
            for (Fluff f : fluffs) {
                str += f.index.toString() + " ";
            }

            Log.d("Fluff Adapter",str);
            Log.d("Fluff Adapter","Total count: " + Integer.toString(this.fluffs.size()));

        }

        public void clear() {
            this.fluffs.clear();
        }

        public void removeFluffAtPosition(int position) {
            this.fluffs.remove(position);
        }

        public void removeFluff(Fluff fluff) {
            this.fluffs.remove(fluff);
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
        public boolean isVisible = false;

        public void show() {
            // bail if already showing
            if (isVisible) return;

//            dialog = new Dialog(BrowserActivity.this, R.style.Theme_Transparent);
            dialog = new Dialog(BrowserActivity.this, R.style.Theme_AppCompat);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_dialog);
            dialog.setCanceledOnTouchOutside(false);

            ImageView splash = (ImageView) dialog.findViewById(R.id.splash);
            splash.setImageResource(R.drawable.fluffr_splash);

            Log.d("LoadingSpinner","showing dialog: " + dialog.toString());
            dialog.show();
            isVisible = true;

        }

        public void dismiss() {
            Log.d("LoadingSpinner","dismissing dialog: " + dialog.toString());
            dialog.dismiss();
            isVisible = false;
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
        drawerLayout.closeDrawer(drawerLinearLayout);

        if (pages.get(position).text.equals(currentState)) return;

        // record the state of the browser window
        if (getCurrentState().equals("Browse")) savePosition();

        if (pages.get(position).text.equals("Browse")) {
            goToBrowse();

        } else if (pages.get(position).text.equals("Favorites")) {
            goToFavorites();

        } else if (pages.get(position).text.equals("Inbox")) {
            goToInbox();
        }

    }

    private void goToBrowse() {
        currentState = "Browse";

        // replace browser's existing data with new list
        Log.d("goToBrowse","clearing current list...");
        this.adapter.clear();

        Log.d("goToBrowse","replacing with browse list...");
        this.adapter.addFluffs(list);

        // set back to previous place in list
        restorePosition();

        updateActionBar();

    }

    private void goToFavorites() {
        currentState = "Favorites";

        // replace browser's existing data with new list
        Log.d("goToFavorites","clearing current list...");
        this.adapter.clear();

        Log.d("goToFavorites","replacing with favorites...");
        this.adapter.addFluffs(favorites);

        // force to top
        listView.setSelection(0);

        updateActionBar();
    }

    private void goToInbox() {
        currentState = "Inbox";

        // replace browser's existing data with new list
        Log.d("goToInbox","clearing current list...");
        this.adapter.clear();

        Log.d("goToInbox","replacing with inbox...");
        this.adapter.addFluffs(inbox);

        updateActionBar();

        // force to top
        listView.setSelection(0);

        // mark fluffs as seen
        ParseUser user = ParseUser.getCurrentUser();
        user.put("hasUnseenFluffs","false");
        user.saveInBackground();
        hasUnseenFluffs = false;
    }

    private void updateActionBar() {

        // get references to views
        TextView title = (TextView) findViewById(R.id.header_title);
        final ImageButton rightButton = (ImageButton) findViewById(R.id.header_right_button);
        final ImageButton navButton = (ImageButton) findViewById(R.id.header_drawer_toggle);
        final ImageView logo = (ImageView) findViewById(R.id.header_logo);

        // set title

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/TaiLeb.ttf");
        title.setTypeface(type);

        if (currentState.equals("Browse")) {
//            logo.setVisibility(View.VISIBLE);
            title.setText("fluffr");
            logo.setImageResource(R.drawable.header_main);
            rightButton.setImageResource(R.drawable.ic_action_read);

        } else if (currentState.equals("Favorites")) {
//            logo.setVisibility(View.GONE);
            title.setText("favorites");
            logo.setImageResource(R.drawable.header_favorites);
            rightButton.setImageResource(R.drawable.ic_action_read);

        } else if (currentState.equals("Inbox")) {
//            logo.setVisibility(View.GONE);
            title.setText("inbox");
            logo.setImageResource(R.drawable.header_inbox);
            rightButton.setImageResource(R.drawable.fluffr_cat_icon);
        }

        // Assign right button

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentState.equals("Inbox")) {
                    goToBrowse();
                } else {
                    goToInbox();
                }
            }
        });

        // Set animation if user has unseen fluffs in his inbox
        if (hasUnseenFluffs && !currentState.equals("Inbox")) {
            Animation blink = AnimationUtils.loadAnimation(BrowserActivity.this, R.anim.glow_blink);
            rightButton.startAnimation(blink);
        } else {
            rightButton.clearAnimation();
        }

        // Nav Drawer Button
        navButton.setImageResource(R.drawable.menu);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(drawerLinearLayout)) {
                    drawerLayout.closeDrawer(drawerLinearLayout);
                    navButton.setImageResource(R.drawable.menu);
                } else {

                    // rotate the image
                    Animation spin = AnimationUtils.loadAnimation(BrowserActivity.this,R.anim.rotate_button);
                    spin.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            navButton.setImageResource(R.drawable.back_arrow);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    navButton.startAnimation(spin);
                    drawerLayout.openDrawer(drawerLinearLayout);
                }
            }
        });



    }

    @Override
    public void FavoritesButtonPressed(Fluff fluff) {

        // Get channel to DB
        ParseQuery query = ParseQuery.getQuery("fluff");
        ParseObject parseFluff = null;

        try {
            parseFluff = query.get(fluff.id);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (parseFluff == null) {
            Log.e("FavoritesButtonPressed","unable to get fluff from Parse");
            return;
        }

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

            // mark as liked in DB
            parseFluff.increment("likes",-1);



        } else {
            fluff.favorited = true;

            // Add to current application favorites list
            this.favorites.add(fluff);

            // Add to Parse Database
            ParseUser user = ParseUser.getCurrentUser();
            user.addUnique("favorites", fluff.id);
            user.saveInBackground();

            // mark as liked in DB
            parseFluff.increment("likes");


            Log.d("FavoritesButtonInterface", "Item added!");
        }

        parseFluff.saveInBackground();
    }

    @Override
    public void DeleteButtonPressed(Fluff fluff) {

        //TODO -- show confirmation dialog

        // remove fluff from all lists
        list.remove(fluff);
        favorites.remove(fluff);
        inbox.remove(fluff);

        // update listview
        adapter.removeFluff(fluff);
        adapter.notifyDataSetChanged();

        // Update db
        ParseQuery query = ParseQuery.getQuery("fluff");
        ParseObject parseFluff = null;

        try {
            parseFluff = query.get(fluff.id);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // mark as disliked in DB
        parseFluff.increment("dislikes");

        // Add to user's dislikes list
        ParseUser user = ParseUser.getCurrentUser();
        user.addUnique("dislikes", fluff.id);
        user.saveInBackground();

        // admin executive powers - mark fluff for permanent deletion!
        if (isAdmin()) {

            parseFluff.put("deletedByAdmin",true);

        }

        parseFluff.saveInBackground();

    }

    @Override
    public void SendButtonPressed(Fluff fluff) {
        ContactsDialog dialog = new ContactsDialog(this, fluff);
        dialog.show();
    }

    private void setUserNumber() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        userPhoneNumber = PhoneNumberFormatter.getFormattedNumber(tMgr.getLine1Number());

        //TODO - barf if userPhoneNumber is null
//      userPhoneNumber = tMgr.getLine1Number();
    }

    private void setParseUser() {

        ParseUser user = null;

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

            String huf = user.getString("hasUnseenFluffs");
            hasUnseenFluffs = huf.equals("true");

        } else {

            // create new user account
            Log.d("setParseUser", "New account for user: " + userPhoneNumber);

            user = new ParseUser();
            user.setUsername(userPhoneNumber);
            user.setPassword("password");
            user.put("platform","android");
            user.put("hasUnseenFluffs","false");
            try {
                user.signUp();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //TODO - display tutorial modal screen
        }


        if (user == null) {
            Log.e("setParseUser","user setup failed.");
            return;
        }

    }

    public static String getCurrentState() {
        return currentState;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        Log.i("checkPlayServices", "Checking...");
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

    public void checkStartupInstructions() {
        Intent i = getIntent();

        if (i != null) {
            if (i.hasExtra("startupMode")) {
                Bundle b = i.getExtras();

                String startupMode = b.getString("startupMode");
                if (startupMode.equals("newFluff")) {
                    // user has tapped a new fluff push notification
                    // go to inbox
                    Log.d("onCreate", "Startup Instructions: new Fluff");
                    goToInbox();
                }
            }
        }

    }

    public void scrollToTop() {
        listView.smoothScrollToPosition(0);
    }

    public void savePosition() {
        // get currently visible Fluff index
        int position = listView.getFirstVisiblePosition();
        Fluff f = adapter.getItem(position);
        int index = f.index;

        // get list offset from top
        Log.d("SaveState","position: " + Integer.toString(position));

        View v = listView.getChildAt(0);
        Log.d("SaveState","view: " + v.toString());
        int top = (v == null) ? 0 : v.getTop();

        listOffset = top;
        listPosition = position;

    }

    public void restorePosition() {
        Log.d("restoring","index:" + Integer.toString(listPosition) + ", offset: " + Integer.toString(listOffset));
        listView.setSelectionFromTop(listPosition, listOffset);
    }

    public boolean isAdmin() {
        return userPhoneNumber.equals("16518155005");
    }
}
