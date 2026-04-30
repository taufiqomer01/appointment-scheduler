package com.scheduler.controller;

import com.scheduler.entity.User;
import com.scheduler.service.UserService;
import com.scheduler.util.SessionManager;
import com.scheduler.util.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginController {
    
    private final UserService userService;
    private final StageManager stageManager;
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }
        
        try {
            User user = userService.authenticate(username, password);
            SessionManager.getInstance().setCurrentUser(user);
            
            String fxmlFile = switch (user.getRole()) {
                case ADMIN -> "/fxml/admin-dashboard.fxml";
                case STAFF -> "/fxml/staff-dashboard.fxml";
                case USER -> "/fxml/user-dashboard.fxml";
            };
            
            stageManager.switchScene(fxmlFile, 1000, 700);
        } catch (Exception e) {
            errorLabel.setText("Invalid credentials");
        }
    }
    
    @FXML
    public void handleRegister() {
        stageManager.switchScene("/fxml/register.fxml", 450, 600);
    }
}
