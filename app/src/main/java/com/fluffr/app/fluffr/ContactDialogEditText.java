package com.fluffr.app.fluffr;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Patrick on 12/15/14.
 */
public class ContactDialogEditText extends EditText {

    private ContactsDialog parent;

    public ContactDialogEditText(Context context) {
        super(context);
    }

    public ContactDialogEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactDialogEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setParent(ContactsDialog cd) {
        parent = cd;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {

        Log.d("EditText","Event Caught");

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("EditText","Back Key caught");
            parent.setKeyboardLayout(false);
        }

        return super.onKeyPreIme(keyCode, event);
    }
}
