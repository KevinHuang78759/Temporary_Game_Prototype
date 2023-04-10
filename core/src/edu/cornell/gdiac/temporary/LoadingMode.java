/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do 
 * anything until loading is complete. You know those loading screens with the inane tips 
 * that want to be helpful?  That is asynchronous loading.  
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the 
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.*;
import edu.cornell.gdiac.util.*;

import java.util.logging.Level;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode implements Screen, InputProcessor, ControllerListener {
	// There are TWO asset managers.  One to load the loading screen.  The other to load the assets
	/** Internal assets for this loading screen */
	private AssetDirectory internal;
	/** The actual assets to be loaded */
	private AssetDirectory assets;
	
	/** Background texture for start-up */
	private Texture background;
	/** Play button to display when done */
	private Texture playButton;
	private Texture calibrationButton;

	/** Texture atlas to support a progress bar */
	private Texture statusBar;

	/** Tempo-Rary logo */
	private Texture title;

	/** button to transition to level editor */
	private Texture levelEditorButton;
	
	// statusBar is a "texture atlas." Break it up into parts.
	/** Left cap to the status background (grey region) */
	private TextureRegion statusBkgLeft;
	/** Middle portion of the status background (grey region) */
	private TextureRegion statusBkgMiddle;
	/** Right cap to the status background (grey region) */
	private TextureRegion statusBkgRight;
	/** Left cap to the status forground (colored region) */
	private TextureRegion statusFrgLeft;
	/** Middle portion of the status forground (colored region) */
	private TextureRegion statusFrgMiddle;
	/** Right cap to the status forground (colored region) */
	private TextureRegion statusFrgRight;	

	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static int STANDARD_WIDTH  = 800;
	/** Standard window height (for scaling) */
	private static int STANDARD_HEIGHT = 700;
	/** Ratio of the bar width to the screen */
	private static float BAR_WIDTH_RATIO  = 0.66f;
	/** Ration of the bar height to the screen */
	private static float BAR_HEIGHT_RATIO = 0.25f;	
	/** Height of the progress bar */
	private static int PROGRESS_HEIGHT = 30;
	/** Width of the rounded cap on left or right */
	private static int PROGRESS_CAP    = 15;
	/** Width of the middle portion in texture atlas */
	private static int PROGRESS_MIDDLE = 200;
	private static float BUTTON_SCALE  = 0.75f;
	
	/** Reference to GameCanvas created by the root */
	private GameCanvas canvas;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The width of the progress bar */
	private int width;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** The height of the canvas window (necessary since sprite origin != screen origin) */
	private int heightY;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;

	/** Current progress (0 to 1) of the asset manager */
	private float progress;
	/** The current state of the play button */
	private int   pressState;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;

	/** Whether or not this player mode is still active */
	private boolean active;

	/* BUTTON LOCATIONS */
	/** Play button x and y coordinates represented as a vector */
	private Vector2 playButtonCoords;
	/** Level editor button x and y coordinates represented as a vector */
	private Vector2 levelEditorButtonCoords;

	/* PRESS STATES **/
	// TODO: potentially change this to enums
	/** Initial button state */
	private static final int INITIAL = 0;
	/** Pressed down button state for the play button */
	private static final int PLAY_PRESSED = 1;
	/** Exit code for button to go to the game */
	public static final int TO_GAME = 2;
	/** Pressed down button state for the level editor button */
	private static final int LEVEL_EDITOR_PRESSED = 3;
	/** Exit code for the button to go to the level editor */
	public static final int TO_LEVEL_EDITOR = 4;

	/**
	 * Returns the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @return the budget in milliseconds
	 */
	public int getBudget() {
		return budget;
	}

	/**
	 * Sets the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param millis the budget in milliseconds
	 */
	public void setBudget(int millis) {
		budget = millis;
	}
	
	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return pressState == TO_GAME || pressState == TO_LEVEL_EDITOR || pressState == 5;
	}

	/**
	 * Returns the asset directory produced by this loading screen
	 *
	 * This asset loader is NOT owned by this loading scene, so it persists even
	 * after the scene is disposed.  It is your responsbility to unload the
	 * assets in this directory.
	 *
	 * @return the asset directory produced by this loading screen
	 */
	public AssetDirectory getAssets() {
		return assets;
	}

	/**
	 * Creates a LoadingMode with the default budget, size and position.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param canvas 	The game canvas to draw to
	 */
	public LoadingMode(String file, GameCanvas canvas) {
		this(file, canvas, DEFAULT_BUDGET);
	}

	/**
	 * Creates a LoadingMode with the default size and position.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param canvas 	The game canvas to draw to
	 * @param millis The loading budget in milliseconds
	 */
	public LoadingMode(String file, GameCanvas canvas, int millis) {
		this.canvas  = canvas;
		budget = millis;
		
		// Compute the dimensions from the canvas
		resize(canvas.getWidth(),canvas.getHeight());

		// We need these files loaded immediately
		internal = new AssetDirectory( "loading.json" );
		internal.loadAssets();
		internal.finishLoading();

		// Load the next two images immediately.
		playButton = null;
		levelEditorButton = null;
		calibrationButton = null;

		title = internal.getEntry("title", Texture.class);
		background = internal.getEntry( "background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		statusBar = internal.getEntry( "progress", Texture.class );

		// Break up the status bar texture into regions
		statusBkgLeft = internal.getEntry( "progress.backleft", TextureRegion.class );
		statusBkgRight = internal.getEntry( "progress.backright", TextureRegion.class );
		statusBkgMiddle = internal.getEntry( "progress.background", TextureRegion.class );

		statusFrgLeft = internal.getEntry( "progress.foreleft", TextureRegion.class );
		statusFrgRight = internal.getEntry( "progress.foreright", TextureRegion.class );
		statusFrgMiddle = internal.getEntry( "progress.foreground", TextureRegion.class );

		// No progress so far.
		progress = 0;
		pressState = INITIAL;

		Gdx.input.setInputProcessor( this );

		// Let ANY connected controller start the game.
		for (XBoxController controller : Controllers.get().getXBoxControllers()) {
			controller.addListener( this );
		}

		// Start loading the real assets
		assets = new AssetDirectory( file );
		assets.loadAssets();
		active = true;
	}

	/**
	 * Getter for the press state of a button
	 * @return value of `pressState`
	 */
	public int getPressState() {
		return pressState;
	}

	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {
		internal.unloadAssets();
		internal.dispose();
	}
	
	/**
	 * Update the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		if (playButton == null || levelEditorButton == null) {
			assets.update(budget);
			this.progress = assets.getProgress();
			if (progress >= 1.0f) {
				this.progress = 1.0f;
				playButton = internal.getEntry("play",Texture.class);
				levelEditorButton = internal.getEntry("level-editor",Texture.class);
				calibrationButton = internal.getEntry("play",Texture.class);
				playButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), centerY + 200);
				levelEditorButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), centerY);
			}
		}
	}

	/**
	 * Draw the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw() {
		canvas.begin();
		canvas.draw(background, 0, 0, canvas.getWidth(), canvas.getHeight());
		if (playButton == null) {
			drawProgress(canvas);
		} else {
			Color playButtonTint = (pressState == PLAY_PRESSED ? Color.GRAY: Color.WHITE);
			canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
					playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

			Color levelEditorButtonTint = (pressState == LEVEL_EDITOR_PRESSED ? Color.GREEN: Color.WHITE);
			canvas.draw(levelEditorButton, levelEditorButtonTint, levelEditorButton.getWidth()/2, levelEditorButton.getHeight()/2,
					levelEditorButtonCoords.x, levelEditorButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

			canvas.draw(title, Color.WHITE, title.getWidth()/2, title.getHeight()/2,
					title.getWidth()/2+50, centerY+300, 0,scale, scale);

			//draw calibration button
			Color tintCalibration = (pressState == 4 ? Color.GRAY: Color.RED);
			canvas.draw(calibrationButton, tintCalibration, calibrationButton.getWidth()/2, calibrationButton.getHeight()/2 + 50,
					centerX + calibrationButton.getWidth()*2 , centerY, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
		}
		canvas.end();
	}
	
	/**
	 * Updates the progress bar according to loading progress
	 *
	 * The progress bar is composed of parts: two rounded caps on the end, 
	 * and a rectangle in a middle.  We adjust the size of the rectangle in
	 * the middle to represent the amount of progress.
	 *
	 * @param canvas The drawing context
	 */	
	private void drawProgress(GameCanvas canvas) {	
		canvas.draw(statusBkgLeft,   centerX-width/2, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgRight,  centerX+width/2-scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgMiddle, centerX-width/2+scale*PROGRESS_CAP, centerY, width-2*scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);

		canvas.draw(statusFrgLeft,   centerX-width/2, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		if (progress > 0) {
			float span = progress*(width-2*scale*PROGRESS_CAP)/2.0f;
			canvas.draw(statusFrgRight,  centerX-width/2+scale*PROGRESS_CAP+span, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
			canvas.draw(statusFrgMiddle, centerX-width/2+scale*PROGRESS_CAP, centerY, span, scale*PROGRESS_HEIGHT);
		} else {
			canvas.draw(statusFrgRight,  centerX-width/2+scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		}
	}

	// ADDITIONAL SCREEN METHODS
	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw();

			// We are ready, notify our listener
			if (isReady() && listener != null) {
				listener.exitScreen(this, 0);
			}
		}
	}

	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// Compute the drawing scale
		float sx = ((float)width)/STANDARD_WIDTH;
		float sy = ((float)height)/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);
		
		this.width = (int)(BAR_WIDTH_RATIO*width);
		centerY = (int)(BAR_HEIGHT_RATIO*height);
		centerX = width/2;
		heightY = height;
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub

	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}
	
	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}
	
	// PROCESSING PLAYER INPUT
	/**
	 * Checks to see if the location clicked at `screenX`, `screenY` are within the bounds of the given button
	 * `buttonTexture` and `buttonCoords` should refer to the appropriate button parameters
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param buttonTexture the specified button texture
	 * @param buttonCoords the specified button coordinates as a Vector2 object
	 * @return whether the button specified was pressed
	 */
	public boolean isButtonPressed(int screenX, int screenY, Texture buttonTexture, Vector2 buttonCoords) {
		// buttons are rectangles
		// buttonCoords hold the center of the rectangle, buttonTexture has the width and height
		// get half the x length of the button portrayed
		float xRadius = BUTTON_SCALE * scale * buttonTexture.getWidth()/2.0f;
		boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

		// get half the y length of the button portrayed
		float yRadius = BUTTON_SCALE * scale * buttonTexture.getHeight()/2.0f;
		boolean yInBounds = buttonCoords.y - yRadius <= screenY && buttonCoords.y + yRadius >= screenY;
		return xInBounds && yInBounds;
	}

	/**
	 * Called when the screen was touched or a mouse button was pressed.
	 *
	 * This method checks to see if the play button is available and if the click
	 * is in the bounds of the play button.  If so, it signals the that the button
	 * has been pressed and is currently down. Any mouse button is accepted.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (playButton == null || pressState == TO_GAME || pressState == TO_LEVEL_EDITOR || pressState == 5) {
			return true;
		}
		
		// Flip to match graphics coordinates
		screenY = heightY-screenY;
		
		// TODO: Fix scaling (?)
		// check if buttons get pressed appropriately
		if (isButtonPressed(screenX, screenY, playButton, playButtonCoords)) {
			pressState = PLAY_PRESSED;
		}
		if (isButtonPressed(screenX, screenY, levelEditorButton, levelEditorButtonCoords)) {
			pressState = LEVEL_EDITOR_PRESSED;
		}

		float radiusCali = BUTTON_SCALE*scale*calibrationButton.getWidth()/2.0f;
		float distCali = (screenX-(centerX + calibrationButton.getWidth()*2))*(screenX-(centerX + calibrationButton.getWidth()*2))+(screenY-centerY)*(screenY-centerY);
		if (distCali < radiusCali*radiusCali) {
			pressState = 4;
		}
		return false;
	}
	
	/** 
	 * Called when a finger was lifted or a mouse button was released.
	 *
	 * This method checks to see if the play button is currently pressed down. If so, 
	 * it signals the that the player is ready to go.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners. 
	 */	
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		switch (pressState){
			case PLAY_PRESSED:
				pressState = TO_GAME;
				return false;
			case LEVEL_EDITOR_PRESSED:
				pressState = TO_LEVEL_EDITOR;
				return false;
			default:
				return true;
		}
	}
	
	/** 
	 * Called when a button on the Controller was pressed. 
	 *
	 * The buttonCode is controller specific. This listener only supports the start
	 * button on an X-Box controller.  This outcome of this method is identical to 
	 * pressing (but not releasing) the play button.
	 *
	 * @param controller The game controller
	 * @param buttonCode The button pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonDown (Controller controller, int buttonCode) {
		// note: does not work for level editor
		if (pressState == INITIAL) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart) {
				pressState = PLAY_PRESSED;
				return false;
			}
		}
		return true;
	}
	
	/** 
	 * Called when a button on the Controller was released. 
	 *
	 * The buttonCode is controller specific. This listener only supports the start
	 * button on an X-Box controller.  This outcome of this method is identical to 
	 * releasing the the play button after pressing it.
	 *
	 * @param controller The game controller
	 * @param buttonCode The button pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonUp (Controller controller, int buttonCode) {
		// note: does not work for level editor
		if (pressState == PLAY_PRESSED) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				pressState = TO_GAME;
				return false;
			}
		}
		return true;
	}
	
	// UNSUPPORTED METHODS FROM InputProcessor

	/** 
	 * Called when a key is pressed (UNSUPPORTED)
	 *
	 * @param keycode the key pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean keyDown(int keycode) { 
		return true; 
	}

	/** 
	 * Called when a key is typed (UNSUPPORTED)
	 *
	 * @param keycode the key typed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean keyTyped(char keycode) {
		return true; 
	}

	/** 
	 * Called when a key is released (UNSUPPORTED)
	 *
	 * @param keycode the key released
	 * @return whether to hand the event to other listeners. 
	 */	
	public boolean keyUp(int keycode) { 
		return true; 
	}
	
	/** 
	 * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @return whether to hand the event to other listeners. 
	 */	
	public boolean mouseMoved(int screenX, int screenY) { 
		return true; 
	}

	/**
	 * Called when the mouse wheel was scrolled. (UNSUPPORTED)
	 *
	 * @param dx the amount of horizontal scroll
	 * @param dy the amount of vertical scroll
	 *
	 * @return whether to hand the event to other listeners.
	 */
	public boolean scrolled(float dx, float dy) {
		return true;
	}

	/** 
	 * Called when the mouse or finger was dragged. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners. 
	 */		
	public boolean touchDragged(int screenX, int screenY, int pointer) { 
		return true; 
	}
	
	// UNSUPPORTED METHODS FROM ControllerListener
	
	/**
	 * Called when a controller is connected. (UNSUPPORTED)
	 *
	 * @param controller The game controller
	 */
	public void connected (Controller controller) {}

	/**
	 * Called when a controller is disconnected. (UNSUPPORTED)
	 *
	 * @param controller The game controller
	 */
	public void disconnected (Controller controller) {}

	/** 
	 * Called when an axis on the Controller moved. (UNSUPPORTED) 
	 *
	 * The axisCode is controller specific. The axis value is in the range [-1, 1]. 
	 *
	 * @param controller The game controller
	 * @param axisCode 	The axis moved
	 * @param value 	The axis value, -1 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean axisMoved (Controller controller, int axisCode, float value) {
		return true;
	}

}