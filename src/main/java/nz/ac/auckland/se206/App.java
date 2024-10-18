package nz.ac.auckland.se206;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.controllers.CrimeController;
import nz.ac.auckland.se206.controllers.GuessingController;
import nz.ac.auckland.se206.controllers.ResultsController;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {
  private static final String introTextToSpeechSource = "/sounds/interact.wav";
  private static final int fadeDelay = 3000;
  private static final int fadeInterval = 25;
  private static final int gameTime = 300;
  private static final int guessTime = 60;
  private static final Timer timer = new Timer();
  private static final Map<String, FXMLLoader> loaders =
      new HashMap<>() {
        {
          String[] keys = {"menu", "crime", "suspect", "guessing", "results"};
          for (String key : keys) {
            put(key, loadFxml(key));
          }
        }
      };
  private static final Map<String, Parent> scenes =
      new HashMap<String, Parent>() {
        {
          try {
            for (String key : loaders.keySet()) {
              put(key, loaders.get(key).load());
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      };

  private static Scene scene;

  private static Boolean guessingMode = false;

  private static Boolean audioMuted = false;

  /**
   * Loads the FXML file and returns the associated node. The method expects that the file is
   * located in "src/main/resources/fxml".
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the root node of the FXML file (or null if the file is not found)
   */
  private static FXMLLoader loadFxml(final String fxml) {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
  }

  /**
   * Returns the status of audio muting.
   *
   * @return the status of whether the audio is muted or not
   */
  public static Boolean isAudioMuted() {
    return audioMuted;
  }

  /** Update the volume muting status to mute. */
  public static void muteAudio() {
    audioMuted = true;
  }

  /** Update the volume muting status to unmuted. */
  public static void unmuteAudio() {
    audioMuted = false;
  }

  /**
   * Returns the loader associated with the specified FXML file.
   *
   * @param loader the name of the FXML file (without extension)
   * @return the loader
   */
  public static FXMLLoader getLoader(String loader) {
    return App.loaders.get(loader);
  }

  /**
   * Returns the timer associated with the application.
   *
   * @return the timer
   */
  public static Timer getTimer() {
    return App.timer;
  }

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Sets the root of the scene to the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setScene(String fxml) throws IOException {
    // Start game
    if (App.timer.getTime() == 0 && fxml.equals("crime")) {
      CrimeController crimeController = (CrimeController) App.loaders.get("crime").getController();
      crimeController.reset();

      GuessingController guessingController =
          (GuessingController) App.loaders.get("guessing").getController();
      guessingController.resetInfo();

      ResultsController resultsController =
          (ResultsController) App.loaders.get("results").getController();
      resultsController.setSuspect(null, "");

      App.timer.setTime(App.gameTime);
      crimeController
          .transitionIntro()
          .setOnFinished(
              event -> {
                App.timer.initialize(App.gameTime);
                crimeController.showInstruction();

                // check if audio is muted first
                if (!audioMuted) {
                  // Play Audio here (Chat with the 3 suspects and find out
                  // who stole the painting)
                  try {
                    new AudioClip(
                            (new Media(
                                    App.class
                                        .getResource(App.introTextToSpeechSource)
                                        .toURI()
                                        .toString()))
                                .getSource())
                        .play();
                  } catch (URISyntaxException e) {
                    e.printStackTrace();
                  }
                }
              });

      // Reset guess timer
    } else if (fxml.equals("guessing")) {
      App.guessingMode = true;

      App.timer.initialize(App.guessTime);

      // End game
    } else if (fxml.equals("results")) {
      App.timer.initialize(0);
    }

    App.scene.setRoot(App.scenes.get(fxml));
  }

  /**
   * Gradually fades out the specified element. Returns the timeline of the fade animation.
   *
   * @return the scene
   */
  public static Timeline fadeAnimation(Node node) {
    // set opacity to 1 (full) to begin with
    node.setOpacity(1);

    // create new timeline
    Timeline finalTimeline =
        new Timeline(
            new KeyFrame(
                Duration.millis(App.fadeInterval),
                e2 -> {
                  // gradually fade out
                  node.setOpacity(node.getOpacity() - 0.02);
                }));
    Timeline labelTimeline =
        new Timeline(
            new KeyFrame(
                Duration.millis(fadeDelay),
                e -> {
                  // refresh every 0.1s (100ms)
                  finalTimeline.setCycleCount(50);
                  // update
                  finalTimeline.play();
                }));
    // play animation
    labelTimeline.play();

    return finalTimeline;
  }

  private final String title = "The Stolen Painting";
  private final String iconSource = "images/main_menu_in-scene_icon.png";

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    // Set game timer actions
    App.timer.setTimeline(
        new KeyFrame(
            Duration.seconds(1),
            e -> {
              App.timer.startCountdown();
              if (App.timer.getTime() == 0) {
                CrimeController crimeController =
                    (CrimeController) App.loaders.get("crime").getController();

                String current = "guessing";
                if (!App.guessingMode && crimeController.checkInteracted()) {
                  App.guessingMode = true;

                  App.timer.initialize(App.guessTime);
                } else {
                  current = "results";

                  App.guessingMode = false;

                  GuessingController guessingController =
                      (GuessingController) App.loaders.get("guessing").getController();
                  try {
                    guessingController.enterGuess();
                  } catch (IOException err) {
                    err.printStackTrace();
                  }
                }
                try {
                  App.setScene(current);
                } catch (IOException err) {
                  err.printStackTrace();
                }
              }
            }));

    // Set scene
    App.scene = new Scene(App.scenes.get("menu"));
    App.scene.getRoot().requestFocus();

    // Settings
    stage.setScene(App.scene);
    stage.setResizable(false);
    stage.setTitle(this.title);
    stage.getIcons().add(new Image(this.iconSource));
    stage.show();
  }
}
