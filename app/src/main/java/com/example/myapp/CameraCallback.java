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

    public CameraCallback(){

    }



    public Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("Camera", "Snímek zachycen.");
            pictureData = data;
            pictureFile = getOutputMediaFile(1);
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


    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "ACameraApp");
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        //		Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("AMyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG.jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

}
