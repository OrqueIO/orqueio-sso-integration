# OrqueIO Expense Workflow SSO

A production-ready Spring Boot application demonstrating **OAuth2/OIDC Single Sign-On (SSO)** integration with OrqueIO BPM platform. This example showcases a complete expense approval workflow with automatic user provisioning and group-based access control.

> **SSO Support**: SSO is available starting from **OrqueIO version 1.0.6+** (current: 2.0.0)

## Overview

This project provides a fully functional expense approval workflow that demonstrates:

- **OAuth2/OIDC Authentication** with support for multiple identity providers (Keycloak, Google, GitHub, Auth0, Okta)
- **Automatic User Provisioning** with group synchronization from identity providers
- **BPMN Workflow** implementing a multi-stage expense approval process
- **Group-Based Authorization** with role-based task assignment
- **Service Task Integration** for automated payment processing

## Architecture

### Workflow Process

The expense approval workflow consists of the following stages:

1. **Expense Submission** - Employee submits an expense report (`employees` group)
2. **Finance Review** - Finance team reviews and approves/rejects (`finance` group)
3. **Conditional Gateway** - Routes based on approval decision
4. **Payment Processing** - Automated payment via `PaymentDelegate` service task
5. **Process Completion** - Final state (approved or rejected)

### SSO Configuration

The application supports OAuth2/OIDC with the following features:

- **Identity Provider Integration**: Keycloak, Google, GitHub, Auth0, Okta
- **User Synchronization**: Automatic user creation and updates
- **Group Mapping**: Identity provider groups mapped to OrqueIO groups
- **Token-Based Authentication**: Secure JWT/OAuth2 token validation

## Prerequisites

- **Java**: 21 or higher
- **Maven**: 3.6+
- **Spring Boot**: 4.0.0
- **OrqueIO BPM**: 2.0.0 (SSO supported from 1.0.6+)
- **Identity Provider** (optional): Keycloak, Google, GitHub, Auth0, or Okta OAuth2 credentials

## Configuration

### Application Configuration

Edit `src/main/resources/application.yml` to configure your OAuth2 providers:

#### Keycloak Configuration

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: YOUR_KEYCLOAK_CLIENT_ID
            client-secret: YOUR_KEYCLOAK_CLIENT_SECRET
            scope: openid, profile, email
        provider:
          keycloak:
            issuer-uri: http://localhost:9090/realms/orqueio
```

#### Google Configuration

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope: openid, profile, email
```

### OrqueIO SSO Settings

The application uses the following OrqueIO SSO configuration:

```yaml
orqueio:
  bpm:
    oauth2:
      identity-provider:
        enabled: true
        group-name-attribute: groups
      user-sync:
        enabled: true
        sync-groups: true
        remove-obsolete-group-memberships: true
```

**Key Features:**
- `identity-provider.enabled`: Enables SSO integration
- `user-sync.enabled`: Automatically creates/updates users from identity provider
- `sync-groups`: Synchronizes group memberships from identity provider
- `remove-obsolete-group-memberships`: Removes groups that no longer exist in identity provider

### Default Admin User

For initial setup and testing, a default admin user is configured:

- **Username**: `demo`
- **Password**: `demo`

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd orqueio-expense-workflow-sso
mvn clean install
```

### 2. Configure OAuth2 Provider

Choose one of the following options:

#### Option A: Using Keycloak

1. Set up a Keycloak instance (or use an existing one)
2. Create a realm named `orqueio`
3. Create a client with OAuth2/OIDC protocol
4. Configure groups: `employees`, `finance`
5. Update `application.yml` with your client credentials

#### Option B: Using Google, GitHub, Auth0, or Okta

1. Configure your chosen OAuth2 provider
2. Create OAuth2 credentials (Web application)
3. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/{provider}`
4. Update `application.yml` with your client credentials and issuer URI

#### Option C: Local Testing (No SSO)

Use the default admin credentials for local testing without SSO configuration.

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access the Application

