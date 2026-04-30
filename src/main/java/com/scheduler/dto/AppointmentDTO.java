package com.scheduler.dto;

import com.scheduler.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long staffId;
    private String staffName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Appointment.Status status;
    private String notes;
    private String staffRemarks;
}
