package com.arwave.skywriter.objects;

import android.util.Log;

import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;

public class ArrowMarker extends Object3D {

	public ArrowMarker() {

		super(15);

		createMesh();

	}

	private void createMesh() {

		Log.i("text", "building for size: = ");

		Object3D obj = this;

		obj.addTriangle(new SimpleVector(-1, 0, 0), 0, 0, new SimpleVector(1,
				0, 0), 0, 0, new SimpleVector(1, 5, 0), 0, 0);

		obj.addTriangle(new SimpleVector(1, 5, 0), 0, 0, new SimpleVector(-1,
				5, 0), 0, 0, new SimpleVector(-1, 0, 0), 0, 0);

		obj.addTriangle(new SimpleVector(-2, 5, 0), 0, 0, new SimpleVector(2,
				5, 0), 0, 0, new SimpleVector(0, 9, 0), 0, 0);
		
		
		obj.build();

		
		this.setAdditionalColor(RGBColor.WHITE);
		this.setCulling(false);
		
	}
	
}
