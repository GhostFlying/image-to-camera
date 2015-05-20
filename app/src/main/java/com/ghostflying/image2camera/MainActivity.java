package com.ghostflying.image2camera;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 10;
    private static final String IMAGE_TYPE = "image/*";

    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent cameraIntent = getIntent();
        if (cameraIntent.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                ClipData clipData = cameraIntent.getClipData();
                outputUri = clipData.getItemAt(0).getUri();
            }

            // compatibility for system below lollipop
            if (outputUri == null){
                Bundle extra = cameraIntent.getExtras();
                outputUri = extra.getParcelable(MediaStore.EXTRA_OUTPUT);
            }
            pickImages();
        }
    }

    private void pickImages() {
        Intent intent = new Intent();
        intent.setType(IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE);
    }

    private void saveFile(Uri source, Uri des){
        try{
            final InputStream inputStream = getContentResolver().openInputStream(source);
            final File outputFile = new File(des.getPath());
            final OutputStream outputStream = new FileOutputStream(outputFile);

            try{
                try{
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = inputStream.read(buffer)) != -1)
                        outputStream.write(buffer, 0, read);

                    outputStream.flush();

                    // copy images successfully.
                    setResult(Activity.RESULT_OK);
                }
                finally {
                    inputStream.close();
                    outputStream.close();
                }
            }
            catch (IOException e){
                handleException(e);
            }
        }
        catch (FileNotFoundException e){
            handleException(e);
        }
    }

    private void handleException(Exception e){
        e.printStackTrace();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.io_exception_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFile(Uri file){
        File outputFile = new File(file.getPath());
        outputFile.delete();
    }

    private void handleNotSelect(){
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
                return;
            }
            if (resultCode == Activity.RESULT_OK){
                new SaveFileTask().execute(data.getData(), outputUri);
            }
        }
    }

    private class SaveFileTask extends AsyncTask<Uri, Void, Void>{
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
        protected void onPostExecute(Void result){
            dialogFragment.dismiss();
            finish();
        }
    }
}
