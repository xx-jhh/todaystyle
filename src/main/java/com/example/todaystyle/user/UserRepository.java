package com.example.todaystyle.user;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * 인증 필터가 매 요청마다 호출한다(JWT 발급시각과 비교용) — preferredStyles까지 EAGER로
     * 물고 오는 findById 대신 필요한 컬럼 하나만 가벼운 쿼리로 가져온다.
     */
    @Query("select u.passwordChangedAt from User u where u.id = :id")
    Optional<LocalDateTime> findPasswordChangedAtById(@Param("id") Long id);
}
