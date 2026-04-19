# Gym Application

## Microservices

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
- Docker Desktop running
- No processes occupying ports 5432, 8080, 8081, 8761, 9000

***Spring Profiles:***

| Profile | Use case | Credentials |
|---------|----------|-------------|
| `local` | Local development | `application-local.yaml` (gitignored) |
| `dev` | Development server | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |
| `stg` | Staging server | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |
| `prod` | Production | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |

***Details:***

- From the project root, run `docker-compose up --build` (or `docker compose up --build` if `docker-compose` command not found).
- Check all services are healthy with `docker compose ps`


- Verify Eureka at `http://localhost:8761`


- Test `gym-core` API at `http://localhost:8080/swagger-ui.html'
  - Postgres DB credentials: 
    - Host: localhost
      Port: 5432
      DB name: gymdb
      User: docker
      Password: docker


- Test `trainer-report-service` API at `http://localhost:8081/swagger-ui.html`
  - Use `curl -s -X POST http://localhost:9000/oauth2/token -u "gym-core:gym-core-dev-secret" -d "grant_type=client_credentials&scope=report:read report:write" | python3 -m json.tool` to get access token for getting reports
  - H2 DB available at `http://localhost:8081/h2-console`. Driver class: org.h2.Driver, JDBC URL: jdbc:h2:mem:reportdb, username: sa, password: (leave empty).


- To get the `transactionId` of the transactions on `gym-core` side that triggered changes in workload reports on `trainer-report-service`, use `docker compose logs trainer-report-service | grep "POST /api/report"`
  - Use `txId` in `docker compose logs gym-core trainer-report-service | grep "<txId>"` to retrieve the relevant logs.


- To stop the application, press `Ctrl+C`
- To remove containers, use `docker-compose down`

***Metrics (Prometheus):***

The application exposes Prometheus metrics at `http://localhost:8080/actuator/prometheus`.

Two custom gauges are included:
- For `gym-core`:
  - `gym_users_registered` — current number of registered users in the database
  - `gym_trainings` — current number of trainings in the database
- For `trainer-report-service`:
  - `report_trainers_tracked` - current number of trainers with recorded workload

To view all metrics, open the URL in a browser
- `http://localhost:8080/actuator/metrics/gym.users.registered` for gym users registered
- `http://localhost:8080/actuator/metrics/gym.trainings.total` for total amount of trainings
- `http://localhost:8081/actuator/metrics/report.trainers.tracked` for total number of trainers with recorded workload

***Health Check:***

The application exposes a health endpoint at `http://localhost:8080/actuator/health` and `http://localhost:8081/actuator/health`.

