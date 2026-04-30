package com.scheduler.service;

import com.scheduler.dto.AppointmentDTO;
import com.scheduler.entity.Appointment;
import com.scheduler.entity.User;
import com.scheduler.repository.AppointmentRepository;
import com.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    
    private static final int MAX_PENDING_PER_SLOT = 3;

    private static final List<String> ALL_SLOTS = List.of(
        "09:00-10:00", "10:00-11:00", "11:00-12:00",
        "12:00-13:00", "13:00-14:00", "14:00-15:00",
        "15:00-16:00", "16:00-17:00"
    );

    @Transactional
    public AppointmentDTO bookAppointment(AppointmentDTO dto) {
        if (dto.getDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new RuntimeException("Appointments can only be booked for future dates");
        }
        if (dto.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Appointments cannot be booked on Sundays");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User staff = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Block if slot already has a confirmed appointment
        List<Appointment> confirmed = appointmentRepository.findConfirmedByStaffDateAndStartTime(
                staff, dto.getDate(), dto.getStartTime());
        if (!confirmed.isEmpty()) {
            throw new RuntimeException("This slot is already confirmed and cannot be booked");
        }

        // Block if pending count has reached max
        List<Appointment> pending = appointmentRepository.findPendingByStaffDateAndStartTime(
                staff, dto.getDate(), dto.getStartTime());
        if (pending.size() >= MAX_PENDING_PER_SLOT) {
            throw new RuntimeException("This slot is fully booked (max " + MAX_PENDING_PER_SLOT + " pending appointments)");
        }

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setStaff(staff);
        appointment.setDate(dto.getDate());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setNotes(dto.getNotes());

        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }
    
    @Transactional
    public AppointmentDTO updateStatus(Long id, Appointment.Status status, String staffRemarks) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() == Appointment.Status.CANCELLED) {
            throw new RuntimeException("Cannot update a cancelled appointment");
        }
        if (appointment.getStatus() == Appointment.Status.CONFIRMED && status == Appointment.Status.CANCELLED) {
            throw new RuntimeException("Cannot cancel a confirmed appointment. Please contact admin.");
        }

        appointment.setStatus(status);
        if (staffRemarks != null && !staffRemarks.isEmpty()) {
            appointment.setStaffRemarks(staffRemarks);
        }
        appointment = appointmentRepository.save(appointment);

        // When confirming, reschedule all other pending appointments in the same slot
        if (status == Appointment.Status.CONFIRMED) {
            reschedulePendingConflicts(appointment);
        }

        return toDTO(appointment);
    }

    private void reschedulePendingConflicts(Appointment confirmed) {
        List<Appointment> others = appointmentRepository.findPendingByStaffDateAndStartTime(
                confirmed.getStaff(), confirmed.getDate(), confirmed.getStartTime());
        for (Appointment other : others) {
            LocalDate newDate = confirmed.getDate();
            LocalTime newStart = confirmed.getStartTime();
            LocalTime newEnd = confirmed.getEndTime();
            boolean found = false;

            // Search up to 14 days ahead for the nearest available slot
            outer:
            for (int d = 0; d < 14 && !found; d++) {
                LocalDate candidate = confirmed.getDate().plusDays(d);
                if (candidate.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                for (String slot : ALL_SLOTS) {
                    String[] parts = slot.split("-");
                    LocalTime s = LocalTime.parse(parts[0]);
                    LocalTime e = LocalTime.parse(parts[1]);
                    if (d == 0 && !s.isAfter(confirmed.getStartTime())) continue;
                    List<Appointment> conf = appointmentRepository.findConfirmedByStaffDateAndStartTime(
                            confirmed.getStaff(), candidate, s);
                    List<Appointment> pend = appointmentRepository.findPendingByStaffDateAndStartTime(
                            confirmed.getStaff(), candidate, s);
                    if (conf.isEmpty() && pend.size() < MAX_PENDING_PER_SLOT) {
                        newDate = candidate;
                        newStart = s;
                        newEnd = e;
                        found = true;
                        break outer;
                    }
                }
            }

            other.setDate(newDate);
            other.setStartTime(newStart);
            other.setEndTime(newEnd);
            other.setStatus(Appointment.Status.RESCHEDULED);
            appointmentRepository.save(other);
        }
    }
    
    @Transactional
    public AppointmentDTO rescheduleAppointment(Long id, LocalDate newDate, 
                                                 java.time.LocalTime newStartTime, 
                                                 java.time.LocalTime newEndTime) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                appointment.getStaff(), newDate, newStartTime, newEndTime);
        
        if (!conflicts.isEmpty() && !conflicts.get(0).getId().equals(id)) {
            throw new RuntimeException("New time slot is not available");
        }
        
        appointment.setDate(newDate);
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newEndTime);
        appointment.setStatus(Appointment.Status.RESCHEDULED);
        
        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }
    
    public List<AppointmentDTO> getUserAppointments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return appointmentRepository.findByUser(user).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getStaffAppointments(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        return appointmentRepository.findByStaff(staff).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<String> getAvailableSlots(Long staffId, LocalDate date) {
        return ALL_SLOTS.stream()
                .filter(slot -> getSlotStatus(staffId, date, slot) != SlotStatus.FULL)
                .collect(Collectors.toList());
    }

    public List<String> getAllSlotsWithStatus(Long staffId, LocalDate date) {
        return ALL_SLOTS;
    }

    public enum SlotStatus { AVAILABLE, PENDING, FULL }

    public SlotStatus getSlotStatus(Long staffId, LocalDate date, String slot) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        LocalTime start = LocalTime.parse(slot.split("-")[0]);
        List<Appointment> confirmed = appointmentRepository.findConfirmedByStaffDateAndStartTime(staff, date, start);
        if (!confirmed.isEmpty()) return SlotStatus.FULL;
        List<Appointment> pending = appointmentRepository.findPendingByStaffDateAndStartTime(staff, date, start);
        if (pending.size() >= MAX_PENDING_PER_SLOT) return SlotStatus.FULL;
        if (!pending.isEmpty()) return SlotStatus.PENDING;
        return SlotStatus.AVAILABLE;
    }

    public boolean isSlotAvailableForStaff(Long staffId, LocalDate date, String slot) {
        return getSlotStatus(staffId, date, slot) != SlotStatus.FULL;
    }
    
    private AppointmentDTO toDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserName(appointment.getUser().getName());
        dto.setStaffId(appointment.getStaff().getId());
        dto.setStaffName(appointment.getStaff().getName());
        dto.setDate(appointment.getDate());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setStaffRemarks(appointment.getStaffRemarks());
        return dto;
    }
}
