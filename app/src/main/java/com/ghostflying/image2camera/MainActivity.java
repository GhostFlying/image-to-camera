package com.ghostflying.image2camera;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
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
            Bundle extra = cameraIntent.getExtras();
            ClipData clipData = cameraIntent.getClipData();
            outputUri = clipData.getItemAt(0).getUri();
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
                    finish();
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
        Toast.makeText(this, R.string.io_exception_toast, Toast.LENGTH_SHORT).show();
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
                saveFile(data.getData(), outputUri);
            }
        }
    }
}