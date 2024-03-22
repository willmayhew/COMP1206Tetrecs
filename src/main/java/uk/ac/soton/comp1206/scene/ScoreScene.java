package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

/**
 * The score scene. Holds the UI components of the score screen.
 */
public class ScoreScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoreScene.class);

    private Communicator communicator = this.gameWindow.getCommunicator();

    private Boolean offline = true;

    /**
     * Observable list which contains pairs of names and scores
     */
    public ObservableList<Pair<String,Integer>> scores = FXCollections.observableArrayList();
    /**
     * List containing pairs of name and score
     */
    public SimpleListProperty<Pair<String,Integer>> localScores = new SimpleListProperty<>(scores);

    /**
     * Observable list which contains pairs of names and scores
     */
    public ObservableList<Pair<String,Integer>> onlineScores = FXCollections.observableArrayList();

    /**
     * List containing pairs of name and score
     */
    public SimpleListProperty<Pair<String,Integer>> remoteScores = new SimpleListProperty<>(onlineScores);

    private ScoresList localScoresList;
    private ScoresList onlineScoresList;

    private Leaderboard multiplayerList;

    private Multimedia multimedia;

    private Integer gameScore;
    private Game game;
    private String name;
    private boolean askedName = false;

    private HBox scoresBoxes = new HBox(30);
    private VBox localScoresBox = new VBox(1);
    private VBox onlineScoresBox = new VBox(1);

    /**
     * Create a new score scene
     * @param gameWindow The score screen
     * @param game The Game just played
     */
    public ScoreScene(GameWindow gameWindow, Game game){
        super(gameWindow);
        gameScore = game.getScore();
        logger.info("Creating Score Scene");
    }

    /**
     * Create a new score scene
     * @param gameWindow The Game Window
     * @param game The Game just played
     * @param multiplayerList The List of players which were in the game
     */
    public ScoreScene(GameWindow gameWindow, Game game, Leaderboard multiplayerList){
        super(gameWindow);
        this.game = game;
        this.multiplayerList = multiplayerList;
        this.offline = false;
        logger.info("Creating Score Scene");
    }


    @Override
    public void initialise() {

        //Exit the instruction screen back to the menu
        gameWindow.getScene().setOnKeyPressed(event -> {
            if(event.getCode()!= KeyCode.ESCAPE)return;
            multimedia.stopMusic();
            gameWindow.startMenu();
            logger.info("Returning to menu");
        });

    }

    @Override
    public void build() {

        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("menu-background");
        root.getChildren().add(scorePane);

        var mainPane = new BorderPane();
        scorePane.getChildren().addAll(mainPane,scoresBoxes);

        multimedia = new Multimedia();
        multimedia.playMusic("music/end.wav");

        //Tetrecs Image
        try{
            var titleImage = new Image(getClass().getResource("/images/tetrECS.png").toString());
            var imageView = new ImageView(titleImage);
            mainPane.getChildren().add(imageView);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(110);
            imageView.setTranslateY(20);
            imageView.setTranslateX(120);
        } catch (Exception e){
            logger.error("Image not found");
        }

        //Game over text
        var gameOver = new Text("Game Over");
        gameOver.getStyleClass().add("gameOver");

        scorePane.getChildren().add(gameOver);
        gameOver.setTranslateY(-140);

        //Local and Online scores HBox
        scoresBoxes.getChildren().addAll(localScoresBox,onlineScoresBox);
        scoresBoxes.setAlignment(Pos.CENTER);
        scoresBoxes.setTranslateY(200);

        localScoresBox.setPrefWidth(250);
        onlineScoresBox.setPrefWidth(250);

        //Load scores from file if offline
        if(offline){
            //Getting local scores
            localScoresList = new ScoresList(10,gameScore);
            localScores.bind(localScoresList.scoresList);
            localScoresList.loadScores();

            if(localScoresList.topScoreChecker()){
                getUsername();
            }
            updateList();
        } else {
            multiplayerScores();
        }

        //Receive online scores
        onlineScoresList = new ScoresList(10,gameScore);
        remoteScores.bind(onlineScoresList.scoresList);
        loadOnlineScores();

        // Listens for a message from the communicator
        communicator.addListener(
            (message) -> {
              if (message.split(" ")[0].equals("HISCORES")) {
                onlineScoresList.loadOnlineScores(message);
                if (offline && onlineScoresList.topScoreChecker()) {
                  if (!askedName) {
                    getUsername();
                  }
                  writeOnlineScores();
                }
                updateOnlineList();
              } else if (message.split(" ")[0].equals("NEWSCORE")) {
                logger.info("New Score Submitted");
              } else if (message.split(" ")[0].equals("ERROR")){
                  logger.info("fklflkajnf");
              }
            });
    }

    /**
     * Asks the player for their username
     */
    public void getUsername(){

        Platform.runLater(() -> {
            askedName = true;
            //Initialise a second pane to overlap the initial score screen to get the users name
            var namePane = new StackPane();
            namePane.setMaxWidth(gameWindow.getWidth());
            namePane.setMaxHeight(gameWindow.getHeight());
            namePane.getStyleClass().add("menu-background");
            root.getChildren().add(namePane);
            namePane.toFront();

            //Holds components of this pane
            var uiObjects = new VBox(50);

            var title = new Text("New High Score!");
            title.getStyleClass().add("bigtitle");
            var heading = new Text("Please enter your name");
            heading.getStyleClass().add("heading");
            var usernameText = new TextField();
            usernameText.setMaxWidth(gameWindow.getWidth()*0.7);
            var enterButton = new Button("Enter");

            uiObjects.setAlignment(Pos.CENTER);
            uiObjects.getChildren().addAll(title,heading,usernameText,enterButton);

            namePane.getChildren().add(uiObjects);

            //Enter button pressed
            enterButton.setOnAction(event -> {
                name = usernameText.getText();
                if(Objects.equals(name, "")){
                    name = "Player";
                }
                root.getChildren().remove(namePane);

                localScoresList.writeScores(gameScore,name);
                updateList();

            });
        });


    }

    /**
     * Loads the multiplayer game scores
     */
    public void multiplayerScores(){

    Platform.runLater(
        () -> {
          int counter = 0; //Counter to prevent more scores than wanted from being displayed
          localScoresBox.getChildren().clear();

          // Local scores VBox
          var lobbyheading = new Text("Game Scores");
          lobbyheading.getStyleClass().add("subheading");
          localScoresBox.getChildren().add(lobbyheading);
          localScoresBox.setAlignment(Pos.BASELINE_CENTER);

          for(int i=0;i<multiplayerList.getNumberOfScores();i++){
              
              counter++;

              if (counter > multiplayerList.getNumberOfScores()) break;

              var scoreBox = new HBox(); //Score box for 1 player
              scoreBox.getStyleClass().add("scorelist");
              scoreBox.setAlignment(Pos.CENTER);

              var name = new Text(multiplayerList.scoresList.get(i).getKey().split(": ")[0] + ": ");
              name.getStyleClass().add("scoreitem");
              name.setTextAlignment(TextAlignment.CENTER);
              HBox.setHgrow(name, Priority.ALWAYS);

              var points = new Text(multiplayerList.scoresList.get(i).getKey().split(": ")[1]);
              points.getStyleClass().add("points");
              points.setTextAlignment(TextAlignment.CENTER);
              HBox.setHgrow(points, Priority.ALWAYS);

              scoreBox.getChildren().addAll(name, points);

              localScoresBox.getChildren().add(scoreBox);

              fadeIn(scoreBox);

          }

          logger.info("Added lobby scores");

        });
    }

    /**
     * Updates the local scores list ui components
     */
    public void updateList(){

        Platform.runLater(
        () -> {
            int counter = 0; //Counter to prevent more scores than wanted from being displayed
            localScoresBox.getChildren().clear();

            // Local scores VBox
            var localHeading = new Text("Local scores");
            localHeading.getStyleClass().add("subheading");
            localScoresBox.getChildren().add(localHeading);
            localScoresBox.setAlignment(Pos.BASELINE_CENTER);

            // Loop each score in the list
            for (Pair<String, Integer> score : localScores) {

                counter++;

                if (counter > localScoresList.getNumberOfScores()) break;

                var scoreBox = new HBox(); //Score box for one player
                scoreBox.getStyleClass().add("scorelist");
                scoreBox.setAlignment(Pos.CENTER);

                var name = new Text(score.getKey() + ": ");
                name.getStyleClass().add("scoreitem");
                name.setTextAlignment(TextAlignment.CENTER);
                HBox.setHgrow(name, Priority.ALWAYS);

                var points = new Text(String.valueOf(score.getValue()));
                points.getStyleClass().add("points");
                points.setTextAlignment(TextAlignment.CENTER);
                HBox.setHgrow(points, Priority.ALWAYS);

                scoreBox.getChildren().addAll(name, points);

                localScoresBox.getChildren().add(scoreBox);

                fadeIn(scoreBox);
            }

            logger.info("Added local scores");

        });

    }

    /**
     * Fades in HBox
     * @param scoreBox HBox which stores the name and score
     */
    public void fadeIn(HBox scoreBox){
        FadeTransition transition;
        transition = new FadeTransition(Duration.millis(500),scoreBox);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.setInterpolator(Interpolator.EASE_IN);
        transition.play();
    }

    /**
     * Requests the online high scores from the network
     */
    public void loadOnlineScores(){
        communicator.send("HISCORES");
    }

    /**
     * Sends a new top score to the network
     */
    public void writeOnlineScores(){
        communicator.send("HISCORE " + name + ":" + gameScore);
    }

    /**
     * Updates the online scores list ui components
     */
    public void updateOnlineList(){

    Platform.runLater(
        () -> {
          int counter = 0; //Counter to prevent more scores than wanted from being displayed
          onlineScoresBox.getChildren().clear();

          // Online scores VBox
          var onlineHeading = new Text("Online Scores");
          onlineHeading.getStyleClass().add("subheading");
          onlineScoresBox.getChildren().add(onlineHeading);
          onlineScoresBox.setAlignment(Pos.BASELINE_CENTER);

          // Loop each score in the list
          for (Pair<String, Integer> score : remoteScores) {

            counter++;

            if (counter > onlineScoresList.getNumberOfScores()) break;

            var scoreBox = new HBox(); //Score box for one player
            scoreBox.getStyleClass().add("scorelist");
            scoreBox.setAlignment(Pos.CENTER);

            var name = new Text(score.getKey() + ": ");
            name.getStyleClass().add("scoreitem");
            name.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(name, Priority.ALWAYS);

            var points = new Text(String.valueOf(score.getValue()));
            points.getStyleClass().add("points");
            points.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(points, Priority.ALWAYS);

            scoreBox.getChildren().addAll(name, points);

            onlineScoresBox.getChildren().add(scoreBox);

            fadeIn(scoreBox);
          }

          logger.info("Added online scores");

        });
    }

}
