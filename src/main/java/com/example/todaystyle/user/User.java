package com.example.todaystyle.user;

import com.example.todaystyle.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StyleCategory preferredStyle;
}
