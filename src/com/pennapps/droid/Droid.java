package com.pennapps.droid;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Droid extends Activity implements SensorEventListener,
OnTouchListener {
	
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// Zoom states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOMING = 2;
	int mode = NONE;
	
	static final int WORK_MODE = 0;
	static final int CRUISE_MODE = 1;
	int workmode = WORK_MODE;
	boolean holding = false;

	// Flags
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f; // used to calculate zoom update threshold
	float lastZoomDist = 1f; // used to send zoom scale values
	float lastDragDist = 1f; // used to send drag values
	private long lastSensorUpdate = -1;
	private long lastZoomUpdate = -1;
	private long lastPinchUpdate = -1;
	private long lastDragUpdate = -1;

	// Accelerometer values
	private float x = 0, y = 0, z = 0;

	/* UPDATE VARIABLES  */
	private float last_x = 0, last_y = 0, last_z = 0;
	private float zoom=1, deltaX=0, deltaY=0;
	
	/* Detection constants -- change to tweak performance */
	private static final long TIME_THRESHOLD = 100;
	private static final float VELOCITY_THRESHOLD = 0.10f;
	private static final float FILTERING_FACTOR = 0.85f;
	
	private static final int MAX_X = 360;
	private static final int MAX_Y = 180;
	private static final int MAX_Z = 180;
	private static final int DEGREES = 360;
	
	private static final float X_THRESHOLD = 50;
	private static final float Y_THRESHOLD = 50;
	private static final float Z_THRESHOLD = 50;
	
	

	private SensorManager sensorManager = null;
	
	private float xOffset;
	private String ipval;
	private boolean connectionValid = true;
	
	private int width;
	private int height;
	
	private Timer timer;
	private TimerTask task;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		setContentView(R.layout.camview);
		
		WindowManager w = getWindowManager(); 
	    Display d = w.getDefaultDisplay(); 
	    width = d.getWidth(); 
	    height = d.getHeight(); 

		ImageView view1 = (ImageView) findViewById(R.id.imageView);
		view1.setOnTouchListener(this);

		xOffset = -1; 
		
		TextView tv = (TextView) findViewById(R.id.orientation_label);
		tv.setText("You are now connected to " + ipval);
		
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        timer = new Timer();
        
	}
	
	public void sendDimensions(){
		sendDimensions(0);
	}
	// 1 if reset requested
	public synchronized void sendDimensions(int reset){
		if ((holding && workmode == WORK_MODE) || reset == 1 || workmode == CRUISE_MODE){
			DataSender.SendDimension(this, 
					Float.toString(last_x), Float.toString(last_y),
					Float.toString(last_z), Float.toString(zoom),
					Float.toString(deltaX), Float.toString(deltaY), 
					Integer.toString(reset));
			zoom = 1;
			deltaX = 0;
			deltaY = 0;
		}
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.optionsmenu, menu);
    	return true;
    }
    
	 public boolean onOptionsItemSelected(MenuItem item){
	    	// Handle item selection
	        switch (item.getItemId()) {
	        case R.id.reset_view:
	        	sendDimensions(1);
	            return true;
	        case R.id.work_mode:
	        	workmode = WORK_MODE;
	        	return true;
	        case R.id.cruise_mode:
	        	workmode = CRUISE_MODE;
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }
	
	public void setConnectionStatus(boolean b){
		connectionValid = b;
	}
	
	public String getIp(){
		return ipval;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;

		// Handle touch events
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = findDistance(event);
			if (oldDist > 10f) {
				lastZoomDist = oldDist;
				savedMatrix.set(matrix);
				findMidPoint(mid, event);
				mode = ZOOMING;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			holding = false;
			break;
		case MotionEvent.ACTION_MOVE:
			long curTime = System.currentTimeMillis();
			holding = true;
			if (mode == DRAG) {
				if ((curTime - lastDragUpdate) > TIME_THRESHOLD) {
					deltaX = (event.getX() - start.x) / (width);
					deltaY = (event.getY() - start.y) / (height);
					lastDragUpdate = curTime;
					start.set(event.getX(), event.getY());
				}
			} else if (mode == ZOOMING) {
				float newDist = findDistance(event);
				
				if (newDist > 10f && (curTime - lastPinchUpdate) > TIME_THRESHOLD) {
					lastPinchUpdate = curTime;
					
					matrix.set(savedMatrix);
					zoom = newDist / lastZoomDist;
					
					if ((curTime - lastZoomUpdate) > TIME_THRESHOLD){
						lastZoomUpdate = curTime;
						lastZoomDist = newDist;
					}

					//matrix.postScale(newDist/oldDist, newDist/oldDist, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	// This method will update the UI on new sensor events
	public void onSensorChanged(SensorEvent sensorEvent) {
		synchronized (this) {
			TextView tv = (TextView) findViewById(R.id.orientation_label);
			if (!connectionValid){
				tv.setText("Error, the IP address is invalid. Please reconnect.");
			}
			else {
				tv.setText("You are connected to " + ipval);
			}
			
			long curTime = System.currentTimeMillis();
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				
				if (xOffset < 0){
					xOffset = sensorEvent.values[0];
				}

				// only allow one update every 100ms.
				if ((curTime - lastSensorUpdate) > TIME_THRESHOLD) {
					lastSensorUpdate = curTime;

					// CURRENT VALUES
					x = sensorEvent.values[0];
					y = sensorEvent.values[1];
					z = sensorEvent.values[2];
					
					x = (x + DEGREES - xOffset) % DEGREES;

					// EXPONENTIAL AVERAGING
					float jump = (x * FILTERING_FACTOR) + (last_x * (1.0f - FILTERING_FACTOR));
					last_x = (Math.abs(jump) > X_THRESHOLD) ? x : jump;
					
					jump = (y * FILTERING_FACTOR) + (last_y * (1.0f - FILTERING_FACTOR));
					last_y = (Math.abs(jump) > Y_THRESHOLD) ? y : jump;
					
					jump = (z * FILTERING_FACTOR) + (last_z * (1.0f - FILTERING_FACTOR));
					last_z = (Math.abs(jump) > Z_THRESHOLD) ? z : jump;

					float xspeed = (x - last_x);
					float yspeed = (y - last_y);
					float zspeed = (z - last_z);

					boolean sendChange = false;

					// averaged positions
					if (xspeed > VELOCITY_THRESHOLD || xspeed < -VELOCITY_THRESHOLD) {
//						orientXValue.setText(Float.toString(last_x));
						sendChange = true;
					}
					if (yspeed > VELOCITY_THRESHOLD || yspeed < -VELOCITY_THRESHOLD) {
//						orientYValue.setText(Float.toString(last_y));
						sendChange = true;
					}
					if (zspeed > VELOCITY_THRESHOLD || zspeed < -VELOCITY_THRESHOLD) {
//						orientZValue.setText(Float.toString(last_z));
						sendChange = true;
					}

					if (sendChange) {
//						DataSender.SendDimension(this, Float.toString(last_x), Float
//								.toString(last_y), Float.toString(last_z), "1", "0", "0", "0");
					}

				}
			}
		}
	}
	
	/** Determine the distance between the first two fingers */
	private float findDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void findMidPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Register this class as a listener for the accelerometer sensor
		sensorManager.registerListener(this, sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, sensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
		
		ipval = getIntent().getExtras().getString("ip");
		
		task = new TimerTask(){
			@Override
			public void run() {
				sendDimensions();
			}
        };
		timer.schedule(task, 0, TIME_THRESHOLD);
		
	}

	@Override
	protected void onStop() {
		// Unregister the listener
		sensorManager.unregisterListener(this);
		task.cancel();
		super.onStop();
	}

	@Override
	protected void onPause() {
		// Unregister the listener
		sensorManager.unregisterListener(this);
		task.cancel();
		super.onPause();
	}

}