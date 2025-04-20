# Exchange Rate Service Implementation

## Overview
This project implements a foreign exchange rate service as a Spring Boot microservice, providing currency exchange rates from the German Central Bank (Bundesbank). While this is a test implementation, I've incorporated several production-ready practices while keeping certain aspects simplified for the test environment.

## Features

### Data Management
- **Data Source**: Exchange rates are fetched from the Bundesbank Daily Exchange Rates API
- **Local Storage**: Implemented using H2 database for data persistence
  - Database file is stored locally to preserve data between application restarts
  - Prevents unnecessary API calls when restarting the application
- **Caching Strategy**: 
  - In-memory caching using Spring Cache
  - Cache invalidation occurs daily through scheduled tasks

### API Documentation
The API is fully documented using OpenAPI 3.0 (Swagger). You can access the interactive API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Documentation: `http://localhost:8080/v3/api-docs`

The documentation includes:
- Detailed endpoint descriptions
- Request/Response schemas
- Example payloads
- Response codes
- Try-it-out functionality for testing endpoints

### API Endpoints

#### Currency Operations
**GET /api/v1/currencies**
- Returns list of all available currency codes
- Response: Array of currency codes (e.g., ["USD", "GBP", "JPY"])
- Cache: Results are cached to minimize database queries
- Rate Limit: 100 requests per minute

#### Exchange Rate Operations
**GET /api/v1/exchange-rates**
- Returns all EUR exchange rates for a specific currency
- Query Parameters:
  - `currency` (required): 3-letter ISO currency code (e.g., "USD")
- Response: Array of exchange rates with dates
- Rate Limit: 100 requests per minute

**GET /api/v1/exchange-rates/{date}**
- Returns exchange rate for a specific date
- Path Parameters:
  - `date` (required): Date in YYYY-MM-DD format
- Query Parameters:
  - `currency` (required): 3-letter ISO currency code
- Response: Single exchange rate object
- Error Responses:
  - 404: Rate not found for date
  - 400: Invalid currency or date format

**GET /api/v1/exchange-rates/convert**
- Converts amount from foreign currency to EUR
- Query Parameters:
  - `currency` (required): Source currency code
  - `amount` (required): Amount to convert
  - `date` (required): Rate date (YYYY-MM-DD)
- Response: Conversion result with rate used
- Error Responses:
  - 400: Invalid parameters
  - 404: Rate not found

## Rate Limiting
The API implements rate limiting to ensure fair usage:
- 100 requests per minute per client
- Status 429 returned when limit exceeded

### Technical Implementation

#### Data Refresh Mechanism
- **Scheduled Updates**: Daily cron job fetches new exchange rates
- **Smart Fetching**: Only fetches dates not already in database
- **Error Handling**: Retry mechanism for failed API calls

#### Error Handling & Logging
- Global exception handling using `@ControllerAdvice`
- Structured logging using SLF4J
- Daily rotating log files
- Detailed error responses with appropriate HTTP status codes

#### Database Design
- Efficient schema design for quick querying
- Indexes on frequently queried columns
- Optimized for read operations

#### Testing
- Comprehensive unit tests for services
- Integration tests for controllers
- Mock tests for external API calls
- Test coverage > 80%

### Best Practices Implemented
- Clean Code principles
- SOLID principles adherence
- RESTful API design
- DTO pattern for data transfer
- Builder pattern for complex objects
- Repository pattern for data access

## Production Considerations
While this implementation is suitable for the test environment, several enhancements would be needed for production:

1. **Database**: 
   - Replace H2 with a production-grade database (e.g., PostgreSQL)
   - Implement proper database migration strategy

2. **Security**:
   - API authentication/authorization
   - Rate limiting
   - HTTPS enforcement

3. **Monitoring**:
   - Metrics collection
   - Health checks
   - Performance monitoring

4. **Scalability**:
   - Containerization (Docker)
   - Load balancing
   - Distributed caching (Redis)

5. **Documentation**:
   - API versioning

### Requirements
- Java 11 or later
- Maven 3.x

### Running the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests
```bash
mvn test
```