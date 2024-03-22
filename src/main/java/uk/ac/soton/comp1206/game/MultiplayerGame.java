package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates a new multiplayer game extending from Game
 */
public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    private Queue<GamePiece> pieceQueue = new LinkedList<>();
    private Communicator communicator;

    private boolean first = true;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator The communicator for the current game
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
    }

    /**
     * Initialises a new game, and anything that needs setting up from the beginning.
     */
    @Override
    public void initialiseGame(){

        communicator.addListener((message) -> {
            String[] messageSplit = message.split(" "); //Message from communicator is seperated. The identifier and the actual message contents
            if(messageSplit[0].equals("PIECE")){
                pieceQueue.add(spawnPiece(Integer.parseInt(messageSplit[1])));
                if(first){
                    followingPiece = pieceQueue.remove();
                    first = false;
                    callPiece();
                }
                nextPiece();
            }
        });

        gameLoop();
        gameLoopListener.gameLoop();
    }

    /**
     * The following piece is set as the current piece
     * And the following piece is obtained via the piece queue
     */
    public void nextPiece(){

        currentPiece = followingPiece;
        followingPiece = pieceQueue.remove();

        logger.info("Current Piece {}",currentPiece.toString());

        if(nextPieceListener != null){
            nextPieceListener.nextPiece(currentPiece,followingPiece);
        }

    }

    /**
     * Calls for the next piece from the network
     */
    public void callPiece(){

        communicator.send("PIECE");

    }

    /**
     * Timer for the game
     * Decrements lives and resets multiplier if timer has finished
     * Next piece is shown
     */
    @Override
    public void gameLoop(){

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if(placed){
                        placed = false;
                        sendBoardState();
                    }
                    else{
                        multimedia.playAudio("sounds/lifelose.wav");
                        multiplier.set(multiplier.get()-1);
                        lives.set(lives.get()-1);
                        gameLoopListener.gameLoop();
                    }
                    if(!(lives.get()<0)){
                        callPiece();
                    }


                });
                logger.info("Time reset");
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,getTimerDelay());

    }

    /**
     * Stops game timer
     */
    @Override
    public void stopTimer(){
        timer.cancel();
    }

    /**
     * Calculates the score from a single move
     * @param lines lines cleared
     * @param blocks blocks cleared
     */
    @Override
    public void score(int lines, int blocks){

        if(blocks > 0){
            int add = lines*blocks*10*getMultiplier();
            score.set(score.get() + add);

            communicator.send("SCORE " + score.getValue());

            logger.info("{} added to score", add);
            logger.info("Current score {}",score.get());

        }

    }

    /**
     * Sends a string of the current pieces on the board
     */
    public void sendBoardState(){
        StringBuilder boardState = new StringBuilder("BOARD"); //Message to send

        for(int x = 0;x<grid.getCols();x++){
            for(int y = 0;y<grid.getRows();y++){
                boardState.append(" ").append(grid.get(x, y));
            }
        }

        communicator.send(String.valueOf(boardState));

    }

}
