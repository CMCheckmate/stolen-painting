package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ChatManager;

/**
 * Controller for the results scene.
 *
 * <p>This scene displays the results of the user's guess.
 */
public class ResultsController {
  private final String thief = "suspectOne";
  private final String hoverClickAudioSource = "/sounds/hover_click.wav";
  private final double hoverOpacity = 0.5;

  @FXML private ImageView guessedImage;

  @FXML private Label resultLabel;

  @FXML private TextArea explanationArea;

  private ChatManager feedbackManager;

  private AudioClip hoverClickPlayer;

  /** Initialize the controller. Load custom font and audio player. */
  @FXML
  private void initialize() {
    // create new chat manager
    this.feedbackManager = new ChatManager(this.explanationArea);

    try {
      // create audio player
      this.hoverClickPlayer =
          new AudioClip(
              // get hover audio file
              (new Media(App.class.getResource(this.hoverClickAudioSource).toURI().toString()))
                  .getSource());
    } catch (URISyntaxException e) {
      // throw error if there's an issue getting audio file
      e.printStackTrace();
    }
  }

  /**
   * Return to the menu scene.
   *
   * @throws IOException If there is an error loading the menu scene.
   */
  @FXML
  private void onReturnToMenu() throws IOException {
    App.setScene("menu");
  }

  /**
   * Restart the crime scene. Reset the game.
   *
   * @throws IOException If there is an error loading the crime scene.
   */
  @FXML
  private void onRestart() throws IOException {
    App.setScene("crime");
  }

  /**
   * Handle the hover event for the text buttons.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onTextButtonHover(MouseEvent event) {
    Node source = (Node) event.getSource();

    if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {

      source.setOpacity(this.hoverOpacity);

      // check if audio is muted
      if (!App.isAudioMuted()) {
        // play hover effect sound
        this.hoverClickPlayer.play();
      }

    } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {

      source.setOpacity(1);
    }
  }

  /**
   * Set the suspect image and feedback message.
   *
   * @param suspect The suspect that the user guessed.
   * @param explanation The feedback message.
   */
  public void setSuspect(String suspect, String explanation) {
    // create suspect controller instance
    SuspectController suspectController =
        (SuspectController) App.getLoader("suspect").getController();
    // load suspect image
    this.guessedImage.setImage(suspectController.getSuspectImage(suspect, "profile"));

    StringBuilder sb = new StringBuilder();

    // if no suspect is guessed in time, return a message saying it wasn't found in time
    if (suspect == null) {
      sb.append("NOT found in time!");

      this.feedbackManager.clearChat();
    } else {
      if (!suspect.equals(this.thief)) {
        // prepend the feedback message with a negation if the user incorrectly guesses the suspect
        sb.append("NOT ");

        this.explanationArea.setText("Sorry, you have guessed the wrong suspect!");
      } else {
        // lowercase/uppercase the 'feedback' keyword
        this.feedbackManager.setPromptMap("feedback", explanation);
        System.out.println(explanation);
        this.feedbackManager.setSuspect("Feedback");
      }
      sb.append("the ");
      sb.append(suspectController.getSuspectName(suspect));
    }
    this.resultLabel.setText(sb.toString());
  }
}