- **Tasklist**: `http://localhost:8080/orqueio/app/tasklist/`
- **Admin**: `http://localhost:8080/orqueio/app/admin/`
- **Cockpit**: `http://localhost:8080/orqueio/app/cockpit/`

## Project Structure

```
expense-sso-example/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/expensessoexample/
│   │   │       ├── ExpenseSsoExampleApplication.java   # Main application
│   │   │       └── PaymentDelegate.java                # Service task delegate
│   │   └── resources/
│   │       ├── application.yml                         # Configuration
│   │       └── expense_report.bpmn                     # BPMN workflow
│   └── test/
└── pom.xml                                             # Maven dependencies
```

## Key Components

### ExpenseSsoExampleApplication.java

Main Spring Boot application class with:
- `@EnableProcessApplication`: Enables OrqueIO BPM
- Auto-starts the expense approval workflow on deployment

### PaymentDelegate.java

Service task implementation that:
- Processes approved expense payments
- Generates payment references
- Logs payment transactions

### expense_report.bpmn

BPMN 2.0 workflow definition with:
- Start event (expense submitted)
- User tasks with group assignments
- Exclusive gateway for approval decision
- Service task for payment processing
- End events for approved/rejected states

## Dependencies

Key dependencies used in this project:

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.6   | Application framework |
| OrqueIO BPM | 2.0.0   | BPM engine with SSO support |
| H2 Database | Runtime | In-memory database |
| Lombok | Latest  | Code generation |
| Spring Security OAuth2 | 4.0.6   | OAuth2/OIDC authentication |

## Usage Examples

### Starting a Process Instance

The application automatically starts a process instance on deployment. You can also start instances programmatically:

```java
@Autowired
private RuntimeService runtimeService;

public void startExpenseProcess() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("employee", "john.doe@example.com");
    variables.put("amount", new BigDecimal("150.00"));

    runtimeService.startProcessInstanceByKey("expense-report", variables);
}
```

### Completing Tasks

Tasks can be completed via the Tasklist UI or programmatically:

```java
@Autowired
private TaskService taskService;

public void approveExpense(String taskId) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("approved", true);

    taskService.complete(taskId, variables);
}
```

## Group Configuration

The workflow uses the following groups:

- **`employees`**: Can submit expense reports
- **`finance`**: Can review and approve/reject expenses

These groups must exist in your identity provider with the exact same names for proper authorization.

## Troubleshooting

### Common Issues

**Issue**: OAuth2 login fails with redirect error
**Solution**: Verify the redirect URI in your identity provider matches: `{baseUrl}/login/oauth2/code/{registrationId}`

**Issue**: User logged in but cannot see tasks
**Solution**: Ensure the user belongs to the correct groups (`employees` or `finance`) in your identity provider

**Issue**: SSO not working
**Solution**: Verify you are using OrqueIO version 1.0.6 or higher (current version: 2.0.0). Check `pom.xml` for `orqueio.version` property.

**Issue**: Groups not synchronized
**Solution**: Ensure `group-name-attribute` in `application.yml` matches your identity provider's group claim attribute

## Security Considerations

- Store OAuth2 client secrets in environment variables or secure vaults (not in `application.yml`)
- Use HTTPS in production environments
- Configure appropriate CORS policies
- Implement rate limiting for API endpoints
- Review and adjust authorization settings based on your security requirements

## Production Deployment

For production deployment:

1. Replace H2 database with a production database (PostgreSQL, MySQL, etc.)
2. Configure environment-specific `application.yml` profiles
3. Use external configuration for sensitive credentials
4. Enable HTTPS with valid SSL certificates
5. Set up monitoring and logging
6. Configure proper session management and timeout

## License

This project is provided as an example for demonstration purposes.

## Support

For OrqueIO-related questions and support:
- Documentation: [OrqueIO Docs](https://docs.orqueio.io)
- Issues: Report issues in your organization's issue tracker

