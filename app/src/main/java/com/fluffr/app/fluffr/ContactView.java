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
public class ContactView extends RelativeLayout {

    private Context context;
    private TextView name;
    private TextView number;
    private ImageView image;

    public static ContactView inflate(ViewGroup parent) {
        ContactView contactView = (ContactView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_view, parent, false);
        return contactView;
    }

    // Constructors
    public ContactView(Context c) {
        this(c,null);
    }

    public ContactView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ContactView (Context context, AttributeSet attributeSet, int defStyle){
        super(context,attributeSet,defStyle);

        this.context = context;

        // inflate the actual view layout and attach it to this instance,
        // giving us access to the views contained therein
        LayoutInflater.from(context).inflate(R.layout.contact_view_children, this, true);

        // connect references between this object's view properties and the inflated layout
        setupChildren();
    }

    private void setupChildren() {
        name = (TextView) findViewById(R.id.contact_name);
        number = (TextView) findViewById(R.id.contact_number);
        image = (ImageView) findViewById(R.id.contact_image);
    }


    public void setItem(PhoneContact contact) {

        name.setText(contact.name);
        number.setText(contact.number);

        if (contact.photoUri != null) {
            image.setImageURI(contact.photoUri);
        } else {
            image.setImageBitmap(null);
        }


    }

    public String getName() {
        return name.getText().toString();
    }

    public String getNumber() {
        return number.getText().toString();
    }
}
