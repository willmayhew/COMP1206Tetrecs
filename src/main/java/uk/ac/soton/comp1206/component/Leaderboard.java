package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 * Stores the in-game leaderboard from a multiplayer game.
 */
public class Leaderboard extends ScoresList{

    /**
     * Observable list which contains the in-game players name, score and lives
     */
    public ObservableList<Pair<String,String>> scores = FXCollections.observableArrayList();

    /**
     * List containing the in-game players name, score and lives
     */
    public SimpleListProperty<Pair<String,String>> scoresList = new SimpleListProperty<>(scores);

    /**
     * Create a new scores list with a set number of scores
     * @param numberOfScores Number of scores to store
     */
    public Leaderboard(Integer numberOfScores) {
        super(numberOfScores);
    }

    /**
     * Updates the scores list with the most up-to-date information.
     * @param playerInfo String of information containing player names, score and life (number of lives or dead).
     */
    public void updateScores(String playerInfo){

        String[] temp = playerInfo.split("\n"); //Array containing each player's information

        scoresList.clear();

        //Stores each player's data in a pair format (Name:Score,Lives) in the scoresList
        for(int i=0;i<getNumberOfScores();i++){
            String[] player = temp[i].split(":");
            scoresList.add(new Pair<>(player[0] + ": " + player[1],player[2]));
        }

        sort();

    }

    /**
     * Sorts the list in ascending order
     */
    @Override
    public void sort(){
        scoresList.sort((o1, o2) -> {
            var player1 = Integer.parseInt(o1.getKey().split(": ")[1]);
            var player2 = Integer.parseInt(o2.getKey().split(": ")[1]);
            return Integer.compare(player2, player1);
        });
    }

}
