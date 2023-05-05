package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * This class is just a ported over LoadingMode without the asset loading part, only the menu part
 */
public class MenuMode implements Screen, InputProcessor, ControllerListener {

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Background texture */
    private Texture background;
    private Texture settingsBackground;
    /** Tempo-Rary logo */
    private Texture logo;
    /** Buttons */
    private Texture playButton;
    private Texture calibrationButton;
    private Texture levelEditorButton;
    private Texture settingsButton;
    private Texture exitButton;

    private Texture backButtonTexture;

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

    /* BUTTON LOCATIONS */
    /** Play button x and y coordinates represented as a vector */
    private Vector2 playButtonCoords;
    /** Level editor button x and y coordinates represented as a vector */
    private Vector2 levelEditorButtonCoords;
    /** Calibration button x and y coordinates represented as a vector */
    private Vector2 calibrationButtonCoords;
    /** Scale at which to draw the buttons */
    private static float BUTTON_SCALE  = 0.75f;
    /** The y-coordinate of the center of the progress bar (artifact of LoadingMode) */
    private int centerY;
    /** The x-coordinate of the center of the progress bar (artifact of LoadingMode) */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    /** Height of the progress bar */
    private static int PROGRESS_HEIGHT = 93;
    /** Height of the inner progress bar */
    private static int INNER_PROGRESS_HEIGHT = 77;
    /** Padding on the left and right sides of the inner bar */
    private static int X_PADDING = 8;
    /** Width of the rounded cap on left or right */
    private static int PROGRESS_CAP    = 42;
    /** Width of the rounded cap on the left or right for the inner bar */
    private static int INNER_PROGRESS_CAP    = 33;

    private int barWidth;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1200;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 800;
    /** Ratio of the bar width to the screen (artifact of LoadingMode) */
    private static float BAR_WIDTH_RATIO  = 0.5f;
    /** Ration of the bar height to the screen (artifact of LoadingMode) */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of each button */
    private int pressState;

    private Vector2 v;

    /* PRESS STATES **/
    /** Initial button state */
    private static final int NO_BUTTON_PRESSED = 100;
    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 101;
    /** Pressed down button state for the level editor button */
    private static final int LEVEL_EDITOR_PRESSED = 102;
    /** Pressed down button state for the calibration button */
    private static final int CALIBRATION_PRESSED = 103;
    private static final int SETTINGS_PRESSED = 104;
    private static final int EXIT_PRESSED = 105;

    // START SETTINGS

    /** */
    private float currBandMemberCount;

    /** Values for the volumes */
    private float musicVolume;
    private float soundFXVolume;

    private MenuState currentMenuState;
    private int innerWidth;
    private boolean holdingBar;

    // SCENES FOR THE SETTINGS PAGE
    private Stage stage;
    private Table mainTable;
    private Container<Table> tableContainer;

    // settings image assets
    private Texture headerLine;
    private Texture primaryBox;
    private Texture secondaryBox;
    private Texture fishOutline;
    private Texture catOutline;
    private Texture sliderBackground;
    private Texture sliderForeground;
    private Texture sliderButton;

    // fonts
    private BitmapFont blinkerBold;
    private BitmapFont blinkerSemiBold;
    private BitmapFont blinkerSemiBoldSmaller;
    private BitmapFont blinkerRegular;

