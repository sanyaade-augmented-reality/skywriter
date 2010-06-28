package com.arwave.skywriter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.threed.jpct.SimpleVector;

/** This class is for sensor handleing. It will (hopefully) take 
 * the devices sensor events and return a vector for the camera angle **/
public class SensorHelper {
	
	float[] ori = new float[3];	
	float grav[] = new float[3];
	float mag[] = new float[3];
	
	/** Return vector from sensor input **/
	public SimpleVector getVectorFromEvt(SensorEvent evt){
		
		SimpleVector cameraVector = new SimpleVector(365,365,365); //indicates no change
		
		
		//get sensor changes reported
		
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
		{
			grav[0] = evt.values[0];
			grav[1] = evt.values[1];
			grav[2] = evt.values[2];			
			
		}
		else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)  
		{
			mag[0] = evt.values[0];
			mag[1] = evt.values[1];
			mag[2] = evt.values[2];
			
		} else if(evt.sensor.getType() == Sensor.TYPE_ORIENTATION) 
		{
				
				ori[0] = evt.values[0];
				ori[1] = evt.values[1];
				ori[2] = evt.values[2]; //roll	
				
				//this isnt a vector, but what the hell, its 3 variables....
				cameraVector.z = ori[0];
				cameraVector.x = -ori[1];
				cameraVector.y = -ori[2];
				
				//need some sort of filtering?
				
				//Log.i("__z","_"+(float) (cameraVector.z));
				

				
				
		}
		
		
		
		
			
		return cameraVector;
		
		
	}
	
	/** For handeling accuracy changes **/
	public void changeAccuracy(Sensor sensor, int accuracy){
		
	}
	
}
