package org.mistkeith.image2camera;


import android.view.View;


public class ImageReadDialogFragment extends org.mistkeith.image2camera.LoadingDialogFragment {

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
