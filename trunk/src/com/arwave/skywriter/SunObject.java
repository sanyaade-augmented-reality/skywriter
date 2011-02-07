package com.arwave.skywriter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.PSA;

import android.content.res.Resources;
import android.location.Location;
import android.util.Log;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.LensFlare;

/** The sun object is a jpct light which is intended to represent the sun 
 * You can set its position by either a baring from north, or by specifying
 * the time of day and hemisphere you are in.
 * The time/direction methodology is here;
 * http://www.wikihow.com/Find-True-North-Without-a-Compass **/

public class SunObject extends Light {
	
	private static final int distance = 200;

	private static double  CurrentBaring = 0;
	
	//visible sun
	Object3D sunObject = Primitives.getSphere(8, 7); //the visible sphere
	//lens flare
	LensFlare SceneLensFlare;

	private boolean LensFlareOn;
	private World sourceWorld;
	
	
	//enum Hemisphere { SouthenHemisphere, NorthenHemisphere};
	
	/** Creates a sun at a specific direction and the default distance (90) 
	 * @param res - used for texturing the lens flare **/
	public SunObject(World world, int BaringFromNorth, Resources res) {
		
		super(world);
		sourceWorld = world;
		
		//set default size and intensity 
		this.setIntensity(250, 250, 250);
		
	
		sunObject.setAdditionalColor(RGBColor.WHITE);

				
		//set visible object for sun
		SimpleVector currentpos = this.getPosition();
		sunObject.translate(currentpos);
		sunObject.build();
		world.addObject(sunObject);
		
		//set up lensflare texture	
		TextureManager tm = TextureManager.getInstance();
		Texture flare_maintexture = new Texture(res.openRawResource(R.raw.mainflare2));
        tm.addTexture("flare_maintexture", flare_maintexture);
	
        
		//set up lensflare
		SceneLensFlare = new LensFlare(this.getPosition(),"flare_maintexture", "flare_maintexture", "flare_maintexture","flare_maintexture");
		SceneLensFlare.setMaximumDistance(1000);
		SceneLensFlare.setTransparency(7);
		
		//set direction
		setDirection(BaringFromNorth,45);
		
	}
	
	/** sets the position **/
	public void setPosition(SimpleVector position){
		
		super.setPosition(position);
		
		//set visible object for sun
		sunObject.setTranslationMatrix(new Matrix());
		sunObject.translate(position);
		
		//move lensflare
		SceneLensFlare.setLightPosition(this.getPosition());
		
	}
	
	/** sets the direction 
	 * @param northdis - the baring from north in degree's (  Azimuth)
     * @param alt - suns altitude in degree's 0 is horizon 90 above */
	public void setDirection(double northdis, double alt){
		
		//convert to radians
		double northBaring = Math.toRadians(northdis);
		alt = Math.toRadians(alt);
		
		SimpleVector sv = new SimpleVector(0,0,0);
		
		double h = Math.sin(alt)*distance;
		sv.y = (float) -h; //height will vary based on time of day; to be worked out, but probably as simple as 180/12hours+displacement for the time of year
		sv.x = (float) (Math.sin(northBaring)* distance);
		sv.z = (float) (Math.cos(northBaring)* distance);
		
		this.setPosition(sv);
		CurrentBaring = northdis;
	}

	/** not tested, but this should set direction based on time **/
	public void setSunPosition(int HoursPassedNoon,Location currentlocation ){
				
		Log.i("test","setting sun hours to"+HoursPassedNoon);
		
		//int DaysAfterMarchTT = 356+7+31;
		//Maths!
		
		//Formulas from; http://www.providence.edu/mcs/rbg/java/sungraph.htm
		
		//This should work, but instead we now use the creative commons lib
		/*
		double lat = Lattitude;
		double T = HoursPassedNoon;
		double t = (Math.PI/12)*T;
		double dec = 23.45 * Math.sin(DaysAfterMarchTT);
		double alt = Math.asin((Math.sin(lat)* Math.sin(dec))+(Math.cos(lat)*Math.cos(dec)*Math.cos(t)));
	
		double az = Math.atan( Math.cos(dec)*Math.sin(t) /   ( (Math.cos(lat)*Math.sin(dec))  -   (Math.sin(lat)*Math.cos(dec) )*Math.cos(t))      );
		
		Log.i("math", "t = "+t);
		Log.i("math", "dec = "+dec);
		Log.i("math", "alt = "+alt);
		Log.i("math", "az = "+az);
		*/
		//get height from dec
		
		
		 final GregorianCalendar dateTime = new GregorianCalendar();
		 dateTime.set(Calendar.HOUR_OF_DAY, HoursPassedNoon);
		 
		 
		 
		 final double latitude = currentlocation.getLatitude();
		 final double longitude = currentlocation.getLongitude();

		 AzimuthZenithAngle position = PSA.calculateSolarPosition(dateTime,latitude,longitude);
		 double az = position.getAzimuth();
		 double alt =position.getZenithAngle();
		 
		 
			Log.i("math", "alt = "+alt);
			Log.i("math", "az = "+az);
			 alt = -(position.getZenithAngle()-90);
			 
		setDirection(az,alt);
	
		/*
		//method based on location
		if (YourHemisphere==Hemisphere.NorthenHemisphere){
			
		    //half the time since noon
			double northdis = HoursPassedNoon / 2;
			//multiple by 30 to get degree's (as each hour is 30 degree's on a clockface)
			northdis = northdis * 30;
			//set the direction
			setDirection(northdis);
			
		}
		
		//method based on location
		if (YourHemisphere==Hemisphere.SouthenHemisphere){
			
		    //half the time since noon
			double northdis = HoursPassedNoon / 2;
			//multiple by 30 to get degree's (as each hour is 30 degree's on a clockface)
			northdis = northdis * 30;
			//set the direction
			setDirection(-northdis); //note the minus, the displacement goes the other way...I think
			
		}*/
		
	}

	public void setLensFlareOn(boolean b) {		
	
		LensFlareOn = b;
		
	}
	
	/** This should be done before render for lensflares to work **/
	public void updatedAndRender(FrameBuffer fb){
		if (LensFlareOn){
			
			SceneLensFlare.update(fb, sourceWorld);
			SceneLensFlare.render(fb);
			
			}
	}
	
}
