package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * The scores list is used to store the list of local or online scores
 */
public class ScoresList {

    private static final Logger logger = LogManager.getLogger(ScoresList.class);

    /**
     * Observable list which contains pairs of names and scores
     */
    public ObservableList<Pair<String,Integer>> scores = FXCollections.observableArrayList();

    /**
     * List containing pairs of name and score
     */
    public SimpleListProperty<Pair<String,Integer>> scoresList = new SimpleListProperty<>(scores);

    private Integer numberOfScores;
    private Integer gameScore;

    /**
     * Create a new scores list with a set number of scores
     * @param numberOfScores Number of scores to store
     * @param gameScore Player's game score
     */
    public ScoresList(Integer numberOfScores,Integer gameScore){
        this.numberOfScores = numberOfScores;
        this.gameScore = gameScore;
    }

    /**
     * Create a new scores list with a set number of scores
     * @param numberOfScores Number of scores to score
     */
    public ScoresList(Integer numberOfScores){
        this.numberOfScores = numberOfScores;
    }

    /**
     * Read the scores file and input the scores into the list
     * If the file is not found, a default scores list is loaded
     */
    public void loadScores(){

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/scores.txt")));
            String line;

            while((line = reader.readLine()) != null){
                scoresList.add(new Pair<>(line.split(":")[0],Integer.parseInt(line.split(":")[1])));
            }
            logger.info("File Found");
            reader.close();

        } catch(Exception e){
            logger.error("File not found");
            loadDefaultScores();
        }

        sort();

    }

    /**
     * If the users score is a new local top score, it will be written into the file
     * @param gameScore Player's game score
     * @param name Player's user name
     */
    public void writeScores(Integer gameScore, String name){

        try{
            PrintStream writer = new PrintStream("src/main/resources/scores.txt");
            boolean inserted = false;

            //Check if game score is larger than any of the current scores in the file
            for(int i = 0; i< scoresList.getSize(); i++){
                if(gameScore> scoresList.get(i).getValue()){
                    scoresList.add(i,new Pair<>(name,gameScore));
                    inserted = true;
                    break;
                }
            }

            //Inserts score in the last index if the score is yet to be inserted or the list is not full
            if(!inserted && scoresList.getSize() < numberOfScores){
                scoresList.add(scoresList.getSize(),new Pair<>(name,gameScore));
                inserted = true;
            }

            //Ensures that the desired number of scores is displayed
            if(scoresList.getSize() == numberOfScores+1){
                scoresList.remove(numberOfScores);
            }

            //If a new score has been entered in the list, rewrite the scores file with the new scores
            if(inserted){
                for(int i = 0; i<Math.min(scoresList.getSize(),numberOfScores); i++){
                    writer.println(scoresList.get(i).getKey() + ":" + scoresList.get(i).getValue());
                }
            }

            writer.close();

        } catch (IOException e){
            logger.error("File not found");
            e.printStackTrace();
        }

    }

    /**
     * Checks if the user's score is higher than any in the list
     * @return top score or not
     */
    public boolean topScoreChecker(){

        for(int i = 0; i < scoresList.getSize(); i++){
            if(gameScore > scoresList.get(i).getValue()){
                return true;
            }
        }

        return scoresList.getSize() < 10;

    }

    /**
     * Loads a default set of scores into the list
     */
    private void loadDefaultScores(){

        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));
        scoresList.add(new Pair<>("Will",10000));

        logger.info("Default scores loaded");

    }

    /**
     * Gets the number of scores which are being stored
     * @return Number of scores
     */
    public Integer getNumberOfScores(){
        return numberOfScores;
    }

    /**
     * Formats and adds the online scores from the message into the list
     * @param message String of scores
     */
    public void loadOnlineScores(String message){

        String[] scores = message.split(" ")[1].split("\\n"); //Array of pairs of each high scores name and score

        for (String score : scores) {
            scoresList.add(new Pair<>(score.split(":")[0], Integer.parseInt(score.split(":")[1])));
        }

        sort();

    }

    /**
     * Sorts the list in ascending order
     */
    public void sort(){
        scoresList.sort((o1, o2) -> {
            if(o1.getValue()>o2.getValue()){
                return -1;
            } else if (o1.getValue().equals(o2.getValue())){
                return 0;
            } else{
                return 1;
            }
        });
    }

}
