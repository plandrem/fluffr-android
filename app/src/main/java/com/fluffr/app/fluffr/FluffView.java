package com.fluffr.app.fluffr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Patrick on 10/20/14.
 */
public class FluffView extends RelativeLayout {

    private Context context;
    private ButtonInterface buttonInterface;

    private ImageButton favoritesButton;
    private ImageButton sendToFriendButton;
    private ImageButton deleteButton;

    private ImageView imageView;

    private TextView title;
    private TextView subtitle;

    private ContactsDialog contactsDialog;

    public static FluffView inflate(ViewGroup parent) {
        FluffView fluffView = (FluffView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return fluffView;
    }

    // Constructors
    public FluffView(Context c) {
        this(c,null);
    }

    public FluffView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FluffView(Context context, AttributeSet attributeSet, int defStyle){
        super(context,attributeSet,defStyle);

//        this.context = context;

        // inflate the actual view layout and attach it to this instance,
        // giving us access to the views contained therein
        LayoutInflater.from(context).inflate(R.layout.item_view_children, this, true);

        // connect references between this object's view properties and the inflated layout
        setupChildren();
    }

    private void setupChildren() {
        favoritesButton = (ImageButton ) findViewById(R.id.item_favoritesButton);
        sendToFriendButton = (ImageButton ) findViewById(R.id.item_sendToFriendButton);
        deleteButton = (ImageButton) findViewById(R.id.item_deleteButton);

        title = (TextView) findViewById(R.id.item_title);
        subtitle = (TextView) findViewById(R.id.item_subtitle);

        imageView = (ImageView) findViewById(R.id.item_imageView);

    }


    public void setItem(Fluff fluff, BrowserActivity parent, int position) {

        // Attach this View object to an item. The item is the
        // abstract class which contains the useful data, whereas the
        // ItemView is the UI representation.

        // As part of the attachment, all the UI functionality should
        // be created at this point (eg. button clicks)

//        Log.d("setItem",item.id);
//        Log.d("setItem",item.title);

        fluff.position = position;

        title.setVisibility(INVISIBLE);
        subtitle.setVisibility(INVISIBLE);

        title.setText(fluff.title);
        subtitle.setText(fluff.subtitle);

        getDrawableWithImageLoader(fluff);

        ButtonClickListener buttonClickListener = new ButtonClickListener(fluff);
        favoritesButton.setOnClickListener(buttonClickListener);
        sendToFriendButton.setOnClickListener(buttonClickListener);
        deleteButton.setOnClickListener(buttonClickListener);

        if (fluff.favorited) {
            favoritesButton.setImageResource(R.drawable.ic_action_important);
        } else {
            favoritesButton.setImageResource(R.drawable.ic_action_not_important);
        }

        // add tag if we're in the inbox
        if (parent.getCurrentState().equals("Inbox")) {
            title.setVisibility(VISIBLE);
            subtitle.setVisibility(VISIBLE);

            title.setText(fluff.sender);
            subtitle.setText(fluff.sendDate);

        }

        buttonInterface = parent;

    }

    private class ButtonClickListener implements OnClickListener {

        private Fluff fluff;

        ButtonClickListener(Fluff fluff) {
            super();
            this.fluff = fluff;
        }

        @Override
        public void onClick(View v) {
            Log.d("ItemView OnClickListener", fluff.title);

            // identify which button was pressed
            if (v.getId() == favoritesButton.getId()) {
                Log.d("ItemView OnClickListener", "Added to Favorites.");

                // toggle button view
                if (fluff.favorited) {
                    // already favorited - unstar
                    favoritesButton.setImageResource(R.drawable.ic_action_not_important);
                } else {
                    favoritesButton.setImageResource(R.drawable.ic_action_important);
                }

                buttonInterface.FavoritesButtonPressed(fluff);

            }

            else if (v.getId() == sendToFriendButton.getId()) {
                Log.d("ItemView OnClickListener", "Send to Friend.");
                buttonInterface.SendButtonPressed(fluff);
            }

            else if (v.getId() == deleteButton.getId()) {
                Log.d("ItemView OnClickListener", "DELETE'D.");
                buttonInterface.DeleteButtonPressed(fluff);
            }

        }
    };

    private void getDrawableWithImageLoader(Fluff fluff) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        String imageUrl = fluff.parseFile.getUrl();

        if (imageUrl != null) {
//            imageLoader.displayImage(imageUrl, imageView);

            ImageAware imageAware = new ImageViewAware(imageView, false);
            imageLoader.displayImage(imageUrl, imageAware);
        }


    }

}
