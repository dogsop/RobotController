package net.smellydog.robotcontroller;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;

public class MainActivity extends Activity  implements Discoverer.Receiver {
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

	RobotServer robotServer;
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        //myWebView.getSettings().setPluginState(WebSettings.PluginState);
        //myWebView.getSettings().set

		webView.loadData("Scanning for robot", "text/html; charset=UTF-8", null);
        timerSetup();
        
        leftVSeekBar = (VerticalSeekBar)findViewById(R.id.leftSeekBar);
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

  
        rightVSeekBar = (VerticalSeekBar)findViewById(R.id.rightSeekBar);
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


        // DDDD
        new Discoverer((WifiManager) getSystemService(Context.WIFI_SERVICE), this).start();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	public void foundRobot(final RobotServer server) {
		runOnUiThread(new Runnable() {
			public void run() {
				if(server != null) {
					robotServer = server;
			        //url = "http://10.20.20.10:5000/?action=snapshot";
					if(robotServer.camSocketPort() > 0) {
						url = String.format("http://%s:5000/?action=snapshot", robotServer.address().getHostAddress());
				        webView.loadUrl(url);
					}
				} else {
			        //url = "http://10.20.20.10:5000/?action=snapshot";
			        //webView.loadUrl(url);
					webView.loadData("Robot not found", "text/html; charset=UTF-8", null);
				}
			}
		});
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
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Actions goes here
						if (url != null) {
							webView.loadUrl(url);
						}
					}
				});
			}
		}, 0, 2000);// refresh rate time interval (ms)

		updateSpeed = new Timer();
		updateSpeed.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Actions goes here
						if(leftMotorSpeed != currentLeftMotorSpeed) {
							Log.v(TAG, "Updating left motor speed");
							currentLeftMotorSpeed = leftMotorSpeed;
					    	Log.v(TAG, "leftMotorSpeed " + String.valueOf(currentLeftMotorSpeed));
						}
						if(rightMotorSpeed != currentRightMotorSpeed) {
							Log.v(TAG, "Updating right motor speed");
							currentRightMotorSpeed = rightMotorSpeed;
					    	Log.v(TAG, "rightMotorSpeed " + String.valueOf(currentRightMotorSpeed));
						}
					}
				});
			}
		}, 0, 250);// refresh rate time interval (ms)
	}
}
