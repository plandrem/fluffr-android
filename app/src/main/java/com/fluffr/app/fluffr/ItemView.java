package com.fluffr.app.fluffr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Patrick on 10/20/14.
 */
public class ItemView extends RelativeLayout {

    private Button favoritesButton;
    private Button sendToFriendButton;
    private Button deleteButton;

    private ImageView imageView;

    private TextView title;
    private TextView subtitle;

    public static ItemView inflate(ViewGroup parent) {
        ItemView itemView = (ItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return itemView;
    }

    // Constructors
    public ItemView(Context c) {
        this(c,null);
    }

    public ItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ItemView (Context context, AttributeSet attributeSet, int defStyle){
        super(context,attributeSet,defStyle);

        // inflate the actual view layout and attach it to this instance,
        // giving us access to the views contained therein
        LayoutInflater.from(context).inflate(R.layout.item_view_children, this, true);

        // connect references between this object's view properties and the inflated layout
        setupChildren();
    }

    private void setupChildren() {
        favoritesButton = (Button) findViewById(R.id.item_favoritesButton);
        sendToFriendButton = (Button) findViewById(R.id.item_sendToFriendButton);
        deleteButton = (Button) findViewById(R.id.item_deleteButton);

        title = (TextView) findViewById(R.id.item_title);
        subtitle = (TextView) findViewById(R.id.item_subtitle);

        imageView = (ImageView) findViewById(R.id.item_imageView);

//        favoritesButton.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                Log.d("Favorites Button", String.format("Favorites Button Pressed for position %d.", position));
//            }
//        });



    }


    public void setItem(Item item) {
        title.setText(item.title);
        subtitle.setText(item.subtitle);
        imageView.setImageDrawable(item.image);
    }
}