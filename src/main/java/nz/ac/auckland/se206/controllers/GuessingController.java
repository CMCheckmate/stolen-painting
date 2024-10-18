package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;

/**
 * Controller for the guessing scene.
 *
 * <p>This scene allows the user to select a suspect and provide an explanation for their choice.
 */
public class GuessingController {

  private final String hoverClickAudioSource = "/sounds/hover_click.wav";
  private final double hoverOpacity = 0.5;

  @FXML private Label timerLabel;

  @FXML private Label confirmRequirementLabel;

  @FXML private GridPane navGrid;

  @FXML private TextArea guessInput;

  @FXML private Rectangle suspectRect;

  private AudioClip hoverClickPlayer;

  /** Initialize the controller. Load custom font and audio player. */
  @FXML
  private void initialize() {

    try {
      // create audio player instance
      this.hoverClickPlayer =
          new AudioClip(
              // get hover audio file source
              (new Media(App.class.getResource(this.hoverClickAudioSource).toURI().toString()))
                  .getSource());
    } catch (URISyntaxException e) {
      // throw an error if there's an error retrieving the audio file
      e.printStackTrace();
    }

    SimpleIntegerProperty timerProperty = App.getTimer().getTimerProperty();
    // set up timer label
    this.timerLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  // format the syntax of the timer
                  int totalSeconds = timerProperty.get();
                  int minutes = totalSeconds / 60;
                  int seconds = totalSeconds % 60;
                  return String.format("%02d:%02d", minutes, seconds);
                },
                timerProperty));
  }

  /**
   * Handle the hover event for the suspect rectangles.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onSuspectHover(MouseEvent event) {
    Node source = (Node) event.getSource();
    if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
      source.setOpacity(1);

      // check if audio is muted first
      if (!App.isAudioMuted()) {
        // play hover effect sound
        this.hoverClickPlayer.play();
      }

    } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)
        && !(source.equals(this.suspectRect))) {
      source.setOpacity(0);
    }
  }

  /**
   * Handle the hover event for the buttons.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onButtonHover(MouseEvent event) {
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
   * Handle the click event for the suspect rectangles.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onSelectSuspect(MouseEvent event) {
    if (this.suspectRect != null) {
      this.suspectRect.setOpacity(0);
    }
    this.suspectRect = (Rectangle) event.getSource();
    this.suspectRect.setOpacity(1);
  }

  /**
   * Handle the click event for the 'Guess' button.
   *
   * @throws IOException If there is an error loading the crime scene.
   */
  @FXML
  private void onGuess() throws IOException {
    this.enterGuess();
  }

  /**
   * Handle the click event for the 'Guess' button.
   *
   * @throws IOException If there is an error loading the crime scene.
   */
  public void enterGuess() throws IOException {
    // get the explanation text input
    String guessExplanation = this.guessInput.getText();

    // if a suspect has been chosen and the explanation is not blank, check it
    if (this.suspectRect != null && !guessExplanation.isEmpty()) {
      this.suspectRect.setOpacity(0);

      // load the results controller
      ResultsController resultsController =
          (ResultsController) App.getLoader("results").getController();
      // get chosen suspect
      resultsController.setSuspect(this.suspectRect.getId().replace("Rect", ""), guessExplanation);

      // hide 'error message' by setting opacity to 0
      this.confirmRequirementLabel.setOpacity(0);

      // change to results scene to show feedback on explanation
      App.setScene("results");
    } else {
      // ensure user actually selected a suspect
      App.fadeAnimation(this.confirmRequirementLabel);
    }
  }

  /** Reset the information on the guessing scene. */
  public void resetInfo() {
    if (this.suspectRect != null) {
      this.suspectRect.setOpacity(0);
    }
    this.suspectRect = null;

    this.guessInput.clear();
  }
}
