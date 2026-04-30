package com.scheduler.repository;

import com.scheduler.entity.Review;
import com.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser(User user);
    
    @Query("SELECT AVG(r.organizationRating) FROM Review r")
    Double getAverageOrganizationRating();
    
    @Query("SELECT AVG(r.staffRating) FROM Review r WHERE r.appointment.staff = ?1")
    Double getAverageStaffRating(User staff);
}
