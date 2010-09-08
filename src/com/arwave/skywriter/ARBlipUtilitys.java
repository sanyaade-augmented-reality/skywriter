package com.arwave.skywriter;

import android.location.Location;
import android.util.Log;

public class ARBlipUtilitys {

	
	
	public static double getRelativeXLocation(double X,Location loc )
	{
//float results[] = new float[3];
		
		//location to comare too
		Location blipLoc = new Location("");
		blipLoc.setLatitude(X);
		blipLoc.setLongitude(loc.getLongitude());
				
		//double dis = loc.distanceTo(blipLoc);
		Log.i("distanceX","getting distance");
		
		
		float[] RE = new float[2];		
		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), blipLoc.getLatitude(), blipLoc.getLongitude(), RE);
		
		double dis = RE[0];
		Log.i("dis", "angle="+RE[1]);
		
		if (RE[1]==180){
			Log.i("dis", "inverting lat");
			dis = -dis;
		}
		Log.i("distanceX","from="+X+" to "+loc.getLatitude()+"="+dis+"  "+RE[1]);
		
		return dis;
	}
	
	public static double getRelativeXLocation(ARBlip blip,Location loc )
	{
		return getRelativeXLocation(blip.x,loc);
		
		//float results[] = new float[3];
		/*
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
		*/
	}
	public static double getRelativeYLocation(ARBlip blip,Location loc )
	{
		return blip.z;
	}
	public static double getRelativeYLocation(double Z,Location loc )
	{
		return Z;
	}
	public static double getRelativeZLocation(double Y,Location loc )
	{
		Location blipLoc = new Location("");
		blipLoc.setLatitude(loc.getLatitude());
		blipLoc.setLongitude(Y);
				
		//double dis = loc.distanceTo(blipLoc);
		
		float[] RE = new float[2];		
		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), blipLoc.getLatitude(), blipLoc.getLongitude(), RE);
		
		Log.i("dis", "angle="+RE[1]);
		
		double dis = RE[0];
		if ((RE[1]<-85) && (RE[1]>-95)){
			Log.i("dis", "inverting longitude");
			
			dis = -dis;			
		}
		
				
		Log.i("distanceY","from="+Y+" to "+loc.getLongitude()+"="+dis+"  "+RE[1]);
		
		
		return dis;
	}
	public static double getRelativeZLocation(ARBlip blip,Location loc )
	{
		return getRelativeZLocation(blip.y,loc);
	}
	
	/** displaces location "old" by a distance and baring
	 * use's formula found at; http://www.movable-type.co.uk/scripts/latlong.html **/
	public static Location displaceLocation(Location location, double distance, double baring){
		
		
		//and now, using a magic formula..... (probably very cpu intensive)
		return destVincenty(location.getLatitude(),location.getLongitude(),distance,baring);		
	}
	
	/**
	 * Calculate destination point given start point lat/long (numeric degrees), 
	 * bearing (numeric degrees) & distance (in m).
	 *
	 * from: Vincenty direct formula - T Vincenty, "Direct and Inverse Solutions of Geodesics on the 
	 *       Ellipsoid with application of nested equations", Survey Review, vol XXII no 176, 1975
	 *       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
	 **/
	public static Location destVincenty(double lat1, double lon1, double dist,double brng) {
	 
	  double a = 6378137.0, b = 6356752.3142,  f = 1/298.257223563;  // WGS-84 ellipsiod
	  double s = dist;
	  double alpha1 = Math.toRadians(brng);
	  double sinAlpha1 = Math.sin(alpha1);
	  double cosAlpha1 = Math.cos(alpha1);
	  
	  double tanU1 = (1.0-f) * Math.tan(Math.toRadians(lat1));
	  double cosU1 = 1.0 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1*cosU1;
	  double sigma1 = Math.atan2(tanU1, cosAlpha1);
	  double sinAlpha = cosU1 * sinAlpha1;
	  double cosSqAlpha = 1 - sinAlpha*sinAlpha;
	  double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
	  double A = 1.0 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
	  double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
	  
	  double sigma = s / (b*A), sigmaP = 2*Math.PI;
	  double sinSigma=0;
	  double cosSigma=0;
	  double cos2SigmaM=0;
	  double deltaSigma=0;
	  while (Math.abs(sigma-sigmaP) > 1e-12) {
		   cos2SigmaM = Math.cos(2*sigma1 + sigma);
		   sinSigma = Math.sin(sigma);
		   cosSigma = Math.cos(sigma);
		   deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
	      B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
	    sigmaP = sigma;
	    sigma = s / (b*A) + deltaSigma;
	  }

	  double tmp = sinU1*sinSigma - cosU1*cosSigma*cosAlpha1;
	  double lat2 = Math.atan2(sinU1*cosSigma + cosU1*sinSigma*cosAlpha1, 
	      (1-f)*Math.sqrt(sinAlpha*sinAlpha + tmp*tmp));
	  double lambda = Math.atan2(sinSigma*sinAlpha1, cosU1*cosSigma - sinU1*sinSigma*cosAlpha1);
	  double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
	  double L = lambda - (1-C) * f * sinAlpha *
	      (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));

	  double revAz = Math.atan2(sinAlpha, -tmp);  // final bearing

	  Location displaced = new Location("");
	  displaced.setLatitude( Math.toDegrees(lat2));
	  displaced.setLongitude(  Math.toDegrees(L)+ lon1);
	  	  
	  return displaced;
	  
	}

	

	
}

