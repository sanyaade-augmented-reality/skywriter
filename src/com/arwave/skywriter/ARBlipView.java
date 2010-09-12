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
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GenericVertexController;
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

/** This is our GLSurface view extended with ARBlip specific functions **/
public class ARBlipView extends GLSurfaceView {

	//The Real Location of the device when this surface was loaded
	public Location startingLocation;
	//the current real location of the camera
	public Location currentRealLocation;
	
	//our world, to which we add stuff
	private World world = null;	

	private Light sun = null;
	//scenary
	private Object3D groundPlane = null;
	private Object3D tree2 = null;
	private Object3D tree1 = null;
	private Object3D grass = null;
    private Object3D rock = null;
	
    private boolean backgroundScenaryVisible = true;
	//renderer
	MyRenderer renderer = null;
	 
	private FrameBuffer fb = null;
	
	//internal list of ARBlip objects
	ArrayList<ARBlip> scenesBlips = new ArrayList<ARBlip>();
	ArrayList<Object3D> scenesObjects = new ArrayList<Object3D>();

	//generic font
	private GLFont glFont;
	Paint paint = new Paint();
	//World setup flag
	boolean worldReadyToGo = false;
	
	//show debug info flag 
	public boolean showDebugInfo = false;
	
	//purely for testing
	//timer
	private TimerTask mTimerTask = null;
	private Timer mTimer = null;
	Object3D rotateingcube;
	int pos=0;
	boolean paused = false;
	int move = 0;
	float turn = 0;
	float touchTurn = 0;
	float touchTurnUp = 0;
	private Matrix CameraMatrix;
	private boolean updateCamRotation = false;
	float newCameraX =0;
	float newCameraY =0;
	float newCameraZ =0;
	final static SimpleVector Y_AXIS = new SimpleVector(0,0,1);	 //Fixed simple vectors used to rotate in one of 3 axis's
	final static SimpleVector X_AXIS = new SimpleVector(1,0,0);	
	final static SimpleVector Z_AXIS = new SimpleVector(0,1,0);	
	
	float cameraHeight = -3;
	
	int TestVar = 0;
	boolean MapModeSet = true;
	
	
	//delete queue 
	ArrayList<Object3D> deleteQueue = new ArrayList<Object3D>();
	
	//object editing
	Object3D CurrentObject; 
	int EditModeSecondsLeft =0; //edit mode ends automaticaly a few seconds after the last key is pressed.
	int newobject_distance=25;
	//modes
	int VIEWING_MODE = 0;
	int EDIT_MODE =1;
	int CurrentMode;
	
	
	public ARBlipView(Context context) {		
		super(context);
		
		
		this.setZOrderMediaOverlay(true);
		
		//mGLView.setBackgroundColor(Color.TRANSPARENT);
		//mGLView.setBackgroundDrawable(null);
		
		//getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		/*
		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});
