package com.arwave.skywriter;

import com.threed.jpct.Object3D;

public class ARBlipObject {

	/** This is a Jcpt 3d object with an associated ARBlip **/	
	
	ARBlip arblip;
	Object3D object3d;
	
	// fast way to ID the object type	 
	 public enum ObjectType {
		 BILLBOARD_OBJECT, PRIMATIVE_OBJECT, MESH_OBJECT
		}
	 
	 
	 ObjectType Object3DType = ObjectType.BILLBOARD_OBJECT; //defaults to billboard
	
	public ARBlipObject(ARBlip arblip, Object3D object3d, ObjectType ot){
		
		this.arblip = arblip;
		this.object3d = object3d;
		this.Object3DType = ot;
		
	}
	/** *Creates a Object3D with no real/specified ARBlip...this is used for internal creation (background elements etc)*/
	public ARBlipObject(Object3D object3d, ObjectType ot){
		
		this.arblip = new ARBlip();
		this.arblip.BlipID = object3d.getName();
		this.object3d = object3d;
		this.Object3DType = ot;
	}
	
	
}

