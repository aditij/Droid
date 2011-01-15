package com.pennapps.droid;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Droid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //new Thread(new ClientRunnable()).start();
    }
    
    private class ClientRunnable implements Runnable {
    	public static final String SERVERIP = "165.123.207.229"; // HARDCODED
    	public static final int SERVERPORT = 14444;
    	
            @Override
            public void run() {
                    try {
                    	TextView tv = (TextView) findViewById(R.id.text_box);
                    	tv.setText("C: Connecting...");

                    	// Retrieve the ServerName
                    	System.out.println("sdfsdf");
                    	InetAddress serverAddr = InetAddress.getByName(SERVERIP);

                    	Log.d("UDP", "C: Connecting...");
                    	/* Create new UDP-Socket */
                    	DatagramSocket socket = new DatagramSocket();

                    	/* Prepare some data to be sent. */
                    	byte[] buf = ("Hello from Client").getBytes();

                    	/* Create UDP-packet with
                    	 * data & destination(url+port) */
                    	DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, SERVERPORT);
                    	Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

                    	/* Send out the packet */
                    	socket.send(packet);
                    	Log.d("UDP", "C: Sent.");
                        Log.d("UDP", "C: Done.");

                    	//TextView tv = (TextView) findViewById(R.id.text_box);
                    	tv.setText("C: Done.");

                    } catch (Exception e) {
                    	Log.e("UDP", "C: Error", e);
                    	TextView tv = (TextView) findViewById(R.id.text_box);
                    	tv.setText("C: Error");
                    }
            }
    }
}