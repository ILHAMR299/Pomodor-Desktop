package com.focusmaxxing.controller;

import com.focusmaxxing.model.Role;
import com.focusmaxxing.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #e57373;");
            errorLabel.setText("Please fill all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setStyle("-fx-text-fill: #e57373;");
            errorLabel.setText("Passwords do not match");
            return;
        }

        try {
            // Default role for new signups is USER.
            authService.register(username, password, Role.USER);
            errorLabel.setStyle("-fx-text-fill: #78a890;");
            errorLabel.setText("Registration successful! Please login.");
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        } catch (IllegalArgumentException e) {
            errorLabel.setStyle("-fx-text-fill: #e57373;");
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: #e57373;");
            errorLabel.setText("Error during registration: " + e.getMessage());
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/focusmaxxing/view/LoginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
