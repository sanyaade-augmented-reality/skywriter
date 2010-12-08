package com.arwave.skywriter;

import com.threed.jpct.Object3D;

public class ARBlipObject {

	/** This is a Jcpt 3d object with an associated ARBlip **/	
	
	ARBlip arblip;
	Object3D object3d;
	
	public ARBlipObject(ARBlip arblip, Object3D object3d){
		
		this.arblip = arblip;
		this.object3d = object3d;
		
	}
	
	
	
}

