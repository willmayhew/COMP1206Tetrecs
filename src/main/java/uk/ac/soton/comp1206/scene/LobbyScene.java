package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The lobby scene. Holds the UI for the different channels and lobbies from the server.
 */
public class LobbyScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    private Communicator communicator = this.gameWindow.getCommunicator();

    /**
     * Holds the state of the channel name input box
     */
    private boolean startChannelBool = false;

    /**
     * User in a channel
     */
    private boolean inLobby = false;

    /**
     * Holds the list of buttons representing the different channels
     */
    private VBox channelsBox = new VBox();

    /**
     * Text field where the user inputs the channel name
     */
    private TextField channelName;

    /**
     * Lobby window on the side
     */
    private StackPane lobby = new StackPane();

    /**
     * Contains the contents of the lobby window
     */
    private VBox lobbyContents = new VBox();

    /**
     * The message outputs
     */
    private TextFlow textChatOutput;

    /**
     * The message input
     */
    private TextField textChatInput = new TextField();

    /**
     * Text containing the different players in the lobby
     */
    private Text currentPlayers;

    /**
     * HBox holding the buttons for starting and leaving a game
     */
    private HBox startLeaveButtons = new HBox();

    /**
     * Button to leave lobby
     */
    private Button leaveLobby;

    /**
     * Users current channel
     */
    private Text currentChannel = new Text();

    /**
     * Game timer
     */
    private Timer timer;

    /**
     * Number of current players
     */
    private int playerCount;

    /**
     * Scroll pane window
     */
    private ScrollPane scroller;

    /**
     * Music player
     */
    private Multimedia multimedia;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     * @param multimedia The current music player
     */
    public LobbyScene(GameWindow gameWindow, Multimedia multimedia) {
        super(gameWindow);
        this.multimedia = multimedia;
    }

    /**
     * Initialise the lobby
     */
    @Override
    public void initialise() {

        //Exit the instruction screen back to the menu
        gameWindow.getScene().setOnKeyPressed(event -> {

            if(event.getCode() == KeyCode.ESCAPE){
                if(inLobby){
                    communicator.send("PART");
                }
                multimedia.stopMusic();
                gameWindow.startMenu();
                logger.info("Returning to menu");
            } else if(event.getCode() == KeyCode.ENTER && startChannelBool){
                if(!channelName.getText().trim().isEmpty()){
                    startChannelBool = false;
                    createChannel(channelName.getText());
                    logger.info("Creating Channel");
                }
            } else if(event.getCode() == KeyCode.ENTER && !startChannelBool){
                sendCurrentMessage(textChatInput.getText());
                textChatInput.setText("");
            }

        });

        //Requesting updated list of channels
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                communicator.send("LIST");
            }
        },0,3000);

        communicator.addListener((message) -> {

            String[] messageSplit = message.split(" "); //Message from communicator is seperated. The identifier and the actual message contents

            if(messageSplit[0].equals("CHANNELS")){
                checkLobby(message);
            } else if(messageSplit[0].equals("JOIN")){
                inLobby = true;
                currentChannel.setText("Current Channel: " + messageSplit[1]);
                Platform.runLater(() -> startLeaveButtons.getChildren().add(leaveLobby));
            } else if(messageSplit[0].equals("USERS")){
                lobbyPlayers(message);
            } else if(messageSplit[0].equals("MSG")){
                receiveMessage(message.substring(4));
            } else if(messageSplit[0].equals("HOST")){
                hostButton();
            } else if(messageSplit[0].equals("START")){
                startGame();
            }
        });

    }

    /**
     * Build the lobby screen
     */
    @Override
    public void build() {

        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);

        var mainPane = new BorderPane();
        lobbyPane.getChildren().addAll(mainPane,channelsBox,lobby);

        //Current channel which the player is in
        lobby.setVisible(false);
        lobby.setMaxSize(450,400);
        lobby.getStyleClass().add("playerBox");
        lobbyPane.setAlignment(lobby,Pos.CENTER_RIGHT);

        //Lobby chat output box
        textChatOutput = new TextFlow();
        textChatOutput.getStyleClass().add("playerBox");
        textChatOutput.setPrefHeight(269); //270 for scroller
        defaultChatText();

        //Scroll bar for text chat
        scroller = new ScrollPane(textChatOutput);
        scroller.setFitToWidth(true);
        scroller.vvalueProperty().bind(textChatOutput.heightProperty());
        scroller.setPrefHeight(500);

        //Lobby chat Input box
        textChatInput = new TextField();
        textChatInput.setMinHeight(40);
        textChatInput.setPrefWidth(350);

        //Send message button
        var sendMessage = new Button("Send");
        sendMessage.getStyleClass().add("channelButton");

        //Bar containing the text input and send button
        var sendMessageBar = new HBox();
        sendMessageBar.getChildren().addAll(textChatInput,sendMessage);

        sendMessage.setOnAction(event -> {
            sendCurrentMessage(textChatInput.getText());
            textChatInput.setText("");
        });

        //Leave lobby button
        leaveLobby = new Button("Leave Game");
        leaveLobby.getStyleClass().add("channelButton");

        leaveLobby.setOnAction(event -> {
            startLeaveButtons.getChildren().clear();
            inLobby=false;
            communicator.send("PART");
            lobby.setVisible(false);
            defaultChatText();
        });

        //title header
        var title = new Text("Lobby");
        title.getStyleClass().add("heading");
        lobbyPane.getChildren().add(title);
        lobbyPane.setAlignment(title,Pos.TOP_CENTER);

        //Create new game button and field
        var startChannel = new Button();
        startChannel.getStyleClass().clear();
        var startChannelText = new Text("Create new game");
        startChannelText.getStyleClass().add("subheading");
        startChannel.setGraphic(startChannelText);
        startChannel.setTranslateX(-260);
        startChannel.setTranslateY(-200);

        channelName = new TextField();
        channelName.setPromptText("Channel Name");
        channelName.setMaxWidth(300);
        channelName.setTranslateX(-220);
        channelName.setTranslateY(-165);
        channelName.setVisible(false);
        lobbyPane.getChildren().addAll(startChannel,channelName);

        //Start channel button action event
        startChannel.setOnAction(event -> {
            if(startChannelBool==true){
                channelName.setVisible(false);
                startChannelBool=false;
            } else{
                channelName.setVisible(true);
                startChannelBool=true;
            }
        });

        //Channel box alignments
        var channelsLab = new Text("Available channels");
        channelsLab.getStyleClass().add("subheading");
        lobbyPane.getChildren().add(channelsLab);
        channelsLab.setTranslateX(-250);
        channelsLab.setTranslateY(-120);
        channelsBox.setTranslateY(200);
        channelsBox.setTranslateX(30);

        //Creates a border between each component
        var sep = new Separator(Orientation.HORIZONTAL);
        var sep2 = new Separator(Orientation.HORIZONTAL);
        var sep3 = new Separator(Orientation.HORIZONTAL);
        var sep4 = new Separator(Orientation.HORIZONTAL);

        lobbyContents.getChildren().addAll(currentChannel,sep,textChatOutput,sep2,scroller,sep3,sendMessageBar,sep4,startLeaveButtons);
        lobbyContents.getStyleClass().add("border");

        lobby.getChildren().add(lobbyContents);

    }

    /**
     * Check every interval for channels open to join
     * @param message String of channels
     */
    public void checkLobby(String message){

        Platform.runLater(() -> {

            channelsBox.getChildren().clear();
            try{
                String[] channels = message.split(" ")[1].split("\n");
                ArrayList<Button> buttons = new ArrayList<>();

                for(int i=0;i<channels.length;i++){
                    Button button = new Button(channels[i]);
                    button.getStyleClass().add("channelButton");
                    buttons.add(button);
                    button.setOnAction(event -> {
                        communicator.send("JOIN " + button.getText());
                        communicator.send("USERS");
                    });
                }
                channelsBox.getChildren().addAll(buttons);
            } catch (Exception ignored){
                logger.info("No channels");
            }

        });

    }

    /**
     * Tells the server to create a new channel.
     * @param channel String of available channels
     */
    public void createChannel(String channel){

        communicator.send("CREATE " + channel);
        Platform.runLater(() -> channelName.setVisible(false));
    }

    /**
     * Formats the current players in the lobby
     * @param users String of users in the lobby
     */
    public void lobbyPlayers(String users){
        Platform.runLater(() -> {

            lobbyContents.getChildren().remove(currentPlayers);

            String[] players = users.split(" ")[1].split("\n");
            currentPlayers = new Text("Players: ");
            currentPlayers.setWrappingWidth(430);

            lobby.setVisible(true);

            for (int i = 0; i < players.length; i++) {
                currentPlayers.setText(currentPlayers.getText() + " " + players[i]);
            }

            lobbyContents.getChildren().add(1,currentPlayers);

            playerCount = players.length;

        });
    }

    /**
     * Sends a message from the user to the lobby chat
     * @param message Send message
     */
    public void sendCurrentMessage(String message){
        if(!message.equals("")){
            if(message.split(" ")[0].equals("/nick")){
                communicator.send("NICK " + message.substring(6));
                communicator.send("USERS");
            } else {
                communicator.send("MSG " + message);
            }

        }
    }

    /**
     * Receives message from the server and adds it to the chat box
     * @param message Received message
     */
    public void receiveMessage(String message){
        Platform.runLater(() -> {
            textChatOutput.getChildren().add(new Text(message + "\n"));
        });

        multimedia.playAudio("sounds/message.wav");

    }

    /**
     * The default information is loaded into the chat box
     */
    public void defaultChatText(){
        Platform.runLater(() -> {
            textChatInput.clear();
            textChatOutput.getChildren().clear();
            textChatOutput.getChildren().add(new Text("Welcome to the lobby \nType /nick <NewName> to change your nickname \n"));
        });
    }

    /**
     * Adds the start game button if the user is the host of the lobby
     */
    public void hostButton(){
        Platform.runLater(() -> {

            var startButton = new Button("Start Game");
            startButton.getStyleClass().add("channelButton");
           startLeaveButtons.getChildren().add(0,startButton);

            startButton.setOnAction(event -> {
                communicator.send("START");
            });

        });
    }

    /**
     * Starts the multiplayer game
     */
    public void startGame(){
        Platform.runLater(() -> {
            timer.cancel();
            multimedia.stopMusic();
            gameWindow.startMultiplayer(communicator,playerCount);
        });

    }

}
