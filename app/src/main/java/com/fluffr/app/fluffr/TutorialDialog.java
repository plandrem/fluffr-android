package com.fluffr.app.fluffr;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * Created by Patrick on 12/17/14.
 */
public class TutorialDialog {

    private Context context;
    private Dialog dialog;
    private ImageButton button;

    public TutorialDialog(Context context) {
        this.context = context;

        dialog = new Dialog(context, R.style.Theme_AppCompat);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        dialog.setContentView(R.layout.tutorial);

        button = (ImageButton) dialog.findViewById(R.id.tutorial_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    public void show() {
        dialog.show();
    }
}
