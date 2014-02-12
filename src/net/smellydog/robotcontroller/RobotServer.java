package net.smellydog.robotcontroller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;


public class RobotServer {
	  private static final String TAG = "RobotServer";

	  private String mVersion;
	  private String mName;
	  private boolean mAuthRequired;
	  private InetAddress mAddr;
	  private int mTcpSocketPort = BAD_PORT;
	  private int mCamSocketPort = BAD_PORT;
	  
	  private Socket socket;
	  private BufferedWriter socketOut;
	  private BufferedReader socketIn;
	  
	  private boolean isConnected = false;
	  

	  static public final int BAD_PORT = -1;
	  
	  public RobotServer(InetAddress address, String data) {
	    mAddr = address;
	    
	    String[] stringArray = data.split(":");
	    
		for (int count = 0; count < stringArray.length; count++) {
			if (stringArray[count].startsWith("ServerSocket") == true) {
				String intString = stringArray[count].substring(13);
				try {
					mTcpSocketPort = Integer.parseInt(intString);
				    Log.d(TAG, "mTcpSocketPort = " + Integer.toString(mTcpSocketPort));
				} catch (NumberFormatException e) {
					mTcpSocketPort = BAD_PORT;
				    Log.d(TAG, "mTcpSocketPort = BAD_PORT");
				}
			} else if (stringArray[count].startsWith("CamSocket") == true) {
				String intString = stringArray[count].substring(10);
				try {
					mCamSocketPort = Integer.parseInt(intString);
				    Log.d(TAG, "mCamSocketPort = " + Integer.toString(mCamSocketPort));
				} catch (NumberFormatException e) {
					mCamSocketPort = BAD_PORT;
				    Log.d(TAG, "mCamSocketPort = BAD_PORT");
				}
			}
		}
	  }

	  public boolean openSocket() {
			if(mTcpSocketPort > 0) {
	            Log.d(TAG, "TCP Client Connecting...");
	            
	            //create a socket to make the connection with the server
	            try {
					socket = new Socket(mAddr.getHostAddress(), mTcpSocketPort);
		            Log.d(TAG, "Socket connected");
	                socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	                socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	                isConnected = true;
	                return true;
				} catch (UnknownHostException e) {
		            Log.e(TAG, e.toString());
		            return false;
				} catch (IOException e) {
		            Log.e(TAG, e.toString());
		            return false;
				}
			}
			return false;
	  }
	  
	public boolean sendSpeed(int leftMotorSpeed, int rightMotorSpeed) {
		String msg = String.format("<%d:%d>", leftMotorSpeed, rightMotorSpeed);
        Log.d(TAG, msg);
		try {
			socketOut.write(msg);
			socketOut.flush();
		} catch (IOException e) {
            Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}
	  
	public boolean isConnected() {
		return isConnected;
	}
	
	  public boolean valid() {
	    return mTcpSocketPort != BAD_PORT && mAddr != null;
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
	    return mTcpSocketPort;
	  }

	  public int camSocketPort() {
		    return mCamSocketPort;
		  }

	  public InetAddress address() {
	    return mAddr;
	  }

	  public String toString() {
	    return String.format("%s at %s:%d %s", mName, mAddr.getHostAddress(), mTcpSocketPort, valid() ? "" : "(broken?)");
	  }
}
