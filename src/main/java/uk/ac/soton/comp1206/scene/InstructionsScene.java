package uk.ac.soton.comp1206.scene;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Instructions screen. Provide details on how to play.
 */
public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  private Multimedia multimedia;

  /**
   * Create a new instruction scene
   * @param gameWindow The instruction window
   * @param multimedia The current music player
   */
  public InstructionsScene(GameWindow gameWindow,Multimedia multimedia){
    super(gameWindow);
    this.multimedia = multimedia;
    logger.info("Creating Instructions Scene");
  }

  /**
   * Initialise the scene
   */
  @Override
  public void initialise() {
    //Exit the instruction screen back to the menu
    gameWindow.getScene().setOnKeyPressed(event -> {
      if(event.getCode()!=KeyCode.ESCAPE)return;
      multimedia.stopMusic();
      gameWindow.startMenu();
      logger.info("Returning to menu");
    });
  }

  /**
   * Build the Instructions menu
   */
  @Override
  public void build() {

    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    var instructionPane = new StackPane();
    instructionPane.setMaxWidth(gameWindow.getWidth());
    instructionPane.setMaxHeight(gameWindow.getHeight());
    instructionPane.getStyleClass().add("menu-background");
    root.getChildren().add(instructionPane);

    var mainPane = new BorderPane();
    instructionPane.getChildren().add(mainPane);

    var pieces = new GridPane(); //Grid of grids of pieces
    var game = new Game(3,3); //Grid of piece
    PieceBoard pieceBoard;

    var title = new Text("Instructions"); //Title
    title.getStyleClass().add("heading");

    instructionPane.setAlignment(title,Pos.TOP_CENTER);

    var heading = new Text("Game Pieces"); //Subheading
    heading.getStyleClass().add("instructions");

    int count=0; //Represents the game piece to be placed

    for(int a=0;a<5;a++){ //Iterated 15 times to generate each piece
      for(int b=0;b<3;b++){
        pieceBoard = new PieceBoard(3,3,gameWindow.getWidth()/12,gameWindow.getWidth()/12, "displayPieces"); //Smaller grid containing a piece
        pieceBoard.notClickable();
        pieceBoard.displayPiece(game.spawnPiece(count));
        pieces.add(pieceBoard,a,b);
        count++;
      }
    }

    pieces.setHgap(5);
    pieces.setVgap(5);
    pieces.setPadding(new Insets(0,10,10,10));
    pieces.setAlignment(Pos.BOTTOM_CENTER);

    var instructions = new VBox(2);
    instructions.getChildren().add(title);
    instructionPane.getChildren().add(instructions);


    try{ //Image of instructions
      var image = new Image(String.valueOf(getClass().getResource("/images/Instructions.png")));
      var imageView = new ImageView(image);
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(500);
      instructions.setAlignment(Pos.TOP_CENTER);
      instructions.getChildren().add(imageView);
    } catch(Exception e){
      logger.error("Instructions image not found");
    }

    instructions.getChildren().addAll(heading,pieces);

  }
}
