# Autocoin Spring API

This is the backend API for the Autocoin application, built with Spring Boot.

## Project Structure

The project follows a Clean Architecture approach with the following structure:

```
autocoin_spring_api/
├── src/
│   ├── main/
│   │   ├── java/com/autocoin/
│   │   │
│   │   │   ├── AutocoinSpringApiApplication.java  # 메인 실행 파일
│   │   │
│   │   │   ├── global/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── S3Config.java
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── CustomException.java
│   │   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── util/
│   │   │   │   │   ├── S3Uploader.java
│   │   │   │   │   ├── JwtUtil.java
│   │   │   │   │   ├── PasswordEncoderUtil.java
│   │   │
│   │   │   ├── user/
│   │   │   │   ├── application/
│   │   │   │   │   ├── UserService.java
│   │   │   │   ├── domain/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── UserRepositoryImpl.java
│   │   │   │   ├── api/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── UserSignupRequestDto.java
│   │   │   │   │   ├── UserLoginRequestDto.java
```

## Features

- JWT-based authentication
- User management (signup, login)
- Exception handling
- Clean Architecture with separation of concerns

## Getting Started

### Prerequisites

- Java 17+
- MySQL
- Gradle

### Running the Application

1. Clone the repository
2. Configure application.yml with your database credentials
3. Run the application:

```bash
./gradlew bootRun
```

## API Endpoints

### Authentication

- POST /api/auth/signup - Register a new user
- POST /api/auth/login - Authenticate and get JWT token

### User

- GET /api/user/me - Get current user information (requires authentication)
