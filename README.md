# Gym Application

## Part 3: Spring Boot

### Notes

**Unit Tests:**

`Jacoco` is utilized for evaluating test coverage.
- `util` - 100% coverage
- `exception/handler` - 100% coverage
- `service` - 82% coverage
- `controller` - 97% coverage

Unit tests for `model`, `dto`, `mapper` and `facade` classes were not implemented since they contain no business logic.

**FOR DEMO**

***Preconditions:***
- Java 17
- PostgreSQL running on `localhost:5432` (`pgcrypto` must be enabled for DB)

***Spring Profiles:***

| Profile | Use case | Credentials |
|---------|----------|-------------|
| `local` | Local development | `application-local.yaml` (gitignored) |
| `dev` | Development server | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |
| `stg` | Staging server | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |
| `prod` | Production | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |

***Steps:***
- Copy `src/main/resources/application-local.yaml.example` to `src/main/resources/application-local.yaml` and fill in your database credentials and JWT secret
  - Generate a JWT secret with: `openssl rand -base64 32`
- Build the JAR with `./mvnw package -DskipTests`
- Run locally with `java -jar target/gym-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=local`
- Run with a profile with `java -jar target/gym-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev`
  - Non-local profiles require the `JWT_SECRET` environment variable in addition to `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- The application will be available at `http://localhost:8080/api/...`
- Use Swagger UI at `http://localhost:8080/swagger-ui.html` to test the API
- To stop the application, press `Ctrl+C`


***Swagger UI:***

Interactive API documentation is available at `http://localhost:8080/swagger-ui.html` once the application is running.

If Swagger UI opens but shows an invalid-definition error, check `http://localhost:8080/v3/api-docs` directly. The response must start with an `openapi` field. If it does not, run a clean rebuild with `./mvnw clean package -DskipTests` and verify the application starts with the expected config and database connection.


***Metrics (Prometheus):***

The application exposes Prometheus metrics at `http://localhost:8080/actuator/prometheus`.

Two custom gauges are included:
- `gym_users_registered` — current number of registered users in the database
- `gym_trainings` — current number of trainings in the database

To view all metrics, open the URL in a browser or run:
```bash
curl http://localhost:8080/actuator/prometheus
```

To filter for custom gym metrics only:
```bash
curl -s http://localhost:8080/actuator/prometheus | grep gym_
```

Or open the following URLs in a browser:
- `http://localhost:8080/actuator/metrics/gym.users.registered` for gym users registered
- `http://localhost:8080/actuator/metrics/gym.trainings.total` for total amount of trainings

***Health Check:***

The application exposes a health endpoint at `http://localhost:8080/actuator/health`.

It includes a custom `trainingTypes` indicator that verifies the `TrainingType` reference data is seeded in the database. Since trainers require a specialization, the app cannot function correctly without it.

Example response when healthy (including auto-enabled indicators):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 994662584320,
        "free": 391698587648,
        "threshold": 10485760,
        "path": "/path/to/your/project/.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    },
    "trainingTypes": {
      "status": "UP",
      "details": {
        "trainingTypes": 5
      }
    }
  }
}
```
