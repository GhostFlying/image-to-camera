package org.mistkeith.image2camera;


import android.view.View;


public class AppLoadingDialogFragment extends org.mistkeith.image2camera.LoadingDialogFragment {

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
