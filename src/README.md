# Duck Social Network 2.0

A social networking application for ducks and people built using Domain-Driven Design principles with Java.

## Current Features

### User Management
- User registration and authentication for both Person and Duck entities
- Profile management with specialized attributes for each user type
- Account operations (create, update, delete)
- User search and listing functionality

### Social Features
- Friendship system between users
- Private messaging between friends
- Conversation history management
- Network analysis and statistics

### Duck-Specific Features
- Duck types with different capabilities (Flying, Swimming, Flying & Swimming)
- Flock management system for organizing ducks by purpose
- Speed and resistance attributes for performance tracking

### Event Management
- Race event creation and management
- Lane configuration for races
- User subscription to events
- Automated race execution with optimal lane assignment algorithm
- Race result reporting with performance metrics

### Data Persistence
- File-based storage using CSV format
- Entity converters for data serialization
- Repository pattern implementation

### User Interface
- Console-based interface with menu navigation
- Authentication flow
- Comprehensive CRUD operations for all entities

## Work In Progress

- PostgreSQL database integration (replacing CSV file storage)
- Password hashing implementation for secure authentication
- EventValidator integration in EventService
- Messages and Groups

## Planned Features

- JavaFX graphical user interface
- Enhanced security features
- Extended event types beyond racing
- Advanced social network analytics
- Real-time notifications

## Technology Stack

- Java 24
- Gradle with Kotlin DSL
- PostgreSQL (in development)
- JUnit Jupiter for testing (in development)
- Domain-Driven Design architecture


## Architecture

The application follows a layered architecture with clear separation of concerns:

- **Domain Layer**: Core business entities and validation logic
- **Service Layer**: Business logic and application services
- **Repository Layer**: Data persistence abstraction
- **UI Layer**: User interface components

Key design patterns implemented include Repository, Strategy, Validator, and Factory patterns.