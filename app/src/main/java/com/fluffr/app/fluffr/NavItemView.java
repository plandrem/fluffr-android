package com.fluffr.app.fluffr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NavItemView extends RelativeLayout {

    private TextView text;
    private ImageView image;


    public static NavItemView inflate(ViewGroup parent) {
        NavItemView navItemView = (NavItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nav_view, parent, false);
        return navItemView;
    }

    // Constructors
    public NavItemView(Context c) {
        this(c,null);
    }

    public NavItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NavItemView (Context context, AttributeSet attributeSet, int defStyle){
        super(context,attributeSet,defStyle);

        // inflate the actual view layout and attach it to this instance,
        // giving us access to the views contained therein
        LayoutInflater.from(context).inflate(R.layout.nav_view_children, this, true);

        // connect references between this object's view properties and the inflated layout
        setupChildren();
    }

    private void setupChildren() {
        text = (TextView) findViewById(R.id.drawer_text);
        image = (ImageView) findViewById(R.id.nav_item_image);

    }


    public void setItem(NavItem navItem) {

        text.setText(navItem.text);

        // set images
        if (navItem.text.equals("Browse")) {
            image.setImageDrawable(getResources().getDrawable(R.drawable.fluffr_cat_icon));
        } else if (navItem.text.equals("Favorites")) {
            image.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white));
        } else if (navItem.text.equals("Inbox")) {
            image.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_read));
        }

    }

}
