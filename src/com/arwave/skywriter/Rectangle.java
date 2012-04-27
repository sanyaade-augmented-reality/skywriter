package com.arwave.skywriter;

import android.util.Log;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

/** Provides a basic rectangle plane with a specified number of quads **/
public class Rectangle extends Object3D {

	int quads = 0;
	
	public Rectangle (int quads, float scalex, float scaley) {
		
		super(quads*quads*2+8);
		this.quads = quads;
		
	      createRect(quads, scalex, scaley);
	   }



	private void createRect(int quads, float scalex, float scaley) {
		
		Log.i("text", "building for size: = "+scalex+","+scaley);
		
		
		float startx=-scalex*(float) quads/2f;
	      float starty=startx;
	      float tx=0f;
	      float ty=0f;
	      float dtex=(1f/(float) quads);
	      Object3D obj=this;
	    
	      
	      for (int i=0; i<quads; i++) {
	         for (int p=0; p<quads; p++) {
	            float dtx=tx+dtex;
	            float dty=ty+dtex;
	            if (dtx>1f) {
	               dtx=1f;
	            }
	            if (dty>1f) {
	               dty=1f;
	            }
	            obj.addTriangle(new SimpleVector(startx, starty, 0), tx, ty, new SimpleVector(startx, starty+scaley, 0), tx, dty, new SimpleVector(startx+scalex, starty, 0),
	                            dtx, ty);
	            obj.addTriangle(new SimpleVector(startx, starty+scaley, 0), tx, dty, new SimpleVector(startx+scalex, starty+scaley, 0), dtx, dty, new SimpleVector(startx+scalex,
	                            starty, 0), dtx, ty);
	            startx+=scalex;
	            tx+=dtex;
	         }
	         starty+=scaley;
	         startx=-scalex*quads/2;
	         tx=0;
	         ty+=dtex;
	      }
	}
	
	
	/** rebuilds whole objec to rescale - pretty wastefull I think **/
	public void setSize(float scalex, float scaley)
	
	{		Log.i("text", "rebuilding for size: = "+scalex+","+scaley);
	
		this.clearObject();
		 createRect(quads, scalex, scaley);
		 super.build();
		
	}
}
