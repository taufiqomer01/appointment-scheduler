package com.scheduler.controller;

import com.scheduler.dto.AppointmentDTO;
import com.scheduler.dto.UserDTO;
import com.scheduler.entity.Appointment;
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
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDashboardController {
    
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final StageManager stageManager;
    
    @FXML private Label welcomeLabel;
    @FXML private TableView<AppointmentDTO> appointmentsTable;
    @FXML private TableColumn<AppointmentDTO, String> staffColumn;
    @FXML private TableColumn<AppointmentDTO, LocalDate> dateColumn;
    @FXML private TableColumn<AppointmentDTO, LocalTime> timeColumn;
    @FXML private TableColumn<AppointmentDTO, String> statusColumn;
    @FXML private ComboBox<UserDTO> staffComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ListView<String> slotsListView;
    @FXML private TextArea notesArea;
    @FXML private TextField searchField;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label upcomingLabel;
    @FXML private Label completedLabel;
    
    private FilteredList<AppointmentDTO> filteredData;
    
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getCurrentUser().getName());
        
        staffColumn.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        staffComboBox.setCellFactory(param -> new ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        staffComboBox.setButtonCell(new ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        loadAppointments();
        loadStaff();
        updateStatistics();
        configureDatePicker(datePicker);
    }
    
    private void setupSearch() {
        filteredData = new FilteredList<>(appointmentsTable.getItems(), p -> true);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(appointment -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (appointment.getStaffName().toLowerCase().contains(lowerCaseFilter)) {
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
        Long userId = SessionManager.getInstance().getCurrentUser().getId();
        List<AppointmentDTO> appointments = appointmentService.getUserAppointments(userId);
        
        appointmentsTable.setItems(FXCollections.observableArrayList(appointments));
        setupSearch();
        updateStatistics();
    }
    
    private void updateStatistics() {
        Long userId = SessionManager.getInstance().getCurrentUser().getId();
        List<AppointmentDTO> appointments = appointmentService.getUserAppointments(userId);
        
        totalAppointmentsLabel.setText(String.valueOf(appointments.size()));
        
        long upcoming = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.PENDING || 
                            a.getStatus() == Appointment.Status.CONFIRMED)
                .count();
        upcomingLabel.setText(String.valueOf(upcoming));
        
        long completed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
                .count();
        completedLabel.setText(String.valueOf(completed));
    }
    
    private void loadStaff() {
        List<UserDTO> staff = userService.getAllStaff();
        staffComboBox.setItems(FXCollections.observableArrayList(staff));
    }
    
    @FXML
    public void handleDateSelected() {
        if (staffComboBox.getValue() != null && datePicker.getValue() != null) {
            loadAvailableSlots();
        }
    }
    
    @FXML
    public void handleStaffSelected() {
        if (staffComboBox.getValue() != null && datePicker.getValue() != null) {
            loadAvailableSlots();
        }
    }
    
    private void loadAvailableSlots() {
        Long staffId = staffComboBox.getValue().getId();
        LocalDate date = datePicker.getValue();
        List<String> allSlots = appointmentService.getAllSlotsWithStatus(staffId, date);
        slotsListView.setItems(FXCollections.observableArrayList(allSlots));
        slotsListView.setCellFactory(param -> slotCell(staffId, date));
    }
    
    @FXML
    public void handleBookAppointment() {
        if (staffComboBox.getValue() == null || datePicker.getValue() == null || 
            slotsListView.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select staff, date, and time slot");
            return;
        }
        
        try {
            String selectedSlot = slotsListView.getSelectionModel().getSelectedItem();
            String[] times = selectedSlot.split("-");
            
            AppointmentDTO dto = new AppointmentDTO();
            dto.setUserId(SessionManager.getInstance().getCurrentUser().getId());
            dto.setStaffId(staffComboBox.getValue().getId());
            dto.setDate(datePicker.getValue());
            dto.setStartTime(LocalTime.parse(times[0]));
            dto.setEndTime(LocalTime.parse(times[1]));
            dto.setNotes(notesArea.getText());
            
            appointmentService.bookAppointment(dto);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment booked successfully!");
            loadAppointments();
            clearForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    
    @FXML
    public void handleEditAppointment() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an appointment to edit");
            return;
        }
        
        // Users cannot edit confirmed appointments
        if (selected.getStatus() == Appointment.Status.CONFIRMED) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cannot edit a confirmed appointment. Please contact staff.");
            return;
        }
        
        if (selected.getStatus() != Appointment.Status.CANCELLED && 
            selected.getStatus() != Appointment.Status.RESCHEDULED && 
            selected.getStatus() != Appointment.Status.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Warning", "You can only edit pending, cancelled, or rescheduled appointments");
            return;
        }
        
        Dialog<AppointmentDTO> dialog = new Dialog<>();
        dialog.setTitle("Edit Appointment");
        dialog.setHeaderText("Modify appointment details");
        
        ButtonType submitButton = new ButtonType("Submit for Approval", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker newDate = new DatePicker(selected.getDate());
        configureDatePicker(newDate);
        ListView<String> slotsList = new ListView<>();
        TextArea newNotes = new TextArea(selected.getNotes());
        newNotes.setPrefRowCount(3);

        Runnable refreshSlots = () -> {
            if (newDate.getValue() != null) {
                List<String> slots = appointmentService.getAllSlotsWithStatus(selected.getStaffId(), newDate.getValue());
                slotsList.setItems(FXCollections.observableArrayList(slots));
                LocalDate d = newDate.getValue();
                slotsList.setCellFactory(param -> slotCell(selected.getStaffId(), d));
            }
        };

        newDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshSlots.run());
        refreshSlots.run();
        slotsList.setPrefHeight(150);
        
        grid.add(new Label("Date:"), 0, 0);
        grid.add(newDate, 1, 0);
        grid.add(new Label("Time Slot:"), 0, 1);
        grid.add(slotsList, 1, 1);
        grid.add(new Label("Notes:"), 0, 2);
        grid.add(newNotes, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                try {
                    String selectedSlot = slotsList.getSelectionModel().getSelectedItem();
                    if (selectedSlot == null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Please select a time slot");
                        return null;
                    }
                    String[] times = selectedSlot.split("-");
                    appointmentService.rescheduleAppointment(
                        selected.getId(),
                        newDate.getValue(),
                        LocalTime.parse(times[0]),
                        LocalTime.parse(times[1])
                    );
                    return selected;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
            return null;
        });
        
        Optional<AppointmentDTO> result = dialog.showAndWait();
        if (result.isPresent()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment updated and submitted for approval!");
            loadAppointments();
        }
    }
    
    @FXML
    public void handleCancelAppointment() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an appointment");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel Appointment");
        confirm.setContentText("Are you sure you want to cancel this appointment?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                appointmentService.updateStatus(selected.getId(), Appointment.Status.CANCELLED, null);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment cancelled");
                loadAppointments();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleSubmitReview() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a completed appointment");
            return;
        }
        
        if (selected.getStatus() != Appointment.Status.COMPLETED) {
            showAlert(Alert.AlertType.WARNING, "Warning", "You can only review completed appointments");
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Submit Review");
        dialog.setHeaderText("Rate your experience");
        
        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Slider orgRating = new Slider(1, 5, 5);
        orgRating.setShowTickLabels(true);
        orgRating.setShowTickMarks(true);
        orgRating.setMajorTickUnit(1);
        orgRating.setBlockIncrement(1);
        orgRating.setSnapToTicks(true);
        
        Slider staffRating = new Slider(1, 5, 5);
        staffRating.setShowTickLabels(true);
        staffRating.setShowTickMarks(true);
        staffRating.setMajorTickUnit(1);
        staffRating.setBlockIncrement(1);
        staffRating.setSnapToTicks(true);
        
        TextArea comment = new TextArea();
        comment.setPrefRowCount(4);
        comment.setPromptText("Share your experience...");
        
        grid.add(new Label("Organization Rating:"), 0, 0);
        grid.add(orgRating, 1, 0);
        grid.add(new Label("Staff Rating:"), 0, 1);
        grid.add(staffRating, 1, 1);
        grid.add(new Label("Comments:"), 0, 2);
        grid.add(comment, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitButton) {
            try {
                reviewService.submitReview(
                    SessionManager.getInstance().getCurrentUser().getId(),
                    selected.getId(),
                    (int) orgRating.getValue(),
                    (int) staffRating.getValue(),
                    comment.getText()
                );
                showAlert(Alert.AlertType.INFORMATION, "Success", "Thank you for your review!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleLogout() {
        SessionManager.getInstance().clearSession();
        stageManager.switchScene("/fxml/login.fxml", 400, 500);
    }
    
    private void configureDatePicker(DatePicker dp) {
        dp.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || !date.isAfter(LocalDate.now()) ||
                        date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
            }
        });
    }

    private ListCell<String> slotCell(Long staffId, LocalDate date) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); setDisable(false); return; }
                setText(item);
                AppointmentService.SlotStatus status = appointmentService.getSlotStatus(staffId, date, item);
                switch (status) {
                    case AVAILABLE -> { setStyle("-fx-padding: 10; -fx-background-color: #e8f5e9; -fx-background-radius: 5;"); setDisable(false); }
                    case PENDING  -> { setStyle("-fx-padding: 10; -fx-background-color: #fff9c4; -fx-background-radius: 5;"); setDisable(false); }
                    case FULL     -> { setStyle("-fx-padding: 10; -fx-background-color: #e0e0e0; -fx-text-fill: #9e9e9e; -fx-background-radius: 5;"); setDisable(true); }
                }
            }
        };
    }

    private void clearForm() {
        staffComboBox.setValue(null);
        datePicker.setValue(null);
        slotsListView.getItems().clear();
        notesArea.clear();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
