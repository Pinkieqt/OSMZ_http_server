package com.example.myapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import android.util.Log;

import static com.example.myapp.HttpServerActivity.mCamera;

public class SocketServer extends Thread {
	
	private ServerSocket serverSocket;
	private final int port = 12345;

	private Semaphore semaphore;
	private boolean bRunning;

	//Camera
	private CameraCallback mCallback;

	SocketServer (int threadCount){
		this.mCallback = new CameraCallback();
		this.semaphore = new Semaphore(threadCount);
	}

	public void run() {
        try {

        	Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
            	Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                semaphore.acquire();

				ClientThread client = new ClientThread(s, semaphore);
				client.start();
            }
        } 
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
        	serverSocket = null;
        	bRunning = false;
        }
    }

	public void close() {
		try {
			serverSocket.close();
			mCamera.setPreviewCallback(null);
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}

}
