package com.arwave.skywriter.utilities;

import android.util.Log;

import com.threed.jpct.GenericVertexController;
import com.threed.jpct.IVertexController;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class ObjectSerializer {

	
	/** IGNORE CLASS - couldnt get it too work
	 * converts a 3dobject to a string in the form
	 * (x,y,z|x,y,z|x,y,z)(x,y,z|x,y,z|x,y,z)(x,y,z|x,y,z|x,y,z)
	 * 
	 * 
	 * Note: Texturing mapping of any sort is not supporting, only the mesh shape 
	 * is serializ ed **/
	static public String ObjectToString(Object3D obj){
		
		//use a vertex modifer to store the object into an array:
	
		
		 IVertexController getdata=new GetDataMod();
		 
		 
			Log.i("obj", "getting data from object");
						
			obj.getMesh().setVertexController(getdata, IVertexController.PRESERVE_SOURCE_MESH);
			obj.getMesh().applyVertexController();
			//after this the data array should be filled
			SimpleVector[] objdata = getdata.getSourceMesh();
			
			String meshdata = "";
			String currentTri = "";
			
			
			int tc =1;
			
			for (int i = 0; i < objdata.length; i++) {

				float x= objdata[i].x;
				float y= objdata[i].y;
				float z= objdata[i].z;
				
				Log.i("obj", " x= "+objdata[i].x);
			Log.i("obj", " y= "+objdata[i].y);
				Log.i("obj", " z= "+objdata[i].z);
			
				currentTri = currentTri + x+","+y+","+z;
				
				Log.i("obj", " tc= "+tc);
				
				if (tc==3){
					tc=0;
					meshdata =meshdata+"("+currentTri+")";
					Log.i("obj", " _______________<<< "+"("+currentTri+")");
					currentTri="";
					
					
				} else {
					currentTri = currentTri + "|";
				}
				

				tc++;
			}
			
			meshdata = meshdata+"("+currentTri+")";
			Log.i("obj", "result: "+meshdata);
			
		//(-9.0,-90.0,0.0|9.0,-90.0,0.0|9.0,-32.0,0.0)
			//(-9.0,-32.0,0.0|-24.0,-32.0,0.0|24.0,-32.0,0.0)
			//(0.0,0.0,0.0|)
			
		 return meshdata;
		 
	}
	
	static public Object3D StringToObject(String objAsString){
		
		//split to co-ordinate sets
		String[] coOrdinateSets = objAsString.split("|");
		

		Log.i("obj", "creating");
		int maxt = (coOrdinateSets.length/3)+1;

		Object3D obj = new Object3D(maxt);
		
		for (int i = 0; i < coOrdinateSets.length; i++) {
			String set = coOrdinateSets[i];
			
			String X = set.split(",")[0];
			String Y = set.split(",")[1];
			String Z = set.split(",")[2];
			
			float fx = Float.parseFloat(X);
			float fy = Float.parseFloat(Y);
			float fz = Float.parseFloat(Z);
			
			//obj.addTriangle(new SimpleVector(fx, fy, fz), tx, ty, new SimpleVector(fx, fy, fz), tx, dty, new SimpleVector(startx+scalex, starty, 0),
           //         dtx, ty);
			
		}
		
		
		
		return null;
		
		
	}
	
	private static class GetDataMod extends GenericVertexController {
		SimpleVector[] output;
		
		public GetDataMod() {
			super();
			
		}

		public void apply() {
			SimpleVector[] s = getSourceMesh();
			SimpleVector[] d = getDestinationMesh();
			output =  s;
			d=s;
		}
		
		public SimpleVector[] getOutput(){
			return output;
			
			
		}
	}
}

