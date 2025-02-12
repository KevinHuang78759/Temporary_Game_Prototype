package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.Locale;

public class LevelSelect implements Screen, InputProcessor, ControllerListener {
    SoundController<Integer> s;
    private static String selectedJson;

    float WON_BUTTON_SCALE = 0.7f;
    /** Whether this player mode is still active */
    private boolean active;

    /** Whether the play button is pressed or not */
    private boolean playPressed;
    private int numSongs;

    private Texture goBack;
    private Vector2 goBackCoords;

    /** Play button to display when done */
    private Texture playButton;


    private Texture tutorialGoRight;

    private Texture playButtonInactive;

    /** Play button to display easy level*/
    private Texture easyButton;

    private Texture easyButtonInactive;

    /** Play button to display medium level */
    private Texture mediumButton;

    private Texture mediumButtonInactive;

    /** Play button to display hard level */
    private Texture hardButton;
    private Texture hardButtonInactive;

    private Texture levelBackground;



    private Texture[] howToPlayBackgrounds;


    /** button for a level */
    private static Texture[] albumCovers;

    /** coordinates for each album; will always have 3 elements;
     * albumCoverCoords[0] left, [1] center, [2] right, */

    private Vector2[] albumCoverCoords;

    private float albumCoverLeftX;
    private float albumCoverMiddleX;
    private float albumCoverRightX;

    private float albumCoverY;


    /** The background texture */
    private Texture background;
    private float scale;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;

    /** true if we want to draw the left album */
    private boolean drawLeft;

    private int hoverState;

    private static float BUTTON_SCALE  = 0.6f;

    /** Play button x and y coordinates represented as a vector */
    private Vector2 playButtonCoords;

    /** easyButton x and y coordinates represented as a vector */
    private Vector2 easyButtonCoords;

    /** mediumButton x and y coordinates represented as a vector */
    private Vector2 mediumButtonCoords;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1200;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 800;

    private Texture howToPlayActive;

    private Texture howToPlayInactive;

//    Map<String, Texture> letterGrades =new HashMap<>();

    private Texture letterGradeA;
    private Texture letterGradeB;
    private Texture letterGradeC;
    private Texture letterGradeD;
    private Texture letterGradeS;


    /** hardButton x and y coordinates represented as a vector */
    private Vector2 hardButtonCoords;


    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** A string array of levels in order;
     * invariant 1: in order; allLevels[0] is song 1 easy level.
     * invariant 2: size is a multiple of 3, because we have 3 difficulties */
    private static String[] allLevels;

    private Texture levelGhost;

    private Texture goLeft;

    private Vector2 goLeftCoords;

    private Texture goRight;

    private Vector2 goRightCoords;

    private AssetDirectory directory;
    private Texture scoreBox;
    private Texture scoreLine;

    private int EASY = 1;
    private int MEDIUM = 2;
    private  int HARD = 3;

    /** the scale for the song in the middle in level select */
    float centerScale;

    /** the scale for the songs on the sides in level select */
    float cornerScale;

    /** Selected song; index from 0. */
    public int selectedLevel;

    /** Selected song; 1 is easy, 2 is medium, 3 is hard. */
    public int selectedDifficulty;
    private boolean pressedEscape;

    int prevLevel=selectedLevel;

    float albumScales[];

    static int nLevels;

    private BitmapFont blinkerBold;


    public LevelSelect(GameCanvas canvas) {
        s = new SoundController<>();
        this.canvas  = canvas;
        selectedDifficulty= 2; // default medium difficulty
        selectedLevel = 0;
        playButton = null;
        easyButton=null;
        mediumButton=null;
        hardButton=null;
        isInTransition = false;
        tutorialIdx = 0;
    }

    /**
     * Return the total number of levels in this game
     */
    public static int getnLevels(){
        return nLevels;
    }

    /**
     * Return the index of the selected song
     */
    public int getSelectedLevel(){
        return selectedLevel;
    }

