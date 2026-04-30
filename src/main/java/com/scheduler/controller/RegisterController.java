package com.scheduler.controller;

import com.scheduler.dto.RegistrationRequest;
import com.scheduler.service.UserService;
import com.scheduler.util.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterController {
    
    private final UserService userService;
    private final StageManager stageManager;
    
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    
    @FXML
    public void handleRegister() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all required fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }
        
        try {
            RegistrationRequest request = new RegistrationRequest(name, username, email, phone, password);
            userService.registerUser(request);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Registration successful! Please login.");
            alert.showAndWait();
            
            handleBackToLogin();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
    
    @FXML
    public void handleBackToLogin() {
        stageManager.switchScene("/fxml/login.fxml", 400, 500);
    }
}
