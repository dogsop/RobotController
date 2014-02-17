package net.smellydog.robotcontroller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class RobotServer {
	  private static final String TAG = "RobotServer";

	  private String mVersion;
	  private String mName;
	  private boolean mAuthRequired;
	  private InetAddress mAddr;
	  private int mUdpCommandPort = BAD_PORT;
	  private int mCamSocketPort = BAD_PORT;
	  
	  private DatagramSocket socket = null;


	  static public final int BAD_PORT = -1;
	  
	  public RobotServer(InetAddress address, int camPort, int commandPort) {
	    mAddr = address;
	    mUdpCommandPort = commandPort;
	    mCamSocketPort = camPort;
	    
        try {
			socket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
	  }

	public boolean sendDisplayMsg(String displayMsg) {

		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "displayMsg");
			object.put("msg", displayMsg);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String msg = object.toString();
		return sendString(msg);
	}
		
	public boolean sendSpeed(int leftMotorSpeed, int rightMotorSpeed) {

		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "setSpeed");
			object.put("leftSpeed", leftMotorSpeed);
			object.put("rightSpeed", rightMotorSpeed);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String msg = object.toString();
		return sendString(msg);
	}	  
	
	public boolean sendString(String msg) {

		Log.d(TAG, msg);

		byte[] sendBuffer = msg.getBytes();

		if (socket != null) {
			DatagramPacket packet = new DatagramPacket(sendBuffer,
					sendBuffer.length, mAddr, mUdpCommandPort);
			// packet = new DatagramPacket( sendBuffer, sendBuffer.length,
			// address, port );

			try {
				socket.send(packet);
			} catch (IOException ioe) {
				Log.d("NETWORK",
						"Failed to send UDP packet due to IOException: "
								+ ioe.getMessage());
				ioe.printStackTrace();
				return false;
			} catch (Exception e) {
				Log.d("NETWORK", "Failed to send UDP packet due to Exeption: "
						+ e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}	  
	

	public boolean valid() {
	    return mUdpCommandPort != BAD_PORT && mAddr != null;
	  }

	  public String version() {
	    return mVersion;
	  }

	  public String name() {
	    return mName;
	  }

	  public boolean authRequired() {
	    return mAuthRequired;
	  }

	  public int tcpSocketPort() {
	    return mUdpCommandPort;
	  }

	  public int camSocketPort() {
		    return mCamSocketPort;
		  }

	  public InetAddress address() {
	    return mAddr;
	  }

	  public String toString() {
	    return String.format("%s at %s:%d %s", mName, mAddr.getHostAddress(), mUdpCommandPort, valid() ? "" : "(broken?)");
	  }
}
