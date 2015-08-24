package com.ghostflying.image2camera;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;


public class AppLoadingDialogFragment extends LoadingDialogFragment {

    public static AppLoadingDialogFragment newInstance() {
        return new AppLoadingDialogFragment();
    }

    public AppLoadingDialogFragment() {
        // Required empty public constructor
    }

    @Override
    protected View getCustomView() {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_load_app_dialog, null);
    }

}
