package com.scheduler.config;

import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class JavaFXConfig {
    
    @Bean
    @Scope("prototype")
    public Stage stage() {
        return new Stage();
    }
}