    // MenuState
    private enum MenuState {
        HOME,
        SETTINGS
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
        logo = directory.getEntry("title", Texture.class);
        background = directory.getEntry( "loading-background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        settingsBackground = directory.getEntry("settings-background", Texture.class);
        playButton = directory.getEntry("play", Texture.class);
        levelEditorButton = directory.getEntry("level-editor", Texture.class);
        calibrationButton = directory.getEntry("play-old", Texture.class);
        settingsButton = directory.getEntry("play-old", Texture.class);
        exitButton = directory.getEntry("quit-button", Texture.class);
        playButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), canvas.getHeight()/2 + 200);
        levelEditorButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), canvas.getHeight()/2);
        calibrationButtonCoords = new Vector2(centerX + calibrationButton.getWidth()*2 , centerY);

        // TODO: delete this
        statusBkgLeft = directory.getEntry( "slider.backleft", TextureRegion.class );
        statusBkgRight = directory.getEntry( "slider.backright", TextureRegion.class );
        statusBkgMiddle = directory.getEntry( "slider.background", TextureRegion.class );

        statusFrgLeft = directory.getEntry( "slider.foreleft", TextureRegion.class );
        statusFrgRight = directory.getEntry( "slider.foreright", TextureRegion.class );
        statusFrgMiddle = directory.getEntry( "slider.foreground", TextureRegion.class );

        // UI assets for settings
        backButtonTexture = directory.getEntry("back-arrow", Texture.class);
        headerLine = directory.getEntry("header-line", Texture.class);
        primaryBox = directory.getEntry("primary-box", Texture.class);
        secondaryBox = directory.getEntry("secondary-box", Texture.class);
        fishOutline = directory.getEntry("fish-outline", Texture.class);
        catOutline = directory.getEntry("cat-outline", Texture.class);
        sliderBackground = directory.getEntry("slider-background", Texture.class);
        sliderForeground = directory.getEntry("slider-foreground", Texture.class);
        sliderButton = directory.getEntry("slider-button", Texture.class);

        // add Scene2D
        addSceneElements();
    }

    private void addSceneElements() {

        Table switchTable = new Table();
        Table controlTable = new Table();
        Table volumeTable = new Table();

//        table.add(buttonTable).colspan(3);
//
//        buttonTable.pad(16);
//        buttonTable.row().fillX().expandX();
//        buttonTable.add(buttonA).width(cw/3.0f);
//        buttonTable.add(buttonB).width(cw/3.0f);

        // TODO: add calibration button

        Button.ButtonStyle backButtonStyle = new Button.ButtonStyle();
        backButtonStyle.up = new TextureRegionDrawable(backButtonTexture);
        backButtonStyle.down = new TextureRegionDrawable(backButtonTexture);

        final Button backButton = new Button(backButtonStyle);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = new TextureRegionDrawable(sliderBackground);
        sliderStyle.knob = new TextureRegionDrawable(sliderButton);
        sliderStyle.knobBefore = new TextureRegionDrawable(sliderForeground);

        final Slider musicVolumeSlider = new Slider(0f, 1f, 0.1f, false, sliderStyle);
        musicVolumeSlider.setValue(musicVolume);

        final Slider fxVolumeSlider = new Slider(0f, 1f, 0.05f, false, sliderStyle);
        fxVolumeSlider.setValue(soundFXVolume);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = blinkerSemiBoldSmaller;

        Label.LabelStyle headerStyle = new Label.LabelStyle();
        headerStyle.font = blinkerBold;

        Label.LabelStyle header2Style = new Label.LabelStyle();
        header2Style.font = blinkerSemiBold;

        Label.LabelStyle regularStyle = new Label.LabelStyle();
        regularStyle.font = blinkerRegular;

        // back button
        mainTable.add(backButton).left();
        // header for the
        mainTable.add(new Label("SETTINGS", headerStyle));
        mainTable.add(switchTable);

        mainTable.row();

        mainTable.add(new Image(headerLine)).bottom().right().expandX();
        mainTable.add(new Label("CLICK TO REBIND", header2Style));
        mainTable.add(new Image(headerLine)).bottom().left().expandX();

        mainTable.row().padLeft(16).padRight(16).expand().fill();

//        controlTable.add(new Label("Notes", labelStyle)).colspan(2);

        // TODO: CHANGE THIS TO LOOP AND ADD SWITCHES

        // controls for note hits
        controlTable.add(new Label("Note Hits", labelStyle));

        for (int i = 0; i < 4; i++) {
            controlTable.add(getControlWidget(i + 1, fishOutline));
        }

        controlTable.row();

        // controls for switching
        controlTable.add(new Label("Members", labelStyle));

        for (int i = 0; i < 4; i++) {
            controlTable.add(getControlWidget(i + 1, catOutline)).expandX();
        }

        mainTable.add(controlTable).expandX().colspan(3);

        mainTable.row();

        // Volume table stuff

        mainTable.add(new Image(headerLine)).bottom().right().expandX();
        mainTable.add(new Label("DRAG TO ADJUST VOLUME", header2Style));
        mainTable.add(new Image(headerLine)).bottom().left().expandX();

        mainTable.row().expand().fill();
        mainTable.add(volumeTable).expand().colspan(3);

        volumeTable.add(new Label("Music", labelStyle)).expandX();
        volumeTable.add(musicVolumeSlider).width(800);
        volumeTable.row().padTop(10).padBottom(10);

        volumeTable.add(new Label("Sound FX", labelStyle)).expandX();
        volumeTable.add(fxVolumeSlider).width(800);

        // LISTENERS
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                switchToHome();
            }
        });

        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                musicVolume = ((Slider) actor).getValue();
            }
        });

        fxVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                soundFXVolume = ((Slider) actor).getValue();
            }
        });
    }

    private Table getControlWidget(int nth, Texture outlineImage) {
        Table tempTable = new Table();

        Label.LabelStyle regularStyle = new Label.LabelStyle();
        regularStyle.font = blinkerRegular;

        Button.ButtonStyle primaryKeyStyle = new Button.ButtonStyle();
        primaryKeyStyle.up = new TextureRegionDrawable(primaryBox);
        primaryKeyStyle.down = new TextureRegionDrawable(primaryBox);

        Button.ButtonStyle secondaryKeyStyle = new Button.ButtonStyle();
        secondaryKeyStyle.up = new TextureRegionDrawable(secondaryBox);
        secondaryKeyStyle.down = new TextureRegionDrawable(secondaryBox);

        tempTable.add(new Label("" + nth, regularStyle));

        tempTable.row();

        Image image = new Image(outlineImage);
        tempTable.add(image);

        VerticalGroup stack = new VerticalGroup();
        stack.addActor(new Button(primaryKeyStyle));
        stack.addActor(new Button(secondaryKeyStyle));

        tempTable.add(stack);

        return tempTable;
    }

    private void switchToHome() {
        reset();
        Gdx.input.setInputProcessor( this );
    }

    /**
     * Returns true if all assets are loaded and the player presses on a button
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == ExitCode.TO_LEVEL
                || pressState == ExitCode.TO_EDITOR
                || pressState == ExitCode.TO_CALIBRATION
                || pressState == ExitCode.TO_EXIT;
    }

    /**
     * Resets the MenuMode
     */
    public void reset() {
        currentMenuState = MenuState.HOME;
        pressState = NO_BUTTON_PRESSED;
    }

    /**
     * Creates a MenuMode
     *
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        this.canvas = canvas;
        stage = new Stage();
        mainTable = new Table();
        tableContainer = new Container<>();

        float sw = canvas.getWidth();
        float sh = canvas.getHeight();

        float cw = sw * 0.90f;
        float ch = sh * 0.95f;

        // scene 2D UI
        tableContainer.setSize(cw, ch);
        tableContainer.setPosition((sw - cw) / 2.0f, (sh - ch) / 2.0f);
        tableContainer.fill();
        tableContainer.setActor(mainTable);
        stage.addActor(tableContainer);
        stage.setDebugAll(true);

        // fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 70;
        parameter.color = Color.WHITE;
        blinkerBold = generator.generateFont(parameter);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-SemiBold.ttf"));
        parameter.size = 50;
        parameter.color = Color.BLACK;
        blinkerSemiBold = generator.generateFont(parameter); // font size 24 pixels

        parameter.size = 35;
        blinkerSemiBoldSmaller = generator.generateFont(parameter);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Regular.ttf"));
        parameter.size = 20;
        blinkerRegular = generator.generateFont(parameter);

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        currBandMemberCount = 4;
        musicVolume = 1f;
        soundFXVolume = 1f;
        active = false;
        v = new Vector2();
        reset();
    }

    @Override
    public void render(float v) {
        if (active) {
            switch (currentMenuState) {
                case HOME:
                    draw();
                    break;
                case SETTINGS:
                    drawSettings();
                    break;
            }
            // We are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, pressState);
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
        // TODO: make these methods instead
        canvas.draw(background, 0, 0, canvas.getWidth(), canvas.getHeight());
        Color playButtonTint = (pressState == PLAY_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color levelEditorButtonTint = (pressState == LEVEL_EDITOR_PRESSED ? Color.GREEN: Color.WHITE);
        canvas.draw(levelEditorButton, levelEditorButtonTint, levelEditorButton.getWidth()/2, levelEditorButton.getHeight()/2,
                    levelEditorButtonCoords.x, levelEditorButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        canvas.draw(logo, Color.WHITE, logo.getWidth()/2, logo.getHeight()/2,
                    logo.getWidth()/2+50, centerY+300, 0,scale, scale);

        //draw calibration button
        Color tintCalibration = (pressState == CALIBRATION_PRESSED ? Color.GRAY: Color.RED);
        canvas.draw(calibrationButton, tintCalibration, calibrationButton.getWidth()/2, calibrationButton.getHeight()/2,
                    calibrationButtonCoords.x, calibrationButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color settingsButtonTint = (pressState == SETTINGS_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(settingsButton, settingsButtonTint, settingsButton.getWidth()/2, settingsButton.getHeight()/2,
                calibrationButtonCoords.x - settingsButton.getWidth(), calibrationButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color exitButtonTint = (pressState == EXIT_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(exitButton, exitButtonTint, exitButton.getWidth()/2, exitButton.getHeight()/2,
                calibrationButtonCoords.x - 2 * 0.25f * exitButton.getWidth(), calibrationButtonCoords.y, 0, 0.25f, 0.25f);
        canvas.end();
    }

    private void drawSettings() {
        canvas.begin();
        canvas.draw(settingsBackground, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // SETTINGS VALUES (this needs to change after the fact)
    // also add keybinds
    public float getMusicVolumeSetting() {
        return musicVolume;
    }

    public float getFXVolumeSetting() {
        return soundFXVolume;
    }

    // TODO: fix this method
    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        barWidth = (int)(BAR_WIDTH_RATIO*width);
        innerWidth = (int)(BAR_WIDTH_RATIO*0.97*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;

        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor( this );
        active = true;
    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {
        canvas = null;
        stage.dispose();
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

    public boolean isButtonPressed(int screenX, int screenY, float width, float height, Vector2 buttonCoords) {
        float xRadius = width/2.0f;
        boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;
        float yRadius = height/2.0f;
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
        if (pressState == ExitCode.TO_LEVEL
                || pressState == ExitCode.TO_EDITOR
                || pressState == ExitCode.TO_CALIBRATION
                || pressState == ExitCode.TO_EXIT) {
            return true;
        }
        float radius = BUTTON_SCALE * scale * calibrationButton.getWidth() / 2.0f;
        // Flip to match graphics coordinates
        screenY = heightY - screenY;
        switch (currentMenuState) {
            case HOME:
                // check if buttons get pressed appropriately
                if (isButtonPressed(screenX, screenY, playButton, playButtonCoords)) {
                    pressState = PLAY_PRESSED;
                }
                if (isButtonPressed(screenX, screenY, levelEditorButton, levelEditorButtonCoords)) {
                    pressState = LEVEL_EDITOR_PRESSED;
                }

                float dist = (screenX - calibrationButtonCoords.x) * (screenX - calibrationButtonCoords.x)
                        + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
                if (dist < radius * radius) {
                    pressState = CALIBRATION_PRESSED;
                }

                float distSettings = (screenX - (calibrationButtonCoords.x - calibrationButton.getWidth())) * (screenX - (calibrationButtonCoords.x - calibrationButton.getWidth()))
                        + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
                if (distSettings < radius * radius) {
                    pressState = SETTINGS_PRESSED;
                }
                v.x = calibrationButtonCoords.x - 2 * 0.25f * exitButton.getWidth();
                v.y = calibrationButtonCoords.y;
                if (isButtonPressed(screenX, screenY, 0.25f * exitButton.getWidth(), 0.25f * exitButton.getHeight(), v)) {
                    pressState = EXIT_PRESSED;
                }
                break;
            case SETTINGS:
                float distBack = (screenX - (calibrationButton.getWidth())) * (screenX - (calibrationButton.getWidth()))
                        + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
                if (distBack < radius * radius) {
                    currentMenuState = MenuState.HOME;
                    Gdx.input.setInputProcessor(this);
                }
                float barWidth = (innerWidth-2*scale*INNER_PROGRESS_CAP);
                v.x = centerX;
                v.y = centerY;
                if (isButtonPressed(screenX, screenY, barWidth, scale*INNER_PROGRESS_HEIGHT, v)) {
                    holdingBar = true;
                }
                break;
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
                pressState = ExitCode.TO_LEVEL;
                return false;
            case LEVEL_EDITOR_PRESSED:
                pressState = ExitCode.TO_EDITOR;
                return false;
            case CALIBRATION_PRESSED:
                pressState = ExitCode.TO_CALIBRATION;
                return false;
            case SETTINGS_PRESSED:
                currentMenuState = MenuState.SETTINGS;
                pressState = NO_BUTTON_PRESSED;
                Gdx.input.setInputProcessor(stage);
                break;
            case EXIT_PRESSED:
                pressState = ExitCode.TO_EXIT;
            default:
                break;
        }
        holdingBar = false;
        return true;
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
        if (pressState == NO_BUTTON_PRESSED) {
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
                pressState = ExitCode.TO_LEVEL;
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the mouse or finger was dragged.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (holdingBar) {
            musicVolume = 1 - ((screenX - centerX - innerWidth / 2) * 1.0f / innerWidth * -1);
            if (musicVolume > 1) musicVolume = 1;
            if (musicVolume < 0) musicVolume = 0;
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
    public boolean keyDown(int keycode) {return true;}

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char keycode) {return true;}

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {return true;}

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {return true;}

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {return true;}

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
    public boolean axisMoved (Controller controller, int axisCode, float value) {return true;}
}
