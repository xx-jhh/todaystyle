-- 현재 엔티티(User/OotdRecord/ClothingItem)가 ddl-auto:update로 실제 운영 DB에 만들어 온
-- 스키마를 그대로 베이스라인으로 옮긴 것. 이 시점부터는 Flyway가 스키마 변경 이력을 관리하고,
-- Hibernate는 ddl-auto:validate로 엔티티-스키마 불일치만 검증한다(auto-DDL로 직접 바꾸지 않음).

CREATE TABLE users (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(255) NOT NULL,
    password         VARCHAR(255) NOT NULL,
    nickname         VARCHAR(255) NOT NULL,
    height           INT          NULL,
    weight           INT          NULL,
    waist_inch       INT          NULL,
    body_type        ENUM('STRAIGHT', 'WAVE', 'NATURAL') NULL,
    preferred_style  ENUM('CASUAL', 'AMEKAJI', 'STREET', 'MINIMAL', 'FORMAL', 'VINTAGE') NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE ootd_records (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                BIGINT       NOT NULL,
    record_date            DATE         NOT NULL,
    photo_url              VARCHAR(255) NOT NULL,
    weather_temp           DOUBLE       NULL,
    weather_sky            VARCHAR(255) NULL,
    weather_precipitation  VARCHAR(255) NULL,
    memo                   TEXT         NULL,
    created_at             DATETIME(6)  NOT NULL,
    updated_at             DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_ootd_records_user_date UNIQUE (user_id, record_date),
    CONSTRAINT fk_ootd_records_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE clothing_items (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    ootd_record_id  BIGINT       NOT NULL,
    category        ENUM('TOP', 'BOTTOM', 'OUTER', 'SHOES', 'ACCESSORY', 'DRESS') NOT NULL,
    color           VARCHAR(255) NULL,
    fit             ENUM('SLIM', 'REGULAR', 'LOOSE', 'OVERSIZED') NULL,
    image_url       VARCHAR(255) NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_clothing_items_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_clothing_items_ootd_record FOREIGN KEY (ootd_record_id) REFERENCES ootd_records (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
