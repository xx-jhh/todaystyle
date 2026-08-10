package com.example.todaystyle.ootd;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OotdRepository extends JpaRepository<OotdRecord, Long> {

    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    List<OotdRecord> findByUserIdOrderByRecordDateDesc(Long userId);

    Optional<OotdRecord> findByIdAndUserId(Long id, Long userId);
}
