package net.smellydog.robotcontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Code for dealing with Boxee server discovery. 
 * This class tries to send a broadcast UDP packet over your wifi network to
 * discover the boxee service.
 */

public class Discoverer extends Thread {
  private static final String TAG = "Discovery";
  private static final String REMOTE_KEY = "b0xeeRem0tE!";
  private static final int DISCOVERY_PORT = 12233;
  private static final int TIMEOUT_MS = 5000;
  private Receiver mReceiver;

  private InetAddress localAddress;
  
  // TODO: Vary the challenge, or it's not much of a challenge :)
  private static final String mChallenge = "myvoice";
  private WifiManager mWifi;

  interface Receiver {
    /**
     * Process the list of discovered servers. This is always called once after
     * a short timeout.
     * 
     * @param servers
     *          list of discovered servers, null on error
     */
    void foundRobot(RobotServer server);
  }

  Discoverer(WifiManager wifi, Receiver receiver) {
    mWifi = wifi;
    mReceiver = receiver;
  }

	public void run() {
		RobotServer server = null;
		
	    HttpClient client = new DefaultHttpClient();
	    HttpGet request = new HttpGet("https://gateway.smellydog.net/robot");
	    HttpResponse response;
	    String resultString = null;
	    try {
	        response = client.execute(request);
	        
	        // CONVERT RESPONSE TO STRING
            resultString = EntityUtils.toString(response.getEntity());
	        
            JSONObject jObject = null;
            try {
            jObject = new JSONObject(resultString);
            } catch (JSONException e) {
             e.printStackTrace();
             return;
            }      
            
            String iNetAddressString = null;
            int controlPort = -1;
            int webPort = -1;
            
			try {
				iNetAddressString = jObject.getString("robotIpAddress");
	            controlPort = jObject.getInt("robotControlPort");
	            webPort = jObject.getInt("robotWebPort");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            InetAddress inetAddress = InetAddress.getByName(iNetAddressString);
            server = new RobotServer(inetAddress, webPort, controlPort);
	    } catch (ClientProtocolException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
	    } catch (IOException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
	    }
	    
		if (server != null) {
			Log.i(TAG, "returning server");
			// if(server.openSocket() == true) {
			// mReceiver.foundRobot(server);
			// }
			
		} else {
			Log.i(TAG, "server == null");
		}
		mReceiver.foundRobot(server);
	}

  public static void main(String[] args) {
    new Discoverer(null, null).start();
    while (true) {
    }
  }
}
