package com.arwave.skywriter;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import android.app.TabActivity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TabHost;
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
public class start extends TabActivity implements SensorEventListener,LocationListener {
	
	private AbstractCommunicationManager acm;
	private ARBlipView arView;
	CameraView cameraView;
	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag,sensorOri;
	
	private boolean paused = false;
	
	private float xpos = -1;
	private float ypos = -1;
	
	private boolean OriginalLocationSet = false;

	private String[] wavesList;
	private WaveListView waveListViewBox;
	  
	  
	  //Camera Orientation Related
	  static SensorHelper sensorfunctions = new SensorHelper();
	  SimpleVector currentCameraVector = new SimpleVector(0,0,0);
	  float[] ori = new float[3];	
		float accels[] = new float[3];
		float mags[] = new float[3];
		float orients[] = new float[3];
float values[] = new float[3];
		Matrix tempR = new Matrix();
		
		
		DigitalAverage[] filter =
        { new DigitalAverage(), new DigitalAverage(), new DigitalAverage(), new DigitalAverage(),
         new DigitalAverage(), new DigitalAverage() };
		
private long lastMagsTime;
private long lastAccelsTime;
private long lastOrientsTime;
		
		//Matrix tempR = new Matrix();
		float RTmp[] = new float[9];
		float Rt[] = new float[9];
		float outR[] = new float[9];
		float I[] = new float[9];
		private boolean isReady;
	 
		TabHost tabHost;
		//--
		private ArrayAdapter<String> usersWaveListAdapter;			
		private List<String> usersWavesList;
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 Resources res = getResources();
		 
		 setContentView(R.layout.main);		
		 
		 //load xml files for other pages
		// LayoutInflater factory = LayoutInflater.from(this); 
		// final View loginpage = factory.inflate(R.layout.loginpage, null); 
		// final LinearLayout LoginPage2 = (LinearLayout)loginpage.findViewById(R.id.MainLoginPage);
		 
		//SET UP TABS
		 tabHost = getTabHost();  // The activity TabHost
		    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		    
		   
		    
		    // Initialize a TabSpec for each tab and add it to the TabHost
		    spec = tabHost.newTabSpec("LoginTab").setIndicator("Login",
		                      res.getDrawable(R.drawable.eye))
		                  .setContent(R.id.MainLoginPage);
		    tabHost.addTab(spec);

		    spec = tabHost.newTabSpec("WavesTab").setIndicator("Waves",
		                      res.getDrawable(R.drawable.ship))
		                  .setContent(R.id.WavePage);
		    tabHost.addTab(spec);

		    spec = tabHost.newTabSpec("WorldTab").setIndicator("World",res.getDrawable(R.drawable.eye))
		                  .setContent(R.id.ARViewPage);
		          
		    
		    tabHost.addTab(spec);

		    
		  //size height
		   // tabHost.getTabWidget().getLayoutParams().height=60;
		    
		    tabHost.setCurrentTab(0);
		
		    
		    
		    
		    

			  //set up camera view and ar overlay
		    FrameLayout arPage = (FrameLayout)findViewById(R.id.ARViewPage);
		    
			cameraView = new CameraView(this);
			arView = new ARBlipView(getApplication());				
						
			//asign to ar page	
			arPage.addView(cameraView);
			arPage.addView(arView,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			
			//, 
			//        	
			//listen for gps
			Log.d("setting", "location checker");
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, this);
		
