package com.cyberlab.controller;

import com.cyberlab.service.AIService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller handling user interactions for the CyberLab Assistant UI.
 * Keeps all UI manipulation on the JavaFX Application Thread and delegates
 * AI communication to AIService.
 */
public class AIController implements Initializable {

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox messageContainer;

    @FXML
    private TextField messageInputField;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statusDot;

    private AIService aiService;
    private Node currentThinkingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the AI service
        this.aiService = new AIService();

        // Update header status chip based on API configuration
        updateStatusDisplay();

        // Auto-scroll chat area when new messages are added
        messageContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        });

        // Display initial welcome message
        addAssistantMessage("Hello! I'm the CyberLab Assistant. Ask me anything about cybersecurity.");
    }

    /**
     * Updates the status chip in the top header.
     */
    private void updateStatusDisplay() {
        if (aiService.isApiKeyConfigured()) {
            statusLabel.setText("Gemini AI Active (" + aiService.getModelName() + ")");
            statusDot.setStyle("-fx-text-fill: #10b981;"); // Green
        } else {
            statusLabel.setText("Offline Mode (Predefined Q&A)");
            statusDot.setStyle("-fx-text-fill: #f59e0b;"); // Amber
        }
    }

    /**
     * Handles the Send button click and Enter key press in the text input field.
     */
    @FXML
    public void handleSendMessage() {
        String userInput = messageInputField.getText();
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }

        userInput = userInput.trim();

        // 1. Add user message bubble to UI
        addUserMessage(userInput);

        // 2. Clear input field and lock controls to prevent duplicate requests
        messageInputField.clear();
        setInputControlsLocked(true);

        // 3. Show temporary thinking indicator
        showThinkingIndicator();

        // 4. Asynchronously query Gemini AI without freezing the JavaFX thread
        aiService.sendMessageAsync(userInput)
                .thenAccept(response -> Platform.runLater(() -> {
                    // Remove thinking indicator
                    removeThinkingIndicator();

                    // Display assistant response
                    addAssistantMessage(response);

                    // Re-enable input controls
                    setInputControlsLocked(false);
                    messageInputField.requestFocus();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        removeThinkingIndicator();
                        addAssistantMessage("An unexpected error occurred: " + throwable.getMessage());
                        setInputControlsLocked(false);
                        messageInputField.requestFocus();
                    });
                    return null;
                });
    }

    /**
     * Adds a user message bubble aligned to the right.
     */
    private void addUserMessage(String text) {
        VBox bubbleCard = new VBox(4);
        bubbleCard.getStyleClass().add("user-bubble");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("user-bubble-label");
        textLabel.setWrapText(true);
        textLabel.maxWidthProperty().bind(chatScrollPane.widthProperty().multiply(0.70));

        bubbleCard.getChildren().add(textLabel);

        Label senderTag = new Label("You");
        senderTag.getStyleClass().add("sender-tag");

        VBox userWrapper = new VBox(2, senderTag, bubbleCard);
        userWrapper.setAlignment(Pos.TOP_RIGHT);

        HBox row = new HBox(userWrapper);
        row.getStyleClass().add("message-row-user");
        row.setAlignment(Pos.CENTER_RIGHT);

        messageContainer.getChildren().add(row);
    }

    /**
     * Adds an assistant message bubble aligned to the left.
     */
    private void addAssistantMessage(String text) {
        VBox bubbleCard = new VBox(4);
        bubbleCard.getStyleClass().add("assistant-bubble");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("assistant-bubble-label");
        textLabel.setWrapText(true);
        textLabel.maxWidthProperty().bind(chatScrollPane.widthProperty().multiply(0.75));

        bubbleCard.getChildren().add(textLabel);

        Label senderTag = new Label("CyberLab Assistant 🛡️");
        senderTag.getStyleClass().addAll("sender-tag", "sender-tag-assistant");

        VBox assistantWrapper = new VBox(2, senderTag, bubbleCard);
        assistantWrapper.setAlignment(Pos.TOP_LEFT);

        HBox row = new HBox(assistantWrapper);
        row.getStyleClass().add("message-row-assistant");
        row.setAlignment(Pos.CENTER_LEFT);

        messageContainer.getChildren().add(row);
    }

    /**
     * Displays a temporary thinking message bubble while waiting for Gemini.
     */
    private void showThinkingIndicator() {
        VBox bubbleCard = new VBox();
        bubbleCard.getStyleClass().add("thinking-bubble");

        Label thinkingLabel = new Label("CyberLab Assistant is thinking...");
        thinkingLabel.getStyleClass().add("thinking-label");

        bubbleCard.getChildren().add(thinkingLabel);

        HBox row = new HBox(bubbleCard);
        row.getStyleClass().add("message-row-assistant");
        row.setAlignment(Pos.CENTER_LEFT);

        currentThinkingIndicator = row;
        messageContainer.getChildren().add(row);
    }

    /**
     * Removes the temporary thinking message.
     */
    private void removeThinkingIndicator() {
        if (currentThinkingIndicator != null) {
            messageContainer.getChildren().remove(currentThinkingIndicator);
            currentThinkingIndicator = null;
        }
    }

    /**
     * Locks or unlocks the input field and send button.
     */
    private void setInputControlsLocked(boolean locked) {
        sendButton.setDisable(locked);
        messageInputField.setDisable(locked);
    }
}
