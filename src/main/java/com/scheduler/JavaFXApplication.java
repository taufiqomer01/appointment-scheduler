package com.scheduler;

import com.scheduler.util.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {
    
    private ConfigurableApplicationContext context;
    
    @Override
    public void init() {
        this.context = new SpringApplicationBuilder(AppointmentSchedulerApplication.class).run();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        StageManager stageManager = context.getBean(StageManager.class);
        stageManager.setPrimaryStage(primaryStage);
        
        primaryStage.setTitle("Appointment Scheduler");
        stageManager.switchScene("/fxml/login.fxml", 400, 500);
    }
    
    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}
