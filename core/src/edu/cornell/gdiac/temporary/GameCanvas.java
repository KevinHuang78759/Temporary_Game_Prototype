/*
 * GameCanvas.java
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.ArrayList;


/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * 
 * This version of GameCanvas only supports (rectangular) Sprite drawing.
 * support for polygonal textures and drawing primitives will be present
 * in future labs.
 */
public class GameCanvas {

	/** While we are not drawing polygons (yet), this spritebatch is more reliable */
	private PolygonSpriteBatch spriteBatch;
	
	/** Track whether or not we are active (for error checking) */
	private boolean active;
	
	/** The current color blending mode */
	private BlendState blend;
	
	/** Value to cache window width (if we are currently full screen) */
	int width;
	/** Value to cache window height (if we are currently full screen) */
	int height;

	// CACHE OBJECTS
	/** Affine cache for current sprite to draw */
	private Affine2 local;
	/** Cache object to unify everything under a master draw method */
	private TextureRegion holder;
	Pixmap pixel;
	ArrayList<Texture> disp;
	Texture pixelTexture;
	/**
	 * Creates a new GameCanvas determined by the application configuration.
	 * 
	 * Width, height, and fullscreen are taken from the LWGJApplicationConfig
	 * object used to start the application.  This constructor initializes all
	 * of the necessary graphics objects.
	 */
	public GameCanvas() {
		active = false;
		spriteBatch = new PolygonSpriteBatch();
		// Set the projection matrix (for proper scaling)
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		pixel = new Pixmap(1,1,Pixmap.Format.RGB888);
		pixel.setColor(Color.WHITE);
		pixel.fill();
		pixelTexture = new Texture(pixel);
	}
		
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		pixel.dispose();
		spriteBatch.dispose();
		pixelTexture.dispose();
		PRG = null;
		pixel = null;
    	spriteBatch = null;
    	local  = null;
    	holder = null;
    }

	/**
	 * Returns the width of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getWidth()
	 *
	 * @return the width of this canvas
	 */
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}
	
	/**
	 * Changes the width of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param width the canvas width
	 */
	public void setWidth(int width) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, getHeight());
		}
		resize();
	}

	/**
	 * Returns the height of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getHeight()
	 *
	 * @return the height of this canvas
	 */
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
	
	/**
	 * Changes the height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param height the canvas height
	 */
	public void setHeight(int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(getWidth(), height);	
		}
		resize();
	}
	
	/**
	 * Returns the dimensions of this canvas
	 *
	 * @return the dimensions of this canvas
	 */
	public Vector2 getSize() {
		return new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
	}
	
	/**
	 * Changes the width and height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param width the canvas width
	 * @param height the canvas height
	 */
	public void setSize(int width, int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, height);
		}
		resize();
	}
	
	/**
	 * Returns whether this canvas is currently fullscreen.
	 *
	 * @return whether this canvas is currently fullscreen.
	 */	 
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen(); 
	}
	
	/**
	 * Sets whether or not this canvas should change to fullscreen.
	 *
	 * If desktop is true, it will use the current desktop resolution for
	 * fullscreen, and not the width and height set in the configuration
	 * object at the start of the application. This parameter has no effect
	 * if fullscreen is false.
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 */	 
	public void setFullscreen(boolean value) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		if (value) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
		}
	}
	
	/**
	 * Resets the SpriteBatch camera when this canvas is resized.
	 *
	 * If you do not call this when the window is resized, you will get
	 * weird scaling issues.
	 */
	 public void resize() {
		// Resizing screws up the spriteBatch projection matrix
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Returns the current color blending state for this canvas.
	 *
	 * Textures draw to this canvas will be composited according
	 * to the rules of this blend state.
	 *
	 * @return the current color blending state for this canvas
	 */
	public BlendState getBlendState() {
		return blend;
	}
	
	/**
	 * Sets the color blending state for this canvas.
	 *
	 * Any texture draw subsequent to this call will use the rules of this blend 
	 * state to composite with other textures.  Unlike the other setters, if it is 
	 * perfectly safe to use this setter while  drawing is active (e.g. in-between 
	 * a begin-end pair).  
	 *
	 * @param state the color blending rule
	 */
	public void setBlendState(BlendState state) {
		if (state == blend) {
			return;
		}
		switch (state) {
		case NO_PREMULT:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);

			break;
		case ALPHA_BLEND:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE);
			break;
		case OPAQUE:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ZERO);
			break;
		}
		blend = state;
	}

	/**
	 * Start and active drawing sequence with the identity transform.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void begin() {
		unitPerp = new Vector2();
    	spriteBatch.begin();
		disp = new ArrayList<>();
    	active = true;

    	// Clear the screen
		Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

	/**
	 * Ends a drawing sequence, flushing textures to the graphics card.
	 */
    public void end() {
		spriteBatch.setColor(Color.WHITE);
    	spriteBatch.end();
    	active = false;
    }

    
	/**
     * Draw the seamless background image.
     *
     * The background image is drawn (with NO SCALING) at position x, y.  Width-wise, 
     * the image is seamlessly scrolled; when we reach the image we draw a second copy.  
     *
     * To work properly, the image should be wide and high enough to fill the screen.
     * 
     * @param image  Texture to draw as an overlay
	 * @param x      The x-coordinate of the bottom left corner
	 * @param y 	 The y-coordinate of the bottom left corner
	 */
    public void drawBackground(Texture image, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		//When drawing we must maintain aspect ratio.
		//To do this effectively we determine which variable to scale by.

		float ratio = (image.getHeight()/(float)image.getWidth())/(getHeight()/(float)getWidth());
		float scale = ratio >= 1 ? getWidth()/(float)image.getWidth() : getHeight()/(float)image.getHeight();
		float trueW = image.getWidth()*scale;
		float trueH = image.getHeight()*scale;
		float tlx = x - (trueW - getWidth())/2f;
		float tly = y - (trueH - getHeight())/2f;

        spriteBatch.draw(image, tlx,   tly, trueW, trueH);

    }

	/**
	 * Pointer used to determine a unit vector in a certain direction for line drawing
	 */

	Vector2 unitPerp;

	/**
	 * Draw a line given the starting and ending coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param thickness
	 * @param c
	 */
	public void drawLine(float x1, float y1, float x2, float y2, int thickness, Color c){

		//Calculate the distance and direction
		float dist = (float) Math.sqrt((x2 - x1)*(x2-x1) + (y2 - y1)*(y2-y1));
		unitPerp.set((y2-y1)/dist, (x1-x2)/dist);
		//Form two triangles - we are essentially drawing a very thin rectangle
		float[] coors = new float[]{x1 + (thickness/2f)*unitPerp.x,y1 + (thickness/2f)*unitPerp.y,
									x1 - (thickness/2f)*unitPerp.x,y1 - (thickness/2f)*unitPerp.y,
									x2 + (thickness/2f)*unitPerp.x,y2 + (thickness/2f)*unitPerp.y,
									x2 - (thickness/2f)*unitPerp.x,y2 - (thickness/2f)*unitPerp.y};


		short[] triangles = new short[]{0, 1, 2, 1, 2, 3};
		//Create the PolygonRegion and set the color, then draw it with no offset
		PolygonRegion PRG = new PolygonRegion(new TextureRegion(pixelTexture), coors, triangles);
		Color tint = new Color(c.r, c.g,c.b,1f);

		spriteBatch.setColor(tint);
		spriteBatch.draw(PRG, 0, 0);

	}
