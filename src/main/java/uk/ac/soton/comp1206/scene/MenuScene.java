package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.io.FileInputStream;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Multimedia multimedia;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        multimedia = new Multimedia();

        try{
            var titleImage = new Image(getClass().getResource("/images/tetrECS.png").toString()); //Title image
            var imageView = new ImageView(titleImage);
            mainPane.getChildren().add(imageView);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(110);
            imageView.setTranslateY(80);
            imageView.setTranslateX(120);

            var rotate = new RotateTransition(Duration.millis(2000)); //Rotation animation
            rotate.setNode(imageView);
            rotate.setCycleCount(Animation.INDEFINITE);
            rotate.setFromAngle(-5);
            rotate.setToAngle(5);
            rotate.setAutoReverse(true);
            rotate.play();
        }
        catch(Exception e){
            logger.error("Title image not found");
        }

        //Start Button
        var buttonStart = new Button();
        var start = new Text("Challenge Mode");
        buttonStart.setGraphic(start);
        start.getStyleClass().add("button");

        //Instruction Button
        var buttonInstruction = new Button();
        var instruction = new Text("How to play");
        buttonInstruction.setGraphic(instruction);
        instruction.getStyleClass().add("button");

        //Multiplayer Button
        var buttonLobby = new Button();
        var lobby = new Text("Multiplayer");
        buttonLobby.setGraphic(lobby);
        lobby.getStyleClass().add("button");

        //Exit Button
        var buttonExit = new Button();
        var exit = new Text("Exit");
        buttonExit.setGraphic(exit);
        exit.getStyleClass().add("button");

         // Adds Buttons to menu
        var buttonVBox = new VBox();
        mainPane.setCenter(buttonVBox);
        buttonVBox.setAlignment(Pos.CENTER);
        buttonVBox.setTranslateY(80);
        VBox.setVgrow(buttonVBox,Priority.NEVER);
        buttonVBox.getChildren().addAll(buttonStart,buttonLobby,buttonInstruction,buttonExit);

        //Bind the button action to the startGame method in the menu
        buttonStart.setOnAction(this::startGame);
        buttonLobby.setOnAction(this::openLobby);
        buttonInstruction.setOnAction(this::openInstructions);
        buttonExit.setOnAction(event -> System.exit(0));

        //Play menu music
        multimedia.playMusic("music/menu.mp3");

        //When mouse is hovering over the button
        buttonStart.setOnMouseEntered(event -> {
            buttonHover(start);
            buttonBounce(buttonStart);
        });
        buttonLobby.setOnMouseEntered(event -> {
            buttonHover(lobby);
            buttonBounce(buttonLobby);
        });
        buttonInstruction.setOnMouseEntered(event -> {
            buttonHover(instruction);
            buttonBounce(buttonInstruction);
        });
        buttonExit.setOnMouseEntered(event -> {
            buttonHover(exit);
            buttonBounce(buttonExit);
        });

    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        multimedia.stopMusic();
        gameWindow.startChallenge();
    }

    /**
     * Handle when the Multiplayer button is pressed
     * @param event event
     */
    private void openLobby(ActionEvent event){
        gameWindow.startLobby(multimedia);
    }

    /**
     * Handle when the Instructions button is pressed
     * @param event event
     */
    private void openInstructions(ActionEvent event) {
        gameWindow.startInstruction(multimedia);
    }

    /**
     * Appends arrow to the button text
     * @param button button which is being hovered
     */
    public void buttonHover(Text button){
        multimedia.playAudio("sounds/hover.mp3");

        var text = button.getText();
        text = text.replace("➤","");

        button.textProperty().bind(
            Bindings.when(button.hoverProperty())
                .then("➤" + text)
                .otherwise(text));

    }

    /**
     * Creates a bouncing effect
     * @param button button which is being hovered
     */
    public void buttonBounce(Button button){

        var bounce = new TranslateTransition(Duration.millis(500));

        bounce.setNode(button);
        bounce.setFromY(0);
        bounce.setToY(-15);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.setInterpolator(Interpolator.EASE_BOTH);
        bounce.play();

        button.setOnMouseExited(event -> {
            bounce.stop();
            button.setTranslateY(0);
        });

    }

}
