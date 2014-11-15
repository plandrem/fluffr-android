package com.fluffr.app.fluffr;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class FavoriteContactView extends RelativeLayout {

    private Context context;
    private TextView name;
    private ImageView picture;

    public static FavoriteContactView inflate(ViewGroup parent) {
        FavoriteContactView contactView = (FavoriteContactView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_contact_view, parent, false);
        return contactView;
    }

    // Constructors
    public FavoriteContactView (Context c) {
        this(c,null);
    }

    public FavoriteContactView (Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FavoriteContactView (Context context, AttributeSet attributeSet, int defStyle){
        super(context,attributeSet,defStyle);

        this.context = context;

        // inflate the actual view layout and attach it to this instance,
        // giving us access to the views contained therein
        LayoutInflater.from(context).inflate(R.layout.favorite_contact_view_children, this, true);

        // connect references between this object's view properties and the inflated layout
        setupChildren();
    }

    private void setupChildren() {
        name = (TextView) findViewById(R.id.favorite_contact_name);
        picture = (ImageView) findViewById(R.id.favorite_contact_image);
    }


    public void setItem(PhoneContact contact) {

        name.setText(contact.name);
//        picture.setImageURI(getPhotoUri(contact.id));

        if (contact.photoUri != null) {
            picture.setImageURI(contact.photoUri);
        } else {
            picture.setImageBitmap(null);
        }


    }

}
