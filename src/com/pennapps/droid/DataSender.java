package com.pennapps.droid;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;

public class DataSender {

	public static final String SERVERIP = "158.130.105.136"; // HARDCODED
	public static final int SERVERPORT = 14444;

	public static void SendDimension(final String x, final String y, final String z, final String zoom) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					InetAddress serverAddr = InetAddress.getByName(SERVERIP);

					byte[] buf = (y + "," + x + "," + z + "," + zoom ).getBytes();

					DatagramSocket socket = new DatagramSocket();

					DatagramPacket packet = new DatagramPacket(buf, buf.length,
							serverAddr, SERVERPORT);

					Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

					/* Send out the packet */
					socket.send(packet);
					Log.d("UDP", "C: Sent.");
					Log.d("UDP", "C: Done.");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
	}
	
	

}
