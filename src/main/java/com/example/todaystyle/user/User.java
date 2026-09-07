package com.example.todaystyle.user;

import com.example.todaystyle.common.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * 비밀번호가 마지막으로 바뀐 시각. 발급된 JWT의 iat(발급시각)가 이 값보다 이르면
     * {@link com.example.todaystyle.security.JwtAuthenticationFilter}가 그 토큰을 무효 처리한다
     * — 그렇지 않으면 비밀번호를 재설정해도 이미 발급된 토큰(탈취된 세션 등)이 만료 전까지
     * 계속 유효하게 남는다.
     */
    @Column(nullable = false)
    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    private String nickname;

    /** cm 단위 */
    private Integer height;

    /** kg 단위 */
    private Integer weight;

    /** 인치 단위 */
    private Integer waistInch;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BodyType bodyType;

    /** 여러 스타일을 동시에 선호할 수 있어(예: 캐주얼+미니멀) 복수 선택으로 저장한다. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_styles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false, length = 20)
    private Set<StyleCategory> preferredStyles = new HashSet<>();
}