//PolygonRegion pointer used for drawing discrete lines and shapes
	PolygonRegion PRG;

	/**
	 * Draw a rectangle given 2 coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param c
	 * @param filled
	 */
	public void drawRect(float x1, float y1, float x2, float y2, Color c, boolean filled){
		//If its filled
		if(filled){
			//Set the triangle coordiantes to the 4 corners
			float[] coors = new float[]{x1,y1,
										x1,y2,
										x2,y1,
										x2,y2};


			short[] triangles = new short[]{0, 1, 2, 1, 3, 2};
			//Generate a polyregion
			PRG = new PolygonRegion(new TextureRegion(pixelTexture), coors, triangles);
			//set the color
			Color tint = new Color(c.r, c.g,c.b,1f);
			spriteBatch.setColor(tint);
			//draw
			spriteBatch.draw(PRG, 0, 0);
		}
		else{
			//If not filled, draw 4 lines representing the boundaries
			drawLine(x1,y1,x1,y2,3, c);
			drawLine(x1,y1,x2,y1,3, c);
			drawLine(x2,y1,x2,y2,3, c);
			drawLine(x1,y2,x2,y2,3, c);
		}
	}

//	/**
//	 * Fills in a rectangle with a certain texture
//	 * @param tr
//	 * @param x1
//	 * @param y1
//	 * @param x2
//	 * @param y2
//	 */
//	public void textureRect(TextureRegion tr, float x1, float y1, float x2, float y2, Texture.TextureWrap uWrap, Texture.TextureWrap vWrap){
//
//	}

	/**
	 * Another method for drawing a rectangle when given the bottom left corner, width, and height
	 * @param BL
	 * @param w
	 * @param h
	 * @param c
	 * @param filled
	 */

	public void drawRect(Vector2 BL, float w, float h, Color c, boolean filled){
		drawRect(BL.x, BL.y, BL.x + w, BL.y + h, c, filled);
	}

	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(image, x, y);
	}

	public void draw(Texture image, float x, float y, float width, float height) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(image, x,  y, width, height);
	}

	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin
	 * @param y 	The y-coordinate of the texture origin
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, 
					float x, float y, float angle, float sx, float sy) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (we have to for transforms)
		holder.setRegion(image);
		draw(holder,tint,ox,oy,x,y,angle,sx,sy);
		tint.a = 1f;
	}
	
	/**
	 * Draws the tinted texture region (filmstrip) at the given position.
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(region, x,  y);
	}
	
	public void draw(TextureRegion region, float x, float y, float width, float height) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(region, x,  y, width, height);
	}
	
	/**
	 * Draws the tinted texture region (filmstrip) with the given transformations
	 *
	 * THIS IS THE MASTER DRAW METHOD (Modify for exercise 4)
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin
	 * @param y 	The y-coordinate of the texture origin
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		computeTransform(ox,oy,x,y,angle,sx,sy);
		spriteBatch.setColor(tint);
		spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
	}

	/**
	 * Draws a proportional scaled subsection of a texture. The texture will be centered at x, y, the actual visible <br>
	 * drawn portion is essentially "cropped" out
	 * @param textureRegion Texture to draw
	 * @param x x coord of where the middle of the whole image should go
	 * @param y y coord of where the middle of the whole image should go
	 * @param scale scaling factor
	 * @param xStart % of image starting from left of image of where the image should start drawing
	 * @param xEnd % of image starting from left of image of where the image should draw to
	 * @param yStart % of image starting from bottom of image of where the image should start drawing
	 * @param yEnd % of image starting from bottom of image of where the image should draw to
	 */
	public void drawSubsection(TextureRegion textureRegion,
							   float x, float y, float scale, float xStart, float xEnd, float yStart, float yEnd) {
		float trueWidth = textureRegion.getRegionWidth() * scale * (xEnd - xStart);
		float trueHeight = textureRegion.getRegionHeight() * scale * (yEnd - yStart);
		spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(textureRegion.getTexture(),
				x - textureRegion.getRegionWidth() * (scale * (0.5f - xStart)),
				y - textureRegion.getRegionHeight() * (scale * (0.5f - yStart)),
				trueWidth,
				trueHeight,
				xStart,
				1f - yStart,
				xEnd,
				1f - yEnd
		);
	}

	/**
	 * Draws a proportional scaled subsection of a texture. The texture will be centered at x, y, the actual visible <br>
	 * drawn portion is essentially "cropped" out
	 * @param textureRegion Texture to draw
	 * @param tint Tint of the texture to draw
	 * @param x x coord of where the middle of the whole image should go
	 * @param y y coord of where the middle of the whole image should go
	 * @param scale scaling factor
	 * @param xStart % of image starting from left of image of where the image should start drawing
	 * @param xEnd % of image starting from left of image of where the image should draw to
	 * @param yStart % of image starting from bottom of image of where the image should start drawing
	 * @param yEnd % of image starting from bottom of image of where the image should draw to
	 */
	public void drawSubsection(TextureRegion textureRegion, Color tint,
							   float x, float y, float scale, float xStart, float xEnd, float yStart, float yEnd) {
		float trueWidth = textureRegion.getRegionWidth() * scale * (xEnd - xStart);
		float trueHeight = textureRegion.getRegionHeight() * scale * (yEnd - yStart);
		spriteBatch.setColor(tint);
		spriteBatch.draw(textureRegion.getTexture(),
				x - textureRegion.getRegionWidth() * (scale * (0.5f - xStart)),
				y - textureRegion.getRegionHeight() * (scale * (0.5f - yStart)),
				trueWidth,
				trueHeight,
				xStart,
				1f - yStart,
				xEnd,
				1f - yEnd
		);
	}
	
	/**
	 * Compute the affine transform (and store it in local) for this image.
	 * 
	 * This helper is meant to simplify all of the math in the above draw method
	 * so that you do not need to worry about it when working on Exercise 4.
	 *
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin
	 * @param y 	The y-coordinate of the texture origin
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */
	private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
		local.setToTranslation(x,y);
		local.rotate(angle);
		local.scale(sx,sy);
		local.translate(-ox,-oy);
	}

    /**
     * Draws text on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param x The x-coordinate of the lower-left corner
     * @param y The y-coordinate of the lower-left corner
     */
    public void drawText(String text, BitmapFont font, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		GlyphLayout layout = new GlyphLayout(font,text);
		font.setColor(Color.BLACK);
		font.draw(spriteBatch, layout, x, y);
    }

	public void drawTextSetColor(String text, BitmapFont font, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		GlyphLayout layout = new GlyphLayout(font,text);
		font.draw(spriteBatch, layout, x, y);
	}

	/**
	 * Draws text on the screen.
	 *
	 * @param text The string to draw
	 * @param font The font to use
	 * @param x The x-coordinate of the lower-left corner
	 * @param y The y-coordinate of the lower-left corner
	 * @param c The color
	 */
	public void drawText(String text, BitmapFont font, float x, float y, Color c) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		GlyphLayout layout = new GlyphLayout(font,text);
		font.setColor(c);

		font.draw(spriteBatch, layout, x, y);
	}

    /**
     * Draws text centered on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param offset The y-value offset from the center of the screen.
     */
    public void drawTextCentered(String text, BitmapFont font, float offset) {
		drawTextCentered(text, font, offset, Color.BLACK);
    }




	/**
	 * Draws text centered on the screen. Also takes color as a parameter
	 *
	 * @param text The string to draw
	 * @param font The font to use
	 * @param offset The y-value offset from the center of the screen.
	 * @param tint The color of the text to be drawn
	 */
	public void drawTextCentered(String text, BitmapFont font, float offset, Color tint) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		GlyphLayout layout = new GlyphLayout(font,text);
		float x = (getWidth()  - layout.width) / 2.0f;
		float y = (getHeight() + layout.height) / 2.0f;
		font.setColor(tint);
		font.draw(spriteBatch, layout, x, y+offset);
	}
    

	/**
	 * Enumeration of supported BlendStates.
	 *
	 * For reasons of convenience, we do not allow user-defined blend functions.
	 * 99% of the time, we find that the following blend modes are sufficient
	 * (particularly with 2D games).
	 */
	public enum BlendState {
		/** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
		ALPHA_BLEND,
		/** Alpha blending on, assuming the colors have no pre-multipled alpha */
		NO_PREMULT,
		/** Color values are added together, causing a white-out effect */
		ADDITIVE,
		/** Color values are draw on top of one another with no transparency support */
		OPAQUE
	}	
}