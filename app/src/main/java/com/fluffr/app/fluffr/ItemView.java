package com.fluffr.app.fluffr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Text;

/**
 * Created by Patrick on 10/20/14.
 */
public class ItemView extends RelativeLayout {

    private Context context;

    private ImageButton favoritesButton;
    private ImageButton sendToFriendButton;
    private ImageButton deleteButton;

    private ImageView imageView;

    private TextView title;
    private TextView subtitle;

    private ContactsDialog contactsDialog;

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

        this.context = context;
        this.contactsDialog = new ContactsDialog(context);

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


    public void setItem(Item item) {

        // Attach this View object to an item. The item is the
        // abstract class which contains the useful data, whereas the
        // ItemView is the UI representation.

        // As part of the attachment, all the UI functionality should
        // be created at this point (eg. button clicks)

//        Log.d("setItem",item.id);
//        Log.d("setItem",item.title);


        title.setText(item.title);
        subtitle.setText(item.subtitle);

        getDrawableWithImageLoader(item);

//        setImageDrawable(item.id);

        ButtonClickListener buttonClickListener = new ButtonClickListener(item);
        favoritesButton.setOnClickListener(buttonClickListener);
        sendToFriendButton.setOnClickListener(buttonClickListener);
        deleteButton.setOnClickListener(buttonClickListener);

    }

    private class ButtonClickListener implements OnClickListener {

        private Item item;

        ButtonClickListener(Item item) {
            super();
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            Log.d("ItemView OnClickListener", item.title);

            // identify which button was pressed
            if (v.getId() == favoritesButton.getId()) {
                Log.d("ItemView OnClickListener", "Added to Favorites.");
            }

            else if (v.getId() == sendToFriendButton.getId()) {
                Log.d("ItemView OnClickListener", "Send to Friend.");
                contactsDialog.show();

            }

            else if (v.getId() == deleteButton.getId()) {
                Log.d("ItemView OnClickListener", "DELETE'D.");
            }

        }
    };

    public void setBitmap(Bitmap bm) {
        imageView.setImageBitmap(bm);
    }

    public void setImageDrawable(String id) {
        // depending on the server backend implementation, the means of storing and retrieving
        // images may change. Therefore, this method provides a static API such that the rest of the
        // application is not sensitive to the specific server details.

        getParseDrawable(id);
    }

    private void getDrawableWithImageLoader(Item item) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        String imageUrl = item.parseFile.getUrl();

        if (imageUrl != null) imageLoader.displayImage(imageUrl, imageView);

    }

    private void getParseDrawable(String id) {

        // Async call to Parse DB. Find the ParseObject associated with the current item;
        // once found, take the 'image' field in the object and download the file on a
        // background thread. Once finished, apply the image to the current ImageView.

        Log.d("getParseDrawable","called for id: " + id);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("fluff");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {

                    // object received; download associated image file
                    ParseFile pf = (ParseFile) parseObject.get("image");

                    pf.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {

                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                imageView.setImageBitmap(bmp);

                            } else {
                                Log.e("ItemView.getParseDrawable", "Parse Error while retrieving image: " + e.getMessage());
                            }
                        }
                    });

                } else {
                    Log.e("ItemView.getParseDrawable", "Parse Error while retrieving object: " + e.getMessage());
                }
            }
        });



    }





}
