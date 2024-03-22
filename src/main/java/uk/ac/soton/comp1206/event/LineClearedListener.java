package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * The Line Cleared Listener is used to listen for when row/column(s) have been cleared
 */
public interface LineClearedListener {

    /**
     * Handles the fading of the game blocks after being cleared
     * @param gameBlockCoordinates Coordinates of blocks which have been cleared
     */
    public void fadeOut(HashSet<GameBlockCoordinate> gameBlockCoordinates);

}
