package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ChatManager;

/**
 * Controller for the suspect scene.
 *
 * <p>This scene allows the user to interact with the suspects.
 */
public class SuspectController {
  private final double hoverOpacity = 0.5;
  private final String hoverClickAudioSource = "/sounds/hover_click.wav";
  private final Map<String, Image> backgrounds =
      new HashMap<String, Image>() {
        {
          put("suspectOne", new Image("images/background_images/owner.png"));
          put("suspectTwo", new Image("images/background_images/guard.jpg"));
          put("suspectThree", new Image("images/background_images/employee.png"));
        }
      };
  private final Map<String, Map<String, Image>> suspectImages =
      new HashMap<String, Map<String, Image>>() {
        {
          put(
              "suspectOne",
              new HashMap<String, Image>() {
                {
                  put("default", new Image("images/character_images/owner.png"));
                  put("profile", new Image("images/character_icons/owner.png"));
                }
              });
          put(
              "suspectTwo",
              new HashMap<String, Image>() {
                {
                  put("default", new Image("images/character_images/guard.png"));
                  put("profile", new Image("images/character_icons/guard.png"));
                }
              });
          put(
              "suspectThree",
              new HashMap<String, Image>() {
                {
                  put("default", new Image("images/character_images/employee.png"));
                  put("profile", new Image("images/character_icons/employee.png"));
                }
              });
        }
      };
  private final Map<String, String> suspectNames =
      new HashMap<String, String>() {
        {
          put("suspectOne", "Shop Owner");
          put("suspectTwo", "Security Guard");
          put("suspectThree", "Employee");
          put("feedback", "Feedback");
        }
      };

  @FXML private Label timerLabel;
  @FXML private Label suspectLabelOne;
  @FXML private Label suspectLabelTwo;
  @FXML private Label suspectLabelThree;
  @FXML private Label crimeLabel;

  @FXML private ImageView suspectButtonOne;
  @FXML private ImageView suspectButtonTwo;
  @FXML private ImageView suspectButtonThree;

  @FXML private ImageView backgroundImage;

  @FXML private ImageView chatProfile;

  @FXML private TextArea chatArea;

  @FXML private TextField chatInput;

  private Map<String, ImageView> suspectButtons;

  private Map<String, Label> suspectLabels;

  private AudioClip hoverClickPlayer;

  private ChatManager chatManager;

  private String suspect;

