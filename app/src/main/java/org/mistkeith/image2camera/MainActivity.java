package org.mistkeith.image2camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 10;
    private static final String IMAGE_TYPE = "image/*";

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL = 100;

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
            if (clipData != null) {
                outputUri = clipData.getItemAt(0).getUri();
            }
        }

        // compatibility for system below lollipop
        if (outputUri == null) {
            Bundle extra = cameraIntent.getExtras();
            if (extra != null) {
                outputUri = extra.getParcelable(MediaStore.EXTRA_OUTPUT);
            }
        }

        if (outputUri == null) {
            Toast.makeText(this, R.string.not_support_message, Toast.LENGTH_LONG).show();
            return;
        }

        if (requestPermissionIfNeeded()) {
            pickImages();
        }
    }

    private boolean requestPermissionIfNeeded() {
        if ("content".equals(outputUri.getScheme())) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setMessage(R.string.permission_write_external_message)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_WRITE_EXTERNAL
                                );
                            }
                        });
                builder.create().show();
            }
            else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_WRITE_EXTERNAL
                );
            }
        }
        else {
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImages();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            final OutputStream outputStream = getContentResolver().openOutputStream(des);

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
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        setResult(Activity.RESULT_CANCELED);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.io_exception_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotSelect() {
        Toast.makeText(this, R.string.not_select_toast, Toast.LENGTH_SHORT).show();
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
            dialogFragment = org.mistkeith.image2camera.AppLoadingDialogFragment.newInstance();
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
