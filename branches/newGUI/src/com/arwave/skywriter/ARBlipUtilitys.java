package com.arwave.skywriter;

import android.location.Location;
import android.util.Log;

public class ARBlipUtilitys {

	
	
	
	
	public static double getRelativeXLocation(ARBlip blip,Location loc )
	{
		//float results[] = new float[3];
		
		//location to comare too
		Location blipLoc = new Location("");
		blipLoc.setLatitude(blip.x);
		blipLoc.setLongitude(loc.getLongitude());
				
		//double dis = loc.distanceTo(blipLoc);
		Log.i("distanceX","getting distance");
		
		
		float[] RE = new float[2];		
		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), blipLoc.getLatitude(), blipLoc.getLongitude(), RE);
		double dis = RE[0];
		if (RE[1]==180){
			dis = -dis;
		}
		Log.i("distanceX","from="+blip.x+" to "+loc.getLatitude()+"="+dis+"  "+RE[1]);
		
		return dis;
	}
	public static double getRelativeYLocation(ARBlip blip,Location loc )
	{
		return blip.z;
	}
	public static double getRelativeZLocation(ARBlip blip,Location loc )
	{
		Location blipLoc = new Location("");
		blipLoc.setLatitude(loc.getLatitude());
		blipLoc.setLongitude(blip.y);
				
		//double dis = loc.distanceTo(blipLoc);
		
		float[] RE = new float[2];		
		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), blipLoc.getLatitude(), blipLoc.getLongitude(), RE);
		
		double dis = RE[0];
		if (RE[1]==180){
			dis = -dis;
		}
		
		
		Log.i("distanceY","from="+blip.y+" to "+loc.getLongitude()+"="+dis+"  "+RE[1]);
		
		
		return dis;
	}
	
}

