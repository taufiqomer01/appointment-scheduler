package com.scheduler.service;

import com.scheduler.entity.Appointment;
import com.scheduler.repository.AppointmentRepository;
import com.scheduler.repository.ReviewRepository;
import com.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    
    public Map<String, Object> getSystemAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalUsers", userRepository.count());
        analytics.put("totalAppointments", appointmentRepository.count());
        analytics.put("pendingAppointments", appointmentRepository.countByStatus(Appointment.Status.PENDING));
        analytics.put("confirmedAppointments", appointmentRepository.countByStatus(Appointment.Status.CONFIRMED));
        analytics.put("completedAppointments", appointmentRepository.countByStatus(Appointment.Status.COMPLETED));
        analytics.put("cancelledAppointments", appointmentRepository.countByStatus(Appointment.Status.CANCELLED));
        analytics.put("noShowAppointments", appointmentRepository.countByStatus(Appointment.Status.NO_SHOW));
        analytics.put("averageOrgRating", reviewRepository.getAverageOrganizationRating());
        analytics.put("totalReviews", reviewRepository.count());
        
        return analytics;
    }
}
