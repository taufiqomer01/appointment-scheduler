package com.scheduler.service;

import com.scheduler.entity.Appointment;
import com.scheduler.entity.Review;
import com.scheduler.entity.User;
import com.scheduler.repository.AppointmentRepository;
import com.scheduler.repository.ReviewRepository;
import com.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Transactional
    public Review submitReview(Long userId, Long appointmentId, 
                               Integer orgRating, Integer staffRating, String comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        Review review = new Review();
        review.setUser(user);
        review.setAppointment(appointment);
        review.setOrganizationRating(orgRating);
        review.setStaffRating(staffRating);
        review.setComment(comment);
        
        return reviewRepository.save(review);
    }
    
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    
    public Double getAverageOrganizationRating() {
        Double avg = reviewRepository.getAverageOrganizationRating();
        return avg != null ? avg : 0.0;
    }
    
    public Double getAverageStaffRating(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        Double avg = reviewRepository.getAverageStaffRating(staff);
        return avg != null ? avg : 0.0;
    }
}
