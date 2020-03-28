package com.example.myapp;

import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.example.myapp.HttpServerActivity.mCamera;
import static com.example.myapp.HttpServerActivity.mPreview;

public class ClientThread extends Thread {

    Semaphore semaphore;
    ServerSocket serverSocket;
    Socket s;
    CameraCallback cameraCallback;
    Gateway gateway;

    public ClientThread(Socket socket, Semaphore semaphore){
        this.s = socket;
        this.semaphore = semaphore;
    }

    public void run() {
        try {
            Log.d("ClientThread", "Socket Accepted");

            OutputStream outputStream = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String line = in.readLine(); //to co příjmul

            String locPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File [] nestedFiles;

            while(!line.isEmpty())
            {
                Log.d("-",line);

                String pathSubString;
                String substring;
                String fileType;
                String mimetype;

                if(line.contains("GET /") && !line.contains("GET /snapshot") && !line.contains("GET /stream") && !line.contains("GET /streamold") && !line.contains("GET /cgi-bin"))
                {
                    sendMessage(line);

                    //Get substring
                    substring = getSubstring(line);
                    pathSubString = getPathSubstring(line);

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
                            outputStream.write(buffer, 0, len);
                        }
                    }

                    //Pokud je daný soubor složka -> pokračovat dále a listovat složku
                    else if(file.exists() && file.isDirectory())
                    {
                        nestedFiles = file.listFiles();

                        StringBuilder sb = new StringBuilder();
                        sb.append("<html><body><h1>List of all files</h1>");

                        if(nestedFiles.length > 0)
                        {
                            for (int i = 0; i < nestedFiles.length; i++)
                            {
                                String fileName = nestedFiles[i].getName();
                                sb.append(" <a href='");
                                sb.append(pathSubString + "/");
                                sb.append(nestedFiles[i].getName());
                                sb.append("/'>");
                                sb.append(fileName);
                                sb.append("</a><br><br>");
                            }
                        }
                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: "+ mimetype +"\n" +
                                "\n");
                        out.write(sb.toString());
                        out.flush();
                    }

                    else
                    {
                        nestedFiles = file.listFiles();

                        StringBuilder sb = new StringBuilder();
                        sb.append("<html><body><h1>List of all files</h1>");

                        if(nestedFiles.length > 0)
                        {
                            for (int i = 0; i < nestedFiles.length; i++)
                            {
                                String fileName = nestedFiles[i].getName();
                                sb.append(" <a href='");
                                sb.append(nestedFiles[i].getName());
                                sb.append("/'>");
                                sb.append(fileName);
                                sb.append("</a><br><br>");
                            }
                        }
                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: "+ mimetype +"\n" +
                                "\n");
                        out.write(sb.toString());
                        out.flush();
                    }
                }

                //camera snapshot
                else if(line.contains("GET /snapshot"))
                {
                    sendMessage(line);
                    cameraCallback = new CameraCallback();
                    mCamera.takePicture(null, null, cameraCallback);

                    while(cameraCallback.getTakenPictureData() == null)
                    {
                    }

                    byte[] data = cameraCallback.getTakenPictureData();


                    //Pokud file existuje -> zobrazí s daným mimetypem
                    if (data.length != 0) {
                        out.write("HTTP/1.1 200 Ok\n" +
                                "Content-Type: "+ "jpeg" +"\n" +
                                "Content-length: " + data.length + "\n" +
                                "\n");
                        out.flush();

                        outputStream.write(data);

                    }

                }

                //camera stream - pomocí camera preview
                else if(line.contains("GET /stream"))
                {
                    sendMessage(line);

                    out.write("HTTP/1.1 200 Ok\n" +
                            "Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"" +"\n" +
                            //"Content-length: " + data.length + "\n" +
                            "\n");
                    out.flush();

                    while(line.contains("GET /stream")) {
                        byte[] data = mPreview.previewData;

                        out.write("--OSMZ_boundary\n" +
                                "Content-Type: image/jpeg" + "\n" +
                                "Content-length: " + data.length + "\n" +
                                "\n");
                        out.flush();

                        outputStream.write(data);
                    }
                }

                else if(line.contains("GET /cgi-bin")){
                    sendMessage(line);

                    out.write("HTTP/1.1 200 Ok\n" +
                            "Content-Type: text/html" +"\n" +
                            //"Content-length: " + data.length + "\n" +
                            "\n");
                    out.flush();

                    gateway = new Gateway();
                    List<String> result = gateway.runCommand(line);

                    if(!result.isEmpty()){
                        for(String lineInResult : result) {
                            out.write("<p>" + lineInResult + "</p>");
                            out.flush();
                        }
                    }
                    else {
                        out.write("Nic neobsahuje");
                        out.flush();
                    }
                }

                //přečtení dalšího řádku
                line = in.readLine();
            }



            outputStream.close();
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

    private static void sendMessage(String stringMessage) {
        HttpServerActivity.sendMessageToUI(stringMessage);
    }
}
