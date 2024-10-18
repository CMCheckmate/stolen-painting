package nz.ac.auckland.se206;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;

/** Timer class for the game. */
public class Timer {
  private SimpleIntegerProperty timer = new SimpleIntegerProperty();
  private Timeline timeline = new Timeline();

  /**
   * Returns the timer property. This is used to bind the timer to the GUI.
   *
   * @return timer
   */
  public SimpleIntegerProperty getTimerProperty() {
    return this.timer;
  }

  /**
   * Returns the time remaining on the timer.
   *
   * @return time
   */
  public int getTime() {
    return this.timer.get();
  }

  /**
   * Sets the time remaining on the timer.
   *
   * @param time time remaining
   */
  public void setTime(int time) {
    this.timer.set(time);
  }

  /** Starts the countdown. Actual timer value is changed. */
  public void startCountdown() {
    this.timer.set(this.timer.get() - 1);
  }

  /**
   * Sets the timeline for the timer.
   *
   * @param keyframe keyframe
   */
  public void setTimeline(KeyFrame keyframe) {
    this.timeline.getKeyFrames().add(keyframe);
  }

  /**
   * Initializes the timer. If the time is greater than 0, the timer will start counting down.
   *
   * @param time time
   */
  public void initialize(int time) {
    this.timer.set(time);
    this.timeline.stop();
    if (time > 0) {
      this.timeline.setCycleCount(time);
      this.timeline.play();
    }
  }
}
