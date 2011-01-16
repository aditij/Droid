package com.pennapps.droid;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Connect extends Activity {
	
	private String ipval;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        
        ButtonAction testListener = new ButtonAction();
        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(testListener);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ipval = "";
        
        /* DEFAULT VALUE FOR TESTING */
        EditText editText = (EditText)findViewById(R.id.entry);
        editText.setText("158.130.105.135");
	}
	
	public void connect(){
		EditText editText = (EditText)findViewById(R.id.entry);
		ipval = editText.getText().toString();
		
		Intent i = new Intent(this, Droid.class);
		i.putExtra("ip", ipval);
        startActivity(i);
	}
	
	private class ButtonAction implements Button.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			connect();
		}
		
	}
}
