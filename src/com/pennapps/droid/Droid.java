package com.pennapps.droid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

public class Droid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TextView tv = (TextView) findViewById(R.id.text_box);
        String currentIP = getLocalIpAddress();
        tv.setText("Current IP is: " + currentIP);
        
        new Thread(new MediaBroadcaster()).start();
    }
    
    private class MediaBroadcaster implements Runnable {
    	public static final String SERVERIP = "165.123.207.229"; // HARDCODED
    	public static final int SERVERPORT = 14444;
    	
		@Override
		public void run() {
			try {
				InetAddress addr = InetAddress.getByName(getLocalIpAddress());
				Log.d("MediaBroadcaster", "S: Prepping Media Broadcaster..." + addr.toString());
				
				TextView tv = (TextView) findViewById(R.id.text_box);
				tv.setText("Current IP is: " + addr.toString());
				
				Socket socket = new Socket(addr, SERVERPORT);
				ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
				
				MediaRecorder recorder = new MediaRecorder();
				/*
				recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
				*/
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP/*MPEG_4*/);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				
				recorder.setOutputFile(pfd.getFileDescriptor());
				recorder.prepare();
				recorder.start();
				
				Log.d("MediaBroadcaster", "S: Streaming...");
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
    }
    
    public String getLocalIpAddress() {
    	try {
    		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
    			NetworkInterface intf = en.nextElement();
    			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ){
    				InetAddress inetAddress = enumIpAddr.nextElement();
    				if (!inetAddress.isLoopbackAddress()) {
    					return inetAddress.getHostAddress().toString();
    				}
    			}
    		}
    	} catch (SocketException ex) {
    		Log.e("IP", ex.toString());
    	}
    	return null;
    }
    
    public String getLocalIP(){
    	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);    	
    	//WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	DhcpInfo dhcpInfo = wifiManager.getDhcpInfo(); 

    	Log.d("IP", "dhcpInfo IP (int) is " + dhcpInfo.ipAddress);
    	Log.d("IP", "dhcpInfo IP (string) is " + intToIp(dhcpInfo.ipAddress));
    	
    	return intToIp(dhcpInfo.ipAddress);
    }
    
    public static String intToIp(int i) {
        return ((i >> 24 ) & 0xFF) + "." +
               ((i >> 16 ) & 0xFF) + "." +
               ((i >>  8 ) & 0xFF) + "." +
               ( i        & 0xFF);
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