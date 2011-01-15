package com.pennapps.droid;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Droid extends Activity implements SensorEventListener {
	// Accelerometer X, Y, and Z values
	private TextView accelXValue;
	private TextView accelYValue;
	private TextView accelZValue;
	private TextView linaccelXValue;
	private TextView linaccelYValue;
	private TextView linaccelZValue;
	
	private float x, y, z;
	private float last_x, last_y, last_z;
	private long xtime, ytime, ztime;
    private long lastUpdate = -1;
    
    /* Detection constants -- change to tweak performance */
	private static final long TIME_THRESHOLD = 1000;
	private static final float MOVEMENT_THRESHOLD = 6;
    private static final float FILTERING_FACTOR = 0.3f;

	// Orientation X, Y, and Z values
	private TextView orientXValue;
	private TextView orientYValue;
	private TextView orientZValue;

	private SensorManager sensorManager = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get a reference to a SensorManager
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		setContentView(R.layout.main);

		// Capture accelerometer related view elements
		accelXValue = (TextView) findViewById(R.id.accel_x_value);
		accelYValue = (TextView) findViewById(R.id.accel_y_value);
		accelZValue = (TextView) findViewById(R.id.accel_z_value);
		linaccelXValue = (TextView) findViewById(R.id.linaccel_x_value);
		linaccelYValue = (TextView) findViewById(R.id.linaccel_y_value);
		linaccelZValue = (TextView) findViewById(R.id.linaccel_z_value);

		// Capture orientation related view elements
		orientXValue = (TextView) findViewById(R.id.orient_x_value);
		orientYValue = (TextView) findViewById(R.id.orient_y_value);
		orientZValue = (TextView) findViewById(R.id.orient_z_value);

		// Initialize accelerometer related view elements
		accelXValue.setText("0.00");
		accelYValue.setText("0.00");
		accelZValue.setText("0.00");
		linaccelXValue.setText("0.00");
		linaccelYValue.setText("0.00");
		linaccelZValue.setText("0.00");

		// Initialize orientation related view elements
		orientXValue.setText("0.00");
		orientYValue.setText("0.00");
		orientZValue.setText("0.00");       
	}

	// This method will update the UI on new sensor events
	public void onSensorChanged(SensorEvent sensorEvent) {
		synchronized (this) {
					
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelXValue.setText(Float.toString(sensorEvent.values[0]));
				accelYValue.setText(Float.toString(sensorEvent.values[1]));
				accelZValue.setText(Float.toString(sensorEvent.values[2]));
				
				long curTime = System.currentTimeMillis();
				// only allow one update every 100ms.
				if ((curTime - lastUpdate) > 100) {
					long diffTime = (curTime - lastUpdate);
					lastUpdate = curTime;
					
					float[] values = sensorEvent.values;
					
					x = sensorEvent.values[0];
					y = sensorEvent.values[1];
					z = sensorEvent.values[2];
					
					last_x = (x * FILTERING_FACTOR) + (last_x * (1.0f - FILTERING_FACTOR));
					last_y = (y * FILTERING_FACTOR) + (last_y * (1.0f - FILTERING_FACTOR));
					last_z = (z * FILTERING_FACTOR) + (last_z * (1.0f - FILTERING_FACTOR));

					float xspeed = x - last_x;
					float yspeed = y - last_y;
					float zspeed = z - last_z;
					if (xspeed > MOVEMENT_THRESHOLD && (curTime-xtime > TIME_THRESHOLD) ) {
						xtime = curTime;
						Toast.makeText(getApplicationContext(), "X direction1", Toast.LENGTH_SHORT).show();
					}
					else if (xspeed < -MOVEMENT_THRESHOLD && (curTime-xtime > TIME_THRESHOLD)) {
						xtime = curTime;
						Toast.makeText(getApplicationContext(), "X direction2", Toast.LENGTH_SHORT).show();
					}
					if (yspeed > MOVEMENT_THRESHOLD && (curTime-ytime > TIME_THRESHOLD)) {
						ytime = curTime;
						Toast.makeText(getApplicationContext(), "Y direction1", Toast.LENGTH_SHORT).show();
					}
					else if (yspeed < -MOVEMENT_THRESHOLD && (curTime-ytime > TIME_THRESHOLD)) {
						ytime = curTime;
						Toast.makeText(getApplicationContext(), "Y direction2", Toast.LENGTH_SHORT).show();
					}
					if (zspeed > MOVEMENT_THRESHOLD && (curTime-ztime > TIME_THRESHOLD)) {
						ztime = curTime;
						Toast.makeText(getApplicationContext(), "Z direction1", Toast.LENGTH_SHORT).show();
					}
					else if (zspeed < -MOVEMENT_THRESHOLD && (curTime-ztime > TIME_THRESHOLD)) {
						ztime = curTime;
						Toast.makeText(getApplicationContext(), "Z direction2", Toast.LENGTH_SHORT).show();
					}
					
					linaccelXValue.setText(Float.toString(xspeed));
					linaccelYValue.setText(Float.toString(yspeed));
					linaccelZValue.setText(Float.toString(zspeed));
					
					/*
					last_x = x;
					last_y = y;
					last_z = z;*/
				}
			}

			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				orientXValue.setText(Float.toString(sensorEvent.values[0]));
				orientYValue.setText(Float.toString(sensorEvent.values[1]));
				orientZValue.setText(Float.toString(sensorEvent.values[2]));       
			}
		}
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Register this class as a listener for the accelerometer sensor
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onStop() {
		// Unregister the listener
		sensorManager.unregisterListener(this);
		super.onStop();
	}

}