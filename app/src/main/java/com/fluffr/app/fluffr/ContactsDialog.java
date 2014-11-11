package com.fluffr.app.fluffr;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 11/2/14.
 */
public class ContactsDialog {
    private Dialog dialog;
    private Context context;
    private ListView list;
    private GridView grid;
    private ContactsAdapter adapter;
    private FavoriteContactsAdapter gridAdapter;
    private ArrayList<PhoneContact> allContacts;
    private ArrayList<PhoneContact> favoriteContacts;

    public ContactsDialog(Context c) {
        this.context = c;
    }

    public void show() {
        dialog = new Dialog(context, R.style.Theme_Contact_Chooser);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.contacts_dialog);

        // setup list of contacts
        allContacts = getAllPhoneContacts();
        this.list = (ListView) dialog.findViewById(R.id.contacts_list);
        adapter = new ContactsAdapter(context,allContacts);

        list.setAdapter(adapter);

        // setup grid of favorite contacts
        favoriteContacts = new ArrayList<PhoneContact>(8);
        for (int i=0; i<8; i++) {
            favoriteContacts.add(allContacts.get(i));
        }

        this.grid = (GridView) dialog.findViewById(R.id.favorite_contacts_grid);
        gridAdapter = new FavoriteContactsAdapter(context,favoriteContacts);
        grid.setAdapter(gridAdapter);


        dialog.show();

    }

    public void dismiss() {
        dialog.dismiss();
    }

    private ArrayList<PhoneContact> getAllPhoneContacts() {

        Log.d("START", "Getting all Contacts");

        ArrayList<PhoneContact> arrContacts = new ArrayList<PhoneContact>();
        PhoneContact phoneContactInfo=null;

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false)
        {
            String contactNumber= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String contactName =  cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            long phoneContactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String contactPhoto = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));

            phoneContactInfo = new PhoneContact();
            phoneContactInfo.id = phoneContactID;
            phoneContactInfo.name = contactName;
            phoneContactInfo.number = contactNumber;
            phoneContactInfo.setPhotoFromString(contactPhoto);

            if (phoneContactInfo != null)
            {
                arrContacts.add(phoneContactInfo);
            }
            phoneContactInfo = null;
            cursor.moveToNext();
        }
        cursor.close();
        cursor = null;

        Log.d("END","Got all Contacts");

        return arrContacts;
    }

    private class ContactsAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<PhoneContact> phoneContacts;
        private LayoutInflater inflater;

        // constructor for class
        ContactsAdapter(Context context, ArrayList<PhoneContact> list) {
            this.context = context;
            this.phoneContacts = list;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return phoneContacts.size();
        }

        @Override
        public PhoneContact getItem(int position) {
            return phoneContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ContactView contactView = (ContactView) convertView;

            if (contactView == null) {
                // add custom row layout into parent viewgroup (the row)
                contactView = ContactView.inflate(parent);
            }

            contactView.setItem(getItem(position));

            return contactView;

        }
    }

    private class FavoriteContactsAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<PhoneContact> phoneContacts;
        private LayoutInflater inflater;

        // constructor for class
        FavoriteContactsAdapter(Context context, ArrayList<PhoneContact> list) {
            this.context = context;
            this.phoneContacts = list;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return phoneContacts.size();
        }

        @Override
        public PhoneContact getItem(int position) {
            return phoneContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            FavoriteContactView contactView = (FavoriteContactView) convertView;

            if (contactView == null) {
                // add custom row layout into parent viewgroup (the row)
                contactView = FavoriteContactView.inflate(parent);
            }

            contactView.setItem(getItem(position));

            return contactView;

        }
    }
}


