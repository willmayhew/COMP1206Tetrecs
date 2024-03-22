package uk.ac.soton.comp1206.event;

/**
 * The Game Loop Listener is used to listen for when a loop of the game has started
 */
public interface GameLoopListener {

    /**
     * Handle what happens after a loop has begun
     */
    public void gameLoop();

}
