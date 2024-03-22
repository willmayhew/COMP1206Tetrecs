package uk.ac.soton.comp1206.component;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.HashSet;
import java.util.List;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The name of the board
     */
    private final String name;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;

    /**
     * The listener to call when right mouse button is pressed
     */
    private RightClickedListener rightClickedListener;

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     * @param name Name of the grid
     */
    public GameBoard(Grid grid, double width, double height, String name) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.name = name;
        this.grid = grid;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     * @param name the name of the board
     */
    public GameBoard(int cols, int rows, double width, double height, String name) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.name = name;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     * @return Game block
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Mouse click handler to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        //Mouse hover handler to trigger hoverBlock method
        block.setOnMouseEntered((e) -> hoverBlock(e,block));
        block.setOnMouseExited((e) -> hoverBlock(e,block));

        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Set the listener to handle an event when right mouse button is clicked
     * @param listener listener to add
     */
    public void setOnRightClicked(RightClickedListener listener){
        this.rightClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * Calls block rotate if there is a right click
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if(blockClickedListener != null && event.getButton() != MouseButton.SECONDARY) {
            blockClickedListener.blockClicked(block);
        }

        if(rightClickedListener != null) {
            if(event.getButton() == MouseButton.PRIMARY && this instanceof PieceBoard){ //Current piece grid
                logger.info("Left Click");
                rightClickedListener.rightClicked(name);
            }
            else if(event.getButton() == MouseButton.SECONDARY && cols > 3){ //Main grid
                logger.info("Right Click");
                rightClickedListener.rightClicked(name);
            }
        }

    }

    /**
     * Adds or removes the hover effect on the current block
     * @param event Mouse event
     * @param block Block being hovered
     */
    public void hoverBlock(MouseEvent event, GameBlock block){

        if(!(this instanceof PieceBoard)){ //Check if main grid
            if(event.getEventType() == MouseEvent.MOUSE_ENTERED){
                block.hover();
            }
            else if(event.getEventType() == MouseEvent.MOUSE_EXITED){
                block.paint();
            }
        }

    }

    /**
     * Fades out a set of blocks at the given coordinates
     * @param blockCoords Hash set of block coordinates
     */
    public void fadeOut(HashSet<GameBlockCoordinate> blockCoords){
        
        for (GameBlockCoordinate coords : blockCoords) {
            blocks[coords.getX()][coords.getY()].fadeOut();
        }
        
    }

}
