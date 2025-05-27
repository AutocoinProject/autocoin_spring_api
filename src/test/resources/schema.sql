-- 테이블이 이미 존재한다면 삭제 (외래 키 제약조건 때문에 참조하는 테이블부터 뒤에 생성되는 테이블을 먼저 삭제)
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;

-- 테이블 생성 전에 외래 키 제약조건 비활성화
SET REFERENTIAL_INTEGRITY FALSE;

-- users 테이블 생성
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    provider VARCHAR(20),
    provider_id VARCHAR(255)
);

-- posts 테이블 생성
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    writer VARCHAR(255) NOT NULL,
    user_id BIGINT,
    file_url VARCHAR(255),
    file_name VARCHAR(255),
    file_key VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 테이블 생성 후 외래 키 제약조건 활성화
SET REFERENTIAL_INTEGRITY TRUE;
