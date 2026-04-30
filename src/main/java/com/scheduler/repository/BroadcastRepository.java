package com.scheduler.repository;

import com.scheduler.entity.Broadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    List<Broadcast> findByStatusOrderByCreatedAtDesc(Broadcast.Status status);
    List<Broadcast> findByTargetAudienceInAndStatusOrderByCreatedAtDesc(List<Broadcast.TargetAudience> audiences, Broadcast.Status status);
}
