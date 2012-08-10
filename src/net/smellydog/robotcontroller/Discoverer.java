package net.smellydog.robotcontroller;

import java.io.IOException;
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
    try {
      DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
      socket.setBroadcast(true);
      socket.setSoTimeout(TIMEOUT_MS);

      sendDiscoveryRequest(socket);
      server = listenForResponses(socket);
      socket.close();
    } catch (IOException e) {
      Log.e(TAG, "Could not send discovery request", e);
    }
    mReceiver.foundRobot(server);
  }

  /**
   * Send a broadcast UDP packet containing a request for boxee services to
   * announce themselves.
   * 
   * @throws IOException
   */
  private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
    String data = "RobotController";
    Log.d(TAG, "Sending data " + data);

    DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
        getBroadcastAddress(), DISCOVERY_PORT);
    socket.send(packet);
  }

  /**
   * Calculate the broadcast IP we need to send the packet along. If we send it
   * to 255.255.255.255, it never gets sent. I guess this has something to do
   * with the mobile network not wanting to do broadcast.
   */
  private InetAddress getBroadcastAddress() throws IOException {
    byte[] quads = new byte[4];
    
    DhcpInfo dhcp = mWifi.getDhcpInfo();
    if (dhcp == null) {
      Log.d(TAG, "Could not get dhcp info");
      return null;
    }

    for (int k = 0; k < 4; k++)
        quads[k] = (byte) ((dhcp.ipAddress >> k * 8) & 0xFF);
    
    localAddress = InetAddress.getByAddress(quads);
    
    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
    
    return InetAddress.getByAddress(quads);
  }

  /**
   * Listen on socket for responses, timing out after TIMEOUT_MS
   * 
   * @param socket
   *          socket on which the announcement request was sent
   * @return list of discovered servers, never null
   * @throws IOException
   */
  private RobotServer listenForResponses(DatagramSocket socket)
      throws IOException {
    long start = System.currentTimeMillis();
    byte[] buf = new byte[1024];

    // Loop and try to receive responses until the timeout elapses. We'll get
    // back the packet we just sent out, which isn't terribly helpful, but we'll
    // discard it in parseResponse because the cmd is wrong.
    try {
      while (true) {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String s = new String(packet.getData(), 0, packet.getLength());
        Log.d(TAG, "Packet received after "
            + (System.currentTimeMillis() - start) + " " + s);
        InetAddress address = ((InetSocketAddress) packet.getSocketAddress()).getAddress();
        if(address.toString().contentEquals(localAddress.toString()) == false) {
            RobotServer server = new RobotServer(address, s);
            if (server != null) {
                return server;
            }
        }
      }
    } catch (SocketTimeoutException e) {
      Log.d(TAG, "Receive timed out");
    }
    return null;
  }

  public static void main(String[] args) {
    new Discoverer(null, null).start();
    while (true) {
    }
  }
}
