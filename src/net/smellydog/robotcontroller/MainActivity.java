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
	public WebView webView;
	public Timer autoUpdate;
	public String url = null;
	
	VerticalSeekBar leftVSeekBar;
	VerticalSeekBar rightVSeekBar;

	int leftMotorSpeed;
	int rightMotorSpeed;

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

        //myWebView.loadUrl("http://10.20.20.6:5000/?action=stream");
        timerSetup();
        
        leftVSeekBar = (VerticalSeekBar)findViewById(R.id.leftSeekBar);
        leftVSeekBar.setMax(100);
        leftVSeekBar.setProgress(50);
        leftVSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {

    		@Override
    		public void onStopTrackingTouch(VerticalSeekBar seekBar) {
    			Log.v("Left", "Stop Touch");
    			releaseLeftSeekBar();
    		}

    		@Override
    		public void onStartTrackingTouch(VerticalSeekBar seekBar) {
    			Log.v("Left", "Start Touch");
    		}

    		@Override
    		public void onProgressChanged(VerticalSeekBar seekBar, int progress,
    				boolean fromUser) {
    			Log.v("Left", String.valueOf(progress));
    			updateLeftMotorSlider(progress);
    		}
    	});

  
        rightVSeekBar = (VerticalSeekBar)findViewById(R.id.rightSeekBar);
        rightVSeekBar.setMax(100);
        rightVSeekBar.setProgress(50);

        //
        new Discoverer((WifiManager) getSystemService(Context.WIFI_SERVICE), this).start();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	public void addAnnouncedServers(final RobotServer server) {
		runOnUiThread(new Runnable() {
			public void run() {
				if(server != null) {
					robotServer = server;
			        //url = "http://10.20.20.10:5000/?action=snapshot";
					url = String.format("http://%s:5000/?action=snapshot", robotServer.address().getHostAddress());
			        webView.loadUrl(url);
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
        updateLeftMotorSlider(0);
    }

    public void updateLeftMotorSlider(int sliderPosition) {
    	int newPosition = sliderPosition - 50;
    	
    	double newSpeedDouble = ((double)newPosition/50.0)*128.0;
    	int newSpeed = (int)newSpeedDouble;
    	setLeftMotorSpeed(newSpeed);
    }
    
    public void setLeftMotorSpeed(int newSpeed) {
		Log.v("Left", "Motor " + String.valueOf(newSpeed));
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
		}, 0, 1000);// refresh rate time interval (ms)
	}
}
