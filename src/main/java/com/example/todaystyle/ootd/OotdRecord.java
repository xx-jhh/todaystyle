package com.example.todaystyle.ootd;

import com.example.todaystyle.common.BaseTimeEntity;
import com.example.todaystyle.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자가 하루에 한 번 업로드하는 착장(OOTD) 기록. 여기서 추출된 개별 아이템은
 * {@link com.example.todaystyle.clothing.ClothingItem}으로 저장된다.
 */
@Entity
@Table(name = "ootd_records", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "record_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OotdRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 실제로 착장한 날짜 (업로드 시각인 createdAt과는 별개) */
    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private String photoUrl;

    /** 업로드 시점 날씨 스냅샷. 위경도 미제공/기상청 API 실패 시 null(선택 정보). */
    private Double weatherTemp;
    private String weatherSky;
    private String weatherPrecipitation;

    /** 사용자가 상세화면에서 직접 작성하는 코디 메모 (선택). */
    @Column(columnDefinition = "TEXT")
    private String memo;
}
