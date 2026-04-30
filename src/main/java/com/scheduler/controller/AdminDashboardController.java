package com.scheduler.controller;

import com.scheduler.dto.AppointmentDTO;
import com.scheduler.dto.RegistrationRequest;
import com.scheduler.dto.UserDTO;
import com.scheduler.entity.Appointment;
import com.scheduler.entity.Review;
import com.scheduler.entity.User;
import com.scheduler.service.AnalyticsService;
import com.scheduler.service.AppointmentService;
import com.scheduler.service.ReviewService;
import com.scheduler.service.UserService;
import com.scheduler.util.SessionManager;
import com.scheduler.util.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final AnalyticsService analyticsService;
    private final ReviewService reviewService;
    private final StageManager stageManager;
    
    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label pendingLabel;
    @FXML private Label completedLabel;
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, String> nameColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> emailColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    @FXML private TableColumn<UserDTO, String> statusColumn;
    @FXML private TableView<AppointmentDTO> appointmentsTable;
    @FXML private TableColumn<AppointmentDTO, String> userNameColumn;
    @FXML private TableColumn<AppointmentDTO, String> staffNameColumn;
    @FXML private TableColumn<AppointmentDTO, LocalDate> dateColumn;
    @FXML private TableColumn<AppointmentDTO, String> apptStatusColumn;
    @FXML private TextField staffNameField;
    @FXML private TextField staffUsernameField;
    @FXML private TextField staffEmailField;
    @FXML private PasswordField staffPasswordField;
    @FXML private ComboBox<User.Role> roleComboBox;
    @FXML private TextField userSearchField;
    @FXML private TextField apptSearchField;
    
    private FilteredList<UserDTO> filteredUsers;
    private FilteredList<AppointmentDTO> filteredAppointments;
    
    @FXML
    public void initialize() {
        welcomeLabel.setText("Admin Dashboard - " + SessionManager.getInstance().getCurrentUser().getName());
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        apptStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        roleComboBox.setItems(FXCollections.observableArrayList(User.Role.STAFF, User.Role.ADMIN));
        
        loadAnalytics();
        loadUsers();
        loadAppointments();
    }
    
    private void setupUserSearch() {
        filteredUsers = new FilteredList<>(usersTable.getItems(), p -> true);
        
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getRole().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getStatus().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        SortedList<UserDTO> sortedData = new SortedList<>(filteredUsers);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);
    }
    
    private void setupAppointmentSearch() {
        filteredAppointments = new FilteredList<>(appointmentsTable.getItems(), p -> true);
        
        apptSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredAppointments.setPredicate(appointment -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (appointment.getUserName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (appointment.getStaffName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (appointment.getStatus().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (appointment.getDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        SortedList<AppointmentDTO> sortedData = new SortedList<>(filteredAppointments);
        sortedData.comparatorProperty().bind(appointmentsTable.comparatorProperty());
        appointmentsTable.setItems(sortedData);
    }
    
    private void loadAnalytics() {
        Map<String, Object> analytics = analyticsService.getSystemAnalytics();
        totalUsersLabel.setText(String.valueOf(analytics.get("totalUsers")));
        totalAppointmentsLabel.setText(String.valueOf(analytics.get("totalAppointments")));
        pendingLabel.setText(String.valueOf(analytics.get("pendingAppointments")));
        completedLabel.setText(String.valueOf(analytics.get("completedAppointments")));
    }
    
    private void loadUsers() {
        List<UserDTO> users = userService.getAllUsers();
        usersTable.setItems(FXCollections.observableArrayList(users));
        setupUserSearch();
    }
    
    private void loadAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        appointmentsTable.setItems(FXCollections.observableArrayList(appointments));
        setupAppointmentSearch();
    }
    
    @FXML
    public void handleCreateStaffOrAdmin() {
        String name = staffNameField.getText();
        String username = staffUsernameField.getText();
        String email = staffEmailField.getText();
        String password = staffPasswordField.getText();
        User.Role role = roleComboBox.getValue();
        
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }
        
        try {
            RegistrationRequest request = new RegistrationRequest(name, username, email, null, password);
            userService.createStaffOrAdmin(request, role);
            showAlert(Alert.AlertType.INFORMATION, "Success", role.name() + " account created successfully!");
            loadUsers();
            loadAnalytics();
            clearStaffForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    
    @FXML
    public void handleActivateUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user");
            return;
        }
        
        try {
            userService.activateUser(selected.getId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "User activated successfully");
            loadUsers();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    
    @FXML
    public void handleDeactivateUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deactivation");
        confirm.setHeaderText("Deactivate User");
        confirm.setContentText("Are you sure you want to deactivate this user?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deactivateUser(selected.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deactivated");
                loadUsers();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleDeleteUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete User");
        confirm.setContentText("Are you sure you want to permanently delete this user? This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(selected.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted");
                loadUsers();
                loadAnalytics();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleViewAppointmentDetails() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an appointment");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Complete Appointment Information");
        
        String details = String.format(
            "User: %s\nStaff: %s\nDate: %s\nTime: %s - %s\nStatus: %s\n\nNotes: %s\n\nStaff Remarks: %s",
            selected.getUserName(),
            selected.getStaffName(),
            selected.getDate(),
            selected.getStartTime(),
            selected.getEndTime(),
            selected.getStatus(),
            selected.getNotes() != null ? selected.getNotes() : "None",
            selected.getStaffRemarks() != null ? selected.getStaffRemarks() : "None"
        );
        
        alert.setContentText(details);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
    
    @FXML
    public void handleViewReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        
        if (reviews.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Reviews", "No reviews available yet");
            return;
        }
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Reviews");
        dialog.setHeaderText("Customer Reviews and Ratings");
        
        TableView<Review> reviewTable = new TableView<>();
        reviewTable.setPrefHeight(400);
        reviewTable.setPrefWidth(700);
        
        TableColumn<Review, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUser().getName()));
        userCol.setPrefWidth(150);
        
        TableColumn<Review, Integer> orgRatingCol = new TableColumn<>("Org Rating");
        orgRatingCol.setCellValueFactory(new PropertyValueFactory<>("organizationRating"));
        orgRatingCol.setPrefWidth(100);
        
        TableColumn<Review, Integer> staffRatingCol = new TableColumn<>("Staff Rating");
        staffRatingCol.setCellValueFactory(new PropertyValueFactory<>("staffRating"));
        staffRatingCol.setPrefWidth(100);
        
        TableColumn<Review, String> commentCol = new TableColumn<>("Comment");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setPrefWidth(350);
        
        reviewTable.getColumns().addAll(userCol, orgRatingCol, staffRatingCol, commentCol);
        reviewTable.setItems(FXCollections.observableArrayList(reviews));
        
        dialog.getDialogPane().setContent(reviewTable);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    @FXML
    public void handleLogout() {
        SessionManager.getInstance().clearSession();
        stageManager.switchScene("/fxml/login.fxml", 400, 500);
    }
    
    private void clearStaffForm() {
        staffNameField.clear();
        staffUsernameField.clear();
        staffEmailField.clear();
        staffPasswordField.clear();
        roleComboBox.setValue(null);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