    public int getSelectedDifficulty(){
        return selectedDifficulty;
    }
    public static Texture[] getAlbumCovers(){
        return albumCovers;
    }

    public static String[] getAllLevels() {
        return allLevels;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public static void setSelectedJson(String json){
        selectedJson = json;
    }

    public static String getSelectedJson(){
        return selectedJson;
    }

    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter;

    Vector2 tutorialGoRightCoords;

    Vector2 tutorialReadyCoords;

    private Texture tutorialReady;

    /**
     * Parse information about the levels .
     *
     */
    public void populate(AssetDirectory assetDirectory) {
        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Bold.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        blinkerBold = generator.generateFont(parameter);
        s.addSound(0, "sound/click.ogg");
        directory  = assetDirectory;
        background  = directory.getEntry("background",Texture.class); //menu background
        goBack = directory.getEntry("go-back", Texture.class);
        JsonReader jr = new JsonReader();
        JsonValue levelData = jr.parse(Gdx.files.internal("assets.json"));
        allLevels = levelData.get("levels").asStringArray();
        numSongs = allLevels.length/3;
        nLevels = allLevels.length;
        assert allLevels.length%3 == 0;
        albumCoverCoords = new Vector2[allLevels.length];
        albumScales = new float[allLevels.length];
        gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
        albumCovers = new Texture[numSongs];
        playButton = directory.getEntry("play-button-active",Texture.class);
        levelGhost = directory.getEntry("level-ghost",Texture.class);
        tutorialReady = directory.getEntry("tutorial-ready", Texture.class);
        playButtonInactive = directory.getEntry("play-button",Texture.class);
        easyButton = directory.getEntry("easy-active",Texture.class);
        easyButtonInactive =directory.getEntry("easy-inactive",Texture.class);
        mediumButton = directory.getEntry("medium-active",Texture.class);
        mediumButtonInactive = directory.getEntry("medium-inactive",Texture.class);
        hardButton = directory.getEntry("hard-active",Texture.class);
        hardButtonInactive = directory.getEntry("hard-inactive",Texture.class);
        goLeft = directory.getEntry("level-select-left",Texture.class);
        tutorialGoRight =directory.getEntry("tutorial-go-right",Texture.class);

        goRight = directory.getEntry("level-select-right",Texture.class);
        letterGradeD = directory.getEntry("score-d",Texture.class);
        letterGradeC= directory.getEntry("score-c",Texture.class);
        letterGradeB= directory.getEntry("score-b",Texture.class);
        letterGradeA = directory.getEntry("score-a",Texture.class);
        letterGradeS = directory.getEntry("score-s",Texture.class);
        levelBackground = directory.getEntry("level-select-background", Texture.class);
        scoreBox = directory.getEntry("score-box",Texture.class);
        scoreLine = directory.getEntry("score-box-line",Texture.class);
        // album covers are called 1, 2, 3 and so on in assets.json
        for (int i = 0; i < numSongs; i++){
            albumCovers[i] = directory.getEntry(Integer.toString(i+1),Texture.class);
        }

        howToPlayBackgrounds = new Texture[5];
        howToPlayBackgrounds[0] = directory.getEntry("htp1",Texture.class);
        howToPlayBackgrounds[1]  = directory.getEntry("htp2",Texture.class);
        howToPlayBackgrounds[2]  = directory.getEntry("htp3",Texture.class);
        howToPlayBackgrounds[3]  = directory.getEntry("htp4",Texture.class);
        howToPlayBackgrounds[4]  = directory.getEntry("htp5",Texture.class);

        setCoords(canvas.getWidth(),canvas.getHeight());
        howToPlayActive = directory.getEntry("how-to-play-active", Texture.class);
        howToPlayInactive = directory.getEntry("how-to-play-inactive", Texture.class);
    }

    Vector2 howToPlayCoords;

    /**
     * loadCoords set the coordinates of all assets
     */
    public void setCoords(int width, int height) {
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;

        scale = (sx < sy ? sx : sy);
        centerScale = (float) (0.09f* Math.log(width));
        cornerScale = (float) (0.07f* Math.log(width));
        goBackCoords=new Vector2 (width/12f, height*9f/10f);
        goLeftCoords = new Vector2(width/14f,height/2f);
        tutorialReadyCoords =new Vector2(width-(width/14f),height/2f);
        goRightCoords = new Vector2(width-(width/14f),height/2f);
        tutorialGoRightCoords = new Vector2(width-(width/14f),height/2f);
        playButtonCoords = new Vector2(width/2f,height*0.15f);
        mediumButtonCoords = new Vector2(width/2f , height*0.35f);
        easyButtonCoords = new Vector2((width/2f)-(width/10f), height*0.35f);
        hardButtonCoords = new Vector2((width/2f)+(width/10f), height*0.35f);
        drawLeft=false;
        albumCoverLeftX = width/4f;
        albumCoverMiddleX = width/2f;
        albumCoverRightX = width/2f + width/4f;
        albumCoverY = height*0.55f;
        if (selectedLevel+1< numSongs){
            albumCoverCoords[selectedLevel+1] = new Vector2(albumCoverRightX,albumCoverY);
            albumScales[selectedLevel+1] = cornerScale;
        }
        if (selectedLevel>=1){
            albumCoverCoords[selectedLevel-1] = new Vector2(albumCoverLeftX,albumCoverY);
            albumScales[selectedLevel-1] = cornerScale;
        }
        albumCoverCoords[selectedLevel] =new Vector2 (albumCoverMiddleX,albumCoverY);
        albumScales[selectedLevel] = centerScale;
        howToPlayCoords= new Vector2(canvas.getWidth()*0.8f, canvas.getHeight()*0.2f);

    }

    /**
     * Resets  LevelSelect
     */
    public void reset() {
        Gdx.input.setInputProcessor( this );
        playButton = null;
        easyButton=null;
        mediumButton=null;
        hardButton=null;
        playPressed=false;
        pressedEscape = false;
        isInTransition = false;
        hoverState = NO_BUTTON_HOVERED;
        howToPlayPressed = false;
        tutorialIdx = 0;
    }


    public void draw(){
        canvas.begin();

        if (howToPlayPressed ){
            canvas.draw(howToPlayBackgrounds[tutorialIdx], 0, 0, canvas.getWidth(), canvas.getHeight());
            if (tutorialIdx<4){
                canvas.draw(tutorialGoRight, Color.WHITE, tutorialGoRight.getWidth()/2, tutorialGoRight.getHeight()/2,
                        tutorialGoRightCoords.x, tutorialGoRightCoords.y,0, 0.9f * scale, 0.9f * scale);
            }
            if (tutorialIdx==4){
                canvas.draw(tutorialReady, Color.WHITE, tutorialReady.getWidth()/2, tutorialReady.getHeight()/2,
                        tutorialReadyCoords.x, tutorialReadyCoords.y,0, 0.9f * scale, 0.9f * scale);
            }
        } else{
            canvas.draw(levelBackground, 0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.draw(goBack, Color.WHITE, goBack.getWidth()/2, goBack.getHeight()/2,
                    goBackCoords.x, goBackCoords.y, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);

            canvas.draw(levelGhost, Color.WHITE, hardButton.getWidth()/2, hardButton.getHeight()/2,
                    canvas.getWidth()/4f, canvas.getHeight()/12f, 0, BUTTON_SCALE*scale*0.1f, BUTTON_SCALE*scale*0.1f);

            // draw each song
            if (selectedLevel>=1){ //check that if there are a song to the left; if so, draw.
                canvas.draw(albumCovers[selectedLevel-1], Color.WHITE, albumCovers[selectedLevel-1].getWidth()/2,
                        albumCovers[selectedLevel-1].getHeight()/2, albumCoverCoords[selectedLevel-1].x,
                        albumCoverCoords[selectedLevel-1].y, 0, albumScales[selectedLevel-1]*scale,
                        albumScales[selectedLevel-1]*scale);
            }

            // It an invariant that selectedLevel is a valid index, so we can simply draw.
            canvas.draw(albumCovers[selectedLevel],Color.WHITE,albumCovers[selectedLevel].getWidth()/2,
                    albumCovers[selectedLevel].getHeight()/2,albumCoverCoords[selectedLevel].x,
                    albumCoverCoords[selectedLevel].y,0, albumScales[selectedLevel]*scale,
                    albumScales[selectedLevel]*scale);

            if (selectedLevel+1< numSongs) {//check that if there are a song to the right; if so, draw.
                canvas.draw(albumCovers[selectedLevel+1],Color.WHITE,albumCovers[selectedLevel+1].getWidth()/2,
                        albumCovers[selectedLevel+1].getHeight()/2,albumCoverCoords[selectedLevel+1].x,
                        albumCoverCoords[selectedLevel+1].y,0, albumScales[selectedLevel+1]*scale,
                        albumScales[selectedLevel+1]*scale);
            }

            // draw the goleft and go right buttons
            if (selectedLevel-1>=0) {
                canvas.draw(goLeft, Color.WHITE, goLeft.getWidth() / 2, goLeft.getHeight() / 2,
                        goLeftCoords.x, goLeftCoords.y, 0, 0.9f * scale, 0.9f * scale);
            }
            if (selectedLevel+1< numSongs) {
                canvas.draw(goRight, Color.WHITE, goRight.getWidth() / 2, goRight.getHeight() / 2,
                        goRightCoords.x, goRightCoords.y, 0, 0.9f * scale, 0.9f * scale);
            }

            if (hoverState == PLAY_HOVERED){
                canvas.draw(playButton, Color.WHITE, playButton.getWidth()/2f, playButtonInactive.getHeight()/2f,
                        playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
            }else{
                canvas.draw(playButtonInactive, Color.WHITE, playButtonInactive.getWidth()/2f, playButtonInactive.getHeight()/2f,
                        playButtonCoords.x,playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
            }

            // draw easy, medium, and hard buttons
            if (selectedLevel != 0) {
                Texture tempEasy = (hoverState == EASY_HOVERED || selectedDifficulty == 1 ? easyButton : easyButtonInactive);
                canvas.draw(tempEasy, Color.WHITE, easyButton.getWidth() / 2, easyButton.getHeight() / 2,
                        easyButtonCoords.x, easyButtonCoords.y, 0, 0.4f * scale, 0.4f * scale);

                Texture tempMedium = (hoverState == MEDIUM_HOVERED || selectedDifficulty == 2 ? mediumButton : mediumButtonInactive);
                canvas.draw(tempMedium, Color.WHITE, mediumButton.getWidth() / 2, mediumButton.getHeight() / 2,
                        mediumButtonCoords.x, mediumButtonCoords.y, 0, 0.4f * scale, 0.4f * scale);

                Texture tempHard = (hoverState == HARD_HOVERED || selectedDifficulty == 3 ? hardButton : hardButtonInactive);
                canvas.draw(tempHard, Color.WHITE, tempHard.getWidth() / 2, tempHard.getHeight() / 2,
                        hardButtonCoords.x, hardButtonCoords.y, 0, 0.4f * scale, 0.4f * scale);
            }


            if (hoverState == HOW_TO_PLAY_HOVERED){
                canvas.draw(howToPlayActive, Color.WHITE, howToPlayActive.getWidth()/2, howToPlayActive.getHeight()/2,
                        howToPlayCoords.x, howToPlayCoords.y, 0, scale, scale);
            } else{
                canvas.draw(howToPlayInactive, Color.WHITE, howToPlayInactive.getWidth()/2, howToPlayActive.getHeight()/2,
                        howToPlayCoords.x, howToPlayCoords.y, 0, scale, scale);
            }

            // draw past scores
            for (int i = 0; i < allLevels.length; i++) {
                String noPath = allLevels[i].substring("levels/".length());
                long highScore = SaveManager.getInstance().getHighScore(noPath);
                String grade = SaveManager.getInstance().getGrade(noPath);
                long highestCombo = SaveManager.getInstance().getHighestCombo(noPath);
                drawPastScores(i / 3, i < 3 ? 1 : (i % 3 + 1), (int) highScore, grade, (int) highestCombo, !grade.equals(""));
            }
        }

        canvas.end();
    }

    /**
     * Draw past scores, if possible
     *
     */
    private void drawPastScores(int level, int diff, int pastScore, String letterGrade, int combo, boolean hasData){
        if (hasData && level == selectedLevel && diff == selectedDifficulty ){
            canvas.draw(scoreBox, Color.WHITE, scoreBox.getWidth()/2f, scoreBox.getHeight()/2f,
                    canvas.getWidth()/2f, canvas.getHeight()*0.85f, 0, scale, scale);

            canvas.draw(scoreLine, Color.WHITE, scoreLine.getWidth()/2f, scoreLine.getHeight()/2f,
                    canvas.getWidth()/2f*1.1f, canvas.getHeight()*0.85f, 0, 0.85f*scale, 0.85f*scale);

            parameter.size = 40;
            canvas.drawText("Combo "+combo, blinkerBold,canvas.getWidth()*0.5f,
                    canvas.getHeight()*0.89f, Color.valueOf("FF00FE"));

            parameter.size = 48;
            canvas.drawText(Integer.toString(pastScore), blinkerBold,canvas.getWidth()*0.5f,
                    canvas.getHeight()*0.83f, Color.valueOf("1531D7"));

            Texture pastLetterGrade = directory.getEntry("score-"+letterGrade.toLowerCase(Locale.ROOT) ,Texture.class);
            canvas.draw(pastLetterGrade, Color.WHITE, pastLetterGrade.getWidth()/2f, pastLetterGrade.getHeight()/2f,
                    canvas.getWidth()/2f*0.85f, canvas.getHeight()*0.85f, 0, 0.55f*scale,
                    0.55f*scale);

        }


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
        float xRadius = scale * buttonTexture.getWidth()/2.0f;
        boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

        // get half the y length of the button portrayed
        float yRadius = scale * buttonTexture.getHeight()/2.0f;
        boolean yInBounds = buttonCoords.y - yRadius <= screenY && buttonCoords.y + yRadius >= screenY;
        return xInBounds && yInBounds;
    }



    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            pressedEscape = true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     *  True when we are in transition
     */
    private boolean isInTransition;

    private static final int NO_BUTTON_PRESSED = 206;
    private static final int EASY_PRESSED = 208;
    private static final int MEDIUM_PRESSED = 209;
    private static final int HARD_PRESSED = 210;

    private int tutorialIdx;

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
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (!isInTransition){
            screenY = canvas.getHeight()-screenY;

            if (playButton == null ) {
                return true;
            }

            if (!howToPlayPressed){
                if (selectedLevel != 0) {
                    if (isButtonPressed(screenX, screenY, easyButton, easyButtonCoords, 0.4f * scale)) {
                        s.playSound(0, 0.3f);
                        selectedDifficulty = 1;
                    }

                    if (isButtonPressed(screenX, screenY, mediumButton, mediumButtonCoords, 0.4f * scale)) {
                        s.playSound(0, 0.3f);
                        selectedDifficulty = 2;
                    }


                    if (isButtonPressed(screenX, screenY, hardButton, hardButtonCoords, 0.4f * scale)) {
                        s.playSound(0, 0.3f);
                        selectedDifficulty = 3;
                    }
                }

                // if there are a previous level, we allow decrement.
                if (isButtonPressed(screenX, screenY, goLeft, goLeftCoords, 0.9f*scale)) {
                    s.playSound(0, 0.3f);
                    if (selectedLevel-1>=0){
                        prevLevel = selectedLevel;
                        selectedLevel--;
                    }
                }

                // if there are a next level, we allow increment.
                if (isButtonPressed(screenX, screenY, goRight, goRightCoords, 0.9f*scale)) {
                    s.playSound(0, 0.3f);
                    if (selectedLevel+1< numSongs){
                        prevLevel = selectedLevel;
                        selectedLevel++;
                    }
                }

    //         check if the albums on the sides are touched; if so, update selected level
                if (selectedLevel-1>=0){
                    Vector2 temp = new Vector2(albumCoverLeftX,albumCoverY);
                    if (isButtonPressed(screenX, screenY, albumCovers[selectedLevel-1], temp, cornerScale)) {
                        s.playSound(0, 0.3f);
                        prevLevel = selectedLevel;
                        selectedLevel--;
                        return true;
                    }
                }

                if (selectedLevel+1< numSongs){
                    Vector2 temp = new Vector2(albumCoverRightX,albumCoverY);
                    if (isButtonPressed(screenX, screenY, albumCovers[selectedLevel], temp, cornerScale)) {
                        s.playSound(0, 0.3f);
                        prevLevel = selectedLevel;
                        selectedLevel++;
                    }
                }

                if (isButtonPressed(screenX, screenY, playButton, playButtonCoords,BUTTON_SCALE*scale)) {
                    s.playSound(0, 0.3f);
                    playPressed=true;
                }

                if (isButtonPressed(screenX, screenY, goBack, goBackCoords,WON_BUTTON_SCALE)){
                    s.playSound(0, 0.3f);
                    listener.exitScreen(this, ExitCode.TO_MENU);
                }
            } else{
                if (tutorialIdx==4){
                    if (isButtonPressed(screenX, screenY, tutorialReady, tutorialReadyCoords, 0.9f*scale)) {
                        tutorialIdx=0;
                        howToPlayPressed = false;
                    }

                } else if (tutorialIdx<5 && howToPlayPressed){
                    if (isButtonPressed(screenX, screenY, tutorialGoRight, tutorialGoRightCoords, 0.9f*scale)) {
                        tutorialIdx++;
                    }

                }
            }

            if (isButtonPressed(screenX, screenY, howToPlayInactive, howToPlayCoords,scale)){
                howToPlayPressed = true;
            }

        }

        return false;
    }


    boolean howToPlayPressed;


    boolean set1 = false;
    /**
     * Update coordinates; called per frame
     *
     */
    private void update(){
//        System.out.println("prev level:"+prevLevel);
//        System.out.println("curr level"+selectedLevel);
        float steps = 30f;
        float scaleChange = centerScale - cornerScale;
        float rightLenX = Math.abs(albumCoverMiddleX - albumCoverLeftX);
        float rightStep = rightLenX / steps;
        float scaleStep = scaleChange / steps;

        if (prevLevel!=selectedLevel){ // need to do transition animation
            isInTransition=true;
            if (prevLevel < selectedLevel){// we need to transition to right
                if (selectedLevel+1< numSongs && !set1 ) {
                    albumCoverCoords[selectedLevel + 1] = new Vector2(canvas.getWidth(), albumCoverY);
                    albumScales[selectedLevel + 1]=cornerScale;
                    set1=true;
                }
                if (albumCoverCoords[prevLevel].x > albumCoverLeftX) { // move previous level from center to left
                    albumCoverCoords[prevLevel].x -= rightStep;
                    albumCoverCoords[selectedLevel].x-=rightStep;
                    if (selectedLevel+1< numSongs){
                        albumCoverCoords[selectedLevel+1].x -=rightStep;
                    }
                    albumScales[prevLevel] -=scaleStep;
                    albumScales[selectedLevel] +=scaleStep;
                } else{ // reset
                    albumCoverCoords[prevLevel]= new Vector2(albumCoverLeftX,albumCoverY);
                    albumCoverCoords[selectedLevel] = new Vector2(albumCoverMiddleX,albumCoverY);
                    if (selectedLevel+1< numSongs) {
                        albumCoverCoords[selectedLevel + 1] = new Vector2(albumCoverRightX, albumCoverY);
                        albumScales[selectedLevel+1] =cornerScale;
                    }
                    albumScales[prevLevel]=cornerScale;
                    albumScales[selectedLevel] = centerScale;
                    prevLevel = selectedLevel;
                    set1=false;
                    isInTransition=false;
                }
            } else{// we need to transition to left (move to the right)
                if (selectedLevel>=1 && !set1) {
                    albumCoverCoords[selectedLevel-1] = new Vector2(0, albumCoverY);
                    albumScales[selectedLevel-1]=cornerScale;
                    set1=true;
                }
                if (albumCoverCoords[prevLevel].x < albumCoverRightX) { // move previous level from center to left
                    albumCoverCoords[prevLevel].x += rightStep;
                    albumCoverCoords[selectedLevel].x+=rightStep;
                    if (selectedLevel>=1){
                        albumCoverCoords[selectedLevel-1].x +=rightStep;
                    }
                    albumScales[prevLevel] -=scaleStep;
                    albumScales[selectedLevel] +=scaleStep;
                } else{ // reset
                    albumCoverCoords[prevLevel]= new Vector2(albumCoverRightX,albumCoverY);
                    albumCoverCoords[selectedLevel] = new Vector2(albumCoverMiddleX,albumCoverY);
                    if (selectedLevel>=1) {
                        albumCoverCoords[selectedLevel - 1] = new Vector2(albumCoverLeftX, albumCoverY);
                        albumScales[selectedLevel-1] =cornerScale;
                    }
                    albumScales[prevLevel]=cornerScale;
                    albumScales[selectedLevel] = centerScale;
                    prevLevel = selectedLevel;
                    set1=false;
                    isInTransition=false;
                }

            }

        }

    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }


    private static final int NO_BUTTON_HOVERED = 106;
    private static final int PLAY_HOVERED = 107;
    private static final int EASY_HOVERED = 108;
    private static final int MEDIUM_HOVERED = 109;
    private static final int HARD_HOVERED = 110;
    private static final int HOW_TO_PLAY_HOVERED = 111;

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        screenY = canvas.getHeight() - screenY;
        if (isButtonPressed(screenX, screenY, playButton, playButtonCoords,BUTTON_SCALE*scale)) {
            hoverState = PLAY_HOVERED;
        } else if (isButtonPressed(screenX, screenY, easyButton, easyButtonCoords,0.4f*scale)) {
            hoverState = EASY_HOVERED;
        }else if (isButtonPressed(screenX, screenY, mediumButton, mediumButtonCoords,0.4f*scale)) {
            hoverState = MEDIUM_HOVERED;
        }else if (isButtonPressed(screenX, screenY, hardButton, hardButtonCoords,0.4f*scale)) {
            hoverState = HARD_HOVERED;
        }else if (isButtonPressed(screenX, screenY, howToPlayInactive, howToPlayCoords,scale)){
            hoverState = HOW_TO_PLAY_HOVERED;
        } else {
            hoverState = NO_BUTTON_HOVERED;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void show() {
        active = true;
    }

    int currLevel;

    public String getLevelString(){
        return allLevels[currLevel];
    }

    @Override
    public void render(float delta) {
        if (active) {
            update();
            draw();
            if (playPressed && listener != null) {
                // go to game
//                System.out.println("selected level:" + selectedLevel);
//                System.out.println("selected difficulty:" + selectedDifficulty);
                int gameIdx = selectedDifficulty+(selectedLevel*3);
                currLevel = gameIdx;
//                System.out.println("game index: "+gameIdx);
                selectedJson=allLevels[gameIdx-1];
                listener.exitScreen(this, ExitCode.TO_PLAYING);
            } else if (pressedEscape && listener != null) {
                listener.exitScreen(this, ExitCode.TO_MENU);
            }
        }
    }



    @Override
    public void resize(int width, int height) {
        setCoords(width,height);

        // recompute scales
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;

    }

    @Override
    public void dispose() {
        directory.unloadAssets();
        directory.dispose();
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
