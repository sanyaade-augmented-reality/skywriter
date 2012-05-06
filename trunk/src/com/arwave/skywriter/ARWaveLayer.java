package com.arwave.skywriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.arwave.skywriter.ARBlipObject.ObjectType;
import com.arwave.skywriter.objects.ArrowMarker;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

public class ARWaveLayer {

	
	/** An ARWaveLayer is a collection of ARBlipObjects, 
	 * corresponding exactly to the contents of a wave **/
	
	ArrayList<ARBlipObject> LayersObjects = new ArrayList<ARBlipObject>();

	String ARWaveLayerID = "";

	
	//Billboard scaling modes;

	 public enum ScaleType {
		 BILLBOARDMODE_REAL, //real scaling 
		 BILLBOARDMODE_LINEAR, ///linear sizing (falls of in size linearly with distance rather then exponetialy)
		 BILLBOARDMODE_FIXED //fixed size relative to the viewport WHATS THE FORMULA FOR THIS?!?!
			
		}
	//----
	 ScaleType BillboardMode = ScaleType.BILLBOARDMODE_LINEAR; // default to linear

	private boolean isVisible=true;
		
	
	public ARWaveLayer(String layerName){
		
		ARWaveLayerID = layerName;
		
	}
	
	public void setBillBoardScaleingMode(ScaleType mode){
		BillboardMode = mode;
	}
	
	public void addObject(ARBlipObject newArBlipObject){		
		
		// only add if not present already
		if (!hasARBlipObject(newArBlipObject.arblip.BlipID)){
		LayersObjects.add(newArBlipObject);
		}
		
		//if wave is invisble then set it invisible
		if (!isVisible){
		newArBlipObject.object3d.setVisibility(false);
		}
		
	}
	
	public void setVisible(boolean visible){
		isVisible = visible;
		
		
		Log.i("wave", "setting to"+visible);
		
		//iterates over its contents and sets its visibility		
		Iterator<ARBlipObject> objects = LayersObjects.iterator();
		Log.i("wave", "total objects to set="+LayersObjects.size());
		
		while (objects.hasNext()){			
			ARBlipObject object = objects.next();
			object.object3d.setVisibility(visible);			
		}
		
	}
	
