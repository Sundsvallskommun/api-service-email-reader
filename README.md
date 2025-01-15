# EmailReader

_Reads emails from mailboxes on a exchange web server. Processes these emails and provides them for clients to retrive._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent services](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-email-reader.git
   cd api-service-email-reader
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **Exchange Web service**
  - **Purpose:** The server the application reads emails from.
  - **External resource:**
    [EWS applications and the Exchange architecture](https://learn.microsoft.com/en-us/exchange/client-developer/exchange-web-services/ews-applications-and-the-exchange-architecture)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Messaging**
  - **Purpose:** Umbrella service to send different types of messages. Used to send SMS-messages for emails configued
    with the action SEND_SMS. Also used to send email reports if emails get stuck in the database.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-messaging)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in `src/integration-test/resources` for the OpenAPI
specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/api/resource
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **External Service URLs:**

  ```yaml
  integration:
    messaging:
      base-url: http://dependency_service_url
      token-uri: http://dependecy_service_token_url
      recipient-adress: your@target-email.se # Used to send reports if emails are stuck
      client-id: your-client-id
      client-secret: your-client-secrect

    properties:
        credentials:     
            secret-key: your-super-secret-key   # Used to encrypt passwords

  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Credentials configuration**

  Use the API to set up the configuration for which email-addresses to monitor

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-email-reader&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-email-reader)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-email-reader&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-email-reader)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-email-reader&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-email-reader)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-email-reader&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-email-reader)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-email-reader&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-email-reader)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-email-reader&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-email-reader)
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

© 2023 Sundsvalls kommun
