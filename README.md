Daily Gratitude App
A clean, minimalist desktop application for daily gratitude journaling built with JavaFX and SQLite database integration.

<img width="1920" height="992" alt="Daily Gratitude" src="https://github.com/user-attachments/assets/56e8a329-8610-465f-8138-a86e74bd9195" />


Features
Daily Gratitude Journaling: Simple text area for writing daily reflections
Entry Tracking: Real-time counter showing total entries and today's entries
Dynamic Inspirational Quotes: Daily motivational quotes with refresh functionality
Recent Entries View: Browse your last 5 gratitude entries
Today's Entries: Quick access to view all entries made today
Local Storage: SQLite database ensures all entries are stored securely offline
Responsive UI: Threaded operations keep the interface smooth and responsive
Cross-Platform: Desktop application that runs on Windows, macOS, and Linux

Tech Stack
Java 22 - Core programming language
JavaFX 22.0.1 - GUI framework for cross-platform desktop interface
SQLite - Local database for data persistence
Maven - Build automation and dependency management
Jackson JSON - JSON processing for data serialization

Installation & Setup
Prerequisites

Java 22 or higher
Maven 3.6 or higher

Clone and Run
# Clone the repository
git clone https://github.com/yourusername/daily-gratitude-app.git

# Navigate to project directory
cd daily-gratitude-app

# Compile and run with Maven
mvn clean javafx:run

Development
Database Schema
The app uses SQLite with a simple schema for storing gratitude entries:

Entry ID (Primary Key)
Date/Timestamp
Gratitude Text
Entry Count

Key Dependencies

javafx-controls and javafx-fxml for UI components
sqlite-jdbc for database connectivity
jackson-databind for JSON processing

Author
Built with ❤️ for mindfulness and gratitude practice.
