package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.util.Duration;
import javafx.util.Pair;
import nz.ac.auckland.se206.App;

/** Controller for the crime scene. Includes all clue functionality. */
public class CrimeController {
  private final String clueAudioSource = "/sounds/radio.wav";
  private final String interactAudioSource = "/sounds/interact.wav";
  private final String hoverClickAudioSource = "/sounds/hover_click.wav";
  private final String intro =
      "Interact with any clue and chat with all the suspects before guessing";
  private final String guessRequirement =
      "Interact with any clue and chat with all the suspects before guessing";
  private final String cluePassword = "helen1973";
  private final double hoverOpacity = 0.5;
  private final Pair<Double, Double> radioRange = new Pair<Double, Double>(25.0, 35.0);
  private Image greenButton = new Image("images/radio_clue/green_radio_button.png");
  private Image redButton = new Image("images/radio_clue/red_radio_button.png");
  private final int textDelay = 2000;
  private final String introText = "Location: Glenn's Art Shop\nCrime: Stolen Painting";

  @FXML private Label transitionLabel;
  @FXML private Pane transitionPane;

  @FXML private Label timerLabel;

  @FXML private Label passwordLabel;

  @FXML private ImageView suspectButtonOne;
  @FXML private ImageView suspectButtonTwo;
  @FXML private ImageView suspectButtonThree;
  @FXML private ImageView radioPlay;

  @FXML private Label suspectLabelOne;
  @FXML private Label suspectLabelTwo;
  @FXML private Label suspectLabelThree;

  @FXML private ImageView guessButton;

  @FXML private Label guessButtonLabel;
  @FXML private Label instructionLabel;

  @FXML private Pane radioLabel;
  @FXML private Pane computerLabel;
  @FXML private Pane frameLabel;

  @FXML private Pane radioPane;
  @FXML private Pane computerPane;
  @FXML private Pane framePane;

  @FXML private Pane guessConfirmPane;

  @FXML private Pane clueTabOne;
  @FXML private ScrollPane clueTabTwo;
  @FXML private ImageView radioSide;
  @FXML private ImageView compDesk;
  @FXML private ImageView framePuzzle;

  @FXML private TextField passwordInput;

  @FXML private ImageView puzzlePieceOne;
  @FXML private ImageView puzzlePieceTwo;
  @FXML private ImageView puzzlePieceThree;
  @FXML private ImageView puzzlePieceFour;
  @FXML private ImageView puzzlePieceFive;

  @FXML private ImageView radioWave;
  @FXML private ImageView radioGreenLight;

  @FXML private Slider radioWaveSlider;

  private double radioInitialScale;

  private Map<ImageView, Pair<Double, Double>> puzzlePositions;

  private Map<Node, Node> labels;

  private Map<Node, Node> clues;

  private Node openedClue;

  private AudioClip mediaPlayer;
  private AudioClip interactPlayer;

  private AudioClip hoverClickPlayer;

  private Boolean cluesInteracted;

  private Set<String> suspectsInteracted = new HashSet<String>();

