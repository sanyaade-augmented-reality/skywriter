package com.arwave.skywriter;

import glfont.GLFont;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GenericVertexController;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.LensFlare;

/** This is our GLSurface view extended with ARBlip specific functions **/
public class ARWaveView extends GLSurfaceView {

	// The Real Location of the device when this surface was loaded
	static public Location startingLocation;
	// the current real location of the camera
	static public Location currentRealLocation;

	// our world, to which we add stuff
	private World world = null;

	private Light sun = null;
	
	//lens flare on/off
	private boolean LensFlareOn = true;
	LensFlare SceneLensFlare;
	
	// ground
	private Object3D groundPlane = null;

	public boolean backgroundScenaryVisible = true;
	// renderer
	MyRenderer renderer = null;

	private static FrameBuffer fb = null;

	//Array containing all the layers open (hidden or not)
	static ArrayList<ARWaveLayer> AllLayersOpen = new ArrayList<ARWaveLayer>();
	
	// The scenes current list of ARBlipObjects
	// This changes based on what wave is open. 	
	static ARWaveLayer CurrentActiveLayer;
	
	//background layer (for scenery and other gui elements local to the device and not linked to a real wave)
	static final ARWaveLayer LocalBackgroundScenaryLayer = new ARWaveLayer();
	static final ARWaveLayer SpecialChristmassScenaryLayer = new ARWaveLayer();
	
	// generic font
	private GLFont glFont;
	static final Paint paint = new Paint();
	
	// World setup flag
	boolean worldReadyToGo = false;

	// show debug info flag
	public boolean showDebugInfo = false;

	// purely for testing
	// timer
	//private TimerTask mTimerTask = null;
	//private Timer mTimer = null;
	Object3D rotateingcube;
	int pos = 0;
	boolean paused = false;
	int move = 0;
	float turn = 0;
	float touchTurn = 0;
	float touchTurnUp = 0;
	private Matrix CameraMatrix;
	private boolean updateCamRotation = false;
	float newCameraX = 0;
	float newCameraY = 0;
	float newCameraZ = 0;

	// simple
	// vectors
	// used to
	// rotate in
	// one of 3
	// axis's
	final static SimpleVector Y_AXIS = new SimpleVector(0, 0, 1); // Fixed
	final static SimpleVector X_AXIS = new SimpleVector(1, 0, 0);
	final static SimpleVector Z_AXIS = new SimpleVector(0, 1, 0);

	float cameraHeight = -3;

	int TestVar = 0;
	boolean MapModeSet = true;

	// delete queue
	ArrayList<Object3D> deleteQueue = new ArrayList<Object3D>();

	// object editing
	Object3D CurrentObject;
	// after the last key is pressed.
	int newobject_distance = 25;


	// modes
	int VIEWING_MODE = 0;
	int EDIT_MODE = 1;
	int EDIT_END_FLAG = 2; //used when the edit mode has requested to end (lets the context menu appear again);
	//used for when the user is on the context menu.

	int CurrentMode;
	public boolean isOnContextMenu = false;
	

	public ARWaveView(Context context) {
		super(context);

		this.setZOrderMediaOverlay(true);

		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		Log.e("render", "setting render");

		renderer = new MyRenderer();
		this.setRenderer(renderer);

		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		
		
		// set up generic font
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.create((String) null, Typeface.BOLD));

		paint.setTextSize(16);
		glFont = new GLFont(paint);

		//set up default layer
		CurrentActiveLayer = LocalBackgroundScenaryLayer; //we start with a default blank layer untill one is selected
		LocalBackgroundScenaryLayer.ARWaveLayerID = "Background"; //The default layer contains the default landscape, and other test and interface objects not tied to "real" waves on a server
		AllLayersOpen.add(CurrentActiveLayer);
		
