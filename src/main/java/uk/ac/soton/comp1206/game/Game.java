package uk.ac.soton.comp1206.game;

import java.util.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The piece which is about to be placed
     */
    public GamePiece currentPiece;

    /**
     * The piece which follows on from currentPiece
     */
    public GamePiece followingPiece;

    /**
     * Player's score
     */
    protected SimpleIntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Player's level
     */
    protected SimpleIntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Player's lives
     */
    protected SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * Player's multiplier
     */
    protected SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Piece listener
     */
    public NextPieceListener nextPieceListener;

    /**
     * Music player
     */
    public Multimedia multimedia = new Multimedia();

    /**
     * Line cleared listener
     */
    private LineClearedListener lineClearedListener;

    /**
     * List of coordinates of blocks cleared
     */
    private HashSet<GameBlockCoordinate> blockCoordinateList = new HashSet<>();

    /**
     * Game loop timer
     */
    public Timer timer;

    /**
     * Timer loop task
     */
    public TimerTask timerTask;

    /**
     * Holds the state of whether a block has been placed or not for the current loop
     */
    public boolean placed = true;

    /**
     * Game loop listener
     */
    public GameLoopListener gameLoopListener;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        followingPiece = spawnPiece();

        gameLoop();
        gameLoopListener.gameLoop();

    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {

        int[] linesblocks;

        if(grid.canPlayPiece(currentPiece,gameBlock)){
            grid.playPiece(currentPiece,gameBlock);
            multimedia.playAudio("sounds/place.wav");

            placed = true;

            timer.cancel();
            gameLoop();
            gameLoopListener.gameLoop();

            linesblocks = afterPiece();

            //Index 0 = lines cleared
            //Index 1 = blocks cleared
            score(linesblocks[0],linesblocks[1]);

            //Calculates the users current multiplier
            if(linesblocks[0] > 0){
                multiplier.set(multiplier.get() + 1);
            }
            else{
                multiplier.set(1);
            }

            //Calculates the users current level
            if(score.get()/1000 > level.get()){
                level.set(score.get()/1000);
                multimedia.playAudio("sounds/level.wav");
                logger.info("Level increased to {}!",level.get());
            }
        }
        else{
            multimedia.playAudio("sounds/fail.wav");
        }

    }

    /**
     * Adds a new listener to receive the piece to place on the piece board
     * @param nextPieceListener Listener
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener){
        this.nextPieceListener = nextPieceListener;
    }

    /**
     * Adds a new listener to receive the coordinates of the blocks cleared on the piece board
     * @param lineClearedListener Listener
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener){
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Adds a new listener to receive when the timer has been started
     * @param gameLoopListener Listener
     */
    public void setGameLoopListener(GameLoopListener gameLoopListener){
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Creates a new GamePiece
     * @return New GamePiece
     */
    public GamePiece spawnPiece(){

        Random random = new Random();

        //Creates a new GamePiece to be placed
        return GamePiece.createPiece(random.nextInt(15));

    }

    /**
     * Creates a specific new GamePiece
     * @param piece piece
     * @return New GamePiece
     */
    public GamePiece spawnPiece(int piece){

        //Creates a new GamePiece to be placed
        return GamePiece.createPiece(piece);

    }

    /**
     * Get the player's score in this game
     * @return player's score
     */
    public int getScore() {
        return score.get();
    }

    public SimpleIntegerProperty scoreProperty(){
        return this.score;
    }

    /**
     * Get the player's level in this game
     * @return player's level
     */
    public int getLevel() {
        return level.get();
    }

    public SimpleIntegerProperty levelProperty(){
        return this.level;
    }

    /**
     * Get the player's lives in this game
     * @return player's lives
     */
    public int getLives() {
        return lives.get();
    }

    public SimpleIntegerProperty livesProperty(){
        return this.lives;
    }

    /**
     * Get the player's multiplier in this game
     * @return player's multiplier
     */
    public int getMultiplier() {
        return multiplier.get();
    }

    public SimpleIntegerProperty multiplierProperty(){
        return this.multiplier;
    }

    /**
     * Current and following GamePiece is generated
     * Passed to listener to update the grids
     */
    private void nextPiece(){

        currentPiece = followingPiece;
        followingPiece = spawnPiece();

        logger.info("Current Piece {}",currentPiece.toString());

        if(nextPieceListener != null){
            nextPieceListener.nextPiece(currentPiece,followingPiece);
        }
    }

    /**
     * Calls the fade method whenever lines have been cleared
     * @param gameBlockCoordinates Hashset of block coordinates
     */
    public void fadeOut(HashSet<GameBlockCoordinate> gameBlockCoordinates){
        if(lineClearedListener != null){
            lineClearedListener.fadeOut(gameBlockCoordinates);
        }
    }

    /**
     * Checks every row and column and clears the values if line made
     * @return The array containing the number of lines and blocks cleared
     */
    public int[] afterPiece(){

        List<Integer> columnVal = new ArrayList<>(); //Stores column numbers to clear
        List<Integer> rowVal = new ArrayList<>(); //Stores row numbers to clear

        int[] linesBlocks = {0,0}; //The total number of lines and blocks cleared. Index 0 = Lines cleared. Index 1 = Blocks cleared.

        for(int row=0;row<rows;row++){
            for(int col=0;col<cols;col++){
                if(grid.get(row,col) == 0){
                    break;
                }
                else if(col == cols-1){
                    columnVal.add(row);
                }
            }
        }

        for(int col=0;col<cols;col++){
            for(int row=0;row<rows;row++){
                if(grid.get(row,col) == 0){
                    break;
                }
                else if(row == rows-1){
                    rowVal.add(col);
                }
            }
        }

        for(int i=0;i<rowVal.size();i++){
            linesBlocks[1] += clearRow(rowVal.get(i));
            linesBlocks[0]++;
        }

        for(int i=0;i<columnVal.size();i++){
            linesBlocks[1] += clearColumn(columnVal.get(i));
            linesBlocks[0]++;
        }

        //Fades out all the blocks which have been cleared
        fadeOut(blockCoordinateList);
        blockCoordinateList.clear();

        //Sound is played if any lines are cleared
        if (linesBlocks[0]!=0 || linesBlocks[1] != 0){
            multimedia.playAudio("sounds/clear.wav");
        }

        return linesBlocks;

    }

    /**
     * Each row in the column is iterated and set to 0
     * @param col Column to be cleared
     * @return number of blocks cleared
     */
    public int clearRow(int col){

        int blocks = 0; //Number of blocks cleared

        for(int i=0;i<rows;i++){
            if(grid.get(i,col) > 0){
                blockCoordinateList.add(new GameBlockCoordinate(i,col));
                blocks++;
                grid.set(i,col,0);
            }
        }

        logger.info("Row cleared");
        return blocks;

    }

    /**
     * Each column in the row is iterated and set to 0
     * @param row Row to be cleared
     * @return number of blocks cleared
     */
    public int clearColumn(int row){

        int blocks = 0; //Number of blocks cleared

        for(int i=0;i<cols;i++){
            if(grid.get(row,i) > 0){
                blockCoordinateList.add(new GameBlockCoordinate(row,i));
                blocks++;
                grid.set(row,i,0);
            }

        }

        logger.info("Column cleared");
        return blocks;

    }

    /**
     * Calculates the score from a single move
     * @param lines lines cleared
     * @param blocks blocks cleared
     */
    public void score(int lines, int blocks){

        if(blocks > 0){
            int add = lines*blocks*10*getMultiplier();
            score.set(score.get() + add);

            logger.info("{} added to score", add);
            logger.info("Current score {}",score.get());

        }

    }

    /**
     * Rotates the current game piece
     * @param rotations Number of tiems to rotate the piece
     */
    public void rotateCurrentPiece(int rotations){
        logger.info("Piece rotated");
        currentPiece.rotate(rotations);
        multimedia.playAudio("sounds/rotate.wav");
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * The current piece and following piece are swapped
     */
    public void swapCurrentPiece(){
        logger.info("Swapped pieces");
        multimedia.playAudio("sounds/rotate.wav");
        GamePiece temp = followingPiece;
        followingPiece = currentPiece;
        currentPiece = temp;
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Calls either rotateCurrentPiece or swapCurrentPiece method dependent on the name of the board passed through
     * @param name name of the board
     */
    public void rightClicked(String name){
        if(name.equals("currentPieceBoard") || name.equals("mainBoard")){
            rotateCurrentPiece(1);
        }
        else if(name.equals("followingPieceBoard")){
            swapCurrentPiece();
        }
    }

    /**
     * Calculates the timer duration
     * Delay must be at least 2500
     * @return Timer delay
     */
    public int getTimerDelay(){

        int temp = 12000-500*level.get(); //Calculated delay

        return Math.max(temp, 2500);

    }

    /**
     * Timer for the game
     * Decrements lives and resets multiplier if timer has finished
     * Next piece is shown
     */
    public void gameLoop(){

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if(placed){
                        placed = false;
                    }
                    else{
                        multimedia.playAudio("sounds/lifelose.wav");
                        multiplier.set(1);
                        lives.set(lives.get()-1);
                        gameLoopListener.gameLoop();
                    }

                    nextPiece();

                });
                logger.info("Time reset");
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,getTimerDelay());

    }

    /**
     * Current timer is stopped
     */
    public void stopTimer(){
        timer.cancel();
    }

}
