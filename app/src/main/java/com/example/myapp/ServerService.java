package com.example.myapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;


public class ServerService extends Service {
    public SocketServer socketServer = null;
    public Intent intent = null;

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
        this.intent = intent;
        socketServer = new SocketServer(this.intent.getIntExtra("threadCount", 25));
        socketServer.start();
        Log.d("Service", "Service started");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
