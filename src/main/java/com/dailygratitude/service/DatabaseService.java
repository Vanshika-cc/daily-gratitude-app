package com.dailygratitude.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.dailygratitude.model.GratitudeEntry;

public class DatabaseService {
    
    private static final String DATABASE_NAME = "dailygratitude.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    
    private Connection connection;
    
    /**
     * Initialize database connection and create tables if they don't exist
     */
    public void initializeDatabase() {
        try {
            // Create connection
            connection = DriverManager.getConnection(DATABASE_URL);
            
            // Enable foreign keys
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            
            // Create tables
            createTables();
            
            System.out.println("✅ Database initialized successfully! File: " + DATABASE_NAME);
            
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Create necessary database tables
     */
    private void createTables() throws SQLException {
        String createGratitudeEntriesTable = 
            "CREATE TABLE IF NOT EXISTS gratitude_entries (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "entry_text TEXT NOT NULL, " +
            "created_date DATE NOT NULL, " +
            "created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "mood_rating INTEGER DEFAULT NULL, " +
            "tags TEXT DEFAULT NULL, " +
            "CONSTRAINT check_mood_rating CHECK (mood_rating IS NULL OR (mood_rating >= 1 AND mood_rating <= 5))" +
            ")";
        
        String createQuotesHistoryTable = 
            "CREATE TABLE IF NOT EXISTS quotes_history (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "quote_text TEXT NOT NULL, " +
            "author TEXT NOT NULL, " +
            "date_shown DATE NOT NULL, " +
            "api_source TEXT DEFAULT NULL, " +
            "created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createGratitudeEntriesTable);
            stmt.execute(createQuotesHistoryTable);
            System.out.println("✅ Database tables created/verified");
        }
    }
    
    /**
     * Save a gratitude entry to the database
     */
    public long saveGratitudeEntry(String entryText) throws SQLException {
        return saveGratitudeEntry(entryText, null, null);
    }
    
    /**
     * Save a gratitude entry with mood rating
     */
    public long saveGratitudeEntry(String entryText, Integer moodRating) throws SQLException {
        return saveGratitudeEntry(entryText, moodRating, null);
    }
    
    /**
     * Save a complete gratitude entry - FIXED for SQLite compatibility
     */
    public long saveGratitudeEntry(String entryText, Integer moodRating, String tags) throws SQLException {
        String sql = "INSERT INTO gratitude_entries (entry_text, created_date, created_datetime, mood_rating, tags) VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, entryText);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setString(3, LocalDateTime.now().toString());
            
            if (moodRating != null) {
                pstmt.setInt(4, moodRating);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            pstmt.setString(5, tags);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // SQLite way to get last inserted ID
                stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    long id = rs.getLong(1);
                    System.out.println("✅ Gratitude entry saved with ID: " + id);
                    return id;
                }
            }
            
            throw new SQLException("Failed to save gratitude entry, no ID obtained.");
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (pstmt != null) pstmt.close();
        }
    }
    
    /**
     * Save a quote to history for tracking what quotes were shown
     */
    public void saveQuoteToHistory(String quoteText, String author, String apiSource) {
        String sql = "INSERT INTO quotes_history (quote_text, author, date_shown, api_source) VALUES (?, ?, ?, ?)";
        
        PreparedStatement pstmt = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, quoteText);
            pstmt.setString(2, author);
            pstmt.setString(3, LocalDate.now().toString());
            pstmt.setString(4, apiSource);
            
            pstmt.executeUpdate();
            System.out.println("📝 Quote saved to history: " + author);
        } catch (SQLException e) {
            System.err.println("❌ Failed to save quote to history: " + e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get all gratitude entries for a specific date
     */
    public List<GratitudeEntry> getEntriesForDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM gratitude_entries WHERE created_date = ? ORDER BY created_datetime DESC";
        List<GratitudeEntry> entries = new ArrayList<>();
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, date.toString());
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                entries.add(mapResultSetToGratitudeEntry(rs));
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        
        return entries;
    }
    
    /**
     * Get recent gratitude entries (last N entries)
     */
    public List<GratitudeEntry> getRecentEntries(int limit) throws SQLException {
        String sql = "SELECT * FROM gratitude_entries ORDER BY created_datetime DESC LIMIT ?";
        List<GratitudeEntry> entries = new ArrayList<>();
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, limit);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                entries.add(mapResultSetToGratitudeEntry(rs));
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        
        return entries;
    }
    
    /**
     * Get total count of gratitude entries
     */
    public int getTotalEntryCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM gratitude_entries";
        
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return 0;
    }
    
    /**
     * Get entries count for today
     */
    public int getTodayEntryCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM gratitude_entries WHERE created_date = ?";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, LocalDate.now().toString());
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        
        return 0;
    }
    
    /**
     * Search gratitude entries by text content
     */
    public List<GratitudeEntry> searchEntries(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM gratitude_entries WHERE entry_text LIKE ? ORDER BY created_datetime DESC";
        List<GratitudeEntry> entries = new ArrayList<>();
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, "%" + searchTerm + "%");
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                entries.add(mapResultSetToGratitudeEntry(rs));
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        
        return entries;
    }
    
    /**
     * Delete a gratitude entry by ID
     */
    public boolean deleteEntry(long entryId) throws SQLException {
        String sql = "DELETE FROM gratitude_entries WHERE id = ?";
        
        PreparedStatement pstmt = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, entryId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("🗑️ Deleted gratitude entry with ID: " + entryId);
                return true;
            }
        } finally {
            if (pstmt != null) pstmt.close();
        }
        
        return false;
    }
    
    /**
     * Helper method to map ResultSet to GratitudeEntry object
     */
    private GratitudeEntry mapResultSetToGratitudeEntry(ResultSet rs) throws SQLException {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setId(rs.getLong("id"));
        entry.setEntryText(rs.getString("entry_text"));
        entry.setCreatedDate(LocalDate.parse(rs.getString("created_date")));
        entry.setCreatedDateTime(LocalDateTime.parse(rs.getString("created_datetime")));
        
        // Handle nullable mood_rating
        int moodRating = rs.getInt("mood_rating");
        if (!rs.wasNull()) {
            entry.setMoodRating(moodRating);
        }
        
        entry.setTags(rs.getString("tags"));
        
        return entry;
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
    
    /**
     * Check if database connection is active
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}