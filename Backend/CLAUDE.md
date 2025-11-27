# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Basic Build Operations
```bash
# Clean and compile all modules
mvn clean compile

# Run tests for all modules
mvn test

# Run tests for specific module
mvn test -pl common
mvn test -pl dao

# Package the application
mvn clean package

# Start the Spring Boot application
mvn spring-boot:run -pl controller
```

### Database Operations
```bash
# Generate database entities and mappers (will overwrite existing generated files)
mvn mybatis-generator:generate -pl dao
```

### Testing Individual Components
```bash
# Run specific test class
mvn test -Dtest=BasicCheckAspectTest -pl common

# Run specific test method
mvn test -Dtest=BasicCheckAspectTest#check_Object -pl common
```

## Project Architecture

### Multi-Module Structure
This is a Maven multi-module project with clear separation of concerns:

- **common** - Shared utilities, validation frameworks, and AOP aspects
- **controller** - REST API controllers and web layer (main Spring Boot application)
- **biz** - Business logic layer
- **dao** - Data access objects, entities, and MyBatis mappers
- **gomoku** - Game logic and mechanics
- **user** - User management functionality
- **matching** - Player matching system
- **room** - Game room management
- **security** - Security configurations

### Key Dependencies
All modules depend on the `common` module for shared utilities. The `controller` module is the main Spring Boot application entry point.

## Common Module: Validation Frameworks

The `common` module contains two custom validation frameworks built with Spring AOP:

### BasicCheck Framework
A lightweight parameter validation system using annotations:
- `@BasicCheck` - Applied to methods with configurable return types (EXCEPTION, EMPTY, NULL)
- Parameter annotations: `@CheckNull`, `@CheckLong`, `@CheckString`, `@CheckCollection`, `@CheckMap`, `@CheckObject`
- `@CheckObject` integrates with Bean Validation (JSR-303) for complex object validation
- Implementation: `NotNullAndPositiveAspect.java`

### ValueChecker Framework  
A flexible validation system with SpEL integration:
- `@ValueCheckers` - Contains array of `@ValueChecker` configurations
- Custom validation handlers implement `IValueCheckerHandler`
- Supports SpEL expressions for parameter extraction
- Thread-safe state management with `ValueCheckerReentrantThreadLocal`
- Implementation: `ValueCheckerAspect.java`

## Database Configuration

### MyBatis Generator
- Configuration: `dao/src/main/resources/generatorConfig.xml`
- Target database: MySQL 8.0+ (`gomoku` schema)
- Generates entities in `com.goody.nus.se.gomoku.model.entity`
- Generates mappers in `com.goody.nus.se.gomoku.model.dao`
- Uses MyBatis3DynamicSql runtime for dynamic queries

### Database Credentials
Update database connection details in:
- `dao/src/main/resources/generatorConfig.xml` (for code generation)
- `controller/src/main/resources/application.yaml` (for runtime)

## Technology Stack Details

- **Java 17** with modern language features
- **Spring Boot 3.5.5** with AOP support
- **MyBatis 3.0.5** with Dynamic SQL
- **MySQL 8.0.28** database
- **Bean Validation (JSR-303)** with Hibernate Validator
- **Lombok** for reduced boilerplate
- **Apache Commons** utilities

## Development Notes

### Code Generation
- MyBatis Generator overwrites existing generated files - backup custom changes
- Generated entities include builder patterns, equals/hashCode, and toString methods
- Database comments are preserved in generated code

### Validation Pattern
When adding new validation:
1. For simple parameter checks, use `@BasicCheck` with parameter annotations
2. For complex validation logic, create a handler implementing `IValueCheckerHandler` and use `@ValueCheckers`
3. For object validation, use `@CheckObject` with Bean Validation annotations

### Module Dependencies
- All business modules depend on `common`
- `controller` depends on business logic modules
- Avoid circular dependencies between modules

### Application Startup
The main application class is `ControllerApplication.java` in the `controller` module. This starts the complete Spring Boot application with all modules.