	/** will create a new arblip object and add it to the layer, as well as returning it to add to the world **/
	public Object3D createNewBlipObject(ARBlip newblip) throws IOException {
		

		Log.e("3ds", "blip being made");
				
		// new object dummy
		Object3D newmarker = Object3D.createDummyObj();
		//variable holding the object type;
		ObjectType object3dtype; //this must be set before the object is loaded
		
		// if blip type is specified
		// load 3d object from url
		if (newblip.MIMEtype.equalsIgnoreCase("application/x-3ds")) {
			//its a 3ds file , so a mesh
			object3dtype = ARBlipObject.ObjectType.MESH_OBJECT;	
			
			HashSet<String> Namesbefore = TextureManager.getInstance()
			.getNames();
			int SizeBefore = Namesbefore.size();

			// load 3d model
			URL downloadfrom = new URL(newblip.ObjectData);
			URLConnection conn = downloadfrom.openConnection();
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(conn
					.getInputStream());
			Log.i("3ds", "got stream");
			Object3D[] newobjects = Loader.load3DS(bis, 1);

			for (int x = 0; x < newobjects.length; x++) {
				newobjects[x].rotateX((float) Math.PI / 2);
				newobjects[x].rotateZ((float) Math.PI);
				newobjects[x].rotateY(-((float) Math.PI / 2));

				newobjects[x].rotateMesh();
				newobjects[x].setRotationMatrix(new Matrix());
			}

			Log.i("3ds", "now merging...");
			newmarker = Object3D.mergeAll(newobjects);

			// This scale is wrong...no idea what the correct scale to use
			// is :(
			// newmarker.scale(2f);

			// clear memory first
			System.gc(); // no idea if this is a good place, but I was
			// getting some out of memory errors without it

			// ok, if occlusion is set, then we simply dont specify a
			// texture
			if (newblip.isOcculisionMask) {

				newmarker.setTexture("occlusion");

				return newmarker;
			}

			HashSet<String> newNames = TextureManager.getInstance()
			.getNames();

			newNames.removeAll(Namesbefore);

			// now get the texture names
			Iterator<String> it = newNames.iterator();

			// get path
			String URLPath = newblip.ObjectData.substring(0,
					newblip.ObjectData.lastIndexOf('/') + 1);

			try {
				ARWaveView.fetchAndSwapTexturesFromURL(it, URLPath);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Log.i("3ds", "created 3ds");

		} else if (newblip.MIMEtype.equalsIgnoreCase("application/x-obj")) {

			//its a obj , so a mesh
			object3dtype = ARBlipObject.ObjectType.MESH_OBJECT;	
			
			
			HashSet<String> Namesbefore = TextureManager.getInstance()
			.getNames();
			int SizeBefore = Namesbefore.size();

			// load 3d model

			// get model source

			Log.i("obj", "getting model stream");
			URL downloadfrom = new URL(newblip.ObjectData);
			URLConnection conn = downloadfrom.openConnection();
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(conn
					.getInputStream());

			// get texture source
			Log.i("obj", "getting texture stream");
			URL downloadtexturefrom = new URL(newblip.ObjectData.substring(
					0, newblip.ObjectData.length() - 4)
					+ ".mtl");
			URLConnection textconn = downloadtexturefrom.openConnection();
			conn.connect();
			BufferedInputStream textbis = new BufferedInputStream(textconn
					.getInputStream());

			Log.i("obj", "got streams");
			Object3D[] newobjects = Loader.loadOBJ(bis, textbis, 1);

			for (int x = 0; x < newobjects.length; x++) {
				newobjects[x].rotateX(3.14f);

				newobjects[x].rotateMesh();
				newobjects[x].setRotationMatrix(new Matrix());
			}

			Log.i("obj", "now merging...");
			newmarker = Object3D.mergeAll(newobjects);

			// This scale is wrong...no idea what the correct scale to use
			// is :(
			// newmarker.scale(2f);

			// clear memory first
			System.gc(); // no idea if this is a good place, but I was
			// getting some out of memory errors without it

			HashSet<String> newNames = TextureManager.getInstance()
			.getNames();

			newNames.removeAll(Namesbefore);

			Log.i("obj", "number of textures;" + newNames.size());

			// now get the texture names
			Iterator<String> it = newNames.iterator();
			// get path
			String URLPath = newblip.ObjectData.substring(0,
					newblip.ObjectData.lastIndexOf('/') + 1);

			try {
				ARWaveView.fetchAndSwapTexturesFromURL(it, URLPath);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (newblip.MIMEtype.equalsIgnoreCase(ObjectType.PRIMATIVE_CONE.name())) {
			
			//its a cone , so a primitive
			object3dtype = ARBlipObject.ObjectType.PRIMATIVE_CONE;	
			
			newmarker = Primitives.getCone(5);

		} else if (newblip.MIMEtype.equalsIgnoreCase(ObjectType.PRIMATIVE_CUBE.name())) {

			//its a cube, so a primitive
			object3dtype = ARBlipObject.ObjectType.PRIMATIVE_CUBE;			

			newmarker = Primitives.getCube(5);

		}  else if (newblip.MIMEtype.equalsIgnoreCase(ObjectType.PRIMATIVE_LOCATION_MARKER.name())) {

			//its a cube, so a primitive
			object3dtype = ARBlipObject.ObjectType.PRIMATIVE_LOCATION_MARKER;			

			//the object data just contains a color string at the moment
			newmarker = new ArrowMarker(newblip.ObjectData);
			
			

			
		} else {
			// if no recognised type, then we assume its a billboard with
			// text
			object3dtype = ARBlipObject.ObjectType.BILLBOARD_TEXT;
			
			// set texture
			String text = newblip.ObjectData;
			
			newmarker = new SkywriterBillboard(text,7,10);
			
			newmarker.setName(newblip.BlipID);
		
			((SkywriterBillboard) newmarker).updatedTexture(newblip.BlipID, text);
			
			// newplane.setOrigin(new SimpleVector(0,-15,0));(used to move
			// it upwards for when it was on a stand)
			newmarker.setTexture(newblip.BlipID);

			// set billboarding off if rotations are set
			if (newblip.isFacingSprite) {
				newmarker.setBillboarding(true);
			} else {
				newmarker.setBillboarding(false);
			}

		}

		newmarker.setName(newblip.BlipID);

		// work out the co-ordinates to place it at
		// we are going to have to work out the best way to convey real
		// world log/lat
		// into a sensible onscreen scale.
		double worldX = ARBlipUtilitys.getRelativeXLocation(newblip,
				ARWaveView.startingLocation); // As the world is set on loading, and
		// then the camera moves, we always
		// messure relative to the loading
		// location.
		double worldY = ARBlipUtilitys.getRelativeYLocation(newblip,
				ARWaveView.startingLocation); // As the world is set on loading, and
		// then the camera moves, we always
		// messure relative to the loading
		// location.
		double worldZ = ARBlipUtilitys.getRelativeZLocation(newblip,
				ARWaveView.startingLocation); // As the world is set on loading, and
		// then the camera moves, we always
		// messure relative to the loading
		// location.

		float x = (float) -worldX;
		float y = (float) -worldY;
		float z = (float) worldZ;

		Log.e("3ds", "positioning at z=" + z + " y=" + y + "x=" + x);

		// newmarker.setTranslationMatrix(new Matrix());
		newmarker.translate(x, y, z);

		// set rotation
		if (!newblip.isFacingSprite) {
			newmarker.setRotationMatrix(new Matrix());
							
			Log.i("add", "source X deg :" + newblip.roll);
			Log.i("add", "source Y deg :" + newblip.baring);
			Log.i("add", "source Z deg :" + newblip.elevation);
			
			
			newmarker.rotateX((float) Math.toRadians(newblip.roll));
			newmarker.rotateY((float) Math.toRadians(newblip.baring));
			newmarker.rotateZ((float) Math.toRadians(newblip.elevation));
			
		}
		//is editable
		if (true){
			newmarker.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		}
		Log.e("3ds", "adding " + newblip.BlipID + " to layers storage");			
		
		this.addObject(new ARBlipObject(newblip,newmarker, object3dtype));
		
		return newmarker;
	}
	
	public void updateBlip(ARBlip newblipdata) {


		Log.i("3ds", "updating2");
		ARBlipObject updateThis = getObjectByID(newblipdata.BlipID);		
		Log.i("3ds","updating object "+updateThis.object3d.getName());
		
		//update blip
		updateThis.arblip = newblipdata;
		//update object
		Object3D updateThisObject = updateThis.object3d;
		
		// update location (oddly, you have to clear the location and then
		// "move" the position, rather then merely setting it)
		// (you can use setOrigin to directly set it, but this wont move child
		// objects)

		// updateThis.setTranslationMatrix(new Matrix());

		double worldX = ARBlipUtilitys.getRelativeXLocation(newblipdata,
				ARWaveView.startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldY = ARBlipUtilitys.getRelativeYLocation(newblipdata,
				ARWaveView.startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldZ = ARBlipUtilitys.getRelativeZLocation(newblipdata,
				ARWaveView.startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.

		float x = (float) -worldX;
		float y = (float) -worldY;
		float z = (float) worldZ;
		Log.i("3ds","moving to ="+worldX+" , "+worldY+" , "+worldZ);

		updateThisObject.setTranslationMatrix(new Matrix());
		updateThisObject.translate(new SimpleVector(x, y, z));

		// update rotation
		updateThisObject.setRotationMatrix(new Matrix());
		updateThisObject.rotateX((float) Math.toRadians(newblipdata.roll));
		updateThisObject.rotateY((float) Math.toRadians(newblipdata.baring));
		updateThisObject.rotateZ((float) Math.toRadians(newblipdata.elevation));

		// update textures if its a billboard
		if (updateThis.Object3DType == ARBlipObject.ObjectType.BILLBOARD_TEXT){
		
			String text = newblipdata.ObjectData;
			((SkywriterBillboard) updateThis.object3d).updatedTexture(newblipdata.BlipID, text);
			
			
		}
		
		// update other stuff

		
		// set billboarding off if rotations are set
		if (newblipdata.isFacingSprite) {
			updateThisObject.setBillboarding(true);
		} else {
			updateThisObject.setBillboarding(false);
		}
	}
	
	private ARBlipObject getObjectByID(String arblipid) {

		Log.i("3ds", "looking for object with ID:"+arblipid);
		
		Iterator<ARBlipObject> objects = LayersObjects.iterator();
		
		while (objects.hasNext()){					
			ARBlipObject object = objects.next();								
			if (object.arblip.BlipID.equals(arblipid)){
				return object;				
			}
		}
		
		return null;
	}

	public boolean hasARBlipObject(String arblipid){
		
		Iterator<ARBlipObject> objects = LayersObjects.iterator();
		
		while (objects.hasNext()){			
			ARBlipObject object = objects.next();			
			if (object.arblip.BlipID.equals(arblipid)){
				return true;				
			}
		}
		
		return false;
	}

		
	public void scaleBillboards(){
		
		//no scale needed as layer is set to real  mode
		if (BillboardMode == ScaleType.BILLBOARDMODE_REAL){
			return;
		} else {
			
			//else we scale based on scaling type
			
			//loop for each object 
			Iterator<ARBlipObject> objects = LayersObjects.iterator();
			
			while (objects.hasNext()){			
				ARBlipObject object = objects.next();			
				if (object.Object3DType == ObjectType.BILLBOARD_TEXT){
					//if its a billboard..
					Log.i("test", "scaleing billboard "+object.arblip.BlipID);

					Object3D scaleThisObject = object.object3d;
					
					//get its distance to world camera...
					float dist = object.object3d.getTransformedCenter().calcSub(ARWaveView.world.getCamera().getPosition()).length();
					Log.i("test", "distance to camera = "+dist);

					//get field of view
					float fov = ARWaveView.world.getCamera().getFOV();
					Log.i("test", "camera fov = "+fov);
					
					//and scale by it!					
					
					float ScaleFactor = 0.003f; //trail and error guess
					float Scale = ScaleFactor  * dist;
					
					Log.i("test", "scale = "+Scale);
					
					scaleThisObject.setScale(1+Scale);
					// the fov needs to be factored in here somehow.
					// if fov = 1 then scale = 1
					
					
					
				}
			}
			
			
			
		}
		
		
		
	}
	
	
}
