package com.dailygratitude;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.dailygratitude.model.Quote;
import com.dailygratitude.model.GratitudeEntry;
import com.dailygratitude.service.QuoteService;
import com.dailygratitude.service.DatabaseService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main extends Application {
    
    private QuoteService quoteService;
    private DatabaseService databaseService;
    private TextArea gratitudeTextArea;
    private Label quoteLabel;
    private Label authorLabel;
    private Label statsLabel;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // MINIMAL SETUP FOR WINDOW CONTROLS
        primaryStage.setTitle("Daily Gratitude");
        
        // Initialize services
        quoteService = new QuoteService();
        databaseService = new DatabaseService();
        databaseService.initializeDatabase();
        
        // Create simple layout
        VBox root = createMainLayout();
        
        // Create scene
        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setScene(scene);
        
        // CRITICAL: Don't set any stage styles, let JavaFX use defaults
        primaryStage.setResizable(true);
        
        // Load content after showing
        primaryStage.show();
        
        // Load data
        loadTodaysQuote();
        updateStats();
        
        // Close handler
        primaryStage.setOnCloseRequest(e -> {
            if (databaseService != null) databaseService.closeConnection();
        });
    }
    
    private VBox createMainLayout() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Daily Gratitude");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");
        
        // Date
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // Stats
        statsLabel = new Label("📊 Loading stats...");
        statsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        
        // Quote section
        VBox quoteSection = createQuoteSection();
        
        // Gratitude section
        VBox gratitudeSection = createGratitudeSection();
        
        // Buttons
        HBox buttonSection = createButtonSection();
        
        root.getChildren().addAll(titleLabel, dateLabel, statsLabel, quoteSection, gratitudeSection, buttonSection);
        return root;
    }
    
    private VBox createQuoteSection() {
        HBox quoteBox = new HBox(10);
        quoteBox.setPadding(new Insets(12));
        quoteBox.setAlignment(Pos.CENTER_LEFT);
        quoteBox.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 8;");
        
        // Left content
        HBox leftContent = new HBox(8);
        leftContent.setAlignment(Pos.CENTER_LEFT);
        
        Label quoteIcon = new Label("✨");
        quoteIcon.setStyle("-fx-font-size: 14px;");
        
        quoteLabel = new Label("Loading quote...");
        quoteLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: white;");
        quoteLabel.setMaxWidth(400);
        
        Label separator = new Label("—");
        separator.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 10px;");
        
        authorLabel = new Label("");
        authorLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.8);");
        
        leftContent.getChildren().addAll(quoteIcon, quoteLabel, separator, authorLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Refresh button
        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 6;");
        refreshBtn.setOnAction(e -> loadRandomQuote());
        
        quoteBox.getChildren().addAll(leftContent, spacer, refreshBtn);
        
        VBox wrapper = new VBox();
        wrapper.getChildren().add(quoteBox);
        return wrapper;
    }
    
    private VBox createGratitudeSection() {
        VBox gratitudeBox = new VBox(10);
        
        Label gratitudeTitle = new Label("🙏 What are you grateful for today?");
        gratitudeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        gratitudeTextArea = new TextArea();
        gratitudeTextArea.setPromptText("Write your gratitude here...");
        gratitudeTextArea.setPrefRowCount(8);
        gratitudeTextArea.setWrapText(true);
        
        gratitudeBox.getChildren().addAll(gratitudeTitle, gratitudeTextArea);
        return gratitudeBox;
    }
    
    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = new Button("💾 Save Entry");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 16;");
        saveBtn.setOnAction(e -> saveGratitudeEntry());
        
        Button viewBtn = new Button("📖 View Entries");
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 16;");
        viewBtn.setOnAction(e -> showRecentEntries());
        
        Button todayBtn = new Button("📅 Today");
        todayBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 8 16;");
        todayBtn.setOnAction(e -> showTodaysEntries());
        
        buttonBox.getChildren().addAll(saveBtn, viewBtn, todayBtn);
        return buttonBox;
    }
    
    private void loadTodaysQuote() {
        new Thread(() -> {
            try {
                Quote quote = quoteService.getTodaysQuote();
                Platform.runLater(() -> {
                    quoteLabel.setText("\"" + quote.getText() + "\"");
                    authorLabel.setText(quote.getAuthor());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    quoteLabel.setText("\"Every day is a gift.\"");
                    authorLabel.setText("Daily Gratitude");
                });
            }
        }).start();
    }
    
    private void loadRandomQuote() {
        new Thread(() -> {
            try {
                Quote quote = quoteService.getRandomQuote();
                Platform.runLater(() -> {
                    quoteLabel.setText("\"" + quote.getText() + "\"");
                    authorLabel.setText(quote.getAuthor());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    quoteLabel.setText("\"Gratitude is everything.\"");
                    authorLabel.setText("Anonymous");
                });
            }
        }).start();
    }
    
    private void saveGratitudeEntry() {
        String text = gratitudeTextArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Empty Entry", "Please write something!");
            return;
        }
        
        try {
            long id = databaseService.saveGratitudeEntry(text);
            updateStats();
            showAlert("Success", "Entry saved! ID: " + id);
            gratitudeTextArea.clear();
        } catch (Exception e) {
            showAlert("Error", "Failed to save: " + e.getMessage());
        }
    }
    
    private void updateStats() {
        new Thread(() -> {
            try {
                int total = databaseService.getTotalEntryCount();
                int today = databaseService.getTodayEntryCount();
                Platform.runLater(() -> {
                    statsLabel.setText("📊 Total: " + total + " | Today: " + today);
                });
            } catch (Exception e) {
                Platform.runLater(() -> statsLabel.setText("📊 Stats unavailable"));
            }
        }).start();
    }
    
    private void showRecentEntries() {
        new Thread(() -> {
            try {
                List<GratitudeEntry> entries = databaseService.getRecentEntries(5);
                Platform.runLater(() -> {
                    if (entries.isEmpty()) {
                        showAlert("No Entries", "No entries found!");
                        return;
                    }
                    StringBuilder sb = new StringBuilder("Recent Entries:\n\n");
                    for (GratitudeEntry entry : entries) {
                        sb.append("• ").append(entry.getPreview(50)).append("\n");
                    }
                    showAlert("Recent Entries", sb.toString());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load entries"));
            }
        }).start();
    }
    
    private void showTodaysEntries() {
        new Thread(() -> {
            try {
                List<GratitudeEntry> entries = databaseService.getEntriesForDate(LocalDate.now());
                Platform.runLater(() -> {
                    if (entries.isEmpty()) {
                        showAlert("No Entries", "No entries today!");
                        return;
                    }
                    StringBuilder sb = new StringBuilder("Today's Entries:\n\n");
                    for (GratitudeEntry entry : entries) {
                        sb.append("• ").append(entry.getEntryText()).append("\n\n");
                    }
                    showAlert("Today's Entries", sb.toString());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load today's entries"));
            }
        }).start();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}