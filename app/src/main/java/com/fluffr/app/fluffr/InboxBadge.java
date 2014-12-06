package com.fluffr.app.fluffr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Patrick on 12/5/14.
 */
public class InboxBadge extends RelativeLayout {

    private ImageView thumbnailImage;
    private TextView nameText;
    private TextView dateText;

    public InboxBadge(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.inbox_badge,this,true);

        thumbnailImage = (ImageView) findViewById(R.id.inbox_badge_image);
        nameText = (TextView) findViewById(R.id.inbox_badge_name_text);
        dateText = (TextView) findViewById(R.id.inbox_badge_date_text);

    }

    public void setFrom(String sender) {
        nameText.setText("From: " + sender);
    }

    public void setDate(String date) {
        dateText.setText("Sent: " + date);
    }

}
