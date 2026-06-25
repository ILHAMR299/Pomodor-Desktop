package com.focusmaxxing;

import com.focusmaxxing.util.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class FocusMaxxingApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Initialize Database Schema on startup
        try {
            DatabaseConfig.initializeDatabase();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }

        // 2. Load the Login View
        URL fxmlLocation = getClass().getResource("/com/focusmaxxing/view/LoginView.fxml");
        if (fxmlLocation == null) {
            throw new IllegalStateException("Cannot find LoginView.fxml in resources. Check your folder structure!");
        }
        
        Parent root = FXMLLoader.load(fxmlLocation);
        Scene scene = new Scene(root, 800, 500);

        primaryStage.setTitle("FocusMaxxing - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
