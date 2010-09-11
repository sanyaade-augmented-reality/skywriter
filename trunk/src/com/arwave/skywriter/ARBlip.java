package com.arwave.skywriter;
import java.util.ArrayList;

/** Current, crude, version of a blip object. A lot needed to refine **/
public class ARBlip {
	
     /** Should be a unique identification of the object **/
	String BlipID = "";
	
	/** The wave this blip belongs too **/
	String ParentWaveID="";	
	
	/**A url pointing to the data to geolocate **/
	String ObjectData = "";
	
    /**MIME type **/
	String MIMEtype = "";
	
	/** Co-ordinate system in use **/
	String CordinateSystemUsed ="";
	
	/**Location **/
	double x = 0.0; /** lat **/
	double y = 0.0; /** long **/
	double z = 0.0; /** alt **/

	/** Is an occlusion mask object **/
	boolean isOcculisionMask = false;
	
	/**Vector **/
    //If no vector is specified, its assumed to be a facing sprite.
	int baring = 0;
	int elevation = 0;
	int roll = 0;
	boolean isFacingSprite = false;
	
	// (should these be doubles too? or is that wastefull? float? half a degree could make quite a difference)
	
	String DataUpdatedTimestamp; //Time the Data was updated/changed, not merely when the blip was changed.
	
//Permissions 
	
	/**Reference URL for positioning on a marker **/
	String MarkerURL="";
	
	/**Metatags **/
	ArrayList<String> MetaTags = new ArrayList<String>();

	
}

