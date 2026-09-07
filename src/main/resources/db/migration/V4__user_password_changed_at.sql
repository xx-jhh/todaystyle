-- 비밀번호 재설정 후에도 이미 발급된 JWT가 계속 유효하던 문제를 막기 위해, 마지막으로
-- 비밀번호가 바뀐 시각을 기록해 토큰 발급시각(iat)과 비교할 수 있게 한다.
ALTER TABLE users
    ADD COLUMN password_changed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

-- 기존 유저는 가입 시점을 기준으로 잡는다 — 그 이전에 발급된 토큰은 있을 수 없으므로,
-- 이 마이그레이션 배포만으로 기존 로그인 세션이 전부 로그아웃되는 일은 없다.
UPDATE users SET password_changed_at = created_at;
