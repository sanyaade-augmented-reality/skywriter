package com.arwave.skywriter;

import java.util.ArrayList;
import java.util.Iterator;

import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class ARWaveLayer {

	
	/** An ARWaveLayer is a collection of ARBlipObjects, 
	 * corresponding exactly to the contents of a wave **/
	
	ArrayList<ARBlipObject> LayersObjects = new ArrayList<ARBlipObject>();

	String ARWaveLayerName = "";
	
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
	
	public void updateBlip(ARBlip newblipdata) {
		ARBlipObject updateThis = this.getObjectByID(newblipdata.BlipID);		

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
				ARBlipView.startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldY = ARBlipUtilitys.getRelativeYLocation(newblipdata,
				ARBlipView.startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.
		double worldZ = ARBlipUtilitys.getRelativeZLocation(newblipdata,
				ARBlipView.startingLocation); // As the world is set on loading, and then
		// the camera moves, we always messure
		// relative to the loading location.

		float x = (float) -worldX;
		float y = (float) -worldY;
		float z = (float) worldZ;
		// Log.i("3ds","moving to ="+worldX+" , "+worldY+" , "+worldZ);

		updateThisObject.setTranslationMatrix(new Matrix());
		updateThisObject.translate(new SimpleVector(x, y, z));

		// update rotation
		updateThisObject.setRotationMatrix(new Matrix());
		updateThisObject.rotateX((float) Math.toRadians(newblipdata.roll));
		updateThisObject.rotateY((float) Math.toRadians(newblipdata.baring));
		updateThisObject.rotateZ((float) Math.toRadians(newblipdata.elevation));

		// update textures
		String text = newblipdata.ObjectData;
		ARBlipView.updatedTexture(newblipdata.BlipID, text);

		// update other stuff

	}
	
	private ARBlipObject getObjectByID(String blipID) {
		
		Iterator<ARBlipObject> objects = LayersObjects.iterator();
		
		while (objects.hasNext()){
			
			ARBlipObject object = objects.next();
			
			if (object.arblip.BlipID.equals("arblipid")){
				return object;				
			}
		}
		
		return null;
	}

	public boolean hasARBlipObject(String arblipid){
		
		
		Iterator<ARBlipObject> objects = LayersObjects.iterator();
		
		while (objects.hasNext()){
			
			ARBlipObject object = objects.next();
			
			if (object.arblip.BlipID.equals("arblipid")){
				return true;				
			}
		}
		
			
		return false;
	}
	
	
	
	
}
