package com.fluffr.app.fluffr;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Patrick on 11/2/14.
 */
public class ContactsDialog {
    private Dialog dialog;
    private Context context;
    private BrowserActivity parentActivity;
    private ContactDialogEditText editText;
    private ListView list;
    private ContactsAdapter adapter;
    private ArrayList<PhoneContact> allContacts;
    private Fluff fluff;

    public ContactsDialog(BrowserActivity b, Fluff f) {
        this.context = (Context) b;
        this.fluff = f;
        this.parentActivity = b;
    }

    public void show() {

        dialog = new Dialog(context, R.style.Theme_Contact_Chooser);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
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
        PhoneContact favoriteContact = null;
        ImageView favoriteContactImageView = null;
        TextView favoriteContactTextView = null;

        ParseUser user = ParseUser.getCurrentUser();
        if (user.getList("recentRecipients") != null) {
            List parseRecents = user.getList("recentRecipients");
            String[] recentNumbers = new String[parseRecents.size()];
            for (int i=0; i<parseRecents.size();i++) {
                recentNumbers[i] = (String) parseRecents.get(i);
            }

            ArrayList<PhoneContact> recents = getSpecificContacts(recentNumbers);


            for (Integer i = 1; i < 9; i++) {

                favoriteContactImageView = (ImageView) dialog.findViewById(
                        context.getResources().getIdentifier("favorite_contact_image_" + i.toString(), "id", context.getPackageName()));

                favoriteContactTextView = (TextView) dialog.findViewById(
                        context.getResources().getIdentifier("favorite_contact_name_" + i.toString(), "id", context.getPackageName()));

                if (i <= recents.size()) {
                    // need to sort returned contacts
                    for (PhoneContact pc : recents) {
                        if (pc.getNumber().equals((String) parseRecents.get(i-1))) {
                            favoriteContact = pc;
                        }
                    }
                    favoriteContactImageView.setImageBitmap(favoriteContact.photo);
                    favoriteContactTextView.setText(favoriteContact.name);

                    favoriteContactImageView.setOnClickListener(new SelectRecentContactListener(favoriteContact));

                } else {
                    // empty tile
                    favoriteContactImageView.setImageBitmap(null);
                    favoriteContactTextView.setText("");

                }

            }
        }

        // configure regex search for edittext element
        this.editText = (ContactDialogEditText) dialog.findViewById(R.id.contact_filter);
        editText.setParent(this);
        editText.addTextChangedListener(new ContactFilterTextWatcher());

        // handle keyboard opening and closing
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardLayout(true);
            }
        });

        dialog.show();

    }

    public void dismiss() {
        dialog.dismiss();
    }

    private ArrayList<PhoneContact> getAllPhoneContacts() {

        Log.d("START", "Getting all Contacts");

        ArrayList<PhoneContact> arrContacts = new ArrayList<PhoneContact>();
        PhoneContact contact=null;

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false)
        {
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactNumber= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            long  contactId= cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String contactPhotoUri= cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

//            contact = new PhoneContact(context,contactNumber);
            contact = new PhoneContact();
            contact.id = contactId;
            contact.name = contactName;
            contact.setPhoneNumber(contactNumber);
            if (contactPhotoUri != null) contact.photoUri = Uri.parse(contactPhotoUri);

            if (contact != null)
            {
                arrContacts.add(contact);
            }
            contact = null;
            cursor.moveToNext();
        }
        cursor.close();
        cursor = null;

        Log.d("END","Got all Contacts");

        return arrContacts;
    }


    private ArrayList<PhoneContact> getSpecificContacts(String[] phoneNumbers) {

        // build SQL clause for WHERE statement
        String num = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String clause = num + " = ?";

        for (int i = 1; i<phoneNumbers.length; i++) {
            clause += " OR " + num + " = ?";
        }

        // strip unwanted characters (+) from phone numbers

//        clause = String.format(clause,phoneNumbers);

        Log.d("getSpecificContacts","clause: " + clause);

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI},
                clause,
                phoneNumbers,
                null);
        cursor.moveToFirst();

        PhoneContact newContact;
        ArrayList<PhoneContact> arrContacts = new ArrayList<PhoneContact>(8);

        while (cursor.isAfterLast() == false) {

            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            long phoneContactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

            newContact = new PhoneContact();
            newContact.id = phoneContactID;
            newContact.name = contactName;
            newContact.setPhoneNumber(contactNumber);

            if (thumbnailUri != null) {
                newContact.photoUri = Uri.parse(thumbnailUri);
                try {
                    newContact.photo = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(thumbnailUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                // No image resource found -- use a letter tile instead
                LetterTileProvider tileProvider = new LetterTileProvider(context);
                final Resources res = context.getResources();
                final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

                String text = contactName;
                newContact.photo = tileProvider.getLetterTile(text,text, tileSize, tileSize);

            }

            arrContacts.add(newContact);
            newContact = null;
            cursor.moveToNext();
        }

        cursor.close();
        cursor = null;

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

        public String getNumber(int position) {
            return this.phoneContacts.get(position).getNumber();
        }

    }

    private class SelectRecentContactListener implements View.OnClickListener {

        private PhoneContact phoneContact;

        private SelectRecentContactListener(PhoneContact pc) {
            this.phoneContact = pc;
        }

        @Override
        public void onClick(View v) {
            Log.d("SelectContactListener", "User clicked: " + phoneContact.name);
            sendFluffToContact(phoneContact);
        }
    }

    private class SelectContactListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            PhoneContact selectedContact = adapter.getItem(position);
            Log.d("SelectContactListener", "User clicked: " + selectedContact.name);

            sendFluffToContact(selectedContact);

        }
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

    public void setKeyboardLayout(boolean keyboardIsVisible) {
        View recents = dialog.findViewById(R.id.contact_dialog_recent_recipients);

        if (keyboardIsVisible) {
            recents.setVisibility(View.GONE);
        } else {
            recents.setVisibility(View.VISIBLE);

        }
    }

    private void sendFluffToContact(PhoneContact phoneContact) {

        PhoneContact[] contacts = {phoneContact};
        new SendFluffInBackgroundTask(context, parentActivity, fluff).execute(contacts);

        ContactsDialog.this.dismiss();

    }
}


