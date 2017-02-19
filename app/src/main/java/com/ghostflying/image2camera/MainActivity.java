package com.ghostflying.image2camera;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 10;
    private static final String IMAGE_TYPE = "image/*";

    private Uri outputUri;
    private List<AppInfo> cameraApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent cameraIntent = getIntent();
        if (cameraIntent.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE)) {
            startPickImages(cameraIntent);
        }
    }

    private void startPickImages(Intent cameraIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ClipData clipData = cameraIntent.getClipData();
            outputUri = clipData.getItemAt(0).getUri();
        }

        // compatibility for system below lollipop
        if (outputUri == null) {
            Bundle extra = cameraIntent.getExtras();
            outputUri = extra.getParcelable(MediaStore.EXTRA_OUTPUT);
        }
        pickImages();
    }

    private void pickImages() {
        Intent intent = new Intent();
        intent.setType(IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE);
    }

    private void saveFile(Uri source, Uri des) {
        try {
            final InputStream inputStream = getContentResolver().openInputStream(source);
            final File outputFile = new File(des.getPath());
            final OutputStream outputStream = new FileOutputStream(outputFile);

            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = inputStream.read(buffer)) != -1)
                        outputStream.write(buffer, 0, read);

                    outputStream.flush();

                    // copy images successfully.
                    setResult(Activity.RESULT_OK);
                } finally {
                    inputStream.close();
                    outputStream.close();
                }
            } catch (IOException e) {
                handleException(e);
            }
        } catch (FileNotFoundException e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.io_exception_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFile(Uri file) {
        File outputFile = new File(file.getPath());
        outputFile.delete();
    }

    private void handleNotSelect() {
        Toast.makeText(this, R.string.not_select_toast, Toast.LENGTH_SHORT).show();
        deleteFile(outputUri);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (data == null || resultCode == Activity.RESULT_CANCELED) {
                handleNotSelect();
                setResult(RESULT_CANCELED);
                return;
            }
            if (resultCode == Activity.RESULT_OK) {
                new SaveFileTask().execute(data.getData(), outputUri);
                setResult(RESULT_OK);
            }
        }
    }

    private class SaveFileTask extends AsyncTask<Uri, Void, Void> {
        DialogFragment dialogFragment;

        @Override
        protected void onPreExecute() {
            dialogFragment = ImageReadDialogFragment.newInstance();
            dialogFragment.show(getFragmentManager(), null);
        }

        @Override
        protected Void doInBackground(Uri... params) {
            saveFile(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialogFragment.dismiss();
            finish();
        }
    }

    private class LoadAppTask extends AsyncTask<Void, Void, Void> {
        private DialogFragment dialogFragment;
        private ArrayList<String> appNames;
        private int checked;

        @Override
        protected void onPreExecute() {
            dialogFragment = AppLoadingDialogFragment.newInstance();
            dialogFragment.show(getFragmentManager(), null);
        }

        @Override
        protected Void doInBackground(Void... params) {
            final Intent mainIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = MainActivity.this.getPackageManager();
            final List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0);

            SharedPreferences preferences = getSharedPreferences(SettingUtil.SETTING_NAME, MODE_PRIVATE);
            String defaultApp = preferences.getString(SettingUtil.DEFAULT_CAMERA_APP_ACTIVITY, null);

            cameraApps = new ArrayList<>(pkgAppsList.size());
            appNames = new ArrayList<>(pkgAppsList.size());
            for (ResolveInfo each : pkgAppsList) {
                if (!each.activityInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                    AppInfo info = new AppInfo();
                    info.appName = each.loadLabel(packageManager).toString();
                    info.activityName = each.activityInfo.name;
                    info.packageName = each.activityInfo.packageName;
                    cameraApps.add(info);
                    appNames.add(info.appName);

                    if (defaultApp != null && info.activityName.equals(defaultApp)) {
                        checked = cameraApps.size() - 1;
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            dialogFragment.dismiss();
            DialogFragment chooseDialog = SingleChooseDialogFragment
                    .newInstance(R.string.dialog_title_app_choose, appNames, checked);
            chooseDialog.show(getFragmentManager(), null);
        }
    }

    private static class AppInfo {
        String packageName;
        String activityName;
        String appName;
    }
}
