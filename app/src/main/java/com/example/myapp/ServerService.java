package com.example.myapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.net.Socket;

public class ServerService extends Service {
    public SocketServer s = null;
    public Intent intent = null;
    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    @Override
    public void onCreate() {
        Log.d("Service", "Service Created");
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                handler.postDelayed(runnable, 10000);
            }
        };

        handler.postDelayed(runnable, 15000);
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        this.intent = intent;

        Bundle bundle = this.intent.getExtras();
        s = bundle.getParcelable("socket");

        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }








    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
