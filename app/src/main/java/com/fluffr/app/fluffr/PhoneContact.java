package com.fluffr.app.fluffr;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Patrick on 11/2/14.
 */
public class PhoneContact {
    private String number;
    public String name;
    public long id;
    public Bitmap photo;
    public Uri photoUri;

    public PhoneContact() {

    }

    public PhoneContact(Context context, String phoneNumber) {
        // try to find contact in local address book

        //TODO -- remove this for production!!!
//        if (phoneNumber.equals("16518155005")) phoneNumber = "19788216761";
//        if (phoneNumber.equals("+16518155005")) phoneNumber = "16513669306";

        String clause = ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?";
        String[] criteria = {phoneNumber};

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI},
                clause,
                criteria,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {

            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            long phoneContactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

            this.id = phoneContactID;
            this.name = contactName;
            this.number = PhoneNumberFormatter.getFormattedNumber(phoneNumber);

            if (thumbnailUri != null) {
                this.photoUri = Uri.parse(thumbnailUri);
                try {
                    this.photo = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(thumbnailUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                // No image resource found -- use a letter tile instead
                LetterTileProvider tileProvider = new LetterTileProvider(context);
                final Resources res = context.getResources();
                final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

                String text = contactName;
                this.photo = tileProvider.getLetterTile(text,text, tileSize, tileSize);

            }
        }

        cursor.close();
        cursor = null;

    }

    public void setPhoneNumber(String number) {
        this.number = PhoneNumberFormatter.getFormattedNumber(number);
    }

    public String getNumber() {
        return number;
    }
}
