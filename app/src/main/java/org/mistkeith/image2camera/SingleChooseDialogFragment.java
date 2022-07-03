package org.mistkeith.image2camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghostflying on 15-8-24.
 */
public class SingleChooseDialogFragment extends org.mistkeith.image2camera.BaseAlertDialogFragment {
    // the fragment initialization parameters
    private static final String ARG_TITLE = "title";
    private static final String ARG_ITEMS = "items";
    private static final String ARG_ITEM_CHECKED = "checked";

    private int mTitle;
    private List<String> mItems;
    private int mChecked;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title the resoucce id of the title of the dialog.
     * @param items the resource id of items to choose.
     * @return A new instance of fragment SingleChooseDialogFragment.
     */
    public static SingleChooseDialogFragment newInstance(int title, ArrayList<String> items, int checked) {
        SingleChooseDialogFragment fragment = new SingleChooseDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putStringArrayList(ARG_ITEMS, items);
        args.putInt(ARG_ITEM_CHECKED, checked);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleChooseDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getInt(ARG_TITLE);
            mItems = getArguments().getStringArrayList(ARG_ITEMS);
            mChecked = getArguments().getInt(ARG_ITEM_CHECKED);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setSingleChoiceItems(mItems.toArray(new String[mItems.size()]), mChecked, onClickListener)
                .setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onPositiveButtonClick(mChecked, mTitle);
                    }
                })
                .setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mChecked = which;
        }
    };
}
