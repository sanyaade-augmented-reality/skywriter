package com.arwave.skywriter.objects;

import android.util.Log;

import com.arwave.skywriter.utilities.ObjectSerializer;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;

/**
 * The ArrowOMatic Arrow Maker! tada!
 * 
 * @author Thomas Wrobel
 **/
public class ArrowMarker extends Object3D {

	private static final long serialVersionUID = -7857376267735094374L;
	int length = -90;
	int halfwidth = 9;

	int headheight = 32;

	public ArrowMarker() {

		super(15);

		createMesh();
		this.setAdditionalColor(RGBColor.BLACK);
		
		//test serialisation
		//ObjectSerializer.ObjectToString(this);
		

	}
	public ArrowMarker(String colour) {

		super(15);

		createMesh();
		
		this.setColour(colour);

	}
	public void setColour(String colour){
		
		if (colour.equalsIgnoreCase("Red")){
			this.setAdditionalColor(RGBColor.RED);
		};
		
		if (colour.equalsIgnoreCase("BLUE")){
			this.setAdditionalColor(RGBColor.BLUE);
		};
		
		if (colour.equalsIgnoreCase("GREEN")){
			this.setAdditionalColor(RGBColor.GREEN);
		};
		
		
	}

	private void createMesh() {

		Log.i("ArrowMarker", "creating");

		Object3D obj = this;

		obj.addTriangle(new SimpleVector(-halfwidth, length, 0), 0, 0,
				new SimpleVector(halfwidth, length, 0), 0, 0, new SimpleVector(
						halfwidth, -headheight, 0), 0, 0);

		obj.addTriangle(new SimpleVector(halfwidth, -headheight, 0), 0, 0,
				new SimpleVector(-halfwidth, -headheight, 0), 0, 0,
				new SimpleVector(-halfwidth, length, 0), 0, 0);

		obj.addTriangle(new SimpleVector(-15 - halfwidth, -headheight, 0), 0,
				0, new SimpleVector(15 + halfwidth, -headheight, 0), 0, 0,
				new SimpleVector(0, 0, 0), 0, 0);

		obj.build();

		Log.i("ArrowMarker", "created");

		this.setCulling(false);

	}

	public void setColour(RGBColor c) {
		this.setAdditionalColor(c);
	}

}
