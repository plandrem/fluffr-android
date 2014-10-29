package com.fluffr.app.fluffr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Patrick on 10/28/14.
 */
public class LoadingDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // inflate custom layout
        View view = (View) LayoutInflater.from(getActivity()).inflate(R.layout.loading_dialog, null);
        builder.setView(view);

        return builder.create();
    }
}
