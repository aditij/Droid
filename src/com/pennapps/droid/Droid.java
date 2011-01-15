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
	private TextView xorient_tv;
	private TextView yorient_tv;
	private TextView zorient_tv;
	private TextView xorientv_tv;
	private TextView yorientv_tv;
	private TextView zorientv_tv;

	private float x = 0, y = 0, z = 0;
	private float last_x = 0, last_y = 0, last_z = 0;
	private long xtime, ytime, ztime;
	private long lastUpdate = -1;

	/* Detection constants -- change to tweak performance */
	private static final long TIME_THRESHOLD = 1000;
	private static final float VELOCITY_THRESHOLD = 0.5f;
	private static final float FILTERING_FACTOR = 0.8f;

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
		xorient_tv = (TextView) findViewById(R.id.accel_x_value);
		yorient_tv = (TextView) findViewById(R.id.accel_y_value);
		zorient_tv = (TextView) findViewById(R.id.accel_z_value);
		xorientv_tv = (TextView) findViewById(R.id.linaccel_x_value);
		yorientv_tv = (TextView) findViewById(R.id.linaccel_y_value);
		zorientv_tv = (TextView) findViewById(R.id.linaccel_z_value);

		// Capture orientation related view elements
		orientXValue = (TextView) findViewById(R.id.orient_x_value);
		orientYValue = (TextView) findViewById(R.id.orient_y_value);
		orientZValue = (TextView) findViewById(R.id.orient_z_value);

		// Initialize accelerometer related view elements
		xorient_tv.setText("0.00");
		yorient_tv.setText("0.00");
		zorient_tv.setText("0.00");
		xorientv_tv.setText("0.00");
		yorientv_tv.setText("0.00");
		zorientv_tv.setText("0.00");

		// Initialize orientation related view elements
		orientXValue.setText("0.00");
		orientYValue.setText("0.00");
		orientZValue.setText("0.00");
	}

	// This method will update the UI on new sensor events
	public void onSensorChanged(SensorEvent sensorEvent) {
		synchronized (this) {
			long curTime = System.currentTimeMillis();
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				xorient_tv.setText(Float.toString(sensorEvent.values[0]));
				yorient_tv.setText(Float.toString(sensorEvent.values[1]));
				zorient_tv.setText(Float.toString(sensorEvent.values[2]));

				// only allow one update every 100ms.
				if ((curTime - lastUpdate) > 100) {
					long diffTime = (curTime - lastUpdate);
					lastUpdate = curTime;

					// CURRENT VALUES
					x = sensorEvent.values[0];
					y = sensorEvent.values[1];
					z = sensorEvent.values[2];

					// EXPONENTIAL AVERAGING
					last_x = (x * FILTERING_FACTOR)
							+ (last_x * (1.0f - FILTERING_FACTOR));
					last_y = (y * FILTERING_FACTOR)
							+ (last_y * (1.0f - FILTERING_FACTOR));
					last_z = (z * FILTERING_FACTOR)
							+ (last_z * (1.0f - FILTERING_FACTOR));

					float xspeed = (x - last_x);
					float yspeed = (y - last_y);
					float zspeed = (z - last_z);

					// current position
					xorient_tv.setText(Float.toString(x));
					yorient_tv.setText(Float.toString(y));
					zorient_tv.setText(Float.toString(z));

					// incremental velocity
					xorientv_tv.setText(Float.toString(xspeed));
					yorientv_tv.setText(Float.toString(yspeed));
					zorientv_tv.setText(Float.toString(zspeed));

					boolean changes = false;

					// averaged positions
					if (xspeed > VELOCITY_THRESHOLD
							|| xspeed < -VELOCITY_THRESHOLD) {
						orientXValue.setText(Float.toString(last_x));
						changes = true;
					}
					if (yspeed > VELOCITY_THRESHOLD
							|| yspeed < -VELOCITY_THRESHOLD) {
						orientYValue.setText(Float.toString(last_y));
						changes = true;
					}
					if (zspeed > VELOCITY_THRESHOLD
							|| zspeed < -VELOCITY_THRESHOLD) {
						orientZValue.setText(Float.toString(last_z));
						changes = true;
					}

					if (changes) {
						DataSender.SendDimension(Float.toString(last_x), Float
								.toString(last_y), Float.toString(last_z));
					}

				}
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
		sensorManager.registerListener(this, sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, sensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onStop() {
		// Unregister the listener
		sensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	protected void onPause() {
		// Unregister the listener
		sensorManager.unregisterListener(this);
		super.onPause();
	}

}