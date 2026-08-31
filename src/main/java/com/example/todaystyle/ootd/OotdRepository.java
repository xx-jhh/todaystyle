package com.example.todaystyle.ootd;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OotdRepository extends JpaRepository<OotdRecord, Long> {

    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    long countByUserId(Long userId);

    Page<OotdRecord> findByUserIdOrderByRecordDateDesc(Long userId, Pageable pageable);

    Optional<OotdRecord> findByIdAndUserId(Long id, Long userId);
}
