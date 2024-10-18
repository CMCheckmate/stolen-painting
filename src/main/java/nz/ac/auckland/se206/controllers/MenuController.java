package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.text.Font;
import nz.ac.auckland.se206.App;

/**
 * Controller for the menu scene.
 *
 * <p>This scene is the main menu of the game.
 */
public class MenuController {

  private final String hoverClickAudioSource = "/sounds/hover_click.wav";
  private final double hoverOpacity = 0.5;

  @FXML private ImageView muteButton;
  @FXML private ImageView unmuteButton;

  @FXML private Label title;

  @FXML private Pane instructionPane;

  private AudioClip hoverClickPlayer;

  /** Initialize the controller. Load custom font and audio player. */
  @FXML
  private void initialize() {
    // show mute button
    muteButton.setVisible(true);
    // hide unmute button (as audio is unmuted by default)
    unmuteButton.setVisible(false);

    // load custom font
    Font customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/a.ttf"), 100);
    title.setFont(customFont);
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
  }

  /**
   * Mute Text To Speech button.
   *
   * @param event The event that triggered this method
   */
  @FXML
  private void onMuteButtonClick(MouseEvent event) {
    // mute the audio player to stop tts from playing
    App.muteAudio();

    // hide mute button (since audio is now muted)
    muteButton.setVisible(false);
    // show unmute button
    unmuteButton.setVisible(true);
  }

  /**
   * Unmute the Text To Speech button.
   *
   * @param event The event that triggered this method
   */
  @FXML
  private void onUnmuteButtonClick(MouseEvent event) {
    // unmute the audio player to allow the tts to play
    App.unmuteAudio();

    // hide unmute button (since audio is now unmuted)
    unmuteButton.setVisible(false);
    // show mute button
    muteButton.setVisible(true);
  }

  /**
   * Start the game. Switch to the crime scene.
   *
   * @param event The event that triggered this method.
   * @throws IOException If there is an error loading the crime scene.
   */
  @FXML
  private void onStartGame(ActionEvent event) throws IOException {
    App.setScene("crime");
  }

  /**
   * Handle the hover effect for buttons.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onButtonHover(MouseEvent event) {
    Node source = (Node) event.getSource();

    if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {

      source.setOpacity(this.hoverOpacity);
      source.setScaleX(1.05);
      source.setScaleY(1.05);

      // check if audio is muted first
      if (!App.isAudioMuted()) {
        // play hover effect sound
        this.hoverClickPlayer.play();
      }
    } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {

      source.setOpacity(1);
      source.setScaleX(1.0);
      source.setScaleY(1.0);
    }
  }

  /** Toggle the instructions pane. Show or hide the instructions. */
  @FXML
  private void onToggleInstructions() {
    this.instructionPane.setVisible(!this.instructionPane.isVisible());
  }

  /**
   * Exit the game. Close the application.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onExit(ActionEvent event) {
    Platform.exit();
  }
}
