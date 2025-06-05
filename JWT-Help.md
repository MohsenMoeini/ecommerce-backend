# JWT Authentication Implementation Guide

## Overview
This document outlines the JWT (JSON Web Token) authentication implementation for the e-commerce application. JWT provides a stateless, secure method for authenticating users and protecting API endpoints.

## Components

### 1. Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2. Configuration
JWT settings are configured in `application.yml`:
```yaml
application:
  security:
    jwt:
      secret-key: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
      expiration: 86400000 # 1 day in milliseconds
      refresh-token:
        expiration: 604800000 # 7 days in milliseconds
```

### 3. Core Security Classes

#### JwtService
- Handles token generation, validation, and parsing
- Extracts user information from tokens
- Verifies token signatures and expiration

#### JwtAuthenticationFilter
- Intercepts all HTTP requests
- Extracts JWT from Authorization header
- Validates tokens and sets up security context

#### SecurityConfig
- Configures security rules and permissions
- Defines protected and public endpoints
- Sets up authentication provider and filters

#### UserDetailsServiceImpl
- Loads user details for authentication
- Maps database user entity to Spring Security's UserDetails

### 4. Authentication Flow

1. **Registration/Login**:
   - User submits credentials via `/api/v1/auth/register` or `/api/v1/auth/login`
   - Server validates credentials and generates JWT tokens
   - Response includes access token and refresh token

2. **API Access**:
   - Client includes token in Authorization header: `Bearer <token>`
   - JwtAuthenticationFilter validates token for each request
   - Security context is populated with user details if token is valid

3. **Token Refresh**:
   - When access token expires, client uses refresh token
   - `/api/v1/auth/refresh-token` endpoint issues new access token
   - Original refresh token remains valid until its expiration

## Security Features

- **Secure Password Storage**: BCrypt hashing for passwords
- **Role-Based Authorization**: Different access rights for customers and admins
- **Token Expiration**: Configurable token lifetimes
- **CSRF Protection**: Cross-Site Request Forgery prevention
- **CORS Support**: Configured for secure communication with frontend

## Frontend Integration

1. Store tokens securely (memory for SPA or HTTP-only cookies)
2. Include token in all API requests:
   ```javascript
   fetch('/api/secured-endpoint', {
     headers: {
       'Authorization': `Bearer ${accessToken}`
     }
   })
   ```
3. Implement token refresh logic when access token expires
4. Redirect to login when authentication fails

## Error Handling

The system provides standardized error responses for various authentication scenarios:
- Invalid credentials
- Expired tokens
- Insufficient permissions
- Invalid token format

## API Endpoints

- **POST /api/v1/auth/register**: Create new user account
- **POST /api/v1/auth/login**: Authenticate and receive tokens
- **POST /api/v1/auth/refresh-token**: Get new access token using refresh token