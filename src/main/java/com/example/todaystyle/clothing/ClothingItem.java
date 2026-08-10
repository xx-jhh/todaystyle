package com.example.todaystyle.clothing;

import com.example.todaystyle.common.BaseTimeEntity;
import com.example.todaystyle.ootd.OotdRecord;
import com.example.todaystyle.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OOTD 사진에서 추출된 개별 옷 아이템. 같은 사용자의 서로 다른 날짜 아이템끼리
 * 조합 추천(코디 매칭)의 대상이 된다.
 */
@Entity
@Table(name = "clothing_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClothingItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 이 아이템이 추출된 OOTD 업로드 기록 (착용 날짜 = ootdRecord.recordDate) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ootd_record_id", nullable = false)
    private OotdRecord ootdRecord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClothingCategory category;

    private String color;

    /**
     * 옷의 실루엣. 업로드 시 이미지 인식 파이프라인이 자동으로 채우며, 파이프라인이
     * 아직 연동되지 않았거나 판별에 실패한 경우 null일 수 있다.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Fit fit;

    /** 크롭된 아이템 이미지 URL (없으면 원본 OOTD 사진을 참조) */
    private String imageUrl;
}
