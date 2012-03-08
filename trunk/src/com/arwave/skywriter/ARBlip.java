package com.arwave.skywriter;

import java.util.ArrayList;

import android.util.Log;

/** Current, crude, version of a blip object. A lot needed to refine **/
public class ARBlip {

	static private String ARWAVEIDENTIFIER = "#ARWAVE#";

	/** Should be a unique identification of the object **/
	String BlipID = "";

	/** The wave this blip belongs too **/
	String ParentWaveID = "";

	/** A url pointing to the data to geolocate **/
	String ObjectData = "";

	/** MIME type **/
	String MIMEtype = "";

	/** Co-ordinate system in use **/
	String CordinateSystemUsed = "";

	/** Location **/
	double x = 0.0;
	/** lat **/
	double y = 0.0;
	/** long **/
	double z = 0.0;
	/** alt **/

	/** Is an occlusion mask object **/
	boolean isOcculisionMask = false;

	/** Vector **/
	// If no vector is specified, its assumed to be a facing sprite.
	int baring = 0;
	int elevation = 0;
	int roll = 0;
	boolean isFacingSprite = false;

	// (should these be doubles too? or is that wastefull? float? half a degree
	// could make quite a difference)

	String DataUpdatedTimestamp; // Time the Data was updated/changed, not
									// merely when the blip was changed.

	// Permissions

	/** Reference URL for positioning on a marker **/
	String MarkerURL = "";

	/** Metatags **/
	ArrayList<String> MetaTags = new ArrayList<String>();

	public ARBlip(String ArBlipString) {
		super();
		deserialise(ArBlipString);
	}

	public ARBlip() {

	}

	/** incomplete serialisation **/
	public String serialise() {
		// converts to a string

		String ArBlipString = ARWAVEIDENTIFIER + BlipID +"#"+ParentWaveID+ "#" + x + "#" + y
				+ "#" + z + "#" + ObjectData + "#" + MIMEtype;
		
		
		return ArBlipString;
	}

	/** incomplete deserialisation **/
	public boolean deserialise(String ArBlipString) {

		Log.i("wave", "object data =" + ArBlipString);

		// check it starts with the identifier
		if (ArBlipString.startsWith(ARWAVEIDENTIFIER)) {

			ArBlipString = ArBlipString.substring(ARWAVEIDENTIFIER.length());

			String newObjectData;
			try {

				String id = ArBlipString.split("#")[0];

				String waveid = ArBlipString.split("#")[1];
				String newx = ArBlipString.split("#")[2];
				String newy = ArBlipString.split("#")[3];
				String newz = ArBlipString.split("#")[4];
				newObjectData = ArBlipString.split("#")[5];

				// optional;
				String mime;
				try {
					mime = ArBlipString.split("#")[6];
				} catch (Exception e) {
					mime = "";

				}

				BlipID = id;
				ParentWaveID=waveid;
				
				x = Double.parseDouble(newx);
				y = Double.parseDouble(newy);
				z = Double.parseDouble(newz);
				MIMEtype = mime;

			} catch (NullPointerException e) {
				return false;
			} catch (NumberFormatException e) {

				// TODO Auto-generated catch block
				return false;
			}

			Log.i("wave", "object data "+BlipID+" " + MIMEtype + " x=" + x + " y=" + y
					+ " z=" + z);

			ObjectData = newObjectData;

			return true;

		} else {
			return false;
		}

	}

}