  /** Initialize the controller. Load custom font and audio player. */
  @FXML
  private void initialize() {
    // create new chat manager instance
    this.chatManager = new ChatManager(this.chatArea);

    try {
      // create new audio player to hover sound
      this.hoverClickPlayer =
          new AudioClip(
              // get audio hover sound file
              (new Media(App.class.getResource(this.hoverClickAudioSource).toURI().toString()))
                  .getSource());
    } catch (URISyntaxException e) {
      // throw error if there's an issue getting the audio file
      e.printStackTrace();
    }

    // create hashmap with the navbar buttons
    this.suspectButtons =
        new HashMap<String, ImageView>() {
          {
            put("suspectOne", suspectButtonOne);
            put("suspectTwo", suspectButtonTwo);
            put("suspectThree", suspectButtonThree);
          }
        };
    // create hashmap with the navbar labels
    this.suspectLabels =
        new HashMap<String, Label>() {
          {
            put("suspectOne", suspectLabelOne);
            put("suspectTwo", suspectLabelTwo);
            put("suspectThree", suspectLabelThree);
            put("crime", crimeLabel);
          }
        };

    SimpleIntegerProperty timerProperty = App.getTimer().getTimerProperty();
    // create timer button
    this.timerLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  int totalSeconds = timerProperty.get();
                  int minutes = totalSeconds / 60;
                  int seconds = totalSeconds % 60;
                  // format timer
                  return String.format("%02d:%02d", minutes, seconds);
                },
                timerProperty));
  }

  /**
   * Handle the hover event for the suspect buttons.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onButtonHover(MouseEvent event) {
    Node source = (Node) event.getSource();
    String suspect = source.getId().replace("Button", "");

    if (!this.suspect.equals(suspect)) {
      if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
        source.setOpacity(this.hoverOpacity);

        // check if audio is muted
        if (!App.isAudioMuted()) {
          // play hover effect sound
          this.hoverClickPlayer.play();
        }

        this.suspectLabels.get(suspect).setVisible(true);
      } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
        source.setOpacity(1);

        this.suspectLabels.get(suspect).setVisible(false);
      }
    }
  }

  /**
   * Switches to the crime scene.
   *
   * @param event The event that triggered this method.
   * @throws IOException If there is an error loading the crime scene.
   */
  @FXML
  private void onToCrime(MouseEvent event) throws IOException {
    App.setScene("crime");
  }

  /**
   * Switches to the suspect scene.
   *
   * @param event The event that triggered this method.
   * @throws IOException If there is an error loading the suspect scene.
   */
  @FXML
  private void onToSuspect(MouseEvent event) throws IOException {
    String scene = ((Node) event.getSource()).getId().replace("Button", "");
    if (!this.suspect.equals(scene)) {
      // if (!this.chatManager.getWaitingReply()) {
      this.switchSuspect(scene);
      // }
    }
  }

  /**
   * Handles suspect chat status and sends user message.
   *
   * @param event The event that triggered this method.
   */
  @FXML
  private void onSendMessage(Event event) throws ApiProxyException, IOException {
    // response when message is submitted through chat with suspects
    if ((!event.getEventType().equals(KeyEvent.KEY_PRESSED)
            || ((KeyEvent) event).getCode().equals(KeyCode.ENTER))
        && !this.chatManager.getWaitingReply()) {
      // trim output (of leading/trailing whitespaces)
      this.chatManager.onSendMessage(this.chatInput.getText().trim());
      // clear text input box to simulate actual chat box clearing once message is sent
      this.chatInput.clear();

      CrimeController crimeController = (CrimeController) App.getLoader("crime").getController();
      crimeController.setSuspectInteraction(this.suspect);
    }
  }

  /**
   * Switches to the suspect scene.
   *
   * @param suspect The suspect to switch to.
   */
  public void switchSuspect(String suspect) {
    // changes to be made when switching suspects
    Node button;
    if (this.suspect != null) {
      button = this.suspectButtons.get(this.suspect);
      // update cursor
      button.setCursor(Cursor.HAND);
      button.setOpacity(1);
    }
    this.suspect = suspect;

    // reset cursor to default style
    button = this.suspectButtons.get(this.suspect);
    button.setCursor(Cursor.DEFAULT);
    button.setOpacity(this.hoverOpacity);

    // update suspect scene contents
    this.suspectLabels.get(this.suspect).setVisible(false);

    this.backgroundImage.setImage(this.backgrounds.get(this.suspect));

    this.chatManager.setSuspect(this.suspectNames.get(this.suspect));

    this.chatProfile.setImage(this.suspectImages.get(this.suspect).get("default"));
  }

  /** Clears the chat history. Clears the chat input as well. */
  public void clearChat() {
    this.chatInput.clear();
    this.chatManager.clearChat();
  }

  /**
   * Returns the suspect name. Returns null if the suspect is not found.
   *
   * @param suspect The suspect to get the name of.
   * @return The name of the suspect.
   */
  public String getSuspectName(String suspect) {
    return this.suspectNames.get(suspect);
  }

  /**
   * Returns the suspect image. Returns null if the suspect is not found.
   *
   * @param suspect The suspect to get the image of.
   * @param type The type of image to get.
   * @return The image of the suspect.
   */
  public Image getSuspectImage(String suspect, String type) {
    return suspect == null ? null : this.suspectImages.get(suspect).get(type);
  }
}