  /** Initialize the controller. Load custom font and audio player. */
  @FXML
  private void initialize() {
    // create hashmap with clue names and their respective pane names
    this.clues =
        new HashMap<Node, Node>() {
          {
            put(radioSide, radioPane);
            put(compDesk, computerPane);
            put(framePuzzle, framePane);
          }
        };

    // create hashmap with puzzle pieces in 'broken label' clue
    this.puzzlePositions =
        new HashMap<ImageView, Pair<Double, Double>>() {
          {
            put(
                puzzlePieceOne,
                new Pair<Double, Double>(puzzlePieceOne.getX(), puzzlePieceOne.getY()));
            put(
                puzzlePieceTwo,
                new Pair<Double, Double>(puzzlePieceTwo.getX(), puzzlePieceTwo.getY()));
            put(
                puzzlePieceThree,
                new Pair<Double, Double>(puzzlePieceThree.getX(), puzzlePieceThree.getY()));
            put(
                puzzlePieceFour,
                new Pair<Double, Double>(puzzlePieceFour.getX(), puzzlePieceFour.getY()));
            put(
                puzzlePieceFive,
                new Pair<Double, Double>(puzzlePieceFive.getX(), puzzlePieceFive.getY()));
          }
        };

    // create hashmap with all the hover labels for all interactables
    this.labels =
        new HashMap<Node, Node>() {
          {
            put(radioSide, radioLabel);
            put(compDesk, computerLabel);
            put(framePuzzle, frameLabel);
            put(suspectButtonOne, suspectLabelOne);
            put(suspectButtonTwo, suspectLabelTwo);
            put(suspectButtonThree, suspectLabelThree);
            put(guessButton, guessButtonLabel);
          }
        };

    this.radioInitialScale = radioWave.getScaleX();

    // set up timer
    SimpleIntegerProperty timerProperty = App.getTimer().getTimerProperty();
    this.timerLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  // format timer
                  int totalSeconds = timerProperty.get();
                  int minutes = totalSeconds / 60;
                  int seconds = totalSeconds % 60;
                  return String.format("%02d:%02d", minutes, seconds);
                },
                timerProperty));

    try {
      // create audio player
      this.mediaPlayer =
          new AudioClip(
              // get clue audio file source
              (new Media(App.class.getResource(this.clueAudioSource).toURI().toString()))
                  .getSource());
    } catch (URISyntaxException e) {
      // throw an exception if there's an issue retrieving the file
      e.printStackTrace();
    }

    try {
      // create audio player
      this.hoverClickPlayer =
          new AudioClip(
              // get hover audio file source
              (new Media(App.class.getResource(this.hoverClickAudioSource).toURI().toString()))
                  .getSource());
    } catch (URISyntaxException e) {
      // throw an exception if there's an issue retrieving the file
      e.printStackTrace();
    }

    try {
      // create audio player
      this.interactPlayer =
          new AudioClip(
              // get audio source
              (new Media(App.class.getResource(this.interactAudioSource).toURI().toString()))
                  .getSource());
    } catch (URISyntaxException e) {
      // throw an exception if there's an issue retrieving the file
      e.printStackTrace();
    }

    this.cluesInteracted = false;
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

      // check if audio is unmuted
      if (!App.isAudioMuted()) {
        // play hover effect sound
        this.hoverClickPlayer.play();
      }
    } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {

      source.setOpacity(1);
    }
  }

  /**
   * Handle hover event for UI buttons.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onButtonHover(MouseEvent event) {
    Node source = (Node) event.getSource();

    if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
      this.labels.get(source).setVisible(true);
      source.setOpacity(this.hoverOpacity);

      // check if audio is muted
      if (!App.isAudioMuted()) {
        // play hover effect sound
        this.hoverClickPlayer.play();
      }

    } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
      this.labels.get(source).setVisible(false);
      source.setOpacity(1);
    }
  }

  /**
   * Handle clue opening event. Switches to the specified clue pane.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onCluePane(MouseEvent event) {
    this.mediaPlayer.stop();
    this.switchClue(this.clues.get(event.getSource()));

    this.cluesInteracted = true;
  }

  /**
   * Set current suspect and switch to suspect scene.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onToSuspect(MouseEvent event) throws IOException {
    String suspect = ((Node) event.getSource()).getId().replace("Button", "");
    SuspectController suspectController =
        (SuspectController) App.getLoader("suspect").getController();
    suspectController.switchSuspect(suspect);

    App.setScene("suspect");
  }

  /**
   * Handle guess confirmation event. Checks if user has interacted with all clues and suspects
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onToggleGuess(Event event) {
    String button = ((Node) event.getSource()).getId();

    if (button.contains("guess")) {

      // if user has interact with at least 1 clue and all 3 suspects
      if (!this.checkInteracted()) {
        //
        if (this.instructionLabel.getText().equals(this.guessRequirement)) {
          interactPlayer.stop();

          // check if audio is muted first
          if (!App.isAudioMuted()) {
            interactPlayer.play();
          }

          App.fadeAnimation(this.instructionLabel);
        }
      } else {
        // confirm that user wants to guess (as a precaution, in case of misclick/change of mind)
        this.guessConfirmPane.setVisible(true);
      }
    } else {
      // hide the precautionary 'confirm message' pane
      this.guessConfirmPane.setVisible(false);
    }
  }

  /**
   * Switch to guessing scene if user has interacted with all clues and suspects.
   *
   * @param event The event that triggered this method.
   * @throws IOException If there is an error loading the guessing scene.
   */
  @FXML
  private void onToGuess(ActionEvent event) throws IOException {
    if (this.checkInteracted()) {
      App.setScene("guessing");
    }
  }

  /**
   * Plays the radio audio when the radio is clicked.
   *
   * @param event The event that triggered this method.
   * @throws Exception If there is an error playing the audio.
   */
  @FXML
  private void onClueOnePlay(MouseEvent event) throws Exception {
    if (this.mediaPlayer.isPlaying()) {
      this.mediaPlayer.stop();
    }
    if (this.radioGreenLight.isVisible()) {
      this.mediaPlayer.play();
    }
  }

  /**
   * Handle the password submission event for the computer clue.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onSubmitPassword(Event event) {
    // when the password is submitted
    if (!event.getEventType().equals(KeyEvent.KEY_PRESSED)
        || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
      // check that given password matches what it should be
      if (this.passwordInput.getText().toLowerCase().equals(this.cluePassword)) {
        // transition from lockscreen to email screen
        this.clueTabOne.setVisible(false);
        this.clueTabTwo.setVisible(true);
      } else {
        // slowly fade out the 'wrong password' label
        App.fadeAnimation(this.passwordLabel);
      }
      // clear password if password is wrong
      this.passwordInput.clear();
    }
  }

  /**
   * Toggles the hand cursor when the mouse is over the object.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onToggleHand(MouseEvent event) {
    Node source = (Node) event.getSource();
    if (source.getCursor().equals(Cursor.OPEN_HAND)) {
      source.setCursor(Cursor.CLOSED_HAND);
    } else {
      source.setCursor(Cursor.OPEN_HAND);
    }
  }

  /**
   * Handle the drag event for the puzzle pieces.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onDragPiece(MouseEvent event) {
    ImageView source = (ImageView) event.getSource();
    source.setX(event.getX() - source.getFitWidth() / 2);
    source.setY(event.getY() - source.getFitHeight() / 2);
  }

  /**
   * Handles opening of clue pane when specified clue is interacted.
   *
   * @param cluePane The clue pane to be opened.
   */
  public void switchClue(Node cluePane) {
    if (this.openedClue != null) {
      this.openedClue.setVisible(false);
    }
    this.openedClue = cluePane;
    if (cluePane != null) {
      this.openedClue.setVisible(true);
      this.openedClue.requestFocus();
    }
  }

  /**
   * Handles hover for suspect buttons.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onObjectHover(MouseEvent event) {
    ImageView image = (ImageView) event.getSource();
    // context.handleRectangleClick(event, clickedRectangle.getId());
    if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
      image.setScaleX(1.05);
      image.setScaleY(1.05);

      // check if audio is muted first
      if (!App.isAudioMuted()) {
        // play hover effect sound
        this.hoverClickPlayer.play();
      }

      Node label = this.labels.get(image);
      if (label != null) {
        label.setVisible(true);
      }
    }
    if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
      image.setScaleX(1);
      image.setScaleY(1);

      Node label = this.labels.get(image);
      if (label != null) {
        label.setVisible(false);
      }
    }
  }

  /**
   * Handles the radio tuner dial event.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onRadioTune(MouseEvent event) {
    // setup the step size for each turn of the tuner dial
    this.radioWave.setScaleX(
        1 + (this.radioInitialScale - 1) * this.radioWaveSlider.getValue() / 100);

    // read the dial value
    double radioValue = this.radioWaveSlider.getValue();

    // check if dial value is within accepted range
    if (radioValue > this.radioRange.getKey() && radioValue < this.radioRange.getValue()) {
      // turn radio light to green
      this.radioGreenLight.setVisible(true);
      radioPlay.setImage(greenButton);
    } else if (this.radioGreenLight.isVisible()) {
      // turn radio light to red
      this.radioGreenLight.setVisible(false);
      radioPlay.setImage(redButton);
      // stop media
      this.mediaPlayer.stop();
    }
  }

  /**
   * Sets the suspect interaction status.
   *
   * @param suspect The suspect that has been interacted with.
   */
  public void setSuspectInteraction(String suspect) {
    this.suspectsInteracted.add(suspect);
  }

  /**
   * Checks if all clues and suspects have been interacted with.
   *
   * @return Boolean value indicating if all clues and suspects have been interacted with.
   */
  public Boolean checkInteracted() {
    return this.cluesInteracted && this.suspectsInteracted.size() == 3;
  }

  /** Resets the crime scene. Makes sure all states of clues and interactions are reset. */
  public void reset() {
    // reset the instruction label
    this.instructionLabel.setText(this.intro);
    this.transitionLabel.setText("");

    // close any clue overlaps and stop any audio
    this.switchClue(null);
    this.mediaPlayer.stop();

    // set number of clues & suspects interacted with to none
    this.cluesInteracted = false;
    this.suspectsInteracted.clear();
    this.guessConfirmPane.setVisible(false);

    // clear password input in computer clue
    this.passwordInput.clear();
    // default the computer clue to the first 'enter password' screen
    this.clueTabOne.setVisible(true);
    this.clueTabTwo.setVisible(false);

    // reset radio wave slider
    this.radioWaveSlider.setValue(100);
    // reset the wave matched light confirmation to red
    this.radioGreenLight.setVisible(false);

    for (ImageView puzzlePiece : this.puzzlePositions.keySet()) {
      Pair<Double, Double> position = this.puzzlePositions.get(puzzlePiece);
      puzzlePiece.setX(position.getKey());
      puzzlePiece.setY(position.getValue());
    }

    // reset radio image
    radioPlay.setImage(redButton);

    // set up new, clear suspect controller
    SuspectController suspectController =
        (SuspectController) App.getLoader("suspect").getController();
    suspectController.clearChat();
  }

  /** Show the on-screen instructions. Fade in the instruction label. */
  public void showInstruction() {
    // show on-screen instructions
    this.instructionLabel.setText(this.intro);
    App.fadeAnimation(this.instructionLabel)
        .setOnFinished(
            event -> {
              this.instructionLabel.setText(this.guessRequirement);
            });
    this.transitionPane.setVisible(false);
  }

  /**
   * Transition to the intro screen.
   *
   * @return the timeline of the transition animation.
   */
  public Timeline transitionIntro() {
    // get the length of the text
    int textLength = this.introText.length();
    int interval = this.textDelay / textLength;
    Timeline textTimeline =
        new Timeline(
            new KeyFrame(
                // set duration in milliseconds
                Duration.millis(interval),
                e -> {
                  // set up text
                  this.transitionLabel.setText(
                      this.introText.substring(0, this.transitionLabel.getText().length() + 1));
                }));
    // refresh cycle
    textTimeline.setCycleCount(textLength);
    textTimeline.play();

    this.transitionPane.setVisible(true);
    // play animation
    return App.fadeAnimation(this.transitionPane);
  }
}
