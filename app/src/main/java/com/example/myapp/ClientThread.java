package com.example.myapp;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Semaphore;

public class ClientThread extends Thread {

    Handler handler;
    Camera mCamera;
    CameraPreview mPreview;
    Semaphore semaphore;
    ServerSocket serverSocket;
    Socket s;
    boolean bRunning;
    CameraCallback cameraCallback;
    Gateway gateway;

    public ClientThread(Socket x, Handler hand, Semaphore semaphore, Camera mCam, CameraPreview mPrev){
        this.handler = hand;
        this.semaphore = semaphore;
        this.mCamera = mCam;
        this.mPreview = mPrev;
        this.s = x;

        //mPreview = new CameraPreview(context, mCamera);

    }

    public void close() {
        try {
            serverSocket.close();

        } catch (IOException e) {
            Log.d("ClientThread", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void run() {
        try {
            Log.d("ClientThread", "Socket Accepted");

            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String tmp = in.readLine(); //to co příjmul

            String locPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File [] arrayfile;

            while(!tmp.isEmpty())
            {
                Log.d("-",tmp);

                String pathSubString = "nothing here";
                String substring = "nothing here";
                String fileType = "nothing here";
                String mimetype = "nothing here";

                if(tmp.contains("GET /") && !tmp.contains("GET /snapshot") && !tmp.contains("GET /stream") && !tmp.contains("GET /streamold") && !tmp.contains("GET /cgi-bin"))
                {
                    sendMessage(tmp);

                    //Get substring
                    substring = getSubstring(tmp);
                    pathSubString = getPathSubstring(tmp);

                    //Get mimetype
                    fileType = MimeTypeMap.getFileExtensionFromUrl(substring);
                    mimetype = getMimeType(fileType);


                    File file = new File(locPath + "/" + pathSubString);

                    //Pokud file existuje -> zobrazí s daným mimetypem
                    if (file.exists() && file.isFile()) {
                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: "+ mimetype +"\n" +
                                "Content-length: " + file.length() + "\n" +
                                "\n");
                        out.flush();

                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];

                        int len;
                        while((len = fileInputStream.read(buffer)) != -1)
                        {
                            o.write(buffer, 0, len);
                        }
                    }

                    //Pokud je daný soubor složka -> pokračovat dále a listovat složku
                    else if(file.exists() && file.isDirectory())
                    {
                        File root = Environment.getExternalStorageDirectory();
                        String fullPath = root.getAbsolutePath() + "/" + substring;

                        File otherFile = new File(fullPath);
                        arrayfile = otherFile.listFiles();


                        Log.d("substring", pathSubString);
                        Log.d("ASRAGS", pathSubString);

                        String html = "<html><body><h1>List of files</h1>";
                        if(arrayfile.length > 0)
                        {
                            for (int i = 0; i < arrayfile.length; i++)
                            {
                                html += "<a href='" + pathSubString + "/" + arrayfile[i].getName() + "'>" + arrayfile[i].getName() + "</a><br><br>";

                            }
                        }

                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: "+ mimetype +"\n" +
                                "\n");
                        out.write(html);
                        out.flush();

                    }

                    else
                    {
                        Log.d("JSEM VENKU", "venku");
                        File root = Environment.getExternalStorageDirectory();

                        File otherFile = new File(root.getAbsolutePath() + "/");
                        arrayfile = otherFile.listFiles();


                        Log.d("substring", pathSubString);
                        Log.d("ASRAGS", pathSubString);

                        String html = "<html><body><h1>List of files</h1>";
                        if(arrayfile.length > 0)
                        {
                            for (int i = 0; i < arrayfile.length; i++)
                            {
                                html += "<a href='" + "/" + arrayfile[i].getName() + "'>" + arrayfile[i].getName() + "</a><br><br>";

                            }
                        }

                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: text/html\n" +
                                "\n");
                        out.write(html);
                        out.flush();
                    }
                }

                //camera snapshot
                if(tmp.contains("GET /snapshot"))
                {
                    sendMessage(tmp);
                    cameraCallback = new CameraCallback();
                    mCamera.takePicture(null, null, cameraCallback.mPicture);

                    while(cameraCallback.pictureData == null)
                    {
                    }

                    byte[] data = cameraCallback.pictureData;


                    //Pokud file existuje -> zobrazí s daným mimetypem
                    if (data.length != 0) {
                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: "+ "jpeg" +"\n" +
                                "Content-length: " + data.length + "\n" +
                                "\n");
                        out.flush();

                        o.write(data);

                    }

                }

                //camera stream - old, pomocí OnPictureTaken a CameraCallbacku - nelze dosáhnout víc jak 1 fps..
                if(tmp.contains("GET /streamold"))
                {
                    sendMessage(tmp);

                    out.write("HTTP/1.1 200 Ok\n" +
                            "Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"" +"\n" +
                            //"Content-length: " + data.length + "\n" +
                            "\n");
                    out.flush();

                    int x = 1;
                    while( x < 8 ){
                        cameraCallback = new CameraCallback();
                        mCamera.takePicture(null, null, cameraCallback.mPicture);

                        while(cameraCallback.pictureData == null)
                        {
                        }

                        byte[] data = cameraCallback.pictureData;

                        out.write("--OSMZ_boundary\n" +
                                "Content-Type: image/jpeg" + "\n" +
                                "Content-length: " + data.length + "\n" +
                                "\n");
                        out.flush();

                        o.write(data);

                        x++;
                    }
                }

                //camera stream - pomocí camera preview
                if(tmp.contains("GET /stream"))
                {
                    sendMessage(tmp);

                    out.write("HTTP/1.1 200 Ok\n" +
                            "Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"" +"\n" +
                            //"Content-length: " + data.length + "\n" +
                            "\n");
                    out.flush();

                    while(tmp.contains("GET /stream")) {
                        byte[] data = mPreview.previewData;

                        out.write("--OSMZ_boundary\n" +
                                "Content-Type: image/jpeg" + "\n" +
                                "Content-length: " + data.length + "\n" +
                                "\n");
                        out.flush();

                        o.write(data);
                    }
                }

                if(tmp.contains("GET /cgi-bin")){
                    sendMessage(tmp);

                    out.write("HTTP/1.1 200 Ok\n" +
                            "Content-Type: text/html" +"\n" +
                            //"Content-length: " + data.length + "\n" +
                            "\n");
                    out.flush();

                    gateway = new Gateway();
                    List<String> result = gateway.runCommand(tmp);

                    if(!result.isEmpty()){
                        for(String line : result) {
                            out.write("<p>" + line + "</p>");
                            out.flush();
                        }
                    }
                    else {
                        out.write("Nic neobsahuje");
                        out.flush();
                    }
                }

                //přečtení dalšího řádku
                tmp = in.readLine();
            }



            o.close();
            s.close();
            Log.d("ClientThread", "Socket Closed");

        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("ClientThread", "Normal exit");
            else {
                Log.d("ClientThread", "Error");
                e.printStackTrace();
            }
        } finally {
            serverSocket = null;
            semaphore.release();
        }
    }




    /*
    *
    *
    *               HELP FUNCTIONS
    *
    *
    */
    private String getMimeType(String fileType){
        String mimetype = "text/html";
        if (fileType != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimetype = mime.getMimeTypeFromExtension(fileType);
        }
        return mimetype;
    }

    private String getPathSubstring(String tmp){
        String substring;

        substring = tmp.replace("GET /", "");
        substring = substring.replace(" HTTP/1.1", "");

        return substring;

    }

    private String getSubstring(String tmp){
        String substring;
        substring = tmp.substring(5);
        substring = substring.substring(0, substring.lastIndexOf(" "));
        substring = substring.split(" ")[0];

        return substring;
    }

    private void sendMessage(String stringMessage) {
        final Message message = Message.obtain();
        final Bundle b = new Bundle();
        b.putString("request", stringMessage);
        message.setData(b);
        handler.sendMessage(message);
    }
}
