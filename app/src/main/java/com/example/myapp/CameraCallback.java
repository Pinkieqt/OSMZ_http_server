package com.example.myapp;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraCallback implements Camera.PictureCallback {

    private File pictureFile;
    private byte[] takenPictureData;

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Log.d("Camera", "Image captured.");
        if (data == null)
            return;

        takenPictureData = data;
        pictureFile = getOutputMediaFile();

        //Zápis vyfoceného snímku na sd kartu
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
            //preview restart
            camera.startPreview();
        }

    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "ACameraApp");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Camera", "Failed to create directory, check permissions");
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG.jpg");

        return mediaFile;
    }

    public byte[] getTakenPictureData() {
        return takenPictureData;
    }

    public File getPictureFile() {
        return pictureFile;
    }


}
