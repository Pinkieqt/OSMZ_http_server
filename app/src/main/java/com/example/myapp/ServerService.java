package com.example.myapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;


public class ServerService extends Service {
    public SocketServer socketServer = null;

    @Override
    public void onCreate() {
        Log.d("Service", "Service created");
    }

    @Override
    public void onDestroy() {
        socketServer.close();
        Log.d("Service", "Service stopped");
    }

    @Override
    public void onStart(Intent intent, int startid) {
        //Default value
        int threadCount = 25;
        if (intent != null)
            threadCount = intent.getIntExtra("threadCount", 25);
        socketServer = new SocketServer(threadCount);
        socketServer.start();
        Log.d("Service", "Service started");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
