package com.arwave.skywriter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.util.Log;

import com.threed.jpct.RGBColor;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

public class SkywriterBillboard extends Rectangle {

	static Paint paint = new Paint();
	
	int TextWidth = 0;
	int TextHeight = 0;
	int TextScaleX = 10;
	int TextScaleY = 10;
	
	int DistanceScaleMultiplier = 1;
	
	public SkywriterBillboard(String TextString, int TextScaleX,int TextScaleY) {
		
		super(1, getWidthFromText(TextString)*TextScaleX, getHeightFromText(TextString)*TextScaleY); // fixed, but in future these should vary based on the size of the text?
				
		this.TextScaleX= TextScaleX;
		this.TextScaleY= TextScaleY;
		
		this.setAdditionalColor(RGBColor.WHITE);
		this.setCulling(false);
		
		//set paint to ARWaveView default
		paint = ARWaveView.paint;
		
		//add text texture to billboard;
		
		//........				
	}

	public void updatedTexture(String Texturename, String text) {

		TextWidth = getWidthFromText(text);
		TextHeight= getHeightFromText(text);
		
		Log.i("add", "update texture triggered with:"+Texturename+"|"+text);

		paint.setColor(Color.BLACK);

		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		FontMetricsInt fontMetrics = paint.getFontMetricsInt();
		int fontHeight = fontMetrics.leading - fontMetrics.ascent
		+ fontMetrics.descent;
		int baseline = -fontMetrics.top -2;
		int height = fontMetrics.bottom - fontMetrics.top;

		// have to add multiline support here
		//Bitmap charImage = Bitmap.createBitmap(closestTwoPower((int) paint
		//		.measureText(text) + 10), 32, config);
		
		Bitmap charImage = Bitmap.createBitmap(closestTwoPower(TextWidth*TextScaleX), closestTwoPower(3+TextHeight*TextScaleY), config);
		
		Canvas canvas = new Canvas(charImage);
		canvas.drawColor(Color.WHITE);
		
		String[] lines = text.split("\r\n|\r|\n");
		int linenumber = 0;
		for (String line : lines) { 			
			canvas.drawText(line, 5, baseline+(linenumber*(TextScaleY+3)), paint); // draw text with a margin
			linenumber++;
		}
		
		// of 10

		TextureManager tm = TextureManager.getInstance();
		Texture testtext = new Texture(charImage, true); // the true specify
		
		// the texture has
		// its own alpha. If
		// not, black is
		// assumed to be
		// alpha!

		//

		if (tm.containsTexture(Texturename)) {

			Log.i("add", "updating texture="+Texturename);

			//tm.removeAndUnload(Texturename,fb);

			Log.i("add", "updated texture="+Texturename);

			//tm.addTexture(Texturename, testtext);
			tm.unloadTexture(ARWaveView.fb, tm.getTexture(Texturename));
			tm.replaceTexture(Texturename, testtext);


		} else {
			tm.addTexture(Texturename, testtext);
		}

	}
	
	/** max number of characters wide 
	 * @return **/
	static private int getWidthFromText(String TextString){
		
		
		//loop over string for max number of characters between new lines
		String[] lines = TextString.split("\r\n|\r|\n");
		int longestline = 0;
		for (String line : lines) { 
		   if (line.trim().length()>longestline){
			   longestline = line.trim().length();
		   }
		}

		
		Log.i("text", "longest line is "+longestline +" chars");
		
		return longestline;
		
	}
	/**
	 * returns the closest power of two that is equal or greater than given
	 * number (good for textures!
	 */
	private static int closestTwoPower(int i) {
		int power = 1;
		while (power < i) {
			power <<= 1;
		}
		return power;
	}
	/** max number of characters tall **/
	
	static private int getHeightFromText(String TextString){
		
		int height = TextString.split("\r\n|\r|\n").length;
		Log.i("text", "tallest line is "+height +" chars");
		
		return height;
		
		
	}
	
	public void scale(int Scale){
		DistanceScaleMultiplier = Scale;
		this.scale(DistanceScaleMultiplier);
	}
}
