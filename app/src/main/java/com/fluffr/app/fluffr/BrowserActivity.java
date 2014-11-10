package com.fluffr.app.fluffr;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class BrowserActivity extends ActionBarActivity {

    /*
    This is the generic scrolling browser view to be used for the Home, Favorites, and Inbox screens.
    The primary element is a custom ListView widget, in which each row contains the image and
    buttons for user interaction.
    */

    public ListView listView;
    public ArrayList<Item> list = new ArrayList<Item>();
    public CustomAdapter adapter;
//    private final LoadingDialogFragment spinner = new LoadingDialogFragment();
    public LoadingSpinner spinner = new LoadingSpinner();

    private ArrayList<NavItem> pages = new ArrayList<NavItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;

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

        title = drawerTitle = getTitle();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(title);
                invalidateOptionsMenu(); // forces redraw of options menu
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
            }

        };

        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Configure Adapter; dataset will be empty.
        adapter = new CustomAdapter(this, list);
        listView.setAdapter(adapter);
        
        //Load initial data
        new LoadFluffs(this, "init").execute();

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
        private final ArrayList<Item> items;
        private LayoutInflater inflater;

        // constructor for class
        CustomAdapter(Context context, ArrayList<Item> list) {
            this.context = context;
            this.items = list;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ItemView itemView = (ItemView) convertView;

            if (itemView == null) {
                // add custom row layout into parent viewgroup (the row)
                itemView = ItemView.inflate(parent);
            }

            itemView.setItem(getItem(position));

            return itemView;

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

        } else if (pages.get(position).text.equals("Favorites")) {
            // replace contents of browser's array
            new LoadFluffs(this, "favorites").execute();
        }






//        // launch selected activity
//        Intent i;
//        if (pages.get(position).text.equals("Favorites")) {
//            i = new Intent(this,FavoritesActivity.class);
//            startActivity(i);
//        }

    }

}
