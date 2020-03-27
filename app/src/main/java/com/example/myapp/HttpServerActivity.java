package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HttpServerActivity extends Activity implements OnClickListener{


	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	private SocketServer s;
	private int threadCount;
	EditText editText;

	FrameLayout preview;
	private Camera mCamera;
	private CameraPreview mPreview;
	private CameraCallback mCallback;



	private final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			String data = inputMessage.getData().getString("request");
			TextView textView = findViewById(R.id.textView);
			data = data + "\n";

			DateFormat df = new SimpleDateFormat("hh:mm:ss");
			String date = df.format(Calendar.getInstance().getTime());

			textView.setText(date + ": " + data + textView.getText());
		}
	};

	private void onStartAndResume(){
		setContentView(R.layout.activity_http_server);
		verifyStoragePermissions(this);

		Button btn1 = (Button)findViewById(R.id.button1);
		Button btn2 = (Button)findViewById(R.id.button2);
		Button shutterBtn = (Button)findViewById(R.id.capturebtn);
		Button serviceStartBtn = (Button)findViewById(R.id.serviceStartBtn);
		editText = (EditText)findViewById((R.id.editText));

		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		shutterBtn.setOnClickListener(this);
		serviceStartBtn.setOnClickListener(this);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}
	@Override
	public void onClick(View v) {
		threadCount = Integer.parseInt(editText.getText().toString());

		//start button (normal)
		if (v.getId() == R.id.button1) {
			s = new SocketServer(handler, threadCount, mCamera, mPreview);
			s.start();
		}
		//start button (service)
		if (v.getId() == R.id.serviceStartBtn) {
			// TODO - aktivita vytvoří background službu po stisku tohoto tlačítka - jiný thread než hlavní UI
			Log.d("Service", "Button clicked");
			Intent intent = new Intent(this, ServerService.class);
			startService(intent);



		}
		//stop button
		if (v.getId() == R.id.button2) {
			s.close();
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//shutter button
		if (v.getId() == R.id.capturebtn) {
			mCallback = new CameraCallback();
			mCamera.takePicture(null, null, mCallback.mPicture);
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		onStartAndResume();
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		onStartAndResume();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}






	//Nefungovalo zapisování na kartu - díky tomuto se Android zeptá na permission a funguje
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have read or write permission
		int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

		if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
}
