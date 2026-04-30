package com.scheduler;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppointmentSchedulerApplication {
    
    public static void main(String[] args) {
        Application.launch(JavaFXApplication.class, args);
    }
}
