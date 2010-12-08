package com.arwave.skywriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.util.Log;

import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;

public class ARWaveLayer {

	
	/** An ARWaveLayer is a collection of ARBlipObjects, 
	 * corresponding exactly to the contents of a wave **/
	
	ArrayList<ARBlipObject> LayersObjects = new ArrayList<ARBlipObject>();

	String ARWaveLayerID = "";
	
	
	public ARWaveLayer(){
		
	}
	
	public void addObject(ARBlipObject newArBlipObject){		
		
		// only add if not present already
		if (!hasARBlipObject(newArBlipObject.arblip.BlipID)){
		LayersObjects.add(newArBlipObject);
		}
		
	
	}
	
	public void setVisible(boolean visible){
		
		//iterates over its contents and sets its visibility		
		Iterator<ARBlipObject> objects = LayersObjects.iterator();
		
		while (objects.hasNext()){			
			ARBlipObject object = objects.next();
			object.object3d.setVisibility(visible);
		}
		
	}
	
	/** will create a new arblip object and add it to the layer, as well as returning it to add to the world **/
	public Object3D createNewBlipObject(ARBlip newblip) throws IOException {
		// new object dummy
		Object3D newmarker = Object3D.createDummyObj();

		// if blip type is specified
		// load 3d object from url
		if (newblip.MIMEtype.equalsIgnoreCase("application/x-3ds")) {

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

				// Bitmap.Config config = Bitmap.Config.ARGB_8888;
				// Bitmap charImage = Bitmap.createBitmap(16, 16, config);
				// Canvas canvas = new Canvas(charImage);
				// canvas.drawColor(Color.BLACK);
				// TextureManager tm = TextureManager.getInstance();
				// Texture newmaxtexture = new Texture(charImage,false);
				// tm.addTexture("occlusion", newmaxtexture);

				// newmarker.setTexture("occlusion");

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

		} else if (newblip.MIMEtype.equalsIgnoreCase("Primative_Bounceing_Cone")) {

			// load 3d model

			newmarker = Primitives.getCone(5);
			// rotate it io
			//newmarker.rotateX(-(float) Math.PI / 2);

		} else if (newblip.MIMEtype.equalsIgnoreCase("Primative_Bounceing_Cube")) {

			// load 3d model

			newmarker = Primitives.getCube(5);
			// rotate it io
			//newmarker.rotateX(-(float) Math.PI / 2);

		} else {
			// if no recognised type, then we assume its a billboard with
			// text
			// if not,create a new arblip placemark
			// newmarker = Primitives.getPyramide(2, 8);

			// newmarker.setName(newblip.BlipID);
			// newmarker.setTexture("rock");
			// newmarker.setAdditionalColor(RGBColor.BLACK);

			// billboard bit at top
			// Object3D newplane = Primitives.getPlane(1, 60);
			// newplane = new Rectangle(1,8,3);

			// newplane.setAdditionalColor(RGBColor.WHITE);
			// newplane.setBillboarding(true);

			// simplifed (no stand for billboard now)
			newmarker = new SkywriterBillboard();
			newmarker.setName(newblip.BlipID);
			// set texture
			String text = newblip.ObjectData;
			ARWaveView.updatedTexture(newblip.BlipID, text);
			// newplane.setOrigin(new SimpleVector(0,-15,0));(used to move
			// it upwards for when it was on a stand)
			newmarker.setTexture(newblip.BlipID);

			// set billboarding off if rotations are set
			if (newblip.isFacingSprite) {
				newmarker.setBillboarding(true);
			}

			//newmarker = newplane;

			// newplane.setBillboarding(true);
			// link objects together
			// newplane.addParent(newmarker);

			// merge the plane and
			// newmarker = newplane.mergeObjects(newplane, newmarker);
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

		Log.i("3ds", "positioning at z=" + z + " y=" + y + "x=" + x);

		// newmarker.setTranslationMatrix(new Matrix());
		newmarker.translate(x, y, z);

		// set rotation
		if (!newblip.isFacingSprite) {
			newmarker.setRotationMatrix(new Matrix());
			Log.i("add", "roating..." + newblip.baring);
			newmarker.rotateX((float) Math.toRadians(newblip.roll));
			newmarker.rotateY((float) Math.toRadians(newblip.baring));
			newmarker.rotateZ((float) Math.toRadians(newblip.elevation));
		}
		//is editable
		if (true){
			newmarker.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		}
		Log.i("3ds", "adding " + newblip.BlipID + " to layers storage");			
		this.addObject(new ARBlipObject(newblip,newmarker));
		
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

		// update textures
		String text = newblipdata.ObjectData;
		ARWaveView.updatedTexture(newblipdata.BlipID, text);

		// update other stuff

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

		
	
	
	
}
