package com.scheduler.repository;

import com.scheduler.entity.Appointment;
import com.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUser(User user);
    List<Appointment> findByStaff(User staff);
    List<Appointment> findByStatus(Appointment.Status status);
    List<Appointment> findByDateBetween(LocalDate start, LocalDate end);
    List<Appointment> findByStaffAndDate(User staff, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.staff = ?1 AND a.date = ?2 AND a.status NOT IN ('CANCELLED', 'RESCHEDULED') AND ((a.startTime < ?4 AND a.endTime > ?3))")
    List<Appointment> findConflictingAppointments(User staff, LocalDate date, LocalTime startTime, LocalTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.staff = ?1 AND a.date = ?2 AND a.startTime = ?3 AND a.status = 'PENDING'")
    List<Appointment> findPendingByStaffDateAndStartTime(User staff, LocalDate date, LocalTime startTime);

    @Query("SELECT a FROM Appointment a WHERE a.staff = ?1 AND a.date = ?2 AND a.startTime = ?3 AND a.status = 'CONFIRMED'")
    List<Appointment> findConfirmedByStaffDateAndStartTime(User staff, LocalDate date, LocalTime startTime);

    long countByStatus(Appointment.Status status);
}