			//initialize the communication manager
			//TODO: other choices will be available in the future
			acm = new FedOneCommunicationManager( this );
		//prompt for a login 
			Button button = (Button)findViewById(R.id.LoginButton);
		    button.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					tabHost.setCurrentTab(1);
					// try to log the user in
					EditText username = (EditText)findViewById(R.id.EditText02);
			        EditText serverAddress = (EditText)findViewById(R.id.EditText03);
			        //EditText serverPort = (EditText)findViewById(R.id.serverPortEdit);
					acm.login(serverAddress.getText().toString(), 9876, username.getText().toString(), new String("") );
				}
		    	
		    });
		//if the user doesn't login we only display the already cached data?
		
		    //setup the page with the wave list
		    FrameLayout wavesListPage = (FrameLayout)findViewById(R.id.WavePage);

			waveListViewBox = new WaveListView(this);

			wavesListPage.addView(waveListViewBox);
			//add default contents and set adapter
			usersWavesList = new ArrayList<String>();		
	      // usersWavesList.add("wave list not updated");
	        usersWaveListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , usersWavesList);			 
		    waveListViewBox.setAdapter(usersWaveListAdapter);
		   
		    

			//waveListView.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item , wavesList));
	}
	/**
	public void loginUser(String name, String password, String server){
		
		//login
		//ARBlipInterface.login(name,password,server)
		
		//assign listeners
		
		
	}
	*/
	
	@Override
	protected void onPause() {
		paused = true;
		super.onPause();
		arView.onPause();
		arView.worldReadyToGo=false;
		
		if (sensorMgr != null)
		{
    		sensorMgr.unregisterListener(this, sensorGrav);
			sensorMgr.unregisterListener(this, sensorMag);
			sensorMgr = null;
		}
		
	}

	@Override
	protected void onResume() {
		paused = false;
		super.onResume();
		arView.onResume();
				
		
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) { sensorGrav = sensors.get(0); }

		sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors.size() > 0) { sensorMag = sensors.get(0); }

		sensors = sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) { sensorOri = sensors.get(0); }
		
		Log.i("skywritter","listerners set");
		
		sensorMgr.registerListener(
			this, 
			sensorGrav,
			SENSOR_DELAY_FASTEST);
		sensorMgr.registerListener(
			this, 
			sensorMag,
			SENSOR_DELAY_FASTEST);
		
		sensorMgr.registerListener(
				this, 
				sensorOri,
				SENSOR_DELAY_FASTEST);
		
	}

	@Override
	protected void onStop() {
		arView.renderer.stop();
		arView.worldReadyToGo=false;
		super.onStop();
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
			//arView.creatingSpinningCube(); //my spinning cube function! 			
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
	
	

	
	public void addTestBlip()
	{
		
		ARBlip testblip = new ARBlip();
		testblip.x = (Math.random()*500);
		testblip.y = (Math.random()*500);
		testblip.z = -100;
		testblip.BlipID = "TestBlip";
		testblip.ObjectData = "Test-"+(int)(Math.random()*10);
		
		
		arView.addBlip(testblip);
		

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
    
	class CameraView extends SurfaceView implements SurfaceHolder.Callback 
	{
		
		

		//ARExplorer app;
		SurfaceHolder holder;
	    private android.hardware.Camera camera;

	    CameraView(Context context) 
	    {
	        super(context);
	        
	        try
	        {
		      //  app = (ARExplorer) context;
	        	
		        holder = getHolder();
		        holder.addCallback(this);
		        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        }
	        catch (Exception ex)
	        {
	        	
	        }
	    }

	    public void surfaceCreated(SurfaceHolder holder) 
	    {
	    	try
	    	{
	    		if (camera != null)
	    		{
	        		try { camera.stopPreview(); } catch (Exception ignore) { }
			    	try { camera.release(); } catch (Exception ignore) { }
			        camera = null;
	    		}
	    		
	        	camera = android.hardware.Camera.open();
	        	camera.setPreviewDisplay(holder);
	    	}
	    	catch(Exception ex)
	    	{
	    		try
	    		{
	    			if (camera != null)
		    		{
		        		try { camera.stopPreview(); } catch (Exception ignore) { }
			    		try { camera.release(); } catch (Exception ignore) { }
			        	camera = null;
		    		}
	    		}
	    		catch (Exception ignore)
	    		{
	    			
	    		}
	    	}
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) 
	    {
	    	try
	    	{
	    		if (camera != null)
	    		{
	        		try { camera.stopPreview(); } catch (Exception ignore) { }
		    		try { camera.release(); } catch (Exception ignore) { }
		        	camera = null;
	    		}
	    	}
	        catch (Exception ex)
	        {
	        	ex.printStackTrace();
	        }
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
	    {
	    	try
	    	{
	    		Log.e("Test","camera set to go");
	    		
	        	camera.startPreview();
	    	}
	        catch (Exception ex)
	        {
	        	ex.printStackTrace();
	        }
	    }
	}

	
	
		

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		sensorfunctions.changeAccuracy(sensor, accuracy);
		
	}


	public void onSensorChanged(SensorEvent s_ev) {
		
		 Sensor sensor = s_ev.sensor;

         int type = sensor.getType();

         switch (type) {
             case Sensor.TYPE_MAGNETIC_FIELD:
                 mags = s_ev.values;
               //  isReady = true;
                 break;
             case Sensor.TYPE_ACCELEROMETER:
                 accels = s_ev.values;
              //   isReady = true;
                 break;
             case Sensor.TYPE_ORIENTATION:
                 orients = s_ev.values;
              //   Exp.mText04.setText("" + (int) orients[0]);
              //  Exp.mText05.setText("" + (int) orients[1]);
              ///   Exp.mText06.setText("" + (int) orients[2]);
                 break;
         }

         if (mags != null && accels != null && isReady) {
             isReady = false;
        // 	Log.i("--", "setting camera0");
             SensorManager.getRotationMatrix(Rt, I, accels, mags);
          //  SensorManager.remapCoordinateSystem(Rt, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            // SensorManager.getOrientation(outR, values);

 			//Rt=outR;
 			
         	tempR.setRow(0, Rt[0], Rt[1], Rt[2],0);
			tempR.setRow(1, Rt[3], Rt[4], Rt[5],0);
			tempR.setRow(2, Rt[6], Rt[7], Rt[8],0);
			tempR.setRow(3, 0, 0, 0,1);
			//Log.i("--", "setting camera1"+Rt[0]);
			tempR.rotateX((float)Math.PI);
			
			//arView.setCameraOrentation(tempR);
			
			
			

		    //arView.setCameraOrentation(tempR);
             
             
          //   int[] v = new int[3];

          // v[0] = filter[0].average(values[0] * 100);
         //  v[1] = filter[1].average(values[1] * 100);
         //  v[2] = filter[2].average(values[2] * 100);
             
         //    currentCameraVector.set(v[0], v[1],v[2]);
             
          // arView.setCameraOrentation(values[0], values[1],values[2]); 
             
             //Exp.mText01.setText("" + v[0]);
            // Exp.mText02.setText("" + v[1]);
            // Exp.mText03.setText("" + v[2]);
         }
         
		/*
		try
		{
		
			//get the sensor data, and process it into camera vector
			//We handel this in the external SensorHelper.
			//currentCameraVector =  sensorfunctions.getVectorFromEvt(evt);
			
			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				grav[0] = evt.values[0];
				grav[1] = evt.values[1];
				grav[2] = evt.values[2];

				arView.postInvalidate();
			} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mag[0] = evt.values[0];
				mag[1] = evt.values[1];
				mag[2] = evt.values[2];

				arView.postInvalidate();
				 isA = true; 
			}
			
			
			 // make sure there are values from magnetic field, 
			//accelerometer, and only calc after an accerlometer reading. 
			            // 
			            if (mag != null && grav != null && isA) { 
			                boolean b = SensorManager.getRotationMatrix(RTmp, I,grav, mag); 
			                // remap to "camera" mode - right side up, screen facing me 
			                SensorManager.remapCoordinateSystem(RTmp,SensorManager.AXIS_X, SensorManager.AXIS_Z, Rt); 
			                SensorManager.getOrientation(Rt, values); 
			                int[] v = new int[3]; 
			                v[0] = (int) (values[0] * 57.2957795);   // radians todegrees 
			                v[1] = (int) (values[1] * 57.2957795); 
			                v[2] = (int) (values[2] * 57.2957795); 
			                isA = false; 
			                
			                // update display 
			                Log.i("--", "" + v[0]); 
			                Log.i("--", "" + v[1]); 
			                Log.i("--", "" + v[2]); 
			                currentCameraVector.set(v[0], v[1],v[2]);
			                
			                arView.setCameraOrentation(currentCameraVector); 
			                
			    		   // Log.i("--", Rt[0] +" "+ Rt[1] +" "+ Rt[2]);
			    		   // Log.i("--", Rt[3] +" "+ Rt[4] +" "+ Rt[5]);
			    		   // Log.i("--", Rt[6] +" "+ Rt[7] +" "+ Rt[8]);
			    			      
			            }
			
/*
			SensorManager.getRotationMatrix(RTmp, I, grav, mag);
			//SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rt);
			
			Rt=RTmp;
			
			tempR.setRow(0, Rt[0], Rt[1], Rt[2],0);
			tempR.setRow(1, Rt[3], Rt[4], Rt[5],0);
			tempR.setRow(2, Rt[6], Rt[7], Rt[8],0);
			tempR.setRow(3, 0, 0, 0,1);
			
			
			
			
			

		    //arView.setCameraOrentation(tempR);
			
			
			//set the camera vector to match the one returned by the sensors if it changed (365 used to indicate no relevant updates)
		//	if (currentCameraVector.x<365){
		//	arView.setCameraOrentation(currentCameraVector); 
		//	}
			
			//arView.setCameraOrentation(currentCameraVector, new SimpleVector(0,0,90)); //nb; I'm not sure the up vector is correct yet
			
			
		}
        catch (Exception ex)
        {
        	Log.e("Sensor", "ProcessingError", ex);
        }
        */
		
	}


	public void onLocationChanged(Location location) {
		//only run if world is set
		Log.d("loading", "gps updated");
		
		Timer blah = new Timer();
				
		TimerTask meep = new TimerTask() {
			   @Override
			public void run() {
			       
				  
				 
				   
					//load the sample blips after the world is set up
					if ((arView.worldReadyToGo) && (OriginalLocationSet))
					{
						
						 this.cancel();
						   Log.d("loading", "loading blips");
						   
						   double blipDataX[] = {51.558695,51.558547,51.558413,51.558290,51.558148,51.557988, 51.557836,51.557709,51.557576,51.557563,51.557526};
						   double blipDataY[] = {5.077808,5.077781,5.077757, 5.077725,5.077706,5.077679,5.077660,5.077644, 5.077652, 5.078052, 5.078572};
						   					   

						   
					for(int i=0;i<=blipDataX.length;) {
						
						//we can now load up some sample blips
						ARBlip testblip1 = new ARBlip();
						testblip1.x = blipDataX[i]; //51.558348 //51.558325
						testblip1.y = blipDataY[i];
						testblip1.z = 0;
						testblip1.BlipID = "NewTestBlip"+i;
						testblip1.ObjectData = ""+i;
						arView.addBlip(testblip1);
						i++;
					}
					
					
					 
					} else {
						Log.d("load", "not ready for blips");
					}
				   
				   
			   }
			};
		
			//if its not set, then set the starting location for the arView
			if (!OriginalLocationSet){
				arView.startingLocation = location;
				Log.i("load", "setting location...");
				OriginalLocationSet=true;
				blah.schedule(meep, 100,100);
			}
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
	public void noOp(String arg0, WaveletData arg1) {
		// TODO Auto-generated method stub
		
	}
	public void onCommitNotice(WaveletData arg0, HashedVersion arg1) {
		// TODO Auto-generated method stub
		
	}


	public void onDeltaSequenceEnd(WaveletData arg0) {
		// TODO Auto-generated method stub
		
	}


	public void onDeltaSequenceStart(WaveletData arg0) {
		// TODO Auto-generated method stub
		
	}


	public void participantAdded(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		// TODO Auto-generated method stub
		
	}


	public void participantRemoved(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		// TODO Auto-generated method stub
		
	}


	public void waveletDocumentUpdated(String arg0, WaveletData arg1,
			WaveletDocumentOperation arg2) {
		// TODO Auto-generated method stub
		
	} 

	public void addMessage( String message ) {
	//	TextView out = (TextView)findViewById(R.id.messages);
	//	out.setText(message);
		Log.i("state",message);
		usersWaveListAdapter.add("test");
	}
	
	public void showWaveList(String[] list) {
		
		
		//clear the list
		usersWavesList.clear();
		
		//add the data to the list
		Log.i("state","getting wave list");
		for (int i=0;   i<list.length;   i++){
		Log.i("wavelist",list[i]);
		usersWavesList.add(i+"_"+list[i]);		
		}
		
		//request the update to the list
		Log.i("wavelist","posting invalidate");
		waveListViewBox.setDataUpdated();
		waveListViewBox.postInvalidate();
		
		//waveListView.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item , list)); 
		
		/*
		waveListView.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		      // When clicked, show a toast with the TextView text
		      acm.openWavelet( ((TextView)view).getText().toString() );
		    }
		  });
		*/
	}
	
	public void setWaveList(String[] list) {

		Log.i("state","getting wave list");
		for (int i=0;   i<list.length;   i++){
		Log.i("wavelist",list[i]);
		}
	//	this.wavesList = list;
	}
}
