package com.fluffr.app.fluffr;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Patrick on 12/5/14.
 */
public class InboxBadge extends RelativeLayout {

    private ImageView thumbnailImage;
    private TextView nameText;
    private TextView dateText;
    private LetterTileProvider tileProvider;

    public InboxBadge(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.tileProvider = new LetterTileProvider(getContext());

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.inbox_badge,this,true);

        thumbnailImage = (ImageView) findViewById(R.id.inbox_badge_image);
        nameText = (TextView) findViewById(R.id.inbox_badge_name_text);
        dateText = (TextView) findViewById(R.id.inbox_badge_date_text);

    }

    public void setFrom(String sender) {

        nameText.setText("From: " + sender);

        //sender is a phone number. Try to find the contact's name from the local address book

    }

    public void setDate(String date) {
        dateText.setText("Sent: " + date);
    }

    public void setThumbnailImage() {
        final Resources res = getResources();
        final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        String text = nameText.getText().toString();
        final Bitmap letterTile = tileProvider.getLetterTile(text,text, tileSize, tileSize);

        thumbnailImage.setImageBitmap(letterTile);

    }



}
