package net.smellydog.robotcontroller;

import java.net.InetAddress;
import java.util.HashMap;


public class RobotServer {
	  private String mVersion;
	  private String mName;
	  private boolean mAuthRequired;
	  private int mPort;
	  private InetAddress mAddr;

	  static public final int BAD_PORT = -1;
	  
	  public RobotServer(InetAddress address) {
	    mAddr = address;
	    mPort = 112233;
//	    mVersion = attributes.get("version");
//	    mName = attributes.get("name");
//	    try {
//	      mPort = Integer.parseInt(attributes.get("httpPort"));
//	    } catch (NumberFormatException e) {
//	      mPort = BAD_PORT;
//	    }
//
//	    String auth = attributes.get("httpAuthRequired");
//	    mAuthRequired = auth != null && auth.equals("true");
	  }

	  public boolean valid() {
	    return mPort != BAD_PORT && mAddr != null;
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

	  public int port() {
	    return mPort;
	  }

	  public InetAddress address() {
	    return mAddr;
	  }

	  public String toString() {
	    return String.format("%s at %s:%d %s", mName, mAddr.getHostAddress(), mPort, valid() ? "" : "(broken?)");
	  }
}
