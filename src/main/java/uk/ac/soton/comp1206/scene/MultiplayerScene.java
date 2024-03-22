package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.security.Key;

/**
 * The multiplayer challenge scene. Extends from the ChallengeScene and alters some UI components
 */
public class MultiplayerScene extends ChallengeScene{

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private Communicator communicator;

    private boolean typing = false;

    private TextField chat;

    private Text chatInfo;

    private Leaderboard onlineScores;

    private int playerCount;

    private VBox leaderboardInfo;

    private boolean dead = false;

    /**
     * Observable list which contains the in-game players name, score and lives
     */
    public ObservableList<Pair<String,String>> scores = FXCollections.observableArrayList();

    /**
     * List containing the in-game players name, score and lives
     */
    public SimpleListProperty<Pair<String,String>> leaderboardScores = new SimpleListProperty<>(scores);

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow The Game Window
     * @param communicator The current lobby's communicator
     * @param playerCount The number of players in the lobby
     */
    public MultiplayerScene(GameWindow gameWindow, Communicator communicator, int playerCount) {
        super(gameWindow);
        this.communicator = communicator;
        this.playerCount = playerCount;
    }

    /**
     * Set up the multiplayer game object and model
     */
    @Override
    public void setupGame(){
        logger.info("Starting a new challenge");

        //Start new game
        game = new MultiplayerGame(5, 5, communicator);

    }

    /**
     * Stops the music and game timer
     */
    @Override
    public void stopGame(){
        multimedia.stopMusic();
        multimedia.playAudio("sounds/explode.wav");
        game.stopTimer();
    }

    /**
     * Builds the multiplayer challenge window
     */
    @Override
    public void build(){

        super.build();

        //Remove unnecessary labels
        challengePane.getChildren().remove(highScore);
        challengePane.getChildren().remove(highScoreTitle);

        //Text chat below grid
        chat = new TextField("chat");
        chat.setMaxWidth(250);
        chat.setVisible(false);
        chat.setTranslateY(250);
        chat.getStyleClass().add("playerBox");
        challengePane.getChildren().add(chat);

        chatInfo = new Text("Press T to start typing in chat");
        chatInfo.setTranslateY(250);
        chatInfo.getStyleClass().add("playerBox");
        challengePane.getChildren().add(chatInfo);

        board.setTranslateX(-140);
        pieceBoards.setTranslateX(560);

        //In-game leaderboard
        var leaderboardBox = new VBox();
        var leaderboardTitle = new Text("Versus");
        leaderboardInfo = new VBox();

        leaderboardTitle.getStyleClass().add("leaderboard");
        leaderboardBox.getChildren().addAll(leaderboardTitle,leaderboardInfo);

        challengePane.getChildren().add(leaderboardBox);
        leaderboardBox.setTranslateX(650);
        leaderboardBox.setTranslateY(180);
        leaderboardTitle.setTranslateX(25);
        leaderboardInfo.setTranslateX(10);

        onlineScores = new Leaderboard(playerCount);
        leaderboardScores.bind(onlineScores.scoresList);

        //Call for initial leaderboard standings
        communicator.send("SCORES");

        communicator.addListener((message) -> {
            String[] messageSplit = message.split(" "); //Message from communicator is seperated. The identifier and the actual message contents

            if(messageSplit[0].equals("SCORES")){
                onlineScores.updateScores(messageSplit[1]);
                updateLeaderboard();
            } else if(messageSplit[0].equals("DIE")){
                if(!dead){
                    communicator.send("SCORES");
                }
            } else if(messageSplit[0].equals("MSG")){
                chatInfo.setText(message.substring(4));
                multimedia.playAudio("sounds/message.wav");
            } else if(messageSplit[0].equals("ERROR")){
                logger.info("fjkasfhajklfhafhawuluia");
            }

        });

    }

    public void initialise(){
        logger.info("Initialising Challenge");
        game.start();

        //Keyboard support
        gameWindow.getScene().setOnKeyPressed(event -> {

            board.getBlock(x,y).paint();

            if(event.getCode() == KeyCode.T && !typing){
                typing = true;
                chat.setVisible(true);
                chatInfo.setVisible(false);
            } else if(event.getCode() == KeyCode.ESCAPE && typing){
                typing = false;
                chat.setVisible(false);
                chatInfo.setVisible(true);
            } else if(event.getCode() == KeyCode.ENTER && typing){
                typing = false;
                chat.setVisible(false);
                chatInfo.setVisible(true);
                sendMessage();
            } else {
                switch (event.getCode()) {
                    case ESCAPE -> {
                        logger.info("Returning to menu");
                        stopGame();
                        communicator.send("PART");
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
            }

        });

    }

    /**
     * Updates the in-game live leaderboard when a new score update is received
     */
    public void updateLeaderboard(){

        Platform.runLater(() -> {

            leaderboardInfo.getChildren().clear();

            for(int i = 0; i<onlineScores.getNumberOfScores();i++){
                var playerInfo = new Text(leaderboardScores.get(i).getKey());
                playerInfo.setWrappingWidth(120);
                if(leaderboardScores.get(i).getValue().equals("DEAD")){
                    playerInfo.getStyleClass().add("onlineDead");
                } else{
                    playerInfo.getStyleClass().add("onlineLeaderboard");
                }
                leaderboardInfo.getChildren().add(playerInfo);
                fadeIn(playerInfo);
            }

        });

    }

    /**
     * Checks if the player has lost all their lives and sends an update to the server.
     */
    @Override
    public void gameOverCheck(){
        if(game.getLives()<0){
            dead = true;
            communicator.send("DIE");
            stopGame();

            gameWindow.startScore(game, onlineScores);
        }
    }

    /**
     * Fades in text
     * @param score Text which stores the name and score
     */
    public void fadeIn(Text score){
        FadeTransition transition;
        transition = new FadeTransition(Duration.millis(500),score);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.setInterpolator(Interpolator.EASE_IN);
        transition.play();
    }

    /**
     * Sends users message to server
     */
    public void sendMessage(){

        communicator.send("MSG " + chat.getText());

    }



}
