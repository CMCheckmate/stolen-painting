package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class ChatManager {
  private final Map<String, String> promptSources =
      new HashMap<String, String>() {
        {
          put("Shop Owner", "prompts/shop_owner.txt");
          put("Security Guard", "prompts/security_guard.txt");
          put("Employee", "prompts/employee.txt");
          put("Feedback", "prompts/feedback.txt");
        }
      };

  private final String returnPrompt =
      "Actually, I have talked to you before, mention my return as the conversation starter and you"
          + " don't need to introduce yourself.";

  @FXML private TextArea txtaChat;

  private ChatCompletionRequest chatCompletionRequest;

  private Map<String, String> chatHistory = new HashMap<String, String>();

  private Map<String, String> promptMap = new HashMap<String, String>();

  private Boolean waitingReply;

  private Task<ChatMessage> chatTask;

  private String suspect;

  /**
   * Constructor for the ChatManager class.
   *
   * @param txtaChat the chat text area.
   */
  public ChatManager(TextArea txtaChat) {
    this.txtaChat = txtaChat;

    txtaChat.selectPositionCaret(0);
    txtaChat.deselect();

    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      this.chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }

    this.waitingReply = false;
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string.
   */
  private String getSystemPrompt() {
    return PromptEngineering.getPrompt(this.promptSources.get(this.suspect), this.promptMap);
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append.
   */
  private void appendChatMessage(ChatMessage msg) {
    // Add message based on role
    StringBuilder message = new StringBuilder();
    message.append((txtaChat.getText().isEmpty() ? "" : "\n\n"));
    message.append(msg.getRole().equals("user") ? "Me" : this.suspect);
    message.append(": ");
    message.append(msg.getContent());

    txtaChat.appendText(message.toString());
    txtaChat.selectPositionCaret(txtaChat.getLength());
    txtaChat.deselect();

    // Save message
    if (this.suspect.equals("feedback")) {
      txtaChat.setScrollTop(0);
    } else {
      chatHistory.put(this.suspect, txtaChat.getText());
    }
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process.
   * @return the response chat message.
   * @throws ApiProxyException if there is an error communicating with the API proxy.
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      // Execute the chat completion request
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());

      System.out.println(result.getChatMessage().getContent());

      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Starts a chat thread with the GPT model.
   *
   * @param msg the chat message to process.
   */
  private void startChatThread(ChatMessage msg) {
    // Set chat waiting message
    String content = this.txtaChat.getText();

    StringBuilder chat = new StringBuilder();
    chat.append((!content.isEmpty() ? "\n\n" : ""));
    chat.append(this.suspect);
    chat.append(": Thinking...");

    this.txtaChat.appendText(chat.toString());

    // Create thread
    Task<ChatMessage> chatTask =
        new Task<>() {
          // create GPT call
          @Override
          protected ChatMessage call() {
            ChatMessage result = null;
            try {
              // make OpenAI API call
              result = runGpt(msg);
            } catch (ApiProxyException e) {
              // catch error if call fails
              e.printStackTrace();
            }
            return result;
          }
        };
    this.chatTask = chatTask;

    chatTask
        .onSucceededProperty()
        .set(
            event -> {
              if (this.chatTask.equals(chatTask)) {
                Platform.runLater(
                    () -> {
                      ChatMessage chatMessage = chatTask.getValue();
                      if (this.waitingReply && chatMessage != null) {
                        txtaChat.setText(content);
                        appendChatMessage(chatMessage);

                        this.waitingReply = false;
                      }
                    });
              }
            });
    Thread chatThread = new Thread(chatTask);
    chatThread.setDaemon(true);
    chatThread.start();

    this.waitingReply = true;
  }

  /**
   * Returns the chat text area.
   *
   * @return the chat text area.
   */
  @FXML
  public Boolean getWaitingReply() {
    return this.waitingReply;
  }

  /**
   * Sends a message to the GPT model.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  public void onSendMessage(String message) throws ApiProxyException, IOException {
    // Message should not be empty or waiting for a reply
    if (!message.isEmpty() && !this.waitingReply) {
      // Set the user message
      ChatMessage msg = new ChatMessage("user", message);
      appendChatMessage(msg);

      // Start the chat thread
      this.startChatThread(msg);
    }
  }

  /**
   * Sets the prompt map for the chat manager.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  public void setPromptMap(String key, String value) {
    this.promptMap.put(key, value);
  }

  /**
   * Sets the suspect for the chat manager.
   *
   * @param suspect the suspect to set.
   */
  public void setSuspect(String suspect) {
    this.suspect = suspect;

    this.promptMap.put("name", this.suspect);

    this.waitingReply = false;

    StringBuilder prompt = new StringBuilder();
    prompt.append(getSystemPrompt());

    // Load chat history
    if (this.suspect.equals("feedback") || chatHistory.get(this.suspect) == null) {
      txtaChat.clear();
    } else {
      txtaChat.setText(chatHistory.get(this.suspect));
      prompt.append(this.returnPrompt);
    }

    // Start chat thread
    this.startChatThread(new ChatMessage("system", prompt.toString()));
  }

  /** Clears the chat history. Clears the chatbox as well. */
  public void clearChat() {
    this.txtaChat.clear();
    this.chatHistory.clear();
  }
}
