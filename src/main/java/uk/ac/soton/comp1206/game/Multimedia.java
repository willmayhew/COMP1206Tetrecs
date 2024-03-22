package uk.ac.soton.comp1206.game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The Multimedia class is used to play a sound which either loops or plays a singular time. */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  private static MediaPlayer audio, music;

  /**
   * Plays an audio file a single time
   *
   * @param audioPath FIle path of the sound
   */
  public void playAudio(String audioPath) {

    try {
      String play = Multimedia.class.getResource("/" + audioPath).toExternalForm();
      Media media = new Media(play);
      audio = new MediaPlayer(media);
      audio.setVolume(0.3);
      audio.play();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Audio file not found");
    }
  }

  /**
   * Plays a music file which is then looped when finished
   *
   * @param musicPath File path of the sound
   */
  public void playMusic(String musicPath) {

    try {
      String play = Multimedia.class.getResource("/" + musicPath).toExternalForm();
      Media media = new Media(play);
      music = new MediaPlayer(media);

      // When the song reaches the end, it is set back to 0 and restarted
      music.setOnEndOfMedia(() -> music.seek(Duration.ZERO));
      music.play();
      music.setVolume(0.3);
      logger.info("Playing {}", musicPath);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Audio file not found");
    }
  }

  /** Stops any currently playing music */
  public void stopMusic() {
    music.stop();
    logger.info("Stopping music");
  }
}
