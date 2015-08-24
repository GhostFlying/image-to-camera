package com.ghostflying.image2camera;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;


public class ImageReadDialogFragment extends LoadingDialogFragment {

    public static ImageReadDialogFragment newInstance() {
        return new ImageReadDialogFragment();
    }

    public ImageReadDialogFragment() {
        // Required empty public constructor
    }

    @Override
    protected View getCustomView() {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_image_read_dialog, null);
    }

}
