/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Particle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements Screen, InputProcessor,ControllerListener {

	/**
	 * Track the current state of the game for the update loop.
	 */
	public enum GameState {
		/** Before the game has started */
		INTRO,
		/** While we are playing the game */
		PLAY,
		/** When the ships is dead (but shells still work) */
		OVER,
		/** When there are no more notes and competency bar is not zero */
		WON
	}

	// Loaded assets
	// background images
	private FilmStrip streetLevelBackground;
	/** The font for giving messages to the player */
	private BitmapFont displayFont;

	/** Play button to display when done */
	private Texture playButton;

	/** Asset directory */

	private AssetDirectory directory;

	/** Internal assets for this loading screen */
	private AssetDirectory internal;

	private static float BUTTON_SCALE  = 0.75f;
	private float scale=1;

	/// CONSTANTS
	/** Offset for the game over message on the screen */
	private static final float GAME_OVER_OFFSET = 40.0f;

	/** Reference to drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	/** Reads input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;
	/** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;

	/** Whether the play button is pressed or not */
	private boolean playPressed;


	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;

	/** Whether or not this player mode is still active */
	private boolean active;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** used for "counting down" before game starts */
	private float waiting = 4f;

	/** Play button x and y coordinates represented as a vector */
	private Vector2 playButtonCoords;

	/** the current level */
	private int currLevel;

	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas)  {
		this.canvas = canvas;
		active = false;
		playButton = null;

		// Null out all pointers, 0 out all ints, etc.
		gameState = GameState.INTRO;

		// Create the controllers.
		gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
	}

	/**
	 * reset current level
	 */
	public void reset(){
		playPressed = false;
		gameState = GameState.INTRO;
	}

	/**
	 * Initializes the offset to use for the gameplayController
	 * @param offset the offset from CalibrationMode
	 */
	public void initializeOffset(int offset) {
		gameplayController.setOffset(offset);
	}

	public void readLevel(AssetDirectory loadDirectory, String level) {
		directory = loadDirectory;
		JsonReader jr = new JsonReader();
//		JsonValue levelData = jr.parse(Gdx.files.internal(level));
		SetCurrLevel("1");
		JsonValue levelData = jr.parse(Gdx.files.internal(level));
		gameplayController.loadLevel(levelData, directory);
		if (gameplayController.NUM_LANES == 2) {
			inputController = new InputController(new int[]{1, 2},  new int[gameplayController.lpl]);
		}
		else {
			inputController = new InputController(gameplayController.NUM_LANES, gameplayController.lpl);
		}
	}

	/**
	 * Get the current level from LevelSelect
	 * Note this function
	 */
	public void SetCurrLevel(String level) {
//		TODO: need to merge with level screen
//		currLevel is some versions of levelscreen.getSelectedJson();
		currLevel = Integer.valueOf(level);
	}


	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		gameplayController.sfx.dispose();
		gameplayController.sb.dispose();
		gameplayController = null;
		canvas = null;
	}

	/**
	 * Populates this mode from the given the directory.
	 *
	 * The asset directory is a dictionary that maps string keys to assets.
	 * Assets can include images, sounds, and fonts (and more). This
	 * method delegates to the gameplay controller
	 *
	 * @param directory 	Reference to the asset directory.
	 */
	public void populate(AssetDirectory directory) {
		streetLevelBackground = new FilmStrip(directory.getEntry("street-background", Texture.class), 1, 1);
		displayFont = directory.getEntry("times",BitmapFont.class);
		gameplayController.populate(directory);

		if (playButton == null) {
			Gdx.input.setInputProcessor( this );
			playPressed=false;
			internal = new AssetDirectory( "loading.json" );
			internal.loadAssets();
			internal.finishLoading();
			playButton = internal.getEntry("play",Texture.class);
			playButtonCoords = new Vector2(4*canvas.getWidth()/5, 2*canvas.getHeight()/7);
			Gdx.input.setInputProcessor( this );
		}

	}

	/**
	 * Update the game state.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */

	private void update(float delta) {
		// Process the game input
		inputController.readInput();
		Gdx.input.setInputProcessor( this );

		// Test whether to reset the game.
		switch (gameState) {
			case INTRO:
				for(boolean k : inputController.getTriggers()){
					if (k){
						gameplayController.sfx.playSound("tap", 0.2f);
					}
				}
				// wait a few frames before starting
				if (waiting == 4f) {
					gameplayController.reset();
					gameplayController.update();
					gameplayController.start();
				}
				waiting -= delta;
				if (waiting < 1f) {
					gameState = GameState.PLAY;
					gameplayController.level.startmusic();
				}
				break;
			case OVER:
			case PLAY:
				if (inputController.didReset()) {
					gameplayController.reset();
					gameplayController.start();
				} else if (inputController.didPause()){
					listener.exitScreen(this, 0);
				}
				else {
					play(delta);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * This method processes a single step in the game loop.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	protected void play(float delta) {
		// Update objects.
		gameplayController.handleActions(inputController);
		gameplayController.update();

		// if we have a competency bar at 0
		if (gameplayController.hasZeroCompetency()) {
			gameState = GameState.OVER;
			gameplayController.level.stopMusic();
		}

		// in the future, we should prob move this else where.
		// so that it is checked near the end of the game.
		if (gameplayController.checkWinCon()){
			gameState = GameState.WON;
			gameplayController.level.stopMusic();
		}

		// Clean up destroyed objects
		gameplayController.garbageCollect();
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
		// First draw the background
		// TODO: SWITCH BACKGROUND BASED ON LEVEL JSON (may need to move this to a different location)
		canvas.drawBackground(streetLevelBackground.getTexture(),0,0);
		if (gameState == GameState.OVER) {
			//Draw game over text
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Game Over!", displayFont, GAME_OVER_OFFSET+50);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Press ENTER to Restart", displayFont, 0);
		} else if (gameState == GameState.WON) {
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("You won!", displayFont, GAME_OVER_OFFSET+50);

			Color playButtonTint = (playPressed ? Color.GRAY: Color.WHITE);
			canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
					playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

		} else{
			//Draw everything in the current level
			gameplayController.level.drawEverything(canvas,
					gameplayController.activeBandMember, gameplayController.goalBandMember,
					inputController.triggerPress, inputController.switches(),
					gameplayController.inBetweenWidth/5f);

			// Draw the particles on top
			for (Particle o : gameplayController.getParticles()) {
				o.draw(canvas);
			}

			for(BandMember bandMember : gameplayController.level.getBandMembers()){
				//Draw the band member sprite and competency bar
				bandMember.drawCharacterSprite(canvas);
				bandMember.drawHPBar(canvas);
			}

			// draw the scoreboard
			gameplayController.sb.displayScore(gameplayController.LEFTBOUND, gameplayController.TOPBOUND + gameplayController.inBetweenWidth/4f, canvas);

			// draw the countdown
			if (gameState == GameState.INTRO) {
				canvas.drawTextCentered("" + (int) waiting, displayFont, 0);
			}
		}
		canvas.end();
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
		gameplayController.resize(Math.max(250,width), Math.max(200,height));
	}

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
			if (inputController.didExit() && listener != null) {
				listener.exitScreen(this, ExitCode.TO_MENU);
			}
		}
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
		waiting = 4f;
		gameState = GameState.INTRO;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
		gameplayController.level.stopMusic();
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}
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
	public boolean isButtonPressed(int screenX, int screenY, Texture buttonTexture, Vector2 buttonCoords, float scale) {
		// buttons are rectangles
		// buttonCoords hold the center of the rectangle, buttonTexture has the width and height
		// get half the x length of the button portrayed

		float xRadius = BUTTON_SCALE * scale * buttonTexture.getWidth()/2.0f;
		boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

		// get half the y length of the button portrayed
		float yRadius = BUTTON_SCALE * scale * buttonTexture.getHeight()/2.0f;
		boolean yInBounds = buttonCoords.y - yRadius <= screenY && buttonCoords.y + yRadius >= screenY;
		System.out.println(xInBounds && yInBounds);
		return xInBounds && yInBounds;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		screenY = canvas.getHeight()-screenY;
		if (isButtonPressed(screenX, screenY, playButton, playButtonCoords,scale)) {
			playPressed=true;
			String nextLevel ="levels/"+(currLevel+1)+".json";
			listener.exitScreen(this, ExitCode.TO_PLAYING);
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

	@Override
	public void connected(Controller controller) {

	}

	@Override
	public void disconnected(Controller controller) {

	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		return false;
	}

}