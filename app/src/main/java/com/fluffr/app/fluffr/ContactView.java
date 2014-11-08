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

    private TextView name;
    private TextView number;

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

        // inflate the actual view layout and attach it to this instance,
        // giving us access to the views contained therein
        LayoutInflater.from(context).inflate(R.layout.contact_view_children, this, true);

        // connect references between this object's view properties and the inflated layout
        setupChildren();
    }

    private void setupChildren() {
        name = (TextView) findViewById(R.id.contact_name);
        number = (TextView) findViewById(R.id.contact_number);
    }


    public void setItem(PhoneContact contact) {

        name.setText(contact.name);
        number.setText(contact.number);

    }

}
