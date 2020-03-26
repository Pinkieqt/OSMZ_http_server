package com.example.myapp;

import android.Manifest;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);
        verifyStoragePermissions(this);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);
        Button shutterBtn = (Button)findViewById(R.id.capturebtn);
        editText = (EditText)findViewById((R.id.editText));

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        shutterBtn.setOnClickListener(this);

		TextView textView = findViewById(R.id.textView);
		textView.append("dsadas\n" + "dsada\n" + "dsadas\n" + "dsada\n" + "dsadas\n" + "dsada\n" + "dsadas\n" + "dsada\n" + "dsadas\n" + "dsada\n" + "sdgdfg");


		Button testbtn = (Button)findViewById(R.id.testbtn);
		testbtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.d("gate", "clicked");
				//Gateway gt = new Gateway();
				//gt.runCommand("GET /cgi-bin/uptime HTTP/1.1");
			}
		});




		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
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


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		threadCount = Integer.parseInt(editText.getText().toString());

		//start button
		if (v.getId() == R.id.button1) {
			s = new SocketServer(handler, threadCount, mCamera, mPreview);
			s.start();
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
