# JWT Authentication System for Autocoin Spring API

This project implements a JWT-based authentication system for the Autocoin Spring API.

## Features

- User signup with email and password validation
- User login with JWT token generation
- JWT tokens include user_id, email, and expiration time
- Token verification for protected endpoints
- CORS configuration for cross-domain requests
- Environment variable management with .env file

## Authentication Endpoints

- `POST /signup` - Create a new user account
- `POST /login` - Authenticate a user and get a JWT token
- `GET /me` - Get current user information (requires authentication)

## Configuration

### Environment Variables

Create a `.env` file in the project root with the following variables:

```
# Database Configuration
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_should_be_at_least_64_characters_long_for_better_security
JWT_EXPIRATION=1800000  # 30 minutes in milliseconds

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### JWT Secret Key

The JWT secret key should be kept secure and not committed to version control. It should be at least 64 characters long for better security. You can generate a secure random key using:

```bash
openssl rand -base64 64
```

## Usage

### Signup

```
POST /signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123!",
  "username": "JohnDoe"
}
```

### Login

```
POST /login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123!"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "username": "JohnDoe",
    "role": "ROLE_USER",
    "createdAt": "2023-05-05T12:34:56",
    "updatedAt": "2023-05-05T12:34:56"
  }
}
```

### Access Protected Endpoints

Use the provided JWT token in the Authorization header for protected endpoints:

```
GET /me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Security Considerations

- The JWT secret key should be secure and not committed to version control
- Passwords are hashed using BCrypt before being stored in the database
- JWT tokens expire after a configurable amount of time (default: 30 minutes)
- CORS is configured to only allow requests from specified origins
