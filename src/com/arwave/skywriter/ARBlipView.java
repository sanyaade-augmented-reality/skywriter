package com.arwave.skywriter;

import glfont.GLFont;
import glfont.TexturePack;
import glfont.TexturePack.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.text.format.Time;
import android.util.FloatMath;
import android.util.Log;

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
	
	//our world, to which we add stuff
	private World world = null;	
	//renderer
	 MyRenderer renderer = null;
	 
	private FrameBuffer fb = null;
	
	//internal list of ARBlip objects
	ArrayList<ARBlip> scenesBlips = new ArrayList<ARBlip>();
	ArrayList<Object3D> scenesObjects = new ArrayList<Object3D>();

	//generic font
	private GLFont glFont;
	Paint paint = new Paint();
	//Worldset up flag
	boolean worldReadyToGo = false;
	
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
		private boolean updateCamRotation;
		float newCameraX =0;
		float newCameraY =0;
		float newCameraZ =0;
		
	
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
	/** Sets the camera orentation **/
	public void setCameraOrentation(SimpleVector xyzAngles)	
	{
		
		//we just set the numbers for display for now
		
		float X = (float) Math.toRadians(xyzAngles.x);
		newCameraX =xyzAngles.x;
		
		float Y = (float) Math.toRadians(xyzAngles.y);
		newCameraY =xyzAngles.y;
		
		float Z = (float) Math.toRadians(xyzAngles.z);
		newCameraZ =xyzAngles.z;
		
		/*
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
	
	public SimpleVector deriveAngles(Matrix mat) {
	    SimpleVector s=new SimpleVector();
	    float[] m=mat.getDump();
	    s.x=(float) Math.atan(m[9]/m[10]);
	    s.y=(float) Math.asin(-m[2]);
	    s.z=(float) Math.atan(m[4]/m[0]);
	    return s;
	}
	
	/** Sets the camera orentation **/
	public void setCameraOrentation(Matrix ma)	
	{
		if (ma!=null){
			
			CameraMatrix = ma;
			if (worldReadyToGo){
				Log.i("--", "setting camera2");
			world.getCamera().setBack(CameraMatrix);
			}
		}
		
		
		//SimpleVector newDirection = deriveAngles(ma);
		
		// setCameraOrentation(newDirection,new SimpleVector(0,0,1));
		 
		
	}
	/** Sets the camera orentation **/
	public void setCameraOrentation(SimpleVector dir,SimpleVector up)	
	{
		world.getCamera().setOrientation(dir, up);
		
		
	}
	
	/** adds a new ARBlip to the scene **/
	/** at the moment, this is cube marker only **/
	public void addBlip (ARBlip newblip)
	{
		//check blip isnt already in scene
		if (this.isBlipInScene(newblip)==true)
		{
			//if so, then we update it
			Log.e("blip", "already exists");
			updateBlip(newblip);
			
		} else {

			//in future, this should be a seperate function supporting many blip types
			//
			
			//if not,create a new arblip placemark			
			Object3D newmarker = Primitives.getPyramide(2, 8);	
			
			newmarker.setName(newblip.BlipID);
			//newmarker.setTexture("rock");
			newmarker.setAdditionalColor(RGBColor.BLACK);
			
			//billboard bit at top
			//Object3D newplane = Primitives.getPlane(1, 60);
			Object3D newplane = new Rectangle(1,8,3);
			
			newplane.setAdditionalColor(RGBColor.WHITE);
			newplane.setBillboarding(true);
			
			
			//set texture
			String text = newblip.ObjectData;
						
			updatedTexture(newblip.BlipID,text);
			newplane.setOrigin(new SimpleVector(0,-15,0));
			newplane.setTexture(newblip.BlipID);
			
			//newplane.setBillboarding(true); 
			//link objects together
			newplane.addParent(newmarker);
						
			
			//work out the co-ordinates to place it at
			//we are going to have to work out the best way to convey real world log/lat
			//into a sensible onscreen scale. 
			double worldX = ARBlipUtilitys.getRelativeXLocation(newblip, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
			double worldY = ARBlipUtilitys.getRelativeYLocation(newblip, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
			double worldZ = ARBlipUtilitys.getRelativeZLocation(newblip, startingLocation); //As the world is set on loading, and then the camera moves, we always messure relative to the loading location.		
			
			float x = (float)worldX;
			float y = (float)-worldY;
			float z = (float)-worldZ;
			
			newmarker.translate(x,y,z);
			
			//need a way to generate a texture from text?
			//String text = blip.ObjectData

			//add to the world.
			world.addObject(newmarker);
			world.addObject(newplane);
			
			world.buildAllObjects();
						
			//add object+blip to internal lists (was going to use the built in getObjectByName, but seems to crash)
			scenesBlips.add(newblip);
			scenesObjects.add(newmarker);
			//(Note these two should never go out of sycn! If a blip is in the scene, then so should its object.
			// Later when we introduce layers we might need to change this system to something better.
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
		updateThis.setTranslationMatrix(new Matrix());
		updateThis.translate(new SimpleVector(newblipdata.x,newblipdata.y,newblipdata.z));
		//update rotation
		
		//update textures
		String text = newblipdata.ObjectData;		
		updatedTexture(newblipdata.BlipID,text);
		
		//update other stuff
		
		
	}
	
	/** upoptimised check for pre-existing blip */
	public boolean isBlipInScene(ARBlip blipToCheck)
	{
		
		//if (world.getObjectByName(blipToCheck.BlipID)==null)
		//{
		//	Log.i("3d","object doesnt exist");			
		//}
		
		//is the blip loaded
		String BlipID = blipToCheck.BlipID;
		
		Iterator<ARBlip> it = scenesBlips.iterator();
		while (it.hasNext())
		{
			
		if (it.next().BlipID.equals(BlipID)){
			return true;			
		}
			
		}
		
	   
		
		return false;
		
	}
	

	
	
	class MyRenderer implements GLSurfaceView.Renderer {

		private Object3D plane = null;
		private Object3D tree2 = null;
		private Object3D tree1 = null;
		private Object3D grass = null;
		private Texture numberFont = null;

		//
		private int fps = 0;
		private int lfps = 0;

		private long time = System.currentTimeMillis();

		private Light sun = null;
		private Object3D rock = null;

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
			
			
			
			
			/*
			//expirment texture
			Texture textTexture = new Texture(64,64, RGBColor.BLACK);

			FrameBuffer meep = new FrameBuffer(gl, 100, 100);

			World tw=new World();
            tw.renderScene(meep);
            tw.draw(meep);
            
			meep.display();	
			
			meep.setRenderTarget(textTexture);				
			meep.clear(RGBColor.BLACK);	
			glFont.blitString(meep, " test test test! ", 
					5, 5, 10, RGBColor.WHITE);		
				
			meep.display();		
			
			meep.removeRenderTarget();
			*/
			
			
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
				plane = Primitives.getPlane(20, 30);
				grass = Loader.load3DS(res.openRawResource(R.raw.grass), 5)[0];
				rock = Loader.load3DS(res.openRawResource(R.raw.rock), 15f)[0];
				tree1 = Loader.load3DS(res.openRawResource(R.raw.tree2), 5)[0];
				tree2 = Loader.load3DS(res.openRawResource(R.raw.tree3), 5)[0];

				plane.setTexture("grassy");
				rock.setTexture("rock");
				grass.setTexture("grass2");
				tree1.setTexture("leaves");
				tree2.setTexture("leaves2");

			//	plane.getMesh().setVertexController(new Mod(), false);
			//	plane.getMesh().applyVertexController();
			//	plane.getMesh().removeVertexController();
			} else {
				// Load the serialized version instead...
				plane = Primitives.getPlane(20, 30);
				plane.setTexture("grassy");
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
			tree1.translate(-200, -184, -200);
			tree1.rotateZ((float) Math.PI);
			tree2.translate(220, -190, 120);
			tree2.rotateZ((float) Math.PI);
			plane.rotateX((float) Math.PI / 2f);

			plane.setName("plane");
			tree1.setName("tree1");
			tree2.setName("tree2");
			grass.setName("grass");
			rock.setName("rock");

			//we scale the plane to fill the landscape
		    rock.scale(0.3f);
				
			
			world.addObject(plane);
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
			//cam.moveCamera(Camera.CAMERA_MOVEOUT, 250);
			cam.moveCamera(Camera.CAMERA_MOVEUP, 100);
			cam.lookAt(plane.getTransformedCenter());

			cam.setFOV(1.5f);
			sun.setIntensity(250, 250, 250);

			SimpleVector sv = new SimpleVector();
			sv.set(plane.getTransformedCenter());
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
						
						
						
						if (move != 0) {
							world.getCamera().moveCamera(cam.getDirection(), move);
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
						/*
						blitNumber(Math.round(CameraMatrix.getXAxis().x*1000), 5, 25);
						blitNumber(Math.round(CameraMatrix.getXAxis().y*1000), 55, 25);
						blitNumber(Math.round(CameraMatrix.getXAxis().z*1000), 105, 25);
						blitNumber(Math.round(CameraMatrix.getYAxis().x*1000), 5, 45);
						blitNumber(Math.round(CameraMatrix.getYAxis().y*1000), 55, 45);
						blitNumber(Math.round(CameraMatrix.getYAxis().z*1000), 105, 45);
						*/
						
						fb.display();
						
						
						sun.rotate(new SimpleVector(0, 0.05f, 0), plane.getTransformedCenter());

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
