package com.arwave.skywriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.text.Layout;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class StaticMapFetcher extends MapView {
	Bitmap mapimage;
	
	public StaticMapFetcher(Context context, String apiKey) {
		super(context, apiKey);
		// TODO Auto-generated constructor stub
	//	this.setLayoutParams(new LayoutParams(56, 56, 0, 0,MapView.LayoutParams.TOP_LEFT));
		this.setSatellite(true);
		this.getController().setZoom(17);
	}
	

	/** gets a bitmap map at the specified location */
	
	public Bitmap getMap(Location thisloc) throws MalformedURLException,IOException  {
		
		
		int Lon = (int)Math.round(thisloc.getLongitude()*1E6);
		int Lat = (int)Math.round(thisloc.getLatitude()*1E6);
		
		
		Log.e("Location", Lat+","+Lon);
		
		this.getController().setCenter(new GeoPoint(Lat,Lon));
		
		//String mapsurl= new String("http://maps.google.com/maps/api/staticmap?center="+Lat+","+Lon+"&zoom=16&size=256x256&maptype=satellite&sensor=true");
		//URL loc = new URL(mapsurl);
		this.buildDrawingCache();
		this.getDrawingCache();
		if (mapimage!= null) mapimage.recycle();
		mapimage = Bitmap.createBitmap(this.getDrawingCache());
	    this.destroyDrawingCache();
		
		return mapimage;
		
		}
	
	/* Cant use this method, as google's static api is only for web browsers 
	   public Bitmap getRemoteImage(final URL aURL) {
		   
           try {

                   final URLConnection conn = aURL.openConnection();

                   conn.connect();

                   final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

                   final Bitmap bm = BitmapFactory.decodeStream(bis);


                   return bm;

           } catch (IOException e) {

                   Log.d("DEBUGTAG", "Oh noooz an error...");

           }

           return null;

   }
	*/
}
