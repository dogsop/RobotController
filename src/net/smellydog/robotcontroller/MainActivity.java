package net.smellydog.robotcontroller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	public WebView webView;
	public Timer updateSpeed;
	public Timer autoUpdate;
	public String url = null;
	
	VerticalSeekBar leftVSeekBar;
	VerticalSeekBar rightVSeekBar;

	int leftMotorSpeed;
	int rightMotorSpeed;
	int currentLeftMotorSpeed = -1;
	int currentRightMotorSpeed = -1;

	RobotServer robotServer = null;
    
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        
        final String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #000; background-color: #fff; text-align: center; vertical-align: middle;}"
                + "</style></head>"
                + "<body>"                          
                + "<h1>Scanning for robot.</h1>" 
                + "</body></html>";			        
		Log.i(TAG, text);
		
		webView.loadData(text, "text/html", "UTF-8");

        leftVSeekBar = (VerticalSeekBar)findViewById(R.id.leftSeekBar);
        if(leftVSeekBar != null) {
            leftVSeekBar.setMax(100);
            leftVSeekBar.setProgress(50);
            leftVSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {

        		@Override
        		public void onStopTrackingTouch(VerticalSeekBar seekBar) {
        			//Log.d("Left", "Stop Touch");
        			releaseLeftSeekBar();
        		}

        		@Override
        		public void onStartTrackingTouch(VerticalSeekBar seekBar) {
        			//Log.d("Left", "Start Touch");
        		}

        		@Override
        		public void onProgressChanged(VerticalSeekBar seekBar, int progress,
        				boolean fromUser) {
        			//Log.d("Left", String.valueOf(progress));
        			updateLeftMotorSlider(progress);
        		}
        	});
        }

  
        rightVSeekBar = (VerticalSeekBar)findViewById(R.id.rightSeekBar);
        if(rightVSeekBar != null) {
            rightVSeekBar.setMax(100);
            rightVSeekBar.setProgress(50);
            rightVSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {

        		@Override
        		public void onStopTrackingTouch(VerticalSeekBar seekBar) {
        			//Log.d("Right", "Stop Touch");
        			releaseRightSeekBar();
        		}

        		@Override
        		public void onStartTrackingTouch(VerticalSeekBar seekBar) {
        			//Log.d("Right", "Start Touch");
        		}

        		@Override
        		public void onProgressChanged(VerticalSeekBar seekBar, int progress,
        				boolean fromUser) {
        			//Log.d("Right onProgressChanged", String.valueOf(progress));
        			updateRightMotorSlider(progress); 
        		}
        	});
        }

        FindRobotTask task = new FindRobotTask();      
        task.execute();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void releaseLeftSeekBar() {
        leftVSeekBar.setProgress(50);
        //updateLeftMotorSlider(0);
    }

    public void updateLeftMotorSlider(int sliderPosition) {
    	int newPosition = sliderPosition - 50;
    	
    	double newSpeedDouble = ((double)newPosition/50.0)*128.0;
    	int newSpeed = (int)newSpeedDouble;
    	setLeftMotorSpeed(newSpeed);
    }
    
    public void setLeftMotorSpeed(int newSpeed) {
    	//Log.d("setLeftMotorSpeed", "leftMotorSpeed " + String.valueOf(newSpeed));
		leftMotorSpeed = newSpeed;
    }
    
    public void releaseRightSeekBar() {
        rightVSeekBar.setProgress(50);
        //updateRightMotorSlider(0);
    }

    public void updateRightMotorSlider(int sliderPosition) {
    	int newPosition = sliderPosition - 50;
    	
    	double newSpeedDouble = ((double)newPosition/50.0)*128.0;
    	int newSpeed = (int)newSpeedDouble;
    	//Log.d("updateRightMotorSlider", "newSpeed " + String.valueOf(newSpeed));
    	setRightMotorSpeed(newSpeed);
    }
    
    public void setRightMotorSpeed(int newSpeed) {
		//Log.d("setRightMotorSpeed", "rightMotorSpeed " + String.valueOf(newSpeed));
		rightMotorSpeed = newSpeed;
    }
    
	// refresh timer//////////////-----------------
	public void timerSetup() {
		updateSpeed = new Timer();
		updateSpeed.schedule(new TimerTask() {
			@Override
			public void run() {
				//Log.d(TAG, "timer.run");
				if(robotServer != null) {
					boolean updateRobot = false;
					// Actions goes here
					if(leftMotorSpeed != currentLeftMotorSpeed) {
						Log.v(TAG, "Updating left motor speed");
						currentLeftMotorSpeed = leftMotorSpeed;
						updateRobot = true;
				    	Log.v(TAG, "leftMotorSpeed " + String.valueOf(currentLeftMotorSpeed));
					}
					if(rightMotorSpeed != currentRightMotorSpeed) {
						Log.v(TAG, "Updating right motor speed");
						currentRightMotorSpeed = rightMotorSpeed;
						updateRobot = true;
				    	Log.v(TAG, "rightMotorSpeed " + String.valueOf(currentRightMotorSpeed));
					}
					if(updateRobot == true) {
						robotServer.sendSpeed(currentLeftMotorSpeed, currentRightMotorSpeed);
					}
				} else {
					Log.d(TAG, "robotServer == null");
				}
			}
		}, 250, 250);// refresh rate time interval (ms)
	}
	
	private class FindRobotTask extends AsyncTask<Void, String, Boolean> {
		private static final String TAG = "FindRobotTask";

		String iNetAddressString = null;
		int controlPort = -1;
		int webPort = -1;

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "doInBackground called");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e(TAG, "Error", e);
			}
			
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("https://gateway.smellydog.net/robot");
			HttpResponse response;
			String resultString = null;
			try {
				Log.d(TAG, "reading https://gateway.smellydog.net/robot");
				response = client.execute(request);

				// CONVERT RESPONSE TO STRING
				resultString = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "resultString - " + resultString);

				JSONObject jObject = null;
				try {
					jObject = new JSONObject(resultString);
					Log.d(TAG, "converted to jObject");
				} catch (JSONException e) {
					Log.e(TAG, "Error", e);
					return false;
				}

				try {
					Log.d(TAG, "reading fields");
					iNetAddressString = jObject.getString("robotIpAddress");
					controlPort = jObject.getInt("robotControlPort");
					webPort = jObject.getInt("robotWebPort");
				} catch (JSONException e) {
					Log.e(TAG, "Error", e);
					return false;
				}

			} catch (ClientProtocolException e1) {
				Log.e(TAG, "Error", e1);
				return false;
			} catch (IOException e1) {
				Log.e(TAG, "Error", e1);
				return false;
			}
			Log.i(TAG, "returning true");
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG, "onPostExecute called");
			if (result == true) {
				InetAddress inetAddress;
				try {
					inetAddress = InetAddress.getByName(iNetAddressString);
				} catch (UnknownHostException e) {
					Log.e(TAG, "Error", e);
					return;
				}
				robotServer = new RobotServer(inetAddress, webPort, controlPort);
				if (robotServer.camSocketPort() > 0) {
					url = String.format("http://%s:%d/javascript_simple.html",
							robotServer.address().getHostAddress(),
							robotServer.camSocketPort());
					
					Log.d(TAG, "loading robot webView - " + url);
					webView.loadUrl(url);
				}
				Log.d(TAG, "sending Connected");
				Runnable runnable = new Runnable() {
					public void run() {
						robotServer.sendDisplayMsg("Connected");
					}
				};
				Thread mythread = new Thread(runnable);
				mythread.start();

				Log.d(TAG, "starting timer");
				timerSetup();
			} else {
				String text = "<html><head>" 
						+ "<style type=\"text/css\">body{color: #000; background-color: #fff; text-align: center; vertical-align: middle;}"
						+ "</style></head>" + "<body>" + "Sync server not found."
						+ "</body></html>";
				Log.i(TAG, text);
				webView.loadData(text, "text/html", "UTF-8");
				webView.reload();
			}
		}

	}
	
}
