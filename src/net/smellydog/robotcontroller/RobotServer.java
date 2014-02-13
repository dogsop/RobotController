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
import java.net.UnknownHostException;

import android.util.Log;


public class RobotServer {
	  private static final String TAG = "RobotServer";

	  private String mVersion;
	  private String mName;
	  private boolean mAuthRequired;
	  private InetAddress mAddr;
	  private int mUdpCommandPort = BAD_PORT;
	  private int mCamSocketPort = BAD_PORT;
	  
	  private DatagramSocket socket;
	  private BufferedWriter socketOut;
	  private BufferedReader socketIn;
	  

	  static public final int BAD_PORT = -1;
	  
	  public RobotServer(InetAddress address, int camPort, int commandPort) {
	    mAddr = address;
	    mUdpCommandPort = commandPort;
	    mCamSocketPort = camPort;
	  }

	public boolean sendSpeed(int leftMotorSpeed, int rightMotorSpeed) {
		
		String msg = String.format("S:%d:%d>", leftMotorSpeed, rightMotorSpeed);
        Log.d(TAG, msg);
        
        byte[] sendBuffer = msg.getBytes();

        DatagramPacket packet = new DatagramPacket( sendBuffer, sendBuffer.length, mAddr, mUdpCommandPort );
        //packet = new DatagramPacket( sendBuffer, sendBuffer.length, address, port );

        try 
        {
            socket.send( packet );
        } 
        catch (IOException ioe) 
        {
            Log.d( "NETWORK", "Failed to send UDP packet due to IOException: " + ioe.getMessage() );
            ioe.printStackTrace();
    		return false;
        }
        catch( Exception e )
        {
            Log.d( "NETWORK", "Failed to send UDP packet due to Exeption: " + e.getMessage() );
            e.printStackTrace();
    		return false;
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
