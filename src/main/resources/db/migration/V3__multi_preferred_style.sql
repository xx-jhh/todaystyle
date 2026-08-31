-- 선호 스타일을 단일 선택에서 복수 선택으로 바꾼다. 기존 users.preferred_style(단일 값)을
-- 새 테이블로 옮기고 원래 컬럼은 지운다 — 이미 가입한 사용자가 고른 값을 잃지 않기 위해
-- DROP 전에 반드시 데이터를 옮긴다.
CREATE TABLE user_preferred_styles (
    user_id BIGINT      NOT NULL,
    style   VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, style),
    CONSTRAINT fk_user_preferred_styles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO user_preferred_styles (user_id, style)
SELECT id, preferred_style FROM users WHERE preferred_style IS NOT NULL;

ALTER TABLE users DROP COLUMN preferred_style;
