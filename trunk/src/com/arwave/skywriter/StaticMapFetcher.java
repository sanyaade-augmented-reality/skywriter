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

//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapView;

public class StaticMapFetcher {
	Bitmap mapimage;
	
	
	public StaticMapFetcher(Context context, String apiKey) {
		//super(context, apiKey);
	//	this.setLayoutParams(new LayoutParams(56, 56, 0, 0,MapView.LayoutParams.TOP_LEFT));
		//this.setSatellite(true);
		//this.getController().setZoom(17);
	}
	

	/** gets a bitmap map at the specified location */
	
	public static LocatedMapBundle getMap(Location thisloc) throws MalformedURLException,IOException  {
		
/*
	//	int Lon = (int)Math.round(thisloc.getLongitude()*1E6);
	//	int Lat = (int)Math.round(thisloc.getLatitude()*1E6);
		String Lon = ""+thisloc.getLongitude();
		String Lat = ""+thisloc.getLatitude();
		
		
		Log.e("Location", Lat+","+Lon);
		
		//this.getController().setCenter(new GeoPoint(Lat,Lon));
		
		String mapsurl= new String("http://maps.google.com/maps/api/staticmap?center="+Lat+","+Lon+"&zoom=16&size=256x256&maptype=satellite&sensor=true");
		Log.i("dis",mapsurl);
		
		URL loc = new URL(mapsurl);
		   final URLConnection conn = loc.openConnection();

           conn.connect();

           final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            Bitmap mapimage = BitmapFactory.decodeStream(bis);
		
		this.buildDrawingCache();
		this.getDrawingCache();
		if (mapimage!= null) mapimage.recycle();
		mapimage = Bitmap.createBitmap(this.getDrawingCache());
	    this.destroyDrawingCache();
		
		LocatedMapBundle newMap = new LocatedMapBundle();
        newMap.map = mapimage;
        newMap.xDis = 0;
        newMap.yDis = 0;
        
        return  newMap;
	    
		*/
	    
		return getMapFromOpenStreetMap(thisloc);
		
		}
	
	public static LocatedMapBundle getMapFromOpenStreetMap(Location thisloc) throws MalformedURLException,IOException  {
		/* Cant use this method, as google's static api is only for web browsers 
		   public Bitmap getRemoteImage(final URL aURL)
		   */
		
		int xtile = (int)Math.floor( (thisloc.getLongitude() + 180) / 360 * (1<<16) ) ;
		int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(thisloc.getLatitude())) + 1 / Math.cos(Math.toRadians(thisloc.getLatitude()))) / Math.PI) / 2 * (1<<16) ) ;

		
		// The tiles should be cached!		
		final  URL aURL = new URL("http://tile.openstreetmap.org/16/"+xtile+"/"+ytile+".png");
		
			   
	           try {

	        	   
	        	   
	                   final URLConnection conn = aURL.openConnection();

	                   conn.connect();

	                   final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

	                   final Bitmap centerMapTile = BitmapFactory.decodeStream(bis);
	                   //because of the fixed-grid arrangement of tiles, we have to also work out the displacement of the tile from the current location;
	                   mapBordersBoundingBox currentmapBB = tile2boundingBox(xtile,ytile,16);
	    	           
	    	           
	    	           
	    	           //now we work out the displacement of this from the real location
	    	           
	    	           /*
	    	           
	    	           double disX = ARBlipUtilitys.getRelativeZLocation(currentmapBB.east, thisloc);
	    	           double disY = ARBlipUtilitys.getRelativeXLocation(currentmapBB.north, thisloc);
	    	           
	    	           //returned distances are relative to the west north side of the map, and thus need to be changed to be center-relative
	    	           
	    	           int MapSize = 378;	    	           
	    	           disX =  (MapSize/2)-disX;
	    	           disY =  (MapSize/2)-disY;
	    	           
	    	           Log.i("dis","loc:"+aURL.getPath());	    	           
	    	           
	    	           Log.i("dis",currentmapBB.east+","+thisloc.getLongitude()+"X= "+disX);
	    	          
	    	           Log.i("dis",currentmapBB.north+","+thisloc.getLatitude()+"Y= "+disY);
		    	          
	    	           //work out quadrant the user is in relative to the tile
	    	           if (disX>(MapSize/2))	    	        	   
	    	           { 
	    	        	   Log.i("dis","east");
	    	        	   
	    	        	   //east
	    	        	   if (disY>(MapSize/2))
		    	           {
	    	        		   //north east
	    	        		   Log.i("dis","north east");
	    	        		   
		    	           } else {
		    	        	   //north west *
		    	        	   Log.i("dis","north west");
	    	        		   
		    	           }
	    	        	   	    	        	   
	    	           } else 
	    	           {
	    	        	   Log.i("dis","west");
	    	        	   //west
	    	        	   if (disY>(MapSize/2))		    	           {
	    	        		   //north west
	    	        		   Log.i("dis","south east");
	    	        		   
		    	           } else {
		    	        	   //south east *
		    	        	   Log.i("dis","south west");
	    	        		   
		    	           }
	    	           }
	    	           
	    	         //update tiles position based on this data (in future, we will have 9 tiles to cover this completely)
	    	           */
	    	           
	    	           LocatedMapBundle newMap = new LocatedMapBundle();
	    	           newMap.centerMap = centerMapTile;
	    	           newMap.lon = (currentmapBB.east + currentmapBB.west)/2; //marks the centre point of the centre tile
	    	           newMap.lat = (currentmapBB.north + currentmapBB.south)/2; 
	    	           
	    	         //  newMap.xDis =disY;
	    	          // newMap.yDis = -disX;// ?? -? +?
	    	           
	                   return  newMap;

	           } catch (IOException e) {

	                   Log.d("DEBUGTAG", "Oh noooz an error...");

	           }
	           
	           
	       

	           return null;

	   
	}
	 // code from: http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
	/*
	 class BoundingBox {
		    double north;
		    double south;
		    double east;
		    double west;   
		    
		    double centerLat;
		    double centerLon;
		  }
	 */
	 
	 static mapBordersBoundingBox tile2boundingBox(final int x, final int y, final int zoom) {
		 mapBordersBoundingBox bb =  new mapBordersBoundingBox();
		    bb.north = tile2lat(y, zoom);
		    bb.south = tile2lat(y + 1, zoom);
		    bb.west = tile2lon(x, zoom);
		    bb.east = tile2lon(x + 1, zoom);
		    
		    //center is half way between (Almost certainly a better way to do this! 
		    
		    return bb;
		  }
		 
		  static double tile2lon(int x, int z) {
		     return x / Math.pow(2.0, z) * 360.0 - 180;
		  }
		 
		  static double tile2lat(int y, int z) {
		    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		    return Math.toDegrees(Math.atan(Math.sinh(n)));
		  }
	
		
	}
		
		
	
	

