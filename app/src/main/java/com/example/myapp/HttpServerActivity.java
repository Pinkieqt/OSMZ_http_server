package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HttpServerActivity extends Activity implements OnClickListener{

	//Permission fix...
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};


	private int threadCount;

	private EditText editText;
	private static TextView textView;
	private FrameLayout previewLayout;

	public static final Camera mCamera = getCameraInstance();
	public static CameraPreview mPreview;
	private CameraCallback mCallback;

	private Intent serverServiceIntent;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);
		verifyStoragePermissions(this);

		Button serviceStartBtn = (Button)findViewById(R.id.serviceStartBtn);
		Button btn2 = (Button)findViewById(R.id.button2);
		Button shutterBtn = (Button)findViewById(R.id.capturebtn);

		editText = (EditText)findViewById((R.id.editText));
		textView = findViewById(R.id.textView);

		btn2.setOnClickListener(this);
		shutterBtn.setOnClickListener(this);
		serviceStartBtn.setOnClickListener(this);

		mPreview = new CameraPreview(this);
		previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
		previewLayout.addView(mPreview);
	}


	@Override
	public void onClick(View v) {
		threadCount = Integer.parseInt(editText.getText().toString());

		//start button
		if (v.getId() == R.id.serviceStartBtn) {
			serverServiceIntent = new Intent(this, ServerService.class);
			serverServiceIntent.putExtra("threadCount", threadCount);
			startService(serverServiceIntent);
		}
		//stop button
		if (v.getId() == R.id.button2) {
			stopService(serverServiceIntent);
		}
		//shutter button
		if (v.getId() == R.id.capturebtn) {
			mCallback = new CameraCallback();
			mCamera.takePicture(null, null, mCallback);
		}
	}


	public static void sendMessageToUI(final String message){
		new Handler(Looper.getMainLooper()).post(new Runnable(){
			@Override
			public void run() {
				String data = message + "\n";

				DateFormat df = new SimpleDateFormat("hh:mm:ss");
				String date = df.format(Calendar.getInstance().getTime());

				if(textView != null)
					textView.setText(date + ": " + data + textView.getText());
			}
		});
	}


	//Safe získání kamery
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open();
		}
		catch (Exception e){
			Log.d("Camera", "Camera is used already or doesn't exist.");
		}
		return c;
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
