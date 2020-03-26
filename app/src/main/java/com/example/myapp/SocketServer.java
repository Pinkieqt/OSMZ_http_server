package com.example.myapp;

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
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class SocketServer extends Thread {
	
	ServerSocket serverSocket;
	Semaphore semaphore;
	Camera mCamera;
	CameraPreview mPreview;
	Handler handler;
	public final int port = 12345;
	private int threadCount;
	boolean bRunning;

	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}

	SocketServer (Handler hand, int threadCount, Camera mCam, CameraPreview mPrev){
		this.handler = hand;
		this.mCamera = mCam;
		this.mPreview = mPrev;
		this.threadCount = threadCount;
		this.semaphore = new Semaphore(50);
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
				ClientThread client = new ClientThread(s, handler, semaphore, mCamera, mPreview);
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

	private void sendMessage(String stringMessage) {
		final Message message = Message.obtain();
		final Bundle b = new Bundle();
		b.putString("request", stringMessage);
		message.setData(b);
		handler.sendMessage(message);
	}
}
