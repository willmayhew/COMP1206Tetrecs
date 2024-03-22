package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece Listener is used to listen for when the current and following game pieces have been updated.
 */
public interface NextPieceListener {

  /**
   * Handle when a piece has been placed
   * @param current Current piece
   * @param following Next piece
   */
  public void nextPiece(GamePiece current,GamePiece following);

}
