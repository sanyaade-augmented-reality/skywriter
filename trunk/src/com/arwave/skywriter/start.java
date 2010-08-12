package com.arwave.skywriter;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TabHost.OnTabChangeListener;

import com.google.android.maps.MapActivity;
import com.threed.jpct.Matrix;
import com.threed.jpct.SimpleVector;

/**
 * A simple demo. This shows more how to use jPCT-AE than it shows how to write
 * a proper application for Android, because i have no idea how to do this. This
 * thing is more or less a hack to get you started...
 * 
 * @author EgonOlsen
 * 
 */
public class start extends MapActivity implements SensorEventListener,
		LocationListener, OnTabChangeListener {

	private static final int OPEN_WAVE_ID = 0;
	private static final int ADD_ARBLIP_ID = 1;
	private static AbstractCommunicationManager acm;

	// screen views
	private ARBlipView arView;
	CameraView cameraView;
	
	FrameLayout arBackground;
	
	FrameLayout arPage;
	static StaticMapFetcher mapView;
	boolean mapModeIsOn = false;

	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag, sensorOri;

	private boolean paused = false;

	private float xpos = -1;
	private float ypos = -1;

	//location handeling
	private boolean OriginalLocationSet = false;
	public Location currentLocation;
	
	
	// Camera Orientation Related
	static SensorHelper sensorfunctions = new SensorHelper();
	SimpleVector currentCameraVector = new SimpleVector(0, 0, 0);
	float[] ori = new float[3];
	float accels[] = new float[3];
	float mags[] = new float[3];
	float orients[] = new float[3];
	float values[] = new float[3];
	Matrix tempR = new Matrix();
	 private float mAzimuth;
	    private float[] mGravs = new float[3];
	    private float[] mGeoMags = new float[3];
	    private float[] mOrientation = new float[3];
	    private float[] mRotationM = new float[9];               
	    // Use [16]  to co-operate with android.opengl.Matrix
	    private float[] mRemapedRotationM = new float[9];
	    private boolean mFailed;
	
	
	DigitalAverage[] filter = { new DigitalAverage(), new DigitalAverage(),
			new DigitalAverage(), new DigitalAverage(), new DigitalAverage(),
			new DigitalAverage() };

	// Menu items
	private static final int MENU_TOGGLE_MAP = 1;
	private static final int MENU_BLITSENSOR = 2;
	private static final int MENU_ADDTEST3DS = 3;
	
	// Matrix tempR = new Matrix();
	float RTmp[] = new float[9];
	float Rt[] = new float[9];
	float outR[] = new float[9];
	float I[] = new float[9];
	private boolean isReady;

	TabHost tabHost;

	private ArrayAdapter<String> usersWaveListAdapter;
	private List<String> usersWavesList;
	private WaveListView waveListViewBox;
	
	
	private LocationManager lm;
	private LocationListener locListener;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources res = getResources();
		setContentView(R.layout.main);
		
		
		// load xml files for other pages
		// LayoutInflater factory = LayoutInflater.from(this);
		// final View loginpage = factory.inflate(R.layout.loginpage, null);
		// final LinearLayout LoginPage2 =
		// (LinearLayout)loginpage.findViewById(R.id.MainLoginPage);

		// SET UP TABS
		//tabHost = getTabHost(); // The activity TabHost
		//tabHost = new TabHost(this);		
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
			
		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("LoginTab").setIndicator("Login",
				res.getDrawable(R.drawable.eye)).setContent(R.id.MainLoginPage);
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("WavesTab").setIndicator("Waves",
				res.getDrawable(R.drawable.ship)).setContent(R.id.WavePage);
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("WorldTab").setIndicator("World",
				res.getDrawable(R.drawable.eye)).setContent(R.id.ARViewPage);

		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("AddBlipTab").setIndicator("AddBlip",
				res.getDrawable(R.drawable.eye)).setContent(
				R.id.add_arblip_layout);

		tabHost.addTab(spec);

		// size height
		// tabHost.getTabWidget().getLayoutParams().height=60;

		tabHost.setCurrentTab(0);

		tabHost.setOnTabChangedListener(this);
		
		// set up camera view and ar overlay
		arPage = (FrameLayout) findViewById(R.id.ARViewPage);

		
		
		cameraView = new CameraView(this);		
		arView = new ARBlipView(getApplication());

		mapView = new StaticMapFetcher(this,"0Dp54Hi6UDERButbqe8rGJ5LDYZdpHi_dAGsDGQ");
	//	mapView.setBackgroundColor(Color.GREEN);
		
	    // asign to ar page
		arPage.addView(cameraView);
		//arPage.addView(mapView,256,256);
		arPage.addView(arView, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		//map tests
		//LinearLayout blipPage = (LinearLayout) findViewById(R.id.add_arblip_layout);		
		
		// ,
		//        	
		// listen for gps
		Log.d("setting", "location checker");
		
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		useLocation(loc);
		
		locListener = new LocListener();				
		
		
//		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, locListener);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
		
	
		
		
		

		// initialize the communication manager
		// TODO: other choices will be available in the future
		acm = new FedOneCommunicationManager(this);
		// prompt for a login
		Button button = (Button) findViewById(R.id.LoginButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// try to log the user in
				EditText username = (EditText) findViewById(R.id.EditText02);
				EditText serverAddress = (EditText) findViewById(R.id.EditText03);
				// EditText serverPort =
				// (EditText)findViewById(R.id.serverPortEdit);
				acm.login(serverAddress.getText().toString(), 9876, username
						.getText().toString(), new String(""));
				tabHost.setCurrentTab(1);
			}

		});
		// if the user doesn't login we only display the already cached data?

		// setup the page with the wave list
		FrameLayout wavesListPage = (FrameLayout) findViewById(R.id.WavePage);

		waveListViewBox = new WaveListView(this);

		// wavesListPage.removeAllViews();
		wavesListPage.addView(waveListViewBox);

		// add default contents and set adapter
		usersWavesList = new ArrayList<String>();
		// usersWavesList.add("wave list not updated");
		usersWaveListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, usersWavesList);
		waveListViewBox.setAdapter(usersWaveListAdapter);

		/*
		 * /add a listener for user selection
		 * waveListViewBox.setOnItemClickListener(new OnItemClickListener() {
		 * public void onItemClick(AdapterView<?> parent, View view, int
		 * position, long id) { // When clicked, show a toast with the TextView
		 * text //acm.openWavelet( ((TextView)view).getText().toString() );
		 * Toast.makeText(getApplicationContext(), acm.getBlips(
		 * ((TextView)view).getText().toString() ), Toast.LENGTH_LONG).show(); }
		 * });
		 */

		// -----------------------------------------------------------
		// for adding blips
		Button cancelButton = (Button) findViewById(R.id.cancelButton);

		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//					
			}
		});

		Button addConfirmBlipButton = (Button) findViewById(R.id.cancelButton);
		addConfirmBlipButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//					
			}
		});

		// add a context menu to the list of waves
		registerForContextMenu(waveListViewBox);
	}

	/**
	 * public void loginUser(String name, String password, String server){
	 * 
	 * //login //ARBlipInterface.login(name,password,server)
	 * 
	 * //assign listeners
	 * 
	 * 
	 * }
	 */

	
	private class LocListener implements LocationListener{
        
		
            public void onLocationChanged(Location location)
                {;
                
//                		Log.d("provider", "Provider is on");
                        if (location != null)
                        {                       	
                               useLocation(location);
                        } 
                }

			
                public void  onProviderDisabled(String provider)
                {
                }

                public void  onProviderEnabled(String provider)
                {
                }

                public void  onStatusChanged(String provider, 
int status, Bundle extras)
                {
                }	
		
	};
	
	
	
	@Override
	protected void onPause() {
		paused = true;
		super.onPause();
		arView.onPause();
		arView.worldReadyToGo = false;

		if (sensorMgr != null) {
			sensorMgr.unregisterListener(this, sensorGrav);
			sensorMgr.unregisterListener(this, sensorMag);
			sensorMgr = null;
		}
		
		lm.removeUpdates(locListener);
		
		
		
		

	}

	@Override
	protected void onResume() {
		paused = false;
		super.onResume();
		arView.onResume();

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

		sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensorGrav = sensors.get(0);
		}

		sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors.size() > 0) {
			sensorMag = sensors.get(0);
		}

		sensors = sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) {
			sensorOri = sensors.get(0);
		}

		Log.i("skywritter", "listerners set");

		sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_FASTEST);
		sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_FASTEST);

		sensorMgr.registerListener(this, sensorOri, SENSOR_DELAY_FASTEST);
		
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);

	}

	@Override
	protected void onStop() {
		super.onStop();
		
		arView.renderer.stop();
		arView.worldReadyToGo = false;
		lm.removeUpdates(locListener);
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			xpos = -1;
			ypos = -1;
			arView.touchTurn = 0;
			arView.touchTurnUp = 0;
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {
			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;

			xpos = me.getX();
			ypos = me.getY();

			arView.touchTurn = xd / 100f;
			arView.touchTurnUp = yd / 100f;
			return true;
		}
		return super.onTouchEvent(me);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

			Log.e("adding", "blip");
			// arView.creatingSpinningCube(); //my spinning cube function!
			this.addTestBlip();

			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			arView.move = 2;
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			arView.move = -2;
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			arView.turn = 0.05f;
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			arView.turn = -0.05f;
			return true;
		}

		return super.onKeyDown(keyCode, msg);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			arView.move = 0;
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			arView.move = 0;
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			arView.turn = 0;
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			arView.turn = 0;
			return true;
		}

		return super.onKeyUp(keyCode, msg);
	}

	public void addTestBlip() {

		ARBlip testblip = new ARBlip();
		testblip.x = (Math.random() * 500);
		testblip.y = (Math.random() * 500);
		testblip.z = -100;
		testblip.BlipID = "TestBlip";
		testblip.ObjectData = "Test-" + (int) (Math.random() * 10);

		try {
			arView.addBlip(testblip);
		} catch (IOException e) {
			// addBlip can cause an error if it has a malformed url, or other problem loading a remote 3d file			
			e.printStackTrace();
		}

	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	// ================================================================================================================
	private class DigitalAverage {

		final int history_len = 4;
		double[] mLocHistory = new double[history_len];
		int mLocPos = 0;

		// ------------------------------------------------------------------------------------------------------------
		int average(double d) {
			float avg = 0;

			mLocHistory[mLocPos] = d;

			mLocPos++;
			if (mLocPos > mLocHistory.length - 1) {
				mLocPos = 0;
			}
			for (double h : mLocHistory) {
				avg += h;
			}
			avg /= mLocHistory.length;

			return (int) avg;
		}
	}

	
	
	class CameraView extends SurfaceView implements SurfaceHolder.Callback {

		// ARExplorer app;
		SurfaceHolder holder;
		private android.hardware.Camera camera;

		CameraView(Context context) {
			super(context);

			try {
				// app = (ARExplorer) context;

				holder = getHolder();
				holder.addCallback(this);
				holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			} catch (Exception ex) {

			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (camera != null) {
					try {
						camera.stopPreview();
					} catch (Exception ignore) {
					}
					try {
						camera.release();
					} catch (Exception ignore) {
					}
					camera = null;
				}

				camera = android.hardware.Camera.open();
				
				//camera.setDisplayOrientation(90);
				
				camera.setPreviewDisplay(holder);
			} catch (Exception ex) {
				try {
					if (camera != null) {
						try {
							camera.stopPreview();
						} catch (Exception ignore) {
						}
						try {
							camera.release();
						} catch (Exception ignore) {
						}
						camera = null;
					}
				} catch (Exception ignore) {

				}
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			try {
				if (camera != null) {
					try {
						camera.stopPreview();
					} catch (Exception ignore) {
					}
					try {
						camera.release();
					} catch (Exception ignore) {
					}
					camera = null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			try {
				Log.e("Test", "camera set to go");

				camera.startPreview();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

		sensorfunctions.changeAccuracy(sensor, accuracy);

	}
	
	

	public void onSensorChanged(SensorEvent s_ev) {

	//	if (true) return;
		
		switch (s_ev.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(s_ev.values, 0, mGravs, 0, 3);
			break;
        case Sensor.TYPE_MAGNETIC_FIELD:
        	System.arraycopy(s_ev.values, 0, mGeoMags, 0, 3);
        	break;
        default:
        	return;
        }
        
 
        
		 
        if (SensorManager.getRotationMatrix(mRotationM, null, mGravs, mGeoMags)){
//              Rotate to the camera's line of view (Y axis along the camera's axis)
                SensorManager.remapCoordinateSystem(mRotationM, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRemapedRotationM);
                SensorManager.getOrientation(mRemapedRotationM, mOrientation);
                
                SimpleVector cameraVector = new SimpleVector();
                cameraVector.x = smoothX(mOrientation[1]);
                cameraVector.y = smoothY(mOrientation[2]);
                cameraVector.z = smoothZ(mOrientation[0]);

                
                
                arView.setCameraOrientation(cameraVector);
        }
        
     

	}



	int xNum = 0;
	int yNum = 0;
	int zNum = 0;
	
	float xArray[] = new float[10];
	float yArray[] = new float[10];
	float zArray[] = new float[10];
	
	
	private float smoothZ(float f) {
		
		zArray[zNum] = f;

		float x = 0;
		float y = 0;

		float zAvg;
		for(int i = 0; i < zArray.length; i++){
			x += Math.cos(zArray[i]);
			y += Math.sin(zArray[i]);
		}
		zAvg = (float) Math.atan2(x, y);		
		
		zNum++;
		if (zNum == zArray.length) zNum = 0;
		
		
		return -zAvg;
	}
	
	
	private float smoothY(float f) {
		yArray[yNum] = f;

		float x = 0;
		float y = 0;

		float yAvg;
		for(int i = 0; i < yArray.length; i++){
			x += Math.cos(yArray[i]);
			y += Math.sin(yArray[i]);
		}
		yAvg = (float) Math.atan2(x, y);		
		
		yNum++;
		if (yNum == yArray.length) yNum = 0;
		
		yAvg = (float) (yAvg - Math.toRadians(90));
		
		return -yAvg;
	}
	
	
	private float smoothX(float f) {
		
		xArray[xNum] = f;

		float x = 0;
		float y = 0;

		float xAvg;
		for(int i = 0; i < xArray.length; i++){
			x += Math.cos(xArray[i]);
			y += Math.sin(xArray[i]);
		}
		xAvg = (float) Math.atan2(x, y);		
		
		xNum++;
		if (xNum == xArray.length) xNum = 0;
		
		
		
		xAvg = (float) (xAvg - Math.toRadians(90));
		
		return -xAvg;
	}
	
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_TOGGLE_MAP, 0, "Toggle Map");
		menu.add(0, MENU_BLITSENSOR, 0, "Bit Sensor");
		menu.add(0, MENU_ADDTEST3DS,0,"Test3DSLoad");
		
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
					
		case MENU_ADDTEST3DS:
			

			ARBlip testblip = new ARBlip();
			//random position relative to real world current location
			testblip.x = 51.560286;
			testblip.y = 5.078049;
			testblip.z = 0;
			testblip.BlipID = "Building";
			testblip.ObjectData = "http://www.darkflame.co.uk/building.3DS";
			testblip.MIMEtype = "application/x-3ds";
			 		
			
			ARBlip testblip2 = new ARBlip();
			//random position relative to real world current location
			testblip2.x = 51.558360;
			testblip2.y = 5.077972;
			testblip2.z = 0;
			testblip2.BlipID = "PetrolStation";
			testblip2.ObjectData = "http://www.darkflame.co.uk/petrolstation.3ds";
			testblip2.MIMEtype = "application/x-3ds";
			
			try {
				arView.addBlip(testblip);
				arView.addBlip(testblip2);
				
				
			} catch (IOException e) {
				// addBlip can cause an error if it has a malformed url, or other problem loading a remote 3d file		
				Log.e("3ds", "failed to load");
				e.printStackTrace();
			}
			
		return true;
		
		case MENU_TOGGLE_MAP:

			if (arView.worldReadyToGo){
			// toggle map
			if (mapModeIsOn) {
				
				arPage.addView(cameraView,0);
				arView.setMapMode(false,null);				
				mapModeIsOn = false;
			} else {
				// turn Camera view off
				//arPage.removeView(cameraView);
				
				
				
				try {
					LocatedMapBundle currentmap =mapView.getMap(currentLocation);
					if (currentmap!=null){
					arPage.removeView(cameraView);						
					arView.setMapMode(true,currentmap);
					mapModeIsOn = true;
					}
				} catch (MalformedURLException e) {
							
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("mapE","io exception");
				}
				
			}
				
				
				
				
				
				
			}

			return true;
		case MENU_BLITSENSOR:

			// toggle the screen blitting
			arView.showDebugInfo =! arView.showDebugInfo;
			Log.i("debug","_"+arView.showDebugInfo);
			
			return true;
		}
		
		
		
		return false;
	}


		public void useLocation(Location location) {
			
			
			// only run if world is set
			Log.d("loading", "gps updated");
	
			Timer blah = new Timer();
	
			TimerTask meep = new TimerTask() {
				public void run() {
	
					// load the sample blips after the world is set up
					if ((arView.worldReadyToGo) && (OriginalLocationSet)) {
	
						this.cancel();
						Log.d("loading", "loading blips");
	
						//A list of sample markers down a street in tilburg! (please change if you wish to test more localy to you)
						
						double blipDataX[] = { 51.560071,51.559150,51.558890,51.55839,51.55759,51.559230};
						double blipDataY[] = { 5.07822,5.07792,5.07785,5.07774,5.07765,5.07974 };
	
						for (int i = 0; i < blipDataX.length;) {
	
							// we can now load up some sample blips
							ARBlip testblip1 = new ARBlip();
							testblip1.x = blipDataX[i]; // 51.558348 //51.558325
							testblip1.y = blipDataY[i];
							testblip1.z = 0;
							testblip1.BlipID = "NewTestBlip" + i;
							testblip1.ObjectData = "" + i;
							try {
								arView.addBlip(testblip1);
							} catch (IOException e) {							
								// addBlip can cause an error if it has a malformed url, or other problem loading a remote 3d file
								e.printStackTrace();
							}
							i++;
						}
	
					} else {
						Log.d("load", "not ready for blips");
					}
	
				}
			};
	
			// if its not set, then set the starting location for the arView
			if (!OriginalLocationSet) {
				arView.startingLocation = location;
				Log.i("loading", "setting location..........");
				OriginalLocationSet = true;
				blah.schedule(meep, 100, 100);
			}
			
			if ((arView.worldReadyToGo) && (OriginalLocationSet)) {
				//set current location
					Log.i("loc", "location changed");
				currentLocation = location;
				arView.currentRealLocation = currentLocation;
				arView.updateLocation(currentLocation);
			}
			currentLocation = location;
			arView.TestVar = arView.TestVar+1;
			arView.currentRealLocation = currentLocation;
		}
	


	public void noOp(String arg0, WaveletData arg1) {
		
	}

	public void onCommitNotice(WaveletData arg0, HashedVersion arg1) {
		
	}

	public void onDeltaSequenceEnd(WaveletData arg0) {
		
	}

	public void onDeltaSequenceStart(WaveletData arg0) {
		
	}

	public void participantAdded(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		
	}

	public void participantRemoved(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		
	}

	public void waveletDocumentUpdated(String arg0, WaveletData arg1,
			WaveletDocumentOperation arg2) {
		
	}

	public void addMessage(String message) {
		// TextView out = (TextView)findViewById(R.id.messages);
		// out.setText(message);
		Log.i("state", message);
		usersWaveListAdapter.add("test");
	}

	public void showWaveList(String[] list) {
		// clear the list
		usersWavesList.clear();

		// add the data to the list
		Log.i("state", "getting wave list");
		for (int i = 0; i < list.length; i++) {
			Log.i("wavelist", list[i]);
			// usersWavesList.add(i+"_"+list[i]);
			usersWavesList.add(list[i]);
		}

		// request the update to the list
		Log.i("wavelist", "posting invalidate");
		waveListViewBox.setDataUpdated();
		waveListViewBox.postInvalidate();
	}

	public void setWaveList(String[] list) {

		Log.i("state", "getting wave list");
		for (int i = 0; i < list.length; i++) {
			Log.i("wavelist", list[i]);
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, OPEN_WAVE_ID, 0, R.string.openWaveText);
		menu.add(0, ADD_ARBLIP_ID, 0, R.string.addARblipText);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case OPEN_WAVE_ID:
			Toast.makeText(
					getApplicationContext(),
					acm.getBlips(((TextView) info.targetView).getText()
							.toString()), Toast.LENGTH_LONG).show();
			return true;

		case ADD_ARBLIP_ID:

			Intent i = new Intent(this, ARBlipAddingView.class);
			i.putExtra("WaveID", ((TextView) info.targetView).getText()
					.toString()); // FIXME: WaveID shouldn't be hardcoded
			startActivity(i);

			// FrameLayout wavesListPage =
			// (FrameLayout)findViewById(R.id.WavePage);
			// wavesListPage.removeAllViews();
			// View v = new View(this);
			// ArrayList<View> bho = new ArrayList<View>();
			// bho.add( findViewById(R.layout.add_arblip));
			// v.addFocusables(bho, 1);
			// wavesListPage.addView(v);
			// setContentView(findViewById(R.id.add_arblip_layout));
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void onTabChanged(String tabId) {
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
