package com.pennapps.droid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class DataSender {

	public static final String SERVERIP = "158.130.105.135"; // HARDCODED
	public static final int SERVERPORT = 14444;
	
	private static int packetNum = 1;

	public static void SendDimension(final Droid droid, final String x, final String y, final String z, final String zoom) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					InetAddress serverAddr = InetAddress.getByName(droid.getIp());

					byte[] buf = (y + "," + x + "," + z + "," + zoom ).getBytes();
					//packetNum++;
					
					DatagramSocket socket = new DatagramSocket();

					DatagramPacket packet = new DatagramPacket(buf, buf.length,
							serverAddr, SERVERPORT);

					Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

					/* Send out the packet */
					socket.send(packet);
					Log.d("UDP", "C: Sent.");
					Log.d("UDP", "C: Done.");
					droid.setConnectionStatus(true);

				} catch (UnknownHostException e) {
					droid.setConnectionStatus(false);
				} catch (SocketException e) {
					droid.setConnectionStatus(false);
				} catch (IOException e) {
					droid.setConnectionStatus(false);
				}
			}

		}).start();
	}
	
	

}