*/
		
		
		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		Log.e("render", "setting render");
		
		renderer = new MyRenderer();
		this.setRenderer(renderer);
		
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		

		//set up generic font
		
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.create((String)null, Typeface.BOLD));
		
		paint.setTextSize(16);
		glFont = new GLFont(paint);
	
	}
	
	/*
	@Override
	public void onCreateContextMenu(menu) {
		
	  
	  menu.add(0, MENU_ADD_NOTE, 0, "Add Note");
	  menu.add(0, MENY_DUMMY, 0, "dummy");
	    
	
	}
	
	
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case MENU_ADD_NOTE:
	    editNote(info.id);
	    return true;
	  case MENY_DUMMY:
	    deleteNote(info.id);
	    return true;
	  default:
	    return super.setOnCreateContextMenuListener(l)
	  }
	}
	
	
	User clicks
	
	Option Menu
	
	AddBillboard
	
	- Place at current GPS + x meters away from camera in direction its facing
	- vol can then move it back/forward.
	
	
*/
	
	public void createBlipInFrontOfCamera(){
		
		// first we get the current camera location
		//double x = currentRealLocation.getLatitude();
		//double y = currentRealLocation.getLongitude();
		
		float x = world.getCamera().getPosition().x;
		float y = world.getCamera().getPosition().y;
		float z = world.getCamera().getPosition().z;
		
		
		// and the current camera direction
		SimpleVector direction = world.getCamera().getDirection();
		Log.i("add", "current directionx="+direction.x);
		Log.i("add", "current directiony="+direction.y);
		Log.i("add", "current directionz="+direction.z);
		
		
		// and the current distance to use from the camera
		newobject_distance = 25;
		
		Object3D temptest =  new Rectangle(1,8,3);
		
		
		temptest.translate(x, y, z);
		temptest.align(world.getCamera());
		
		//SimpleVector test = temptest.getZAxis();
		//test.scalarMul(5);
		//temptest.translate(test);
				
		temptest.setAdditionalColor(RGBColor.BLACK);
		
		
		world.addObject(temptest);
		
		temptest.build();
		
		CurrentObject = temptest;
		
		//set edit mode on
		CurrentMode = EDIT_MODE;
		EditModeSecondsLeft = 5;
		
		//start edit mode timer
		
		
		
		
		// then we add the blip
		/*
		ARBlip newblip = new ARBlip();
		
		
		newblip.x = x; 
		newblip.y = y;
		newblip.z = 0;
		newblip.BlipID = "_NEWBLIP_";
		newblip.ObjectData = "(newly created blip)";
		1
		try {
			this.addBlip(newblip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("add", "io exception");
			
		}
		*/
		
	}
	

	/** finish an object creation **/
	public void confirmObjectCreation(){
	
		//get internal co-ordinates
		
		double x = CurrentObject.getTranslation().x;
		double y = CurrentObject.getTranslation().y;
		double z = CurrentObject.getTranslation().z;
		
		double obj_roll = Math.toDegrees(CurrentObject.getZAxis().x);
		
		double obj_baring =Math.toDegrees(CurrentObject.getZAxis().y);		
		
		double obj_elevation = Math.toDegrees(CurrentObject.getZAxis().z);
		
		Log.e("add", "roation x ="+obj_roll+" z = "+obj_elevation+" y (baring) = "+obj_baring);
		
		//get baring from noth
		//tan a = o/a
		double baring = Math.toDegrees(Math.atan2(x,z)); 
		double distance = Math.hypot(x, z);
		
		Log.e("add", "baring ="+baring);
		Log.e("add", "distance ="+distance);
		
		//work out real world co-ordinates
		
		Log.i("add", "base Long = "+startingLocation.getLongitude());
		Log.i("add", "base Lat = "+startingLocation.getLatitude());
		
		Log.i("add", "displacing by = "+distance+" at "+baring);
		
		
		Location newLocation = ARBlipUtilitys.displaceLocation(startingLocation, distance, baring+90); //baring is 90 degree's off!
		
		
		
			//remove tempobject
		world.removeObject(CurrentObject);
	
		//int i=0;
		//while (i<10) {
		//	i=i+1;
			//temptest
			ARBlip newblip = new ARBlip();
			newblip.x = newLocation.getLatitude();
			newblip.y = newLocation.getLongitude();
			newblip.z = -y; //note the co-ordinate switch. ARBlips have "z" as vertical, but the game engine use's "y" as the vertical.
	
			//	newblip.baring = (int) obj_baring;			
		//	newblip.elevation = (int) obj_elevation;
		//	newblip.roll =(int) obj_roll;
			
			newblip.isFacingSprite=true;
			newblip.BlipID = "_NEWBLIP_"+Math.random(); //crude tempID only
			newblip.ObjectData = "(newly created blip)";
			try {
				Log.i("add", "creating blip:"+newblip.BlipID);
				this.addBlip(newblip);
			} catch (IOException e) {
				Log.i("add", "io exception");
			}			
			
			//update blip submit page and bring it to the front so people can fill in the text
			
			//--------------------------
			
			start.sendToAddBlipPage(newblip);
									
			//-----------------------
			
			
			
			
			
			
			
			
			
		//set 3d scene back to viewing mode
		CurrentMode = VIEWING_MODE;
	}	
	
	
	/** cancel an object creation **/
	public void cancelObjectCreation(){
	
		CurrentMode = VIEWING_MODE;
		world.removeObject(CurrentObject);
		
	}	
	
	/** Made as a test **/
	public void creatingSpinningCube()
	{
				
		//animated cube
		rotateingcube = Primitives.getCube(10);	
		rotateingcube.translate(50, -250, 50);
   	 
		world.addObject(rotateingcube);
		
		// kick off the timer task for counter update if not already
        // initialized
		mTimer = new Timer();
		
             mTimerTask = new TimerTask() {
                 public void run() {
                	 
                	 pos=pos+6;
                	 if (pos>360)
                	 { 
                		 pos=0;
                	 };
                	 double rad = Math.toRadians(pos);
                	 int x=(int)Math.round(Math.sin(rad)*100);
                	 int z=(int)Math.round(Math.cos(rad)*100);
                	 
                	//set a random color
             	    RGBColor newcolor = new RGBColor(x,0,z);
             	    rotateingcube.setAdditionalColor(newcolor);
             	   
                	 rotateingcube.setOrigin(new SimpleVector(x, 200, z));
                	 
                 }
             };
             
             
         mTimer.schedule(mTimerTask, 0,50);

        
	}
	
	public void setCameraOrentation(float x, float y, float z)	
	{
		updateCamRotation=true;
        newCameraX =x;		
		newCameraY =y;		
		newCameraZ =z;
	}
	
	
	/** Sets the camera orientation **/
	public void setCameraOrientation(SimpleVector xyzAngles)	
	{
		
		
		if (worldReadyToGo){
			//Camera worldcam = world.getCamera();
			
			//worldcam.getBack().setIdentity(); 
			
			float Z = xyzAngles.z;	
			float Y = xyzAngles.y;			
			float X = xyzAngles.x;
			
			updateCamRotation=true;
	        newCameraX =X;		
			newCameraY =-Y;		
			newCameraZ =-Z;
			
			//worldcam.rotateCameraAxis(new SimpleVector(0,1,0), -Z);
			//worldcam.rotateCameraAxis(new SimpleVector(1,0,0), X);
			//worldcam.rotateCameraAxis(new SimpleVector(0,0,1), -Y);
			
		//	worldcam.rotateCameraAxis(new SimpleVector(0,1,0), -0.081920266f);
		//	worldcam.rotateCameraAxis(new SimpleVector(1,0,0), 0.5303804f);
		//	worldcam.rotateCameraAxis(new SimpleVector(0,0,1), 0.84379244f);
			
		//	 Log.i("GameCamera", ","+worldcam.getDirection().x+","+worldcam.getDirection().y+","+worldcam.getDirection().z+",");
			 
	            
			
			}
		/*
		//we just set the numbers for display for now
		
		float X = (float) Math.toRadians(xyzAngles.x);
		newCameraX =xyzAngles.x;
		
		float Y = (float) Math.toRadians(xyzAngles.y);
		newCameraY =xyzAngles.y;
		
		float Z = (float) Math.toRadians(xyzAngles.z);
		newCameraZ =xyzAngles.z;
		
		
		if (worldReadyToGo && world.getCamera() !=null){
		//Camera worldcam = world.getCamera();
		
		//worldcam.getBack().setIdentity(); //seems to reset the camera for the relative rotations below
	//	worldcam.setOrientation(new SimpleVector(0,1,0),  new SimpleVector(0,0,1)); //point down
		
		 
		//world.getCamera().rotateCameraX(xyzAngles.x);
		//world.getCamera().rotateCameraY(xyzAngles.y);
		
		//world.getCamera().rotateX(xyzAngles.x);
		updateCamRotation = true;
		
		float X = (float) Math.toRadians(xyzAngles.x);
		newCameraX =X;
		
		float Y = (float) Math.toRadians(xyzAngles.y);
		newCameraY =Y;
		
		float Z = (float) Math.toRadians(xyzAngles.z);
		newCameraZ =Z;

		
		//Log.i("__x","_"+xyzAngles.x);
	   Log.i("__y","_"+Y);
	//	Log.i("__z","_"+xyzAngles.z);
		
		
		
		}
		*/
	}
	
	
	/** deletes a blip from the 3d view **/
	public void deleteBlip (ARBlip removethis){
				
		//get the ID to remove
		String BlipID = removethis.BlipID;
		this.deleteBlip(BlipID);
		
		
	}
	
	/** deletes a blip from the 3d view **/
	public void deleteBlip(String BlipsID){
		
		Log.d("deleteing", "deleteing blips2");
		
		//ensure it exists and isn't already in delete queue
		if (this.isBlipInScene(BlipsID)==true && (!deleteQueue.contains(deleteQueue)))
		{
		//get the ID to remove
		//String BlipID = removethis.BlipID;
				
		//find it by its blip ID, and return its internal (JBCT) id
			Object3D object = world.getObjectByName(BlipsID);
			
			
		//remove it from storage	
		
		Iterator<ARBlip> it = scenesBlips.iterator();
		while (it.hasNext())
		{
			ARBlip currentBlip = it.next();
		if (currentBlip.BlipID.equals(BlipsID)){
			scenesBlips.remove(currentBlip);	
		}
			
		}
		
		Log.d("deleteing", "deleteing blips3");
		
		//remove from scene objects
		scenesObjects.remove(object);
		
		
		Log.d("deleteing", "deleteing blips4");
		
		//remove it from view
		
		Log.i("deleteing", "object="+object.getName());
		
		//add to delete queue
		
		//world.removeObject(object);
		
		//deleteQueue.add(object); <--causes a crash later :-/
		
		
			
		}
		
	}
	
	
	public SimpleVector deriveAngles(Matrix mat) {
	    SimpleVector s=new SimpleVector();
	    float[] m=mat.getDump();
	    s.x=(float) Math.atan(m[9]/m[10]);
	    s.y=(float) Math.asin(-m[2]);
	    s.z=(float) Math.atan(m[4]/m[0]);
	    return s;
	}
	
	/** Sets the camera orientation **/
	public void setCameraOrientation(Matrix ma)	
	{
		
		
		
		
		
		if (ma!=null){
		//	Log.i("--", "setting camera1");
			CameraMatrix = ma;
			TestVar = (int) (Math.random()*10);
			updateCamRotation=true;
			if (worldReadyToGo){
				//Log.i("--", "setting camera2");
			//world.getCamera().setBack(CameraMatrix);
			}
			
		}
		
		
		
		//SimpleVector newDirection = deriveAngles(ma);
		
		// setCameraOrentation(newDirection,new SimpleVector(0,0,1));
		 
		
	}
	/** Sets the camera orientation **/
	public void setCameraOrientation(SimpleVector dir,SimpleVector up)	
	{
		world.getCamera().setOrientation(dir, up);
		
		
	}
	
	/** a map tile is just flat object with a map on it **/
	/** in future, when the protocol is more worked out, this method could probably just use a standard arblip for a textured plan */
	/** for now, these are positioned on the ground...maybe the sky would be nicer **/
	
	public void  addMapTile(LocatedMapBundle loc)
	{
		Object3D MapTile = Primitives.getPlane(1, 378);	
		
		//we should exit if map exists already!
		//--world.getObjectByName("MapTile_"+loc.lat+"_"+loc.lon);		
		//--
		
		MapTile.setName("MapTile_"+loc.lat+"_"+loc.lon);
		
		Log.i("dis", "MapTile_"+loc.lat+"_"+loc.lon);
		
	    TextureManager tm = TextureManager.getInstance();
		Texture maptiletexture = new Texture(loc.centerMap, true); //the true specifys the texture has its own alpha. If not, black is assumed to be alpha!
		tm.addTexture("MapTile_"+loc.lat+"_"+loc.lon, maptiletexture);
		MapTile.setTexture("MapTile_"+loc.lat+"_"+loc.lon);
		MapTile.setAdditionalColor(RGBColor.WHITE);
		
		//position
		double worldX = ARBlipUtilitys.getRelativeXLocation(loc.lat, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		double worldY = ARBlipUtilitys.getRelativeYLocation(0, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		double worldZ = ARBlipUtilitys.getRelativeZLocation(loc.lon, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		
		float x = (float)-worldX;
		float y = (float)-worldY;
		float z = (float)worldZ;
		
		MapTile.translate(x,y,z);
		//add to world
		Log.i("dis", "adding map tile at "+x+" "+y+" "+z);
		
		MapTile.rotateX((float) Math.PI / 2f);
		MapTile.rotateY((float) -Math.PI / 2f);
		
		world.addObject(MapTile);
				
		world.buildAllObjects();
		
		
	}
	
	public void updateLocation(Location current)
	{
		double worldX = ARBlipUtilitys.getRelativeXLocation(current.getLatitude(), startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		double worldY = ARBlipUtilitys.getRelativeYLocation(0, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		double worldZ = ARBlipUtilitys.getRelativeZLocation(current.getLongitude() , startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		
		float x = (float)-worldX;
		float y = (float)-worldY;
		float z = (float)worldZ;
		//update camera location
		Camera cam = world.getCamera();
		cam.setPosition(x, cameraHeight, z); //we might want to animate this at some point
		
		//if map showing, then update it (or try to)
//		if (MapModeSet){
//			
//			try {
//				LocatedMapBundle currentmap = StaticMapFetcher.getMap(current);
//				this.addMapTile(currentmap);	
//								
//			} catch (MalformedURLException e) {
//						
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				Log.e("mapE","io exception");
//			}
//			
//		}
		
	}

	/** Supposed to set the subs rotation **/
	public void setSunRotation (SimpleVector degrees){

		//arg, this works relatively..no clue how to fix this
		degrees = new SimpleVector(0, 0.05f, 0);
	
		sun.rotate(degrees, groundPlane.getTransformedCenter());

		
	}
	/** adds a new ARBlip to the scene **/
	/** 
	 * @throws IOException **/
	
	public void addBlip (ARBlip newblip) throws IOException
	{
		
	
		
		//check blip isnt already in scene
		if (this.isBlipInScene(newblip)==true)
		{
			//if so, then we update it
			Log.e("blip", "already exists");
			updateBlip(newblip);
			
		} else {
			//new object dummy
			Object3D newmarker = Object3D.createDummyObj();
			Object3D newplane = Object3D.createDummyObj();
			
			//if blip type is specified
			// load 3d object from url
			if (newblip.MIMEtype.equalsIgnoreCase("application/x-3ds")){
				
	
          		HashSet<String> Namesbefore = TextureManager.getInstance().getNames(); 
          		int SizeBefore = Namesbefore.size();
          		
				//load 3d model
				URL downloadfrom = new URL(newblip.ObjectData);
				URLConnection conn = downloadfrom.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                Log.i("3ds","got stream");
                Object3D[] newobjects = Loader.load3DS(bis, 1);
                
                for( int x = 0; x < newobjects.length; x++ )
                {
                	newobjects[x].rotateX( (float) Math.PI / 2 );
                	newobjects[x].rotateZ( (float) Math.PI );
                	newobjects[x].rotateY( -((float) Math.PI /2));
                	
                	newobjects[x].rotateMesh();
                	newobjects[x].setRotationMatrix( new Matrix() );
                }
                
                Log.i("3ds","now merging...");
                newmarker = Object3D.mergeAll(newobjects);
              
                //This scale is wrong...no idea what the correct scale to use is :(
                //newmarker.scale(2f);
                

                //clear memory first
                System.gc(); //no idea if this is a good place, but I was getting some out of memory errors  without it
                
                
                //ok, if occlusion is set, then we simply dont specify a texture
                if (newblip.isOcculisionMask){

            		//Bitmap.Config config = Bitmap.Config.ARGB_8888;			
                	//Bitmap charImage = Bitmap.createBitmap(16, 16, config);
            		//Canvas canvas = new Canvas(charImage);			
            		//canvas.drawColor(Color.BLACK);			
            		//TextureManager tm = TextureManager.getInstance();          		
             		//Texture newmaxtexture = new Texture(charImage,false);
            		//tm.addTexture("occlusion", newmaxtexture);
            		             		
             		//newmarker.setTexture("occlusion");
             		
                	return;
                }
                
                HashSet<String> newNames = TextureManager.getInstance().getNames(); 
               
                
                newNames.removeAll(Namesbefore);
               
                //now get the texture names
                Iterator<String> it = newNames.iterator();
                
                
                //get path
                String URLPath = newblip.ObjectData.substring(0,newblip.ObjectData.lastIndexOf('/')+1);
                
                fetchAndSwapTexturesFromURL(it, URLPath);
                
                              
                Log.i("3ds","created 3ds");
                
                
                
                
			} else if (newblip.MIMEtype.equalsIgnoreCase("application/x-obj")) { 
			
				HashSet<String> Namesbefore = TextureManager.getInstance().getNames(); 
          		int SizeBefore = Namesbefore.size();
          		
				//load 3d model
          		
          		//get model source

                Log.i("obj","getting model stream");
				URL downloadfrom = new URL(newblip.ObjectData);
				URLConnection conn = downloadfrom.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                
                //get texture source
                Log.i("obj","getting texture stream");
                URL downloadtexturefrom = new URL(newblip.ObjectData.substring(0, newblip.ObjectData.length()-4)+".mtl");
				URLConnection textconn = downloadtexturefrom.openConnection();
                conn.connect();
                BufferedInputStream textbis = new BufferedInputStream(textconn.getInputStream());
                
                
                Log.i("obj","got streams");
                Object3D[] newobjects = Loader.loadOBJ(bis, textbis, 1);

                
                for( int x = 0; x < newobjects.length; x++ )
                {
                	newobjects[x].rotateX(3.14f);
                	
                	newobjects[x].rotateMesh();
                	newobjects[x].setRotationMatrix( new Matrix() );
                }
                
                
                Log.i("obj","now merging...");
                newmarker = Object3D.mergeAll(newobjects);
                
                
                //This scale is wrong...no idea what the correct scale to use is :(
              //  newmarker.scale(2f);
                
                //clear memory first
                System.gc(); //no idea if this is a good place, but I was getting some out of memory errors  without it
                
                HashSet<String> newNames = TextureManager.getInstance().getNames(); 
               
                
                newNames.removeAll(Namesbefore);
               
                Log.i("obj","number of textures;"+newNames.size());
                
                //now get the texture names
                Iterator<String> it = newNames.iterator();
                //get path
                String URLPath = newblip.ObjectData.substring(0,newblip.ObjectData.lastIndexOf('/')+1);
                
                fetchAndSwapTexturesFromURL(it, URLPath);
               
                
			
			} else if (newblip.MIMEtype.equalsIgnoreCase("TESTONLY")) {
				
				//load 3d model
			
                newmarker = Primitives.getCube(10);
                //rotate it io
                newmarker.rotateX(-(float) Math.PI / 2);
                		
			
			
			} else {
				//if no recognised type, then we assume its a billboard with text
				//if not,create a new arblip placemark			
			//	newmarker = Primitives.getPyramide(2, 8);	
				
			//	newmarker.setName(newblip.BlipID);
				//newmarker.setTexture("rock");
			//	newmarker.setAdditionalColor(RGBColor.BLACK);
				
				//billboard bit at top
				//Object3D newplane = Primitives.getPlane(1, 60);
				//newplane = new Rectangle(1,8,3);
				
				//newplane.setAdditionalColor(RGBColor.WHITE);
				//newplane.setBillboarding(true);
				
				//simplifed (no stand for billboard now)
				newplane = new SkywriterBillboard();
				newplane.setName(newblip.BlipID);
				//set texture
				String text = newblip.ObjectData;							
				updatedTexture(newblip.BlipID,text);
				//newplane.setOrigin(new SimpleVector(0,-15,0));(used to move it upwards for when it was on a stand)
				newplane.setTexture(newblip.BlipID);

				//set billboarding off if rotations are set				
				if (newblip.isFacingSprite){
				newplane.setBillboarding(true);
				}
				
				newmarker = newplane;
				
				//newplane.setBillboarding(true); 
				//link objects together
				//newplane.addParent(newmarker);
						
				//merge the plane and 
				//newmarker = newplane.mergeObjects(newplane, newmarker);
			}
			
			newmarker.setName(newblip.BlipID);
			
			//work out the co-ordinates to place it at
			//we are going to have to work out the best way to convey real world log/lat
			//into a sensible onscreen scale. 
			double worldX = ARBlipUtilitys.getRelativeXLocation(newblip, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
			double worldY = ARBlipUtilitys.getRelativeYLocation(newblip, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
			double worldZ = ARBlipUtilitys.getRelativeZLocation(newblip, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
			
			float x = (float)-worldX;
			float y = (float)-worldY;
			float z = (float)worldZ;
								
			Log.i("3ds", "positioning at z="+z+" y="+y+"x="+x);
			
			//newmarker.setTranslationMatrix(new Matrix());
			newmarker.translate(x,y,z);
			
			//set rotation
			if (!newblip.isFacingSprite){
				newmarker.setRotationMatrix(new Matrix());
				Log.i("add","roating..."+newblip.baring);				
				newmarker.rotateX( (float)Math.toRadians(newblip.roll));
				newmarker.rotateY( (float)Math.toRadians(newblip.baring ));
				newmarker.rotateZ( (float)Math.toRadians(newblip.elevation));
			}
			
            Log.i("3ds", "adding "+newblip.BlipID+" to scene");
			world.addObject(newmarker);
			
			//this one shouldn't always be added, only for billboards!
			world.addObject(newplane);
			
			world.buildAllObjects();
						
			//add object+blip to internal lists (was going to use the built in getObjectByName, but seems to crash)
			scenesBlips.add(newblip);
			scenesObjects.add(newmarker);
			//(Note these two should never go out of sycn! If a blip is in the scene, then so should its object.
			// Later when we introduce layers we might need to change this system to something better.
		}
		
		
	}

	private void fetchAndSwapTexturesFromURL(Iterator<String> it, String URLPath)
			throws MalformedURLException, IOException {
		while (it.hasNext()){
			
			String textureNameToLoad = (String)it.next();
			Log.i("3ds", "Loading Texture:-"+textureNameToLoad);
			
			//get the url
			//NOTE; filename must be lower case 
			// In future we might want to check for uppercase too.
			// Unfortuntely, 3dsmax stores all its filenames uppercase internaly, so we cant have mixed cases
			// in filenames, as they wont be recognised.
			String TextureURL = URLPath+textureNameToLoad.toLowerCase();
			
			Log.i("3d","getting texture at "+TextureURL);
			
			//make the texture
			URL texturedownloadfrom = new URL(TextureURL);
			URLConnection textureconnection = texturedownloadfrom.openConnection();
			
			
		    textureconnection.connect();
		    BufferedInputStream texturebis = new BufferedInputStream(textureconnection.getInputStream());
		     
		    Bitmap maxtexture1 = BitmapFactory.decodeStream(texturebis);
		    TextureManager tm = TextureManager.getInstance();          		
			Texture newmaxtexture = new Texture(maxtexture1);
		
			
			//swap it in
			// Note; This automaticaly assigns the correct textures onto the model, because
			// when the model was loaded, it was assigned the file names in the 3ds file for its texture names.
			// neat eh?
			tm.replaceTexture(textureNameToLoad, newmaxtexture);
			
		}
	}
/** updates a texture to a bit of text **/
	private void updatedTexture(String Texturename,String text) {
		paint.setColor(Color.BLACK);	
					
		Bitmap.Config config = Bitmap.Config.ARGB_8888;			
		FontMetricsInt fontMetrics = paint.getFontMetricsInt();			
		int fontHeight = fontMetrics.leading - fontMetrics.ascent + fontMetrics.descent;
		int baseline = -fontMetrics.top;
		int height = fontMetrics.bottom - fontMetrics.top;
		
		//have to add multiline support here
		Bitmap charImage = Bitmap.createBitmap(closestTwoPower((int)paint.measureText(text)+10), 32, config);
		
		Canvas canvas = new Canvas(charImage);			
		canvas.drawColor(Color.WHITE);				
		canvas.drawText(text, 10, baseline, paint); //draw text with a margin of 10
		
		
		TextureManager tm = TextureManager.getInstance();
		
		Texture testtext = new Texture(charImage, true); //the true specifys the texture has its own alpha. If not, black is assumed to be alpha!
		
		if (tm.containsTexture(Texturename))
		{
			tm.removeTexture(Texturename);
			tm.unloadTexture(fb, tm.getTexture(Texturename));				
			tm.addTexture(Texturename, testtext);
		} else {
			tm.addTexture(Texturename, testtext);
		}
		
		
		
		
	}
	/**
	 * returns the closest power of two that is equal or greater than given
	 * number (good for textures!
	 */
	private int closestTwoPower(int i) {
		int power = 1;
		while (power < i) {
			power <<= 1;
		}
		return power;
	}
	/** updates an existing blip in the scene 
	 * crashs if blip/object doesnt exist **/
	
	public void updateBlip(ARBlip newblipdata)
	{
		Object3D updateThis = world.getObjectByName(newblipdata.BlipID);
		
		//update location (oddly, you have to clear the location and then "move" the position, rather then merely setting it)
		//                (you can use setOrigin to directly set it, but this wont move child objects)
		
		//updateThis.setTranslationMatrix(new Matrix());
		
		double worldX = ARBlipUtilitys.getRelativeXLocation(newblipdata, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		double worldY = ARBlipUtilitys.getRelativeYLocation(newblipdata, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		double worldZ = ARBlipUtilitys.getRelativeZLocation(newblipdata, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
		
		//Log.i("3ds","moving to ="+worldX+" , "+worldY+" , "+worldZ);
		
		updateThis.setTranslationMatrix(new Matrix());
		updateThis.translate(new SimpleVector(worldX,worldY,worldZ));
		
		//update rotation
		updateThis.setRotationMatrix(new Matrix());
		updateThis.rotateX( (float)Math.toRadians(newblipdata.roll));
		updateThis.rotateY( (float)Math.toRadians(newblipdata.baring ));
		updateThis.rotateZ( (float)Math.toRadians(newblipdata.elevation));
		
		
		//update textures
		String text = newblipdata.ObjectData;		
		updatedTexture(newblipdata.BlipID,text);
		
		//update other stuff
		
		
	}
	
	
	public boolean isBlipInScene(String BlipsID)
	{
		
		
		Iterator<ARBlip> it = scenesBlips.iterator();
		while (it.hasNext())
		{
			
		if (it.next().BlipID.equals(BlipsID)){
			return true;			
		}
			
		}

		return false;
	}
	
	/** upoptimised check for pre-existing blip */
	public boolean isBlipInScene(ARBlip blipToCheck)
	{
		
		//if (world.getObjectByName(blipToCheck.BlipID)==null)
		//{
		//	Log.i("3d","object doesnt exist");			
		//}
		
		//is the blip loaded
		String BlipsID = blipToCheck.BlipID;
		this.isBlipInScene(BlipsID);
		
		/*
		Iterator<ARBlip> it = scenesBlips.iterator();
		while (it.hasNext())
		{
			
		if (it.next().BlipID.equals(BlipID)){
			return true;			
		}
			
		}
		*/
	   
		
		return false;
		
	}
	
	
	public void toggleBackgroundScenary(){
		
		backgroundScenaryVisible =! backgroundScenaryVisible;
		
		groundPlane.setVisibility(backgroundScenaryVisible);
		rock.setVisibility(backgroundScenaryVisible);
		tree1.setVisibility(backgroundScenaryVisible);
		tree2.setVisibility(backgroundScenaryVisible);
		grass.setVisibility(backgroundScenaryVisible);
	}
	
	/** Map mode **/
	/** This will add a google map image as the ground plan and set camera overhead **/
	public void setMapMode(boolean setMapMode, LocatedMapBundle MapBundle)
	{
		MapModeSet = setMapMode;

		if (MapModeSet){
												
			this.addMapTile(MapBundle);
			//set camera overhead
			Camera cam = world.getCamera();
			cam.setPosition(cam.getPosition().x, -250, cam.getPosition().z); //we might want to animate this at some point
			cam.lookAt(groundPlane.getTransformedCenter());
			groundPlane.setVisibility(false);
		}else {		
			groundPlane.setVisibility(true);
		groundPlane.setTexture("grassy");
		}
		
		
		
		/*
		if (MapModeSet){
		//set map mode on
			
		
		TextureManager tm = TextureManager.getInstance();
	

		Texture testtext = new Texture(Map.map, true); //the true specifys the texture has its own alpha. If not, black is assumed to be alpha!
		
		if (tm.containsTexture("MapTexture"))
		{
			tm.removeTexture("MapTexture");
			tm.unloadTexture(fb, tm.getTexture("MapTexture"));				
			tm.addTexture("MapTexture", testtext);
		} else {
			tm.addTexture("MapTexture", testtext);
		}
		
		//set to ground plane
		groundPlane.setTexture("MapTexture");
		
		//set new position
		groundPlane.setTranslationMatrix(new Matrix());
		groundPlane.translate(new SimpleVector(Map.xDis, 0, Map.yDis));
		
		//set camera overhead
		Camera cam = world.getCamera();
		cam.setPosition(Math.round(Map.xDis), -250, Math.round(Map.yDis)); //we might want to animate this at some point
		cam.lookAt(groundPlane.getTransformedCenter());
		
		} else {			
		groundPlane.setTexture("grassy");
		}
		*/
		
	}
	
	
	class MyRenderer implements GLSurfaceView.Renderer {

	
		private Texture numberFont = null;

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
		}

		
		
		
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			TextureManager.getInstance().flush();
			world = new World();
			Resources res = getResources();
			
			TextureManager tm = TextureManager.getInstance();
			
			
			
			
			
			
			//set up pre-made landscape
			Texture grass2 = new Texture(res.openRawResource(R.raw.grassy));
			Texture leaves = new Texture(res.openRawResource(R.raw.tree2y));
			Texture leaves2 = new Texture(res.openRawResource(R.raw.tree3y));
			Texture rocky = new Texture(res.openRawResource(R.raw.rocky));
			
			
			Texture planetex = new Texture(res.openRawResource(R.raw.planetex));

			numberFont = new Texture(res.openRawResource(R.raw.numbers));

			tm.addTexture("grass2", grass2);
			tm.addTexture("leaves", leaves);
			tm.addTexture("leaves2", leaves2);
			tm.addTexture("rock", rocky);
			tm.addTexture("grassy", planetex);

			if (!deSer) {
				// Use the normal loaders...
				groundPlane = Primitives.getPlane(1, 378);
				grass = Loader.load3DS(res.openRawResource(R.raw.grass), 5)[0];
				rock = Loader.load3DS(res.openRawResource(R.raw.rock), 15f)[0];
				tree1 = Loader.load3DS(res.openRawResource(R.raw.tree2), 2)[0];
				tree2 = Loader.load3DS(res.openRawResource(R.raw.tree3), 6)[0];

				groundPlane.setTexture("grassy");
				rock.setTexture("rock");
				grass.setTexture("grass2");
				tree1.setTexture("leaves");
				tree2.setTexture("leaves2");

			//	plane.getMesh().setVertexController(new Mod(), false);
			//	plane.getMesh().applyVertexController();
			//	plane.getMesh().removeVertexController();
			} else {
				// Load the serialized version instead...
				groundPlane = Primitives.getPlane(1, 378);
				groundPlane.setTexture("grassy");
				//plane = Loader.loadSerializedObject(res.openRawResource(R.raw.serplane));
				rock = Loader.loadSerializedObject(res.openRawResource(R.raw.serrock));
				tree1 = Loader.loadSerializedObject(res.openRawResource(R.raw.sertree1));
				tree2 = Loader.loadSerializedObject(res.openRawResource(R.raw.sertree2));
				grass = Loader.loadSerializedObject(res.openRawResource(R.raw.sergrass));
			}

			grass.translate(-90, -34, -100);
			grass.rotateZ((float) Math.PI);
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
			//tree2.setName("tree2");
			grass.setName("grass");
			rock.setName("rock");

		    rock.scale(0.1f);
				
		    //note the rock is at 0,0,0 to mark the center point.
		    //the tree is at -200,-180,0 
		    //tree2 is at 0,-190,200)
			
			world.addObject(groundPlane);
			world.addObject(tree1);
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

			world.setAmbientLight(20, 20, 20);
			world.buildAllObjects();

			sun = new Light(world);

			Camera cam = world.getCamera();
			cam.moveCamera(Camera.CAMERA_MOVEUP, 100);
			cam.lookAt(groundPlane.getTransformedCenter());

			
			cam.setFOV(0.5f); // used to be 1.5, 0.5 seemed closer to my phones camera -thomas
			sun.setIntensity(250, 250, 250);

			SimpleVector sv = new SimpleVector();
			sv.set(groundPlane.getTransformedCenter());
			sv.y -= 300;
			sv.x -= 100;
			sv.z += 200;
			sun.setPosition(sv);
			
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
						
						
						
						//If theres objects in the delete queue
						if (deleteQueue.size()>0){
							
							Iterator<Object3D> dqit = deleteQueue.iterator();
							while (dqit.hasNext()){
							Log.d("deleteing", "removing");
							
								Object3D toremove = dqit.next();
								world.removeObject(toremove);
								dqit.remove();
							}
							
						}
						
						
						
						
						
						
						
						
						if (CurrentMode == VIEWING_MODE){
						if (move != 0) {
							
							world.getCamera().moveCamera(cam.getDirection(), move);
							
							
							/*  FOV adjustment;
							float FOV = world.getCamera().convertDEGAngleIntoFOV( Math.abs(move));
							
							if (move>0){
								
								world.getCamera().increaseFOV(FOV);
							} else {
								world.getCamera().decreaseFOV(FOV);									
							}
							Log.i("FOV", "="+world.getCamera().getFOV());
							*/
						}}
						
						if (CurrentMode == EDIT_MODE){
							

							CurrentObject.setTranslationMatrix(new Matrix());
							
							SimpleVector CameraPosition = world.getCamera().getPosition();
							
							CurrentObject.translate(CameraPosition);
							
							//change distance of last added object
							CurrentObject.align(world.getCamera());							
							SimpleVector test = CurrentObject.getZAxis();
							newobject_distance=newobject_distance+move;
							test.scalarMul(newobject_distance);
							CurrentObject.translate(test);
							
							
							
							
							
						//	Log.i("add", "exiting adding mode"+move);
							
							
						}
						
						
						//set rotation
						
						/*
						if (updateCamRotation)
						{
							world.getCamera().getBack().setIdentity(); 
							world.getCamera().rotateX(newCameraX);
							world.getCamera().rotateY(newCameraY);
							world.getCamera().rotateZ(newCameraZ);
							updateCamRotation=false;
						}*/
						//world.getCamera().setBack(CameraMatrix);
						
						fb.clear();
						world.renderScene(fb);
						world.draw(fb);
						
						
						blitNumber(lfps, 5, 5);
						if (currentRealLocation!=null){
						blitNumber(Math.round(Math.round(currentRealLocation.getLatitude()*1E6)), 15, 25);
						blitNumber(Math.round(Math.round(currentRealLocation.getLongitude()*1E6)), 15, 45);
						blitNumber(TestVar, 200, 25);
						}
						
						if (CurrentMode == EDIT_MODE){
						//	int currentXvalue = (int) Math.toDegrees(CurrentObject.getZAxis().x);							
						//	int currentYvalue = (int) Math.toDegrees(CurrentObject.getZAxis().y);							
						//	int currentZvalue = (int) Math.toDegrees(CurrentObject.getZAxis().z);		
														
						//	Log.i("add", "test x="+currentXvalue+" y "+currentYvalue+" z="+currentZvalue);
							
						//	blitNumber(1000+currentYvalue, 50, 55);
						}
						if (updateCamRotation){
							
							
					//	Log.d("newx","x="+CameraMatrix.getXAxis().x);
														
						 world.getCamera().getBack().setIdentity(); 
							
					   	updateCamRotation=false;							
						world.getCamera().rotateCameraAxis(Z_AXIS, newCameraZ);
						world.getCamera().rotateCameraAxis(X_AXIS, newCameraX);
						world.getCamera().rotateCameraAxis(Y_AXIS, newCameraY);
													
							//worldcam.rotateCameraAxis(new SimpleVector(0,1,0), -Z);
							//worldcam.rotateCameraAxis(new SimpleVector(1,0,0), X);
							//worldcam.rotateCameraAxis(new SimpleVector(0,0,1), -Y);
						
						//first we bleet a random number to help diagnoise
						/*
						if (showDebugInfo){
							
							Log.i("debug", "__debug messages");
							
						blitNumber(TestVar, 200, 25);
						
						blitNumber(Math.round(CameraMatrix.getXAxis().x*1000), 5, 25);
						blitNumber(Math.round(CameraMatrix.getXAxis().y*1000), 55, 25);
						blitNumber(Math.round(CameraMatrix.getXAxis().z*1000), 105, 25);
						blitNumber(Math.round(CameraMatrix.getYAxis().x*1000), 5, 45);
						blitNumber(Math.round(CameraMatrix.getYAxis().y*1000), 55, 45);
						blitNumber(Math.round(CameraMatrix.getYAxis().z*1000), 105, 45);
						blitNumber(Math.round(CameraMatrix.getZAxis().x*1000), 5, 65);
						blitNumber(Math.round(CameraMatrix.getZAxis().y*1000), 55, 65);
						blitNumber(Math.round(CameraMatrix.getZAxis().z*1000), 105, 65);
						}
						*/
						
						
						}
						
						
						fb.display();
						
						//sun.rotate(new SimpleVector(0, 0.05f, 0), groundPlane.getTransformedCenter());

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
			

			worldReadyToGo= true;
			
		}

		
		
		
		
		//Seems to be for making the hilly groundplane...probably not needed by us, but 
		//usefull as a referance for vectex manipulation.
		private class Mod extends GenericVertexController {
			private static final long serialVersionUID = 1L;

			public void apply() {
				SimpleVector[] s = getSourceMesh();
				SimpleVector[] d = getDestinationMesh();
				for (int i = 0; i < s.length; i++) {
					d[i].z = s[i].z - (10f * (FloatMath.sin(s[i].x / 50f) + FloatMath.cos(s[i].y / 50f)));
					d[i].x = s[i].x;
					d[i].y = s[i].y;
				}
			}
		}
		
		//seems to be for picking numbers from a texture and displaying them for the framerate
		private void blitNumber(int number, int x, int y) {
			if (numberFont != null) {
				String sNum = Integer.toString(number);
				for (int i = 0; i < sNum.length(); i++) {
					char cNum = sNum.charAt(i);
					int iNum = cNum - 48;
					fb.blit(numberFont, iNum * 5, 0, x, y, 5, 9, FrameBuffer.TRANSPARENT_BLITTING);
					x += 5;
				}
			}
		}
	}
	
	
	
}
