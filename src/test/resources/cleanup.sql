-- 게시글 테이블 초기화
DELETE FROM posts;
-- 사용자 테이블 초기화
DELETE FROM users;

-- 테스트용 사용자 데이터 삽입
INSERT INTO users (email, password, username, role, created_at, updated_at) VALUES
('test1@test.com', '$2a$10$ePD9i8bIbVjxGRh7nWlvQexhFXvEUXlV8OEhJN1d6PhaT88X749lW', 'testuser1', 'ROLE_USER', NOW(), NOW()),
('test2@test.com', '$2a$10$ePD9i8bIbVjxGRh7nWlvQexhFXvEUXlV8OEhJN1d6PhaT88X749lW', 'testuser2', 'ROLE_USER', NOW(), NOW());