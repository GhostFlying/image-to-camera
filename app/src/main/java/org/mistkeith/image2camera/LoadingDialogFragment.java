package org.mistkeith.image2camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

/**
 * Created by ghostflying on 15-8-25.
 */
public abstract class LoadingDialogFragment extends DialogFragment{

    public LoadingDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View view = getCustomView();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    protected abstract View getCustomView();
}
