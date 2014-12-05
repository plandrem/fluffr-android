package com.fluffr.app.fluffr;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Patrick on 11/2/14.
 */
public class ContactsDialog {
    private Dialog dialog;
    private Context context;
    private BrowserActivity parentActivity;
    private EditText editText;
    private ListView list;
    private GridView grid;
    private ContactsAdapter adapter;
    private FavoriteContactsAdapter gridAdapter;
    private ArrayList<PhoneContact> allContacts;
    private ArrayList<PhoneContact> favoriteContacts;
    private Fluff fluff;

    //TODO -- prevent dialog from changing dimension when filtering contacts

    public ContactsDialog(BrowserActivity b, Fluff f) {
        this.context = (Context) b;
        this.fluff = f;
        this.parentActivity = b;
    }

    public void show() {
        dialog = new Dialog(context, R.style.Theme_Contact_Chooser);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.contacts_dialog);

        // setup list of contacts
        allContacts = getAllPhoneContacts();
        this.list = (ListView) dialog.findViewById(R.id.contacts_list);
        adapter = new ContactsAdapter(context,new ArrayList<PhoneContact>());
        adapter.addContacts(allContacts);
        adapter.notifyDataSetChanged();

        list.setAdapter(adapter);
        list.setOnItemClickListener(new SelectContactListener());

        // setup grid of favorite contacts
        favoriteContacts = new ArrayList<PhoneContact>(8);
        for (int i=0; i<8; i++) {
            favoriteContacts.add(allContacts.get(i));
        }

        this.grid = (GridView) dialog.findViewById(R.id.favorite_contacts_grid);
        gridAdapter = new FavoriteContactsAdapter(context,favoriteContacts);
        grid.setAdapter(gridAdapter);

        // configure regex search for edittext element
        this.editText = (EditText) dialog.findViewById(R.id.contact_filter);
        editText.addTextChangedListener(new ContactFilterTextWatcher());

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
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false)
        {
            String contactNumber= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String contactName =  cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            long phoneContactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));


            phoneContactInfo = new PhoneContact();
            phoneContactInfo.id = phoneContactID;
            phoneContactInfo.name = contactName;
            phoneContactInfo.number = contactNumber;

            if (thumbnailUri != null) phoneContactInfo.photoUri = Uri.parse(thumbnailUri);

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

        public void clear() {
            phoneContacts.clear();
        }

        public void addContacts(ArrayList<PhoneContact> newContacts) {
            phoneContacts.addAll(newContacts);
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

    private class SelectContactListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            PhoneContact selectedContact = allContacts.get(position);
            Log.d("SelectContactListener", "User clicked: " + selectedContact.name);

            //TODO - use actual recipient number
            String recipient = "16518155005";
//            String recipient = selectedContact.number;

            //TODO - convert message sending to an Async Task

            boolean pushSuccessful = false;
            try {
                pushSuccessful = sendReceivedFluffPushNotification(recipient);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (pushSuccessful == false) {

                SmsManager smsManager = SmsManager.getDefault();
                String ownerName = "";
                String message = "";
                String number = "16518155005";

                // get name of user for personalized message
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    Cursor c = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
                    int count = c.getCount();
                    String[] columnNames = {ContactsContract.Profile.DISPLAY_NAME};
                    boolean b = c.moveToFirst();
                    int p = c.getPosition();
                    if (count == 1 && p == 0) {
                        for (int j = 0; j < columnNames.length; j++) {
                            String columnName = columnNames[j];
                            String columnValue = c.getString(c.getColumnIndex(columnName));
                            Log.d("SelectContactListener", columnName + " - " + columnValue);

                            if (columnName != null) {
                                if (columnName.equals(ContactsContract.Profile.DISPLAY_NAME)) {
                                    ownerName = columnValue.split(" ")[0];
                                }
                            }

                        }
                    }
                    c.close();

                    // Send SMS message
                    message = context.getResources().getString(R.string.non_user_sms);
                    smsManager.sendTextMessage(number, null, ownerName + " " + message, null, null);


                } else {
                    // Pre-Ice-Cream-Sandwich
                    message = context.getResources().getString(R.string.old_non_user_sms);
                    smsManager.sendTextMessage(number, null, message, null, null);

                }
            }

            //TODO - add selected contact to recent contacts list

            ContactsDialog.this.dismiss();

            Toast.makeText(context.getApplicationContext(),"Fluff sent!", Toast.LENGTH_SHORT).show();


        }
    }

    private boolean sendReceivedFluffPushNotification(String recipient) throws ParseException {
        // check if recipient already has an account. If yes, push as normal. If no, add the fluff to a pending account
        // and return false to send an SMS message.

        boolean returnVal = true;

        ParseQuery userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username",recipient);
        if (userQuery.count() == 0) {
            // account does not exist; check pending accounts

            // if no pending accounts, create a pending account.

            // need to send an SMS
            returnVal = false;

        } else {
            // recipient account exists
            parentActivity.sendFluffPushNotification(recipient, fluff);

        }



        return returnVal;

    }

    private class ContactFilterTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            Log.d("ContactDialog:onTextChanged","new search: " + s);

            //initialize array to return
            ArrayList<PhoneContact> matches = new ArrayList<PhoneContact>();

            //bail if user has deleted all characters
            if (s.equals("") || s.length() == 0) {
                Log.d("ContactDialog:onTextChanged","setting all contacts");
                matches = allContacts;

            } else {
                //generate regex pattern (any characters + word boundary + string + any characters)
                Pattern pattern = Pattern.compile(".*\\b" + s.toString().toLowerCase() + ".*");

                //iterate through list of contacts and apply pattern filter
                for (PhoneContact contact : allContacts) {
                    if (pattern.matcher(contact.name.toLowerCase()).matches()) {
                        matches.add(contact);
                    }
                }
            }

            //replace adapter data with match data
            adapter.clear();
            adapter.addContacts(matches);
            adapter.notifyDataSetChanged();

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}


