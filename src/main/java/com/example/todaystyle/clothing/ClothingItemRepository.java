package com.example.todaystyle.clothing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

    /** 내 모든 옷 아이템 (착용 날짜 참조를 위해 OOTD를 함께 로딩, 최신 등록순, 페이지네이션). */
    @Query(
            value = "select ci from ClothingItem ci join fetch ci.ootdRecord "
                    + "where ci.user.id = :userId order by ci.id desc",
            countQuery = "select count(ci) from ClothingItem ci where ci.user.id = :userId")
    Page<ClothingItem> findByUserIdWithOotd(@Param("userId") Long userId, Pageable pageable);

    /** 내 옷 아이템 단건 (수정/삭제 전 소유권 확인용, OOTD 함께 로딩). */
    @Query("select ci from ClothingItem ci join fetch ci.ootdRecord "
            + "where ci.id = :id and ci.user.id = :userId")
    Optional<ClothingItem> findByIdAndUserIdWithOotd(@Param("id") Long id, @Param("userId") Long userId);

    /** 특정 카테고리의 내 옷 아이템 (OOTD 함께 로딩). */
    @Query("select ci from ClothingItem ci join fetch ci.ootdRecord "
            + "where ci.user.id = :userId and ci.category = :category")
    List<ClothingItem> findByUserIdAndCategoryWithOotd(
            @Param("userId") Long userId,
            @Param("category") ClothingCategory category);

    /** 특정 OOTD 기록에 이미 태깅된 내 옷 아이템들 (수동 태깅 화면에서 중복 등록을 막기 위해 먼저 보여줌). */
    @Query("select ci from ClothingItem ci join fetch ci.ootdRecord "
            + "where ci.ootdRecord.id = :ootdRecordId and ci.user.id = :userId order by ci.id asc")
    List<ClothingItem> findByOotdRecordIdAndUserIdWithOotd(
            @Param("ootdRecordId") Long ootdRecordId,
            @Param("userId") Long userId);

    /** OOTD 삭제 시 그 안에서 추출된 옷 아이템도 함께 지운다(FK 제약 때문에 먼저 지워야 함). */
    void deleteByOotdRecordId(Long ootdRecordId);
}