		//set up Christmas layer
		SpecialChristmassScenaryLayer.ARWaveLayerID = "CMLayer";
		AllLayersOpen.add(SpecialChristmassScenaryLayer);
	}

	public void onResume(){
		
		Log.i("_______", "_______resumeing.....");
		
		super.onResume();
		
	}

	/** handels screen interactions **/
	public boolean onTouchEvent(MotionEvent event) {


		int touchX = (int) event.getX();
		int touchY = (int) event.getY();




		// if in edit mode, touch determains distance (like +/- on the volume controls
		if ((CurrentMode==EDIT_MODE)||(CurrentMode==EDIT_END_FLAG)){

			double screenwidth = this.getWidth(); //probably should be screen width.
			double screenheight= this.getHeight();

			Log.i("touch","screenwidth "+screenwidth);

			//resize based on distance from center x
			int distancefromscreencenterx =  Math.abs((int) ((screenwidth/2)-touchX));
			int distancefromscreencentery =  Math.abs((int) ((screenwidth/2)-touchY));

			//if its in the center, and its a click, we exit edit more
			if (event.getAction()==MotionEvent.ACTION_DOWN){				
				if ((distancefromscreencenterx<30)&&(distancefromscreencentery<40)){
					CurrentMode = EDIT_END_FLAG;
					move = 0;
				}	else {
					CurrentMode = EDIT_MODE;
				}
			}
			if ((event.getAction()==MotionEvent.ACTION_CANCEL)||(event.getAction()==MotionEvent.ACTION_UP )){
				Log.d("action","=cancel movement");
				move = 0;

				return false;
			}


			Log.i("touch","screen from center= "+distancefromscreencenterx);

			move = (int)(((screenheight/2)-touchY)/100);

			Log.i("touch","move from center= "+(move));

			//(maybe later we can change this to a funky multi-touch pinch gesture?)



		} else {
			Camera camera = world.getCamera();

			SimpleVector dir=Interact2D.reproject2D3DWS(world.getCamera(), fb,touchX, touchY);

			Object[] res=world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 1000 /*or whatever*/);

			if (res[1]!=null){

				//If its canceling;				

				if ((event.getAction()==MotionEvent.ACTION_CANCEL)||(event.getAction()==MotionEvent.ACTION_UP )){
					//Dont cancel is on the menu!
					if (!isOnContextMenu){
						Log.d("action","cancel movement so clear color and unset object");

						if (CurrentObject!=null){
							CurrentObject.clearAdditionalColor();
							CurrentObject.setSpecularLighting(true);
						} 				
						CurrentObject=null;
						newobject_distance=25;
					}

				} else {

					// else
					Log.d("action","setting object");

					//get the object number and distance
					Object3D pickedObject = (Object3D)res[1];	
					Object distance = (Object)res[0];
					float dis = ((float)((Float)distance).floatValue());


					Log.i("touch", "object="+pickedObject.getID());
					Log.i("touch", "distance="+dis);

					//if theres an old object,reset its color
					if (CurrentObject!=null){
						CurrentObject.clearAdditionalColor();
						CurrentObject.setSpecularLighting(true);
					} 	

					//set as current object
					CurrentObject = pickedObject;
					CurrentObject.setAdditionalColor(RGBColor.RED);
					CurrentObject.setSpecularLighting(false);

					//set current distance
					newobject_distance = (int)dis;

					//fill in submission fields to match current objects existing data 
					String BlipsID = CurrentObject.getName();
					ARBlip currentBlip = this.getBlipFromScene(BlipsID);
					//set content
					start.AddBlipText.setText(currentBlip.ObjectData);
					//set occlusion status
					//set all other things except position/rot which are set automaticly by placement.

					//===
				}
			} else {

				if (CurrentObject!=null){
					CurrentObject.clearAdditionalColor();
					CurrentObject.setSpecularLighting(true);
				} 				
				CurrentObject=null;
				newobject_distance=25;
			}


		}


		return super.onTouchEvent(event);
	}


	public void createBlipInFrontOfCamera() {

		// first we get the current camera location
		// double x = currentRealLocation.getLatitude();
		// double y = currentRealLocation.getLongitude();

		float x = world.getCamera().getPosition().x;
		float y = world.getCamera().getPosition().y;
		float z = world.getCamera().getPosition().z;

		// and the current camera direction
		SimpleVector direction = world.getCamera().getDirection();
		Log.i("add", "current directionx=" + direction.x);
		Log.i("add", "current directiony=" + direction.y);
		Log.i("add", "current directionz=" + direction.z);

		// and the current distance to use from the camera
		newobject_distance = 25;

		Object3D temptest = new Rectangle(1, 8, 3);

		temptest.translate(x, y, z);
		temptest.align(world.getCamera());

		// SimpleVector test = temptest.getZAxis();
		// test.scalarMul(5);
		// temptest.translate(test);

		temptest.setAdditionalColor(RGBColor.BLACK);

		world.addObject(temptest);

		temptest.build();

		CurrentObject = temptest;

		// set edit mode on
		CurrentMode = EDIT_MODE;

		// start edit mode timer

		// then we add the blip
		/*
		 * ARBlip newblip = new ARBlip();
		 * 
		 * 
		 * newblip.x = x; newblip.y = y; newblip.z = 0; newblip.BlipID =
		 * "_NEWBLIP_"; newblip.ObjectData = "(newly created blip)"; 1 try {
		 * this.addBlip(newblip); } catch (IOException e) { // TODO
		 * Auto-generated catch block Log.i("add", "io exception");
		 * 
		 * }
		 */

	}

	/** finish an object creation **/
	public void confirmObjectCreation() {

		// get internal co-ordinates

		double x = CurrentObject.getTranslation().x;
		double y = CurrentObject.getTranslation().y;
		double z = CurrentObject.getTranslation().z;

		double obj_roll = Math.toDegrees(CurrentObject.getZAxis().x);

		double obj_baring = Math.toDegrees(CurrentObject.getZAxis().y);

		double obj_elevation = Math.toDegrees(CurrentObject.getZAxis().z);

		Log.e("add", "roation x =" + obj_roll + " z = " + obj_elevation
				+ " y (baring) = " + obj_baring);

		// get baring from noth
		// tan a = o/a
		double baring = Math.toDegrees(Math.atan2(x, z));
		double distance = Math.hypot(x, z);

		Log.e("add", "baring =" + baring);
		Log.e("add", "distance =" + distance);

		// work out real world co-ordinates

		Log.i("add", "base Long = " + startingLocation.getLongitude());
		Log.i("add", "base Lat = " + startingLocation.getLatitude());

		Log.i("add", "displacing by = " + distance + " at " + baring);

		Location newLocation = ARBlipUtilitys.displaceLocation(
				startingLocation, distance, baring + 90); // baring is 90
		// degree's off!

		// remove tempobject
		world.removeObject(CurrentObject);

		// int i=0;
		// while (i<10) {
		// i=i+1;
		// temptest
		ARBlip newblip = new ARBlip();
		newblip.x = newLocation.getLatitude();
		newblip.y = newLocation.getLongitude();
		newblip.z = -y; // note the co-ordinate switch. ARBlips have "z" as
		// vertical, but the game engine use's "y" as the
		// vertical.

		// newblip.baring = (int) obj_baring;
		// newblip.elevation = (int) obj_elevation;
		// newblip.roll =(int) obj_roll;

		newblip.isFacingSprite = true;
		newblip.BlipID = "_NEWBLIP_" + Math.random(); // crude tempID only
		int randomint = (int) (Math.random()*1000);
		newblip.ObjectData = "(newly blip:"+randomint+")";
		/*
		try {
			Log.i("add", "creating blip:" + newblip.BlipID);
			//this.addBlip(newblip);
		} catch (IOException e) {
			//Log.i("add", "io exception");
		}*/

		// update blip submit page and bring it to the front so people can fill
		// in the text

		// --------------------------

		start.sendToAddBlipPage(newblip);

		// -----------------------

		// set 3d scene back to viewing mode
		CurrentMode = VIEWING_MODE;
	}

	/** cancel an object creation **/
	public void cancelObjectCreation() {

		CurrentMode = VIEWING_MODE;
		world.removeObject(CurrentObject);

	}

	/** Made as a test **/
	public void creatingBouncingCone() {

		
	}

	public void setCameraOrentation(float x, float y, float z) {
		updateCamRotation = true;
		newCameraX = x;
		newCameraY = y;
		newCameraZ = z;
	}

	/** Sets the camera orientation **/
	public void setCameraOrientation(SimpleVector xyzAngles) {

		if (worldReadyToGo) {
			// Camera worldcam = world.getCamera();

			// worldcam.getBack().setIdentity();

			float Z = xyzAngles.z;
			float Y = xyzAngles.y;
			float X = xyzAngles.x;

			updateCamRotation = true;
			newCameraX = X;
			newCameraY = -Y;
			newCameraZ = -Z;

			// worldcam.rotateCameraAxis(new SimpleVector(0,1,0), -Z);
			// worldcam.rotateCameraAxis(new SimpleVector(1,0,0), X);
			// worldcam.rotateCameraAxis(new SimpleVector(0,0,1), -Y);

			// worldcam.rotateCameraAxis(new SimpleVector(0,1,0),
			// -0.081920266f);
			// worldcam.rotateCameraAxis(new SimpleVector(1,0,0), 0.5303804f);
			// worldcam.rotateCameraAxis(new SimpleVector(0,0,1), 0.84379244f);

			// Log.i("GameCamera",
			// ","+worldcam.getDirection().x+","+worldcam.getDirection().y+","+worldcam.getDirection().z+",");

		}
		/*
		 * //we just set the numbers for display for now
		 * 
		 * float X = (float) Math.toRadians(xyzAngles.x); newCameraX
		 * =xyzAngles.x;
		 * 
		 * float Y = (float) Math.toRadians(xyzAngles.y); newCameraY
		 * =xyzAngles.y;
		 * 
		 * float Z = (float) Math.toRadians(xyzAngles.z); newCameraZ
		 * =xyzAngles.z;
		 * 
		 * 
		 * if (worldReadyToGo && world.getCamera() !=null){ //Camera worldcam =
		 * world.getCamera();
		 * 
		 * //worldcam.getBack().setIdentity(); //seems to reset the camera for
		 * the relative rotations below // worldcam.setOrientation(new
		 * SimpleVector(0,1,0), new SimpleVector(0,0,1)); //point down
		 * 
		 * 
		 * //world.getCamera().rotateCameraX(xyzAngles.x);
		 * //world.getCamera().rotateCameraY(xyzAngles.y);
		 * 
		 * //world.getCamera().rotateX(xyzAngles.x); updateCamRotation = true;
		 * 
		 * float X = (float) Math.toRadians(xyzAngles.x); newCameraX =X;
		 * 
		 * float Y = (float) Math.toRadians(xyzAngles.y); newCameraY =Y;
		 * 
		 * float Z = (float) Math.toRadians(xyzAngles.z); newCameraZ =Z;
		 * 
		 * 
		 * //Log.i("__x","_"+xyzAngles.x); Log.i("__y","_"+Y); //
		 * Log.i("__z","_"+xyzAngles.z);
		 * 
		 * 
		 * 
		 * }
		 */
	}

	/** deletes a blip from the 3d view **/
	public void deleteBlip(ARBlip removethis) {

		// get the ID to remove
		String BlipID = removethis.BlipID;
		this.deleteBlip(BlipID);

	}

	/** deletes a blip from the 3d view **/
	public void deleteBlip(String BlipsID) {

		Log.d("deleteing", "deleteing blips2");
		// ensure it exists and isn't already in delete queue
		if (this.isBlipInScene(BlipsID) == true
				&& (!deleteQueue.contains(deleteQueue))) {
			// get the ID to remove
			// String BlipID = removethis.BlipID;

			// find it by its blip ID
			Object3D object = world.getObjectByName(BlipsID);

			// remove it from storage

			Iterator<ARBlipObject> it = CurrentActiveLayer.LayersObjects.iterator();
			while (it.hasNext()) {
				ARBlip currentBlip = it.next().arblip;
				if (currentBlip.BlipID.equals(BlipsID)) {

					//deletes object from storage
					//scenesARBlipObjects.remove(currentBlip);
					CurrentActiveLayer.LayersObjects.remove(currentBlip);
				}

			}

						// remove from scene objects
			//scenesObjects.remove(object); //(not need anymore)

			Log.d("deleteing", "deleteing blips4");

			// remove it from view

			Log.i("deleteing", "object=" + object.getName());

			// add to delete queue

			// world.removeObject(object);

			//test get object
			//world.getObjectByName("rock");
			//world.removeObject(rock);
			//Object3D testob = new SkywriterBillboard();

			//testob.setName("__");
			// set texture
			//String text = "test test";
			//updatedTexture("_temp_", text);
			// newplane.setOrigin(new SimpleVector(0,-15,0));(used to move
			// it upwards for when it was on a stand)
			//	testob.setTexture("_temp_");




			Object3D meep = world.getObjectByName(BlipsID);
			deleteQueue.add(meep);// <--causes a crash later :-/


		}

	}

	public SimpleVector deriveAngles(Matrix mat) {
		SimpleVector s = new SimpleVector();
		float[] m = mat.getDump();
		s.x = (float) Math.atan(m[9] / m[10]);
		s.y = (float) Math.asin(-m[2]);
		s.z = (float) Math.atan(m[4] / m[0]);
		return s;
	}

	/** Sets the camera orientation **/
	public void setCameraOrientation(Matrix ma) {

		if (ma != null) {
			// Log.i("--", "setting camera1");
			CameraMatrix = ma;
			TestVar = (int) (Math.random() * 10);
			updateCamRotation = true;
			if (worldReadyToGo) {
				// Log.i("--", "setting camera2");
				// world.getCamera().setBack(CameraMatrix);
			}

		}

		// SimpleVector newDirection = deriveAngles(ma);

		// setCameraOrentation(newDirection,new SimpleVector(0,0,1));

	}

	/** Sets the camera orientation **/
	public void setCameraOrientation(SimpleVector dir, SimpleVector up) {
		world.getCamera().setOrientation(dir, up);

	}

	/** a map tile is just flat object with a map on it **/
	/**
	 * in future, when the protocol is more worked out, this method could
	 * probably just use a standard arblip for a textured plan
	 */
	/**
	 * for now, these are positioned on the ground...maybe the sky would be
	 * nicer
	 **/

	public void addMapTile(LocatedMapBundle loc) {
		Object3D MapTile = Primitives.getPlane(1, 378);

		// we should exit if map exists already!
		// --world.getObjectByName("MapTile_"+loc.lat+"_"+loc.lon);
		// --

		MapTile.setName("MapTile_" + loc.lat + "_" + loc.lon);

		Log.i("dis", "MapTile_" + loc.lat + "_" + loc.lon);

		TextureManager tm = TextureManager.getInstance();
		Texture maptiletexture = new Texture(loc.centerMap, true); // the true
		// specifys
		// the
		// texture
		// has its
		// own
		// alpha. If
		// not,
		// black is
		// assumed
		// to be
		// alpha!
		tm.addTexture("MapTile_" + loc.lat + "_" + loc.lon, maptiletexture);
		MapTile.setTexture("MapTile_" + loc.lat + "_" + loc.lon);
		MapTile.setAdditionalColor(RGBColor.WHITE);

		// position
		double worldX = ARBlipUtilitys.getRelativeXLocation(loc.lat,
				startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldY = ARBlipUtilitys
		.getRelativeYLocation(0, startingLocation); // As the world is
		// set on loading,
		// and then the
		// camera moves, we
		// always messure
		// relative to the
		// loading location.
		double worldZ = ARBlipUtilitys.getRelativeZLocation(loc.lon,
				startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.

		float x = (float) -worldX;
		float y = (float) -worldY;
		float z = (float) worldZ;

		MapTile.translate(x, y, z);
		// add to world
		Log.i("dis", "adding map tile at " + x + " " + y + " " + z);

		MapTile.rotateX((float) Math.PI / 2f);
		MapTile.rotateY((float) -Math.PI / 2f);

		world.addObject(MapTile);

		world.buildAllObjects();

	}

	public void updateLocation(Location current) {
		double worldX = ARBlipUtilitys.getRelativeXLocation(current
				.getLatitude(), startingLocation); // As the world is set on
		// loading, and then the
		// camera moves, we always
		// messure relative to the
		// loading location.
		double worldY = ARBlipUtilitys
		.getRelativeYLocation(0, startingLocation); // As the world is
		// set on loading,
		// and then the
		// camera moves, we
		// always messure
		// relative to the
		// loading location.
		double worldZ = ARBlipUtilitys.getRelativeZLocation(current
				.getLongitude(), startingLocation); // As the world is set on
		// loading, and then the
		// camera moves, we always
		// messure relative to the
		// loading location.

		float x = (float) -worldX;
		float y = (float) -worldY;
		float z = (float) worldZ;
		// update camera location
		Camera cam = world.getCamera();
		cam.setPosition(x, cameraHeight, z); // we might want to animate this at
		// some point

		// if map showing, then update it (or try to)
		// if (MapModeSet){
		//			
		// try {
		// LocatedMapBundle currentmap = StaticMapFetcher.getMap(current);
		// this.addMapTile(currentmap);
		//								
		// } catch (MalformedURLException e) {
		//						
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// Log.e("mapE","io exception");
		// }
		//			
		// }

	}

	/** Supposed to set the subs rotation **/
	public void setSunRotation(SimpleVector degrees) {

		// arg, this works relatively..no clue how to fix this
		degrees = new SimpleVector(0, 0.05f, 0);

		sun.rotate(degrees, groundPlane.getTransformedCenter());

	}

	/** adds a new ARBlip to the currently active ARWaveLayer 
	 *  (currently there is only one ARWaveLayer) **/
	/**
	 * @throws IOException
	 **/

	public void addBlip(ARBlip newblip) throws IOException {

		// check blip isnt already in scene
		if (this.isBlipInScene(newblip) == true) {
			
			// if so, then we update it
			Log.e("blip", "already exists");
			updateBlip(CurrentActiveLayer,newblip);

		} else {
			
			Object3D newmarker = CurrentActiveLayer.createNewBlipObject(newblip);			
			
			Log.i("3ds", "adding " + newblip.BlipID + " to scene");
			
			world.addObject(newmarker);

			world.buildAllObjects();
	
			
		}

	}

	static void fetchAndSwapTexturesFromURL(Iterator<String> it, String URLPath)
	throws MalformedURLException, IOException {
		while (it.hasNext()) {

			String textureNameToLoad = (String) it.next();
			Log.i("3ds", "Loading Texture:-" + textureNameToLoad);

			// get the url
			// NOTE; filename must be lower case
			// In future we might want to check for uppercase too.
			// Unfortuntely, 3dsmax stores all its filenames uppercase
			// internaly, so we cant have mixed cases
			// in filenames, as they wont be recognised.
			String TextureURL = URLPath + textureNameToLoad.toLowerCase();

			Log.i("3d", "getting texture at " + TextureURL);

			// make the texture
			URL texturedownloadfrom = new URL(TextureURL);
			URLConnection textureconnection = texturedownloadfrom
			.openConnection();

			textureconnection.connect();
			BufferedInputStream texturebis = new BufferedInputStream(
					textureconnection.getInputStream());

			Bitmap maxtexture1 = BitmapFactory.decodeStream(texturebis);
			TextureManager tm = TextureManager.getInstance();
			Texture newmaxtexture = new Texture(maxtexture1);

			// swap it in
			// Note; This automaticaly assigns the correct textures onto the
			// model, because
			// when the model was loaded, it was assigned the file names in the
			// 3ds file for its texture names.
			// neat eh?
			tm.replaceTexture(textureNameToLoad, newmaxtexture);

		}
	}

	/** updates a texture to a bit of text **/
	public static void updatedTexture(String Texturename, String text) {

		Log.i("add", "update texture triggered with:"+Texturename+"|"+text);

		paint.setColor(Color.BLACK);

		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		FontMetricsInt fontMetrics = paint.getFontMetricsInt();
		int fontHeight = fontMetrics.leading - fontMetrics.ascent
		+ fontMetrics.descent;
		int baseline = -fontMetrics.top;
		int height = fontMetrics.bottom - fontMetrics.top;

		// have to add multiline support here
		Bitmap charImage = Bitmap.createBitmap(closestTwoPower((int) paint
				.measureText(text) + 10), 32, config);

		Canvas canvas = new Canvas(charImage);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(text, 10, baseline, paint); // draw text with a margin
		// of 10

		TextureManager tm = TextureManager.getInstance();
		Texture testtext = new Texture(charImage, true); // the true specify
		
		// the texture has
		// its own alpha. If
		// not, black is
		// assumed to be
		// alpha!

		//

		if (tm.containsTexture(Texturename)) {

			Log.i("add", "updating texture="+Texturename);

			//tm.removeAndUnload(Texturename,fb);

			Log.i("add", "updated texture="+Texturename);

			//tm.addTexture(Texturename, testtext);
			tm.unloadTexture(fb, tm.getTexture(Texturename));
			tm.replaceTexture(Texturename, testtext);


		} else {
			tm.addTexture(Texturename, testtext);
		}

	}

	/**
	 * returns the closest power of two that is equal or greater than given
	 * number (good for textures!
	 */
	private static int closestTwoPower(int i) {
		int power = 1;
		while (power < i) {
			power <<= 1;
		}
		return power;
	}

	/** if a wave with the specified ID is open already, we set its visibility **/
	public void setWaveVisiblity(String WaveID, boolean Visible){
		
		Iterator<ARWaveLayer> OpenWaves = AllLayersOpen.iterator();
		Log.i("wave", "setting wave:"+WaveID+"");
		
		while (OpenWaves.hasNext()){			
			ARWaveLayer Wave = OpenWaves.next();
			if (Wave.ARWaveLayerID.equals(WaveID)){
				Log.i("wave", "setting wave2:"+Wave.ARWaveLayerID+"");
				Wave.setVisible(Visible);
			}			
		}
		
		//if not already open, then we open it
		
		// (insert code here) 
		
		
		
	}
	
	/**
	 * updates an existing blip in the specified scene crashs if blip/object doesnt exist
	 **/

	public void updateBlip(ARWaveLayer scene, ARBlip newblipdata) {
		
		Log.i("3ds", "updating1");
		scene.updateBlip(newblipdata);
		
		/*
		Object3D updateThis = world.getObjectByName(newblipdata.BlipID);

		// update location (oddly, you have to clear the location and then
		// "move" the position, rather then merely setting it)
		// (you can use setOrigin to directly set it, but this wont move child
		// objects)

		// updateThis.setTranslationMatrix(new Matrix());

		double worldX = ARBlipUtilitys.getRelativeXLocation(newblipdata,
				startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldY = ARBlipUtilitys.getRelativeYLocation(newblipdata,
				startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldZ = ARBlipUtilitys.getRelativeZLocation(newblipdata,
				startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.

		float x = (float) -worldX;
		float y = (float) -worldY;
		float z = (float) worldZ;
		// Log.i("3ds","moving to ="+worldX+" , "+worldY+" , "+worldZ);

		updateThis.setTranslationMatrix(new Matrix());
		updateThis.translate(new SimpleVector(x, y, z));

		// update rotation
		updateThis.setRotationMatrix(new Matrix());
		updateThis.rotateX((float) Math.toRadians(newblipdata.roll));
		updateThis.rotateY((float) Math.toRadians(newblipdata.baring));
		updateThis.rotateZ((float) Math.toRadians(newblipdata.elevation));

		// update textures
		String text = newblipdata.ObjectData;
		updatedTexture(newblipdata.BlipID, text);

		// update other stuff
*/
	}

	public boolean isBlipInScene(String BlipsID) {

		Log.i("blip","checking for "+BlipsID+" in current active layer");

		Iterator<ARBlipObject> it = CurrentActiveLayer.LayersObjects.iterator();
		while (it.hasNext()) {
			String currentID = it.next().arblip.BlipID;
			Log.i("check","checking for "+BlipsID+" against "+currentID);

			if (currentID.equals(BlipsID)) {
				return true;
			}

		}

		return false;
	}
	/** returns a blip from a blipID **/
	public ARBlip getBlipFromScene(String BlipsID) {

		Log.i("blip","checking for "+BlipsID+" in current active layer");

		Iterator<ARBlipObject> it = CurrentActiveLayer.LayersObjects.iterator();
		while (it.hasNext()) {
			ARBlip currentblip = it.next().arblip;
			String currentID = currentblip.BlipID;
			Log.i("check","checking for "+BlipsID+" against "+currentID);

			if (currentID.equals(BlipsID)) {
				return currentblip;
			}

		}

		return null;
	}

	/** Unoptimised check for pre-existing blip */
	public boolean isBlipInScene(ARBlip blipToCheck) {

		// if (world.getObjectByName(blipToCheck.BlipID)==null)
		// {
		// Log.i("3d","object doesnt exist");
		// }

		// is the blip loaded
		String BlipsID = blipToCheck.BlipID;
		boolean exists = this.isBlipInScene(BlipsID);

		/*
		 * Iterator<ARBlip> it = scenesBlips.iterator(); while (it.hasNext()) {
		 * 
		 * if (it.next().BlipID.equals(BlipID)){ return true; }
		 * 
		 * }
		 */

		return exists;

	}

	public void toggleBackgroundScenary() {

		backgroundScenaryVisible = !backgroundScenaryVisible;
		this.setBackgroundScenary(backgroundScenaryVisible);
		LocalBackgroundScenaryLayer.setVisible(backgroundScenaryVisible);
		
	}

	public void setBackgroundScenary(Boolean backgroundScenaryVisible){

		this.backgroundScenaryVisible = backgroundScenaryVisible;
		LocalBackgroundScenaryLayer.setVisible(backgroundScenaryVisible); 

	}



	/** Map mode **/
	/**
	 * This will add a google map image as the ground plan and set camera
	 * overhead
	 **/
	public void setMapMode(boolean setMapMode, LocatedMapBundle MapBundle) {
		MapModeSet = setMapMode;

		if (MapModeSet) {

			this.addMapTile(MapBundle);
			// set camera overhead
			Camera cam = world.getCamera();
			cam.setPosition(cam.getPosition().x, -250, cam.getPosition().z); // we
			// might
			// want
			// to
			// animate
			// this
			// at
			// some
			// point
			cam.lookAt(groundPlane.getTransformedCenter());
			groundPlane.setVisibility(false);
		} else {
			groundPlane.setVisibility(true);
			groundPlane.setTexture("grassy");
		}

		/*
		 * if (MapModeSet){ //set map mode on
		 * 
		 * 
		 * TextureManager tm = TextureManager.getInstance();
		 * 
		 * 
		 * Texture testtext = new Texture(Map.map, true); //the true specifys
		 * the texture has its own alpha. If not, black is assumed to be alpha!
		 * 
		 * if (tm.containsTexture("MapTexture")) {
		 * tm.removeTexture("MapTexture"); tm.unloadTexture(fb,
		 * tm.getTexture("MapTexture")); tm.addTexture("MapTexture", testtext);
		 * } else { tm.addTexture("MapTexture", testtext); }
		 * 
		 * //set to ground plane groundPlane.setTexture("MapTexture");
		 * 
		 * //set new position groundPlane.setTranslationMatrix(new Matrix());
		 * groundPlane.translate(new SimpleVector(Map.xDis, 0, Map.yDis));
		 * 
		 * //set camera overhead Camera cam = world.getCamera();
		 * cam.setPosition(Math.round(Map.xDis), -250, Math.round(Map.yDis));
		 * //we might want to animate this at some point
		 * cam.lookAt(groundPlane.getTransformedCenter());
		 * 
		 * } else { groundPlane.setTexture("grassy"); }
		 */

	}

	class MyRenderer implements GLSurfaceView.Renderer {

		private Texture numberFont = null;

		Object3D cmtree = null;
		Object3D cmtree2 = null;
		Object3D cmtree3 = null;
		Object3D atmos = null;
		Object3D atmos2 = null;
		//
		private int fps = 0;
		private int lfps = 0;

		private long time = System.currentTimeMillis();

		private boolean stop = false;

		private float ind;

		private boolean deSer = true;

		public MyRenderer() {
			Config.maxPolysVisible = 5000;
			Config.farPlane = 1500;
		}

		public void stop() {
			stop = true;
			if (fb != null) {
				fb.dispose();
				fb = null;
			}
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);
			
			if (start.surface_activity_master == null) {	
				
				setupWorld();
				Log.i("____","_______________saving surface data");
				
				start.surface_activity_master = ARWaveView.this;
				
				//start.saveMaster();
				
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//	setupWorld();
			


		}

		private void setupWorld() {
			TextureManager.getInstance().flush();
			world = new World();
			

			Object3D tree2 = null;
			Object3D tree1 = null;
			Object3D grass = null;
			Object3D rock = null;
			
			Resources res = getResources();

			TextureManager tm = TextureManager.getInstance();

			//set up default textures

			//Occulsion texture, for making things mask out the enviroment and show the camera view
			Bitmap charImage = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888 );
			Canvas canvas = new Canvas(charImage);
			canvas.drawColor(Color.TRANSPARENT);			
			Texture occlusion = new Texture(charImage, true); // the true specify
			tm.addTexture("occlusion", occlusion);
			//-------------------
			
			// set up pre-made landscape
			Texture grass2 = new Texture(res.openRawResource(R.raw.grassy));
			Texture cmtreetexture =  new Texture(res.openRawResource(R.raw.cmtree));
			Texture snow =  new Texture(res.openRawResource(R.raw.snw));
			
			Texture leaves = new Texture(res.openRawResource(R.raw.tree2y));
			Texture leaves2 = new Texture(res.openRawResource(R.raw.tree3y));
			Texture rocky = new Texture(res.openRawResource(R.raw.rocky));

			Texture planetex = new Texture(res.openRawResource(R.raw.planetex));

			numberFont = new Texture(res.openRawResource(R.raw.numbers));

			tm.addTexture("grass2", grass2);
			tm.addTexture("cmtree", cmtreetexture);
			tm.addTexture("leaves", leaves);
			tm.addTexture("leaves2", leaves2);
			tm.addTexture("rock", rocky);
			tm.addTexture("grassy", planetex);
			tm.addTexture("snow",snow);
			
			if (!deSer) {
				// Use the normal loaders...
				groundPlane = Primitives.getPlane(1, 378);
				grass = Loader.load3DS(res.openRawResource(R.raw.grass), 5)[0];
				rock = Loader.load3DS(res.openRawResource(R.raw.rock), 15f)[0];				
				tree1 = Loader.load3DS(res.openRawResource(R.raw.tree2), 2)[0];
				tree2 = Loader.load3DS(res.openRawResource(R.raw.tree3), 6)[0];
				cmtree = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];
				cmtree2 = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];
				cmtree3 = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];
				
				groundPlane.setTexture("grassy");
				rock.setTexture("rock");
				grass.setTexture("grass2");				
				tree1.setTexture("leaves");
				tree2.setTexture("leaves2");

				cmtree = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];				
				cmtree.setTexture("cmtree");
				atmos = Loader.load3DS(res.openRawResource(R.raw.atmosphere), 2)[0];				
				atmos.setTexture("snow");
				atmos2= Loader.load3DS(res.openRawResource(R.raw.atmosphere2), 2)[0];				
				atmos2.setTexture("snow");
				
			} else {
				// Load the serialized version instead...
				groundPlane = Primitives.getPlane(1, 378);
				groundPlane.setTexture("grassy");
				// plane =
				// Loader.loadSerializedObject(res.openRawResource(R.raw.serplane));
				rock = Loader.loadSerializedObject(res
						.openRawResource(R.raw.serrock));
				
				//cmtree = Loader.loadSerializedObject(res
				//		.openRawResource(R.raw.sertree1));
				
				tree1 = Loader.loadSerializedObject(res
						.openRawResource(R.raw.sertree1));
				tree2 = Loader.loadSerializedObject(res
						.openRawResource(R.raw.sertree2));
				grass = Loader.loadSerializedObject(res
						.openRawResource(R.raw.sergrass));
			
				//these should be seralised to speed up loading
				cmtree = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];				
				cmtree.setTexture("cmtree");
				cmtree2 = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];
				cmtree2.setTexture("cmtree");
				cmtree3 = Loader.load3DS(res.openRawResource(R.raw.ct), 2)[0];
				cmtree3.setTexture("cmtree");
				atmos = Loader.load3DS(res.openRawResource(R.raw.atmosphere), 2)[0];				
				atmos.setTexture("snow");
				atmos2= Loader.load3DS(res.openRawResource(R.raw.atmosphere2), 2)[0];				
				atmos2.setTexture("snow");
			
			}

			grass.translate(-90, -34, -100);
			grass.rotateZ((float) Math.PI);
			
			cmtree.translate(-30, -15, 25);
			cmtree.rotateX(-(float) Math.PI /2);
			cmtree2.translate(0, -15, -50);
			cmtree2.rotateX(-(float) Math.PI /2);
			cmtree3.translate(85, -15, 45);
			cmtree3.rotateX(-(float) Math.PI /2);
			
			atmos.rotateX(-(float) Math.PI /2);
			atmos2.rotateX(-(float) Math.PI /2);
			atmos.setCulling(Object3D.CULLING_DISABLED);
			atmos2.setCulling(Object3D.CULLING_DISABLED);
			atmos.setTransparency(30);
			atmos2.setTransparency(30);
			
			rock.translate(0, 0, 0);
			rock.rotateX(-(float) Math.PI / 2);
			tree1.translate(-189, -34, 0);
			tree1.rotateZ((float) Math.PI);
			tree2.translate(0, -34, 189);
			tree2.rotateZ((float) Math.PI);
			groundPlane.rotateX((float) Math.PI / 2f);
			groundPlane.rotateY((float) -Math.PI / 2f);
			groundPlane.translate(0, 0, 0);
			groundPlane.setName("plane");
			tree1.setName("tree1");
			// tree2.setName("tree2");
			grass.setName("grass");
			cmtree.setName("cmtree");
			rock.setName("rock");

			rock.scale(0.1f);

			// note the rock is at 0,0,0 to mark the center point.
			// the tree is at -200,-180,0
			// tree2 is at 0,-190,200)
			world.addObject(groundPlane);
			world.addObject(tree1);
			world.addObject(cmtree);
			world.addObject(cmtree2);
			world.addObject(cmtree3);
			world.addObject(atmos);
			world.addObject(atmos2);
			world.addObject(tree2);
			world.addObject(grass);
			world.addObject(rock);

			RGBColor dark = new RGBColor(100, 100, 100);

			grass.setTransparency(10);
			tree1.setTransparency(0);
			tree2.setTransparency(0);
			
			
			
			
			tree1.setAdditionalColor(dark);
			tree2.setAdditionalColor(dark);
			grass.setAdditionalColor(dark);
			//cmtree.setAdditionalColor(dark);
			atmos.setAdditionalColor(RGBColor.WHITE);
			atmos2.setAdditionalColor(RGBColor.WHITE);
			//set up atmos animation
			Timer atmosTimer = new Timer();
			
			TimerTask atmosphereAnimation = new TimerTask() {
				int h=-130;
				int h2=-130;
				@Override
				public void run() {					
					h=h+1;
					if (h>120){
						h=-130;		
						}
					h2=h2+4;
					if (h2>120){
						h2=-130;		
						}
					
					atmos.setTranslationMatrix(new Matrix());
					atmos.translate(0, h, 0);
					
					atmos2.setTranslationMatrix(new Matrix());
					atmos2.translate(0, h2-30, 0);
					
				}				
			};
			atmosTimer.scheduleAtFixedRate(atmosphereAnimation, 500, 100);
			
			world.setAmbientLight(20, 20, 20);
			world.buildAllObjects();

			//add objects to default layer
			LocalBackgroundScenaryLayer.addObject(new ARBlipObject(grass));
			LocalBackgroundScenaryLayer.addObject(new ARBlipObject(tree1));
			LocalBackgroundScenaryLayer.addObject(new ARBlipObject(tree2));
			LocalBackgroundScenaryLayer.addObject(new ARBlipObject(rock));
			LocalBackgroundScenaryLayer.addObject(new ARBlipObject(groundPlane));	
			LocalBackgroundScenaryLayer.setVisible(backgroundScenaryVisible);			
			//-----
			// This can be removed when not in season....
			SpecialChristmassScenaryLayer.addObject(new ARBlipObject(atmos));
			SpecialChristmassScenaryLayer.addObject(new ARBlipObject(atmos2));
			SpecialChristmassScenaryLayer.addObject(new ARBlipObject(cmtree));	
			//-----
			
			
			
			
			
			sun = new Light(world);

			Camera cam = world.getCamera();
			cam.moveCamera(Camera.CAMERA_MOVEUP, 100);
			cam.lookAt(groundPlane.getTransformedCenter());

			cam.setFOV(0.5f); // used to be 1.5, 0.5 seemed closer to my phones
			// camera -thomas
			sun.setIntensity(250, 250, 250);

			SimpleVector sv = new SimpleVector();
			sv.set(groundPlane.getTransformedCenter());
			sv.y -= 300;
			sv.x -= 100;
			sv.z += 200;
			sun.setPosition(sv);


			
			//set up lensflare
			if (LensFlareOn)
		    {   //set up textures
				Texture flare_maintexture = new Texture(res.openRawResource(R.raw.mainflare2));
                tm.addTexture("flare_maintexture", flare_maintexture);
				
                //for a more pretty flare, we can use different textures for each halo.
				SceneLensFlare = new LensFlare(sun.getPosition(),"flare_maintexture", "flare_maintexture", "flare_maintexture","flare_maintexture");
				SceneLensFlare.setMaximumDistance(1000);
				SceneLensFlare.setTransparency(7);
				
		    
		    }
		}

		public void onDrawFrame(GL10 gl) {

			try {
				if (!stop) {
					if (paused) {
						Thread.sleep(500);
					} else {

						Camera cam = world.getCamera();
						if (turn != 0) {
							world.getCamera().rotateY(-turn);
						}

						if (touchTurn != 0) {
							world.getCamera().rotateY(touchTurn);
							touchTurn = 0;
						}

						if (touchTurnUp != 0) {
							world.getCamera().rotateX(touchTurnUp);
							touchTurnUp = 0;

						}

						// If theres objects in the delete queue

						if (deleteQueue.size() > 0) {

							Iterator<Object3D> dqit = deleteQueue.iterator();
							while (dqit.hasNext()) {
								Log.d("deleteing", "removing");

								Object3D toremove = dqit.next();
								world.removeObject(toremove);
								dqit.remove();
							}

						}


						if (CurrentMode == VIEWING_MODE) {
							if (move != 0) {

								world.getCamera().moveCamera(
										cam.getDirection(), move);

								/*
								 * FOV adjustment; float FOV =
								 * world.getCamera().convertDEGAngleIntoFOV(
								 * Math.abs(move));
								 * 
								 * if (move>0){
								 * 
								 * world.getCamera().increaseFOV(FOV); } else {
								 * world.getCamera().decreaseFOV(FOV); }
								 * Log.i("FOV", "="+world.getCamera().getFOV());
								 */
							}
						}

						if ((CurrentMode == EDIT_MODE)||(CurrentMode == EDIT_END_FLAG)) {

							CurrentObject.setTranslationMatrix(new Matrix());

							SimpleVector CameraPosition = world.getCamera()
							.getPosition();

							CurrentObject.translate(CameraPosition);

							// change distance of last added object
							CurrentObject.align(world.getCamera());
							SimpleVector test = CurrentObject.getZAxis();
							newobject_distance = newobject_distance + move;

							//not closer then 2 meters
							if (newobject_distance<2){
								newobject_distance=2;
							}

							test.scalarMul(newobject_distance);
							CurrentObject.translate(test);

							// Log.i("add", "exiting adding mode"+move);

						}

						// set rotation

						/*
						 * if (updateCamRotation) {
						 * world.getCamera().getBack().setIdentity();
						 * world.getCamera().rotateX(newCameraX);
						 * world.getCamera().rotateY(newCameraY);
						 * world.getCamera().rotateZ(newCameraZ);
						 * updateCamRotation=false; }
						 */
						// world.getCamera().setBack(CameraMatrix);

						fb.clear();
						
						
						
						world.renderScene(fb);
						
						if (LensFlareOn){
							
							SceneLensFlare.update(fb, world);
							SceneLensFlare.render(fb);
							
							}
						
						world.draw(fb);

						blitNumber(lfps, 5, 5);
						if (currentRealLocation != null) {
							blitNumber(Math
									.round(Math.round(currentRealLocation
											.getLatitude() * 1E6)), 15, 25);
							blitNumber(Math
									.round(Math.round(currentRealLocation
											.getLongitude() * 1E6)), 15, 45);
							blitNumber(TestVar, 200, 25);
						}

						if (CurrentMode == EDIT_MODE) {
							// int currentXvalue = (int)
							// Math.toDegrees(CurrentObject.getZAxis().x);
							// int currentYvalue = (int)
							// Math.toDegrees(CurrentObject.getZAxis().y);
							// int currentZvalue = (int)
							// Math.toDegrees(CurrentObject.getZAxis().z);

							// Log.i("add",
							// "test x="+currentXvalue+" y "+currentYvalue+" z="+currentZvalue);

							// blitNumber(1000+currentYvalue, 50, 55);
						}
						if (updateCamRotation) {

							// Log.d("newx","x="+CameraMatrix.getXAxis().x);

							world.getCamera().getBack().setIdentity();

							updateCamRotation = false;
							world.getCamera().rotateCameraAxis(Z_AXIS,
									newCameraZ);
							world.getCamera().rotateCameraAxis(X_AXIS,
									newCameraX);
							world.getCamera().rotateCameraAxis(Y_AXIS,
									newCameraY);

							// worldcam.rotateCameraAxis(new
							// SimpleVector(0,1,0), -Z);
							// worldcam.rotateCameraAxis(new
							// SimpleVector(1,0,0), X);
							// worldcam.rotateCameraAxis(new
							// SimpleVector(0,0,1), -Y);

							// first we bleet a random number to help diagnoise
							/*
							 * if (showDebugInfo){
							 * 
							 * Log.i("debug", "__debug messages");
							 * 
							 * blitNumber(TestVar, 200, 25);
							 * 
							 * 
							 * blitNumber(Math.round(CameraMatrix.getXAxis().x*1000
							 * ), 5, 25);
							 * blitNumber(Math.round(CameraMatrix.getXAxis
							 * ().y*1000), 55, 25);
							 * blitNumber(Math.round(CameraMatrix
							 * .getXAxis().z*1000), 105, 25);
							 * blitNumber(Math.round
							 * (CameraMatrix.getYAxis().x*1000), 5, 45);
							 * blitNumber
							 * (Math.round(CameraMatrix.getYAxis().y*1000), 55,
							 * 45);
							 * blitNumber(Math.round(CameraMatrix.getYAxis()
							 * .z*1000), 105, 45);
							 * blitNumber(Math.round(CameraMatrix
							 * .getZAxis().x*1000), 5, 65);
							 * blitNumber(Math.round
							 * (CameraMatrix.getZAxis().y*1000), 55, 65);
							 * blitNumber
							 * (Math.round(CameraMatrix.getZAxis().z*1000), 105,
							 * 65); }
							 */

						}

						fb.display();

						// sun.rotate(new SimpleVector(0, 0.05f, 0),
						// groundPlane.getTransformedCenter());

						if (System.currentTimeMillis() - time >= 1000) {
							lfps = (fps + lfps) >> 1;
			fps = 0;
			time = System.currentTimeMillis();
						}
						fps++;
						ind += 0.02f;
						if (ind > 1) {
							ind -= 1;
						}
					}
				} else {
					if (fb != null) {
						fb.dispose();
						fb = null;

					}
				}
			} catch (Exception e) {
				Logger.log("Drawing thread terminated!", Logger.MESSAGE);
			}

			worldReadyToGo = true;

		}

		// Seems to be for making the hilly groundplane...probably not needed by
		// us, but
		// usefull as a referance for vectex manipulation.
		private class Mod extends GenericVertexController {
			private static final long serialVersionUID = 1L;

			public void apply() {
				SimpleVector[] s = getSourceMesh();
				SimpleVector[] d = getDestinationMesh();
				for (int i = 0; i < s.length; i++) {
					d[i].z = s[i].z
					- (10f * (FloatMath.sin(s[i].x / 50f) + FloatMath
							.cos(s[i].y / 50f)));
					d[i].x = s[i].x;
					d[i].y = s[i].y;
				}
			}
		}

		// seems to be for picking numbers from a texture and displaying them
		// for the framerate
		private void blitNumber(int number, int x, int y) {
			if (numberFont != null) {
				String sNum = Integer.toString(number);
				for (int i = 0; i < sNum.length(); i++) {
					char cNum = sNum.charAt(i);
					int iNum = cNum - 48;
					fb.blit(numberFont, iNum * 5, 0, x, y, 5, 9,
							FrameBuffer.TRANSPARENT_BLITTING);
					x += 5;
				}
			}
		}
	}

}
