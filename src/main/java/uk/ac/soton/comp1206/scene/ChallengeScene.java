package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Game
     */
    public Game game;

    /**
     * Music player
     */
    public Multimedia multimedia;
    private PieceBoard currentPieceBoard;
    private PieceBoard followingPieceBoard;

    private Rectangle timerBar;

    /**
     * Initial keyboard grid x coordinate
     */
    public int x = 2;

    /**
     * Initial keyboard grid y coordinate
     */
    public int y = 2;

    /**
     * Pane containing contents to display
     */
    public StackPane challengePane;

    /**
     *High score value
     */
    public Text highScore;

    /**
     * High score title
     */
    public Text highScoreTitle;

    /**
     * Main game board
     */
    public GameBoard board;

    /**
     * Contains the current piece to play and the next piece after that.
     */
    public VBox pieceBoards;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //Display main grid
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2, "mainBoard");
        mainPane.setCenter(board);
        board.setTranslateX(-80);
        board.setTranslateY(30);

        //Display secondary grid
        currentPieceBoard = new PieceBoard(3,3,gameWindow.getWidth()/5,gameWindow.getWidth()/5, "currentPieceBoard");
        followingPieceBoard = new PieceBoard(3,3,gameWindow.getWidth()/7,gameWindow.getWidth()/7, "followingPieceBoard");

        //Handle block on game board grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnRightClicked(this::rightClicked);

        //Handle block events on the current and following grids being clicked
        currentPieceBoard.setOnRightClicked(this::rightClicked);
        followingPieceBoard.setOnRightClicked(this::rightClicked);

        //Display the smaller boards
        pieceBoards = new VBox(15);
        mainPane.getChildren().add(pieceBoards);
        pieceBoards.setTranslateY(350);
        pieceBoards.setTranslateX(660);
        VBox.setVgrow(pieceBoards,Priority.NEVER);
        pieceBoards.setAlignment(Pos.CENTER);
        pieceBoards.getChildren().addAll(currentPieceBoard,followingPieceBoard);

        //Handle displaying the next piece on the pieceBoard grid
        game.setNextPieceListener((current, following) -> {
            currentPieceBoard.displayPiece(current);
            currentPieceBoard.setCircle();
            followingPieceBoard.displayPiece(following);
        });

        game.setLineClearedListener(gameBlockCoordinate -> {
            board.fadeOut(gameBlockCoordinate);
        });

        //Time bar
        timerBar = new Rectangle(gameWindow.getWidth()*0.95,gameWindow.getHeight()*0.04);
        timerBar.setFill(Color.RED);
        timerBar.setTranslateY(280);
        challengePane.getChildren().add(timerBar);

        game.setGameLoopListener(this::animateBar);

        //Sets of labels for the headings
        var title = new Text("Challenge Mode");
        title.getStyleClass().add("heading");
        challengePane.getChildren().add(title);
        challengePane.setAlignment(title,Pos.TOP_CENTER);

        var score = new Text();
        var level = new Text();
        var lives = new Text();
        var scoreTitle = new Text("Score");
        var levelTitle = new Text("Level");
        var livesTitle = new Text("Lives");

        highScore = new Text();
        highScoreTitle = new Text("High score");

        score.textProperty().bind(game.scoreProperty().asString());
        score.getStyleClass().add("score");
        score.setTranslateX(-300);
        score.setTranslateY(-205);

        level.textProperty().bind(game.levelProperty().asString());
        level.getStyleClass().add("level");
        level.setTranslateX(300);
        level.setTranslateY(-205);

        lives.textProperty().bind(game.livesProperty().asString());
        lives.getStyleClass().add("lives");
        lives.setTranslateY(-205);

        scoreTitle.getStyleClass().add("subheading");
        scoreTitle.setTranslateX(-300);
        scoreTitle.setTranslateY(-235);

        levelTitle.getStyleClass().add("subheading");
        levelTitle.setTranslateX(300);
        levelTitle.setTranslateY(-235);

        livesTitle.getStyleClass().add("subheading");
        livesTitle.setTranslateY(-235);

        highScore.setText(String.valueOf(getHighScore()));
        highScore.getStyleClass().add("score");
        highScore.setTranslateX(260);
        highScore.setTranslateY(-130);

        highScoreTitle.getStyleClass().add("subheading");
        highScoreTitle.setTranslateX(260);
        highScoreTitle.setTranslateY(-160);

        challengePane.getChildren().addAll(score,level,lives,scoreTitle,levelTitle,livesTitle,highScore,highScoreTitle);

        score.textProperty().addListener(observable -> {
            if(Integer.parseInt(score.getText())>Integer.parseInt(highScore.getText())){
                highScore.setText(score.getText());
            }
        });

        //Play game music
        multimedia = new Multimedia();
        multimedia.playMusic("music/game.wav");

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    public void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * When mouse if right-clicked on the grids
     * @param name Board name
     */
    public void rightClicked(String name){
        game.rightClicked(name);
    }


    /**
     * Sets up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();

        //Keyboard support
        gameWindow.getScene().setOnKeyPressed(event -> {

            //Will clear any previous hover effects
            for(int x=0;x<board.getRowCount();x++){
                for(int y=0;y<board.getColumnCount();y++){
                    board.getBlock(x,y).paint();
                }
            }

            switch (event.getCode()) {
                case ESCAPE -> {
                    logger.info("Returning to menu");
                    stopGame();
                    gameWindow.startMenu();
                }
                case W,UP -> {
                    y--;
                    if(y<0){
                        y=4;
                    }
                }
                case A,LEFT -> {
                    x--;
                    if(x<0){
                        x=4;
                    }
                }
                case S,DOWN ->{
                    y++;
                    if(y>4){
                        y=0;
                    }
                }
                case D,RIGHT -> {
                    x++;
                    if(x>4){
                        x=0;
                    }
                }
                case Q,Z,OPEN_BRACKET -> game.rotateCurrentPiece(1);
                case E,C,CLOSE_BRACKET -> game.rotateCurrentPiece(3);
                case SPACE,R -> game.swapCurrentPiece();
                case ENTER,X -> game.blockClicked(board.getBlock(x, y));
            }

            board.getBlock(x,y).hover();

        });

    }

    /**
     * Cleans up the game
     */
    public void stopGame(){
        multimedia.stopMusic();
        multimedia.playAudio("sounds/explode.wav");
        game.stopTimer();
    }

    /**
     * Animates the time bar
     */
    public void animateBar(){

        gameOverCheck();

        Duration duration = Duration.millis(game.getTimerDelay());

        ScaleTransition scaleTransition = new ScaleTransition(duration,timerBar);

        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(0.0);
        scaleTransition.setInterpolator(Interpolator.LINEAR);

        FillTransition fillTransition = new FillTransition(duration,timerBar,Color.GREEN,Color.RED);

        scaleTransition.play();
        fillTransition.play();

    }

    /**
     * Checks if the user has run out of lives
     */
    public void gameOverCheck(){
        if(game.getLives()<0){
            logger.info("GAME OVER");
            stopGame();
            gameWindow.startScore(game);
        }
    }

    /**
     * Gets the current local high score to beat
     * @return Integer value of top score
     */
    public Integer getHighScore(){

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/scores.txt")));
            String line;

            if((line = reader.readLine()) != null){
                return(Integer.parseInt(line.split(":")[1]));
            }

            reader.close();

        } catch(Exception e){
            logger.error("File not found");
            e.printStackTrace();
        }

        return 0;

    }

}
