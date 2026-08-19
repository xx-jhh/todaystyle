package com.example.todaystyle.clothing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

    /** 내 모든 옷 아이템 (착용 날짜 참조를 위해 OOTD를 함께 로딩). */
    @Query("select ci from ClothingItem ci join fetch ci.ootdRecord where ci.user.id = :userId")
    List<ClothingItem> findByUserIdWithOotd(@Param("userId") Long userId);

    /** 특정 카테고리의 내 옷 아이템 (OOTD 함께 로딩). */
    @Query("select ci from ClothingItem ci join fetch ci.ootdRecord "
            + "where ci.user.id = :userId and ci.category = :category")
    List<ClothingItem> findByUserIdAndCategoryWithOotd(
            @Param("userId") Long userId,
            @Param("category") ClothingCategory category);

    /** OOTD 삭제 시 그 안에서 추출된 옷 아이템도 함께 지운다(FK 제약 때문에 먼저 지워야 함). */
    void deleteByOotdRecordId(Long ootdRecordId);
}
