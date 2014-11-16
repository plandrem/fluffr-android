package com.fluffr.app.fluffr;

import android.app.Dialog;
import android.content.Context;
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

import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BrowserActivity extends ActionBarActivity implements ButtonInterface {

    /*
    This is the generic scrolling browser view to be used for the Home, Favorites, and Inbox screens.
    The primary element is a custom ListView widget, in which each row contains the image and
    buttons for user interaction.
    */

    //UI Stuff
    private static String currentState = "Browse";

    // ListView and Data Stuff
    public ListView listView;
    public ArrayList<Fluff> list = new ArrayList<Fluff>();
    public ArrayList<Fluff> favorites = new ArrayList<Fluff>();
    public CustomAdapter adapter;
    public LoadingSpinner spinner = new LoadingSpinner();

    // Nav Drawer Stuff
    private ArrayList<NavItem> pages = new ArrayList<NavItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

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

        //Handle Parse User Account
        setParseUser();

        //Configure Adapter; dataset will be empty.
        adapter = new CustomAdapter(this, list);
        listView.setAdapter(adapter);

        //Load initial data
        new LoadFluffs(this, "init").execute();

        //Finalize UI
        updateActionBar();

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

        drawerList.setItemChecked(position,true);
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

    private void setParseUser() {

        ParseUser user = ParseUser.getCurrentUser();

        if (user == null) {
            // user has not been registered - create new account

            spinner.show();

            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String userPhoneNumber = tMgr.getLine1Number();

            Log.d("setParseUser","New account for user: " + userPhoneNumber);

            user = new ParseUser();
            user.setUsername(userPhoneNumber);
            user.setPassword("password");
            try {
                user.signUp();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            spinner.dismiss();

            //TODO - display tutorial modal screen

        } else {
            // user already registered

            Log.d("setParseUser","Resuming session for this user.");

            // get favorites list
            new LoadFluffs(this,"favorites").execute();


        }

    }

}
