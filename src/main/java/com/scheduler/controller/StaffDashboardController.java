package com.scheduler.controller;

import com.scheduler.dto.AppointmentDTO;
import com.scheduler.entity.Appointment;
import com.scheduler.service.AppointmentService;
import com.scheduler.service.ReviewService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StaffDashboardController {
    
    private final AppointmentService appointmentService;
    private final ReviewService reviewService;
    private final StageManager stageManager;
    
    @FXML private Label welcomeLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label completedLabel;
    @FXML private Label avgRatingLabel;
    @FXML private TableView<AppointmentDTO> appointmentsTable;
    @FXML private TableColumn<AppointmentDTO, String> userColumn;
    @FXML private TableColumn<AppointmentDTO, LocalDate> dateColumn;
    @FXML private TableColumn<AppointmentDTO, LocalTime> timeColumn;
    @FXML private TableColumn<AppointmentDTO, String> statusColumn;
    @FXML private ComboBox<Appointment.Status> statusComboBox;
    @FXML private TextField searchField;
    
    private FilteredList<AppointmentDTO> filteredData;
    
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getCurrentUser().getName());
        
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        statusComboBox.setItems(FXCollections.observableArrayList(Appointment.Status.values()));
        
        loadAppointments();
        updateStatistics();
    }
    
    private void setupSearch() {
        filteredData = new FilteredList<>(appointmentsTable.getItems(), p -> true);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(appointment -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (appointment.getUserName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (appointment.getStatus().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (appointment.getDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        SortedList<AppointmentDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(appointmentsTable.comparatorProperty());
        appointmentsTable.setItems(sortedData);
    }
    
    private void loadAppointments() {
        Long staffId = SessionManager.getInstance().getCurrentUser().getId();
        List<AppointmentDTO> appointments = appointmentService.getStaffAppointments(staffId);
        
        appointmentsTable.setItems(FXCollections.observableArrayList(appointments));
        setupSearch();
        updateStatistics();
    }
    
    private void updateStatistics() {
        Long staffId = SessionManager.getInstance().getCurrentUser().getId();
        List<AppointmentDTO> appointments = appointmentService.getStaffAppointments(staffId);
        
        totalAppointmentsLabel.setText(String.valueOf(appointments.size()));
        
        long today = appointments.stream()
                .filter(a -> a.getDate().equals(LocalDate.now()))
                .count();
        todayAppointmentsLabel.setText(String.valueOf(today));
        
        long completed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
                .count();
        completedLabel.setText(String.valueOf(completed));
        
        Double avgRating = reviewService.getAverageStaffRating(staffId);
        avgRatingLabel.setText(String.format("%.1f", avgRating != null ? avgRating : 0.0));
    }
    
    @FXML
    public void handleUpdateStatus() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        Appointment.Status newStatus = statusComboBox.getValue();
        
        if (selected == null || newStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an appointment and status");
            return;
        }
        
        if (newStatus == Appointment.Status.COMPLETED) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Complete Appointment");
            dialog.setHeaderText("Add your remarks about the session");
            
            ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextArea remarks = new TextArea();
            remarks.setPrefRowCount(5);
            remarks.setPromptText("Enter your remarks about the session...");
            remarks.setWrapText(true);
            
            grid.add(new Label("Remarks:"), 0, 0);
            grid.add(remarks, 0, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == submitButton) {
                    return remarks.getText();
                }
                return null;
            });
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    appointmentService.updateStatus(selected.getId(), newStatus, result.get());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment marked as completed with remarks");
                    loadAppointments();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        } else {
            try {
                appointmentService.updateStatus(selected.getId(), newStatus, null);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated successfully");
                loadAppointments();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleViewDetails() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an appointment");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Appointment Information");
        
        String details = String.format(
            "User: %s\nDate: %s\nTime: %s - %s\nStatus: %s\nNotes: %s\nRemarks: %s",
            selected.getUserName(),
            selected.getDate(),
            selected.getStartTime(),
            selected.getEndTime(),
            selected.getStatus(),
            selected.getNotes() != null ? selected.getNotes() : "None",
            selected.getStaffRemarks() != null ? selected.getStaffRemarks() : "None"
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    @FXML
    public void handleLogout() {
        SessionManager.getInstance().clearSession();
        stageManager.switchScene("/fxml/login.fxml", 400, 500);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
