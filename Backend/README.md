# GoMuKu Backend

A multi-player Gomoku (Five-in-a-Row) game backend built with Spring Boot and Java 17.

## Overview

GoMuKu Backend is a robust, scalable backend system for the classic Gomoku game. It provides real-time multiplayer functionality, user management, game room creation, and intelligent player matching. The system is built using modern Java technologies and follows a modular Maven multi-module architecture with clear separation of concerns.

## Features

- 🎮 **Real-time Multiplayer Gaming** - Play Gomoku with other players in real-time
- 👥 **User Management** - User registration, authentication, and profile management
- 🏠 **Game Rooms** - Create and join game rooms with customizable settings
- ⚡ **Intelligent Matching** - Automatic player matching based on skill level and preferences
- 🎯 **Game Logic** - Complete Gomoku game engine with win detection
- 📊 **Statistics Tracking** - Track wins, losses, and player statistics
- 🔒 **Secure** - Built with security best practices

## Architecture

This project follows a multi-module Maven architecture with clear separation of concerns:

```
gomoku-backend/
├── common/          # Shared utilities, validation frameworks, and AOP aspects
├── controller/      # REST API controllers and web layer (main Spring Boot app)
├── biz/            # Business logic layer
├── dao/            # Data access objects, entities, and MyBatis mappers
├── gomoku/         # Game logic and mechanics
├── user/           # User management functionality
├── matching/       # Player matching system
├── room/           # Game room management
└── security/       # Security configurations and authentication
```

### Key Architectural Features

- **Custom Validation Frameworks**: The `common` module provides two AOP-based validation systems:
  - **BasicCheck**: Lightweight parameter validation with annotations (`@CheckNull`, `@CheckString`, etc.)
  - **ValueChecker**: Advanced validation with SpEL expressions and custom handlers
- **MyBatis Dynamic SQL**: Modern ORM approach with code generation support
- **Multi-Module Dependencies**: All modules depend on `common` for shared utilities
- **Single Entry Point**: `controller` module contains the main Spring Boot application

## Technology Stack

- **Java 17** - Modern Java with enhanced features
- **Spring Boot 3.5.5** - Web framework and dependency injection
- **MyBatis 3.0.5** - ORM with dynamic SQL support
- **MySQL 8.0.28** - Relational database
- **Maven** - Build tool and dependency management
- **Lombok** - Reduces boilerplate code

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Gomoku-Wuziqi/Backend
   ```

2. **Configure environment variables**
   - Copy the example file and edit it with your local values:
     ```bash
     cp .env.example .env
     ```
   - Set the variables in `.env` (or your shell/secret manager), including:
     - `GOMOKU_DB_URL`, `GOMOKU_DB_USERNAME`, `GOMOKU_DB_PASSWORD`
     - `USER_DB_URL`, `USER_DB_USERNAME`, `USER_DB_PASSWORD`
     - `RANKING_DB_URL`, `RANKING_DB_USERNAME`, `RANKING_DB_PASSWORD`
     - `REDIS_PASSWORD`
     - `SENDGRID_API_KEY` (if you enable email verification)
     - `OPENAI_API_KEY` (if you enable GPT-based AI)

3. **Set up the databases**
   ```bash
   # Example: create MySQL databases
   mysql -u root -p
   CREATE DATABASE gomoku;
   CREATE DATABASE user;
   CREATE DATABASE ranking;
   ```

4. **Build the project**
   ```bash
   mvn clean compile
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run -pl controller
   ```

The application will start on `http://localhost:8080`

## Development

### Building the Project

```bash
# Clean and compile all modules
mvn clean compile

# Run tests
mvn test

# Package the application
mvn clean package
```

### Database Operations

```bash
# Generate database entities and mappers
mvn mybatis-generator:generate -pl dao

# Note: This will overwrite existing generated files
# Update database credentials in dao/src/main/resources/generatorConfig.xml before running
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl common
mvn test -pl dao

# Run specific test class
mvn test -Dtest=BasicCheckAspectTest -pl common

# Run specific test method
mvn test -Dtest=BasicCheckAspectTest#check_Object -pl common
```

## Project Structure

### Module Dependencies

All modules depend on the `common` module for shared utilities:

- `controller` → `common` + business logic modules
- `biz` → `common` + `dao`
- `dao` → `common`
- `gomoku` → `common`
- `user` → `common`  
- `matching` → `common`
- `room` → `common`

### Key Directories

- `src/main/java/` - Java source code
- `src/main/resources/` - Configuration files
- `src/test/java/` - Test code
- `target/` - Build output (generated)

## API Documentation

Once the application is running, API documentation will be available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write unit tests for new functionality
- Follow the existing architecture patterns

## Database Schema

The application uses MySQL with the following key tables:
- `account_record` - User account and transaction records
- Additional tables are defined through MyBatis generator configuration

## Configuration

### Application Configuration
- Main config: `controller/src/main/resources/application.yaml`
- MyBatis config: `dao/src/main/resources/generatorConfig.xml`

### Environment Variables

Backend services rely on environment variables rather than hard-coded secrets. The most important ones are:

| Variable              | Description                                   |
|-----------------------|-----------------------------------------------|
| `GOMOKU_DB_URL`       | JDBC URL for the Gomoku service database     |
| `GOMOKU_DB_USERNAME`  | DB username for Gomoku service               |
| `GOMOKU_DB_PASSWORD`  | DB password for Gomoku service               |
| `USER_DB_URL`         | JDBC URL for the User service database       |
| `USER_DB_USERNAME`    | DB username for User service                 |
| `USER_DB_PASSWORD`    | DB password for User service                 |
| `RANKING_DB_URL`      | JDBC URL for the Ranking service database    |
| `RANKING_DB_USERNAME` | DB username for Ranking service              |
| `RANKING_DB_PASSWORD` | DB password for Ranking service              |
| `REDIS_PASSWORD`      | Redis password (if enabled)                  |
| `SENDGRID_API_KEY`    | SendGrid key for email verification          |
| `OPENAI_API_KEY`      | OpenAI key for GPT-based AI move suggestions |

See `Backend/.env.example` for a complete template.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team

---

Built with ❤️ by the GoMuKu Team.
