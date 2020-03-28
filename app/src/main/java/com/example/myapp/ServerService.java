package com.example.myapp;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;


public class ServerService extends Service {
    public SocketServer socketServer = null;
    public Intent intent = null;
    public static Runnable runnable = null;

    @Override
    public void onCreate() {
        Log.d("Service", "Service Created");
    }

    @Override
    public void onDestroy() {
        socketServer.close();
        Log.d("Service", "Service stopped");
    }

    @Override
    public void onStart(Intent intent, int startid) {
        this.intent = intent;

        socketServer = new SocketServer(25);
        socketServer.start();
        Log.d("Service", "Service Started");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
