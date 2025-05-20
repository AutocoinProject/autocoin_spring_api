
-- 테스트용 사용자 데이터 삽입
INSERT INTO users (email, password, username, role, created_at, updated_at) VALUES
('test1@test.com', '$2a$10$ePD9i8bIbVjxGRh7nWlvQexhFXvEUXlV8OEhJN1d6PhaT88X749lW', 'testuser1', 'ROLE_USER', NOW(), NOW()),
('test2@test.com', '$2a$10$ePD9i8bIbVjxGRh7nWlvQexhFXvEUXlV8OEhJN1d6PhaT88X749lW', 'testuser2', 'ROLE_USER', NOW(), NOW());

-- 테스트용 게시글 데이터는 각 테스트에서 직접 추가하므로 여기서는 추가하지 않음
-- INSERT INTO posts (title, content, writer, user_id, created_at, updated_at) VALUES
-- ('테스트 게시글 1', '테스트 내용 1', '테스트 작성자 1', 1, NOW(), NOW()),
-- ('테스트 게시글 2', '테스트 내용 2', '테스트 작성자 2', 2, NOW(), NOW());
