package com.fluffr.app.fluffr;

import android.content.Context;
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
    private ImageView shadowLeft;
    private ImageView shadowRight;
    private ImageView shadowTop;
    private ImageView shadowBottom;

    private TextView title;
    private TextView subtitle;

    private InboxBadge badge;

    private ContactsDialog contactsDialog;

    public static FluffView inflate(ViewGroup parent) {
        FluffView fluffView = (FluffView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fluff_view, parent, false);
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
        LayoutInflater.from(context).inflate(R.layout.fluff_view_children, this, true);

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
        shadowBottom = (ImageView) findViewById(R.id.fluff_shadow_bottom);
        shadowTop = (ImageView) findViewById(R.id.fluff_shadow_top);
        shadowLeft = (ImageView) findViewById(R.id.fluff_shadow_left);
        shadowRight = (ImageView) findViewById(R.id.fluff_shadow_right);

        badge = (InboxBadge) findViewById(R.id.fluff_inbox_badge);

    }


    public void setItem(Fluff fluff, BrowserActivity parent, int position) {

        // Attach this View object to an item. The item is the
        // abstract class which contains the useful data, whereas the
        // ItemView is the UI representation.

        // As part of the attachment, all the UI functionality should
        // be created at this point (eg. button clicks)

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
            badge.setVisibility(VISIBLE);
            badge.setFrom(fluff.sender);
            badge.setDate(fluff.sendDate);

            // apply shadows
            shadowBottom.setBackgroundResource(R.drawable.shadows_with_inbox_badge_bottom);
            shadowTop.setBackgroundResource(R.drawable.shadows_with_inbox_badge_top);
            shadowLeft.setBackgroundResource(R.drawable.shadows_with_inbox_badge_left);
            shadowRight.setBackgroundResource(R.drawable.shadows_with_inbox_badge_right);

        } else {
            // not in inbox -- remove badge from layout
            badge.setVisibility(GONE);

            // apply shadows
            shadowBottom.setBackgroundResource(R.drawable.shadows_bottom);
            shadowTop.setBackgroundResource(R.drawable.shadows_top);
            shadowLeft.setBackgroundResource(R.drawable.shadows_left);
            shadowRight.setBackgroundResource(R.drawable.shadows_right);
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
