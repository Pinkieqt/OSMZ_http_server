package com.example.myapp;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static androidx.core.content.ContextCompat.startActivity;

public class CameraCallback {

    public File pictureFile;
    public byte[] pictureData;

    public CameraCallback(){}

    public Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("Camera", "Image captured.");
            pictureData = data;
            pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d("Camera", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Camera", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Camera", "Error accessing file: " + e.getMessage());
            }
            finally {
                //restart kamery
                camera.startPreview();
            }
        }
    };


    private static File getOutputMediaFile(){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "ACameraApp");
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("AMyCameraApp", "failed to create directory, check permissions");
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG.jpg");

        return mediaFile;
    }

}
