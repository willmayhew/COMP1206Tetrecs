package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Piece Board is used to display the smaller grids containing a singular piece.
 */
public class PieceBoard extends GameBoard{

  private static final Logger logger = LogManager.getLogger(PieceBoard.class);

  /**
   * Create a new Piece Board object
   * @param cols Number of Columns
   * @param rows Number of Rows
   * @param width Width of Grid
   * @param height Height of Grid
   * @param name Name of Grid
   */
  public PieceBoard(int cols, int rows, double width, double height, String name) {
    super(cols, rows, width, height, name);
  }

  /**
   * Displays the given piece on the smaller grid
   * @param gamePiece piece to be played next
   */
  public void displayPiece(GamePiece gamePiece){

    for (int x=0;x<3;x++){
      for (int y=0;y<3;y++){
        grid.set(x,y,0);
        if(gamePiece.getBlocks()[y][x] > 0){
          grid.set(x,y,gamePiece.getValue());
        }
      }
    }

  }

  /**
   * Make the board not clickable
   */
  public void notClickable(){
    this.mouseTransparentProperty().setValue(true);
  }

  /**
   * Sets a circle in the centre of the board
   */
  public void setCircle(){
    this.blocks[1][1].setCircle();
  }


}
