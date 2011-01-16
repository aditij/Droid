package com.pennapps.droid;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Droid extends Activity implements SensorEventListener,
		OnTouchListener {

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	// Accelerometer X, Y, and Z values
	private float x = 0, y = 0, z = 0;
	private float last_x = 0, last_y = 0, last_z = 0;
	private long lastUpdate = -1;

	/* Detection constants -- change to tweak performance */
	private static final long TIME_THRESHOLD = 100;
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

		// Capture orientation related view elements
		orientXValue = (TextView) findViewById(R.id.orient_x_value);
		orientYValue = (TextView) findViewById(R.id.orient_y_value);
		orientZValue = (TextView) findViewById(R.id.orient_z_value);

		// Initialize orientation related view elements
		orientXValue.setText("0.00");
		orientYValue.setText("0.00");
		orientZValue.setText("0.00");

		ImageView view1 = (ImageView) findViewById(R.id.imageView);
		view1.setOnTouchListener(this);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				break;
				/*
				 * matrix.set(savedMatrix); matrix.postTranslate(event.getX() -
				 * start.x, event.getY() - start.y);
				 */
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;

					// ****** SEND SCALE VARIABLE HERE ******
					TextView tv = (TextView) findViewById(R.id.zoom_value);
					tv.setText(Float.toString(scale));
					DataSender.SendDimension(Float.toString(last_x), Float
							.toString(last_y), Float.toString(last_z), Float
							.toString(scale));

					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	// This method will update the UI on new sensor events
	public void onSensorChanged(SensorEvent sensorEvent) {
		synchronized (this) {
			long curTime = System.currentTimeMillis();
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {

				// only allow one update every 100ms.
				if ((curTime - lastUpdate) > TIME_THRESHOLD) {
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
								.toString(last_y), Float.toString(last_z), "1");
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