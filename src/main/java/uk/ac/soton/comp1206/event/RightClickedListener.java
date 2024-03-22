package uk.ac.soton.comp1206.event;

/**
 * The Right Clicked Listener is used for listening when the user has either right or left clicked.
 */
public interface RightClickedListener {

  /**
   * Handle a right or left click from the user
   * @param name The name of the board being clicked
   */
  public void rightClicked(String name);

}
