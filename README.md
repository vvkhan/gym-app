# Gym Application

## Microservices

### Notes

**Unit Tests:**

`Jacoco` is utilized for evaluating test coverage.

- `gym-core`:
  - `util` - 100% coverage
  - `exception/handler` - 100% coverage
  - `service` - 87% coverage
  - `controller` - 98% coverage
  - `messaging` - 57%

- `trainer-report-service`:
  - `exception` - 100%
  - `service` - 95%
  - `controller` - 100%
  - `messaging` - 73%

Check the report with ` mvn test && open gym-core/target/site/jacoco/index.html trainer-report-service/target/site/jacoco/index.html`.

**FOR DEMO**

***Preconditions:***
- Java 17
- Docker Desktop running
- No processes occupying ports 5432, 8080, 8081, 8761, 9000, 61616, 8161

***Spring Profiles:***

| Profile | Use case | Credentials |
|---------|----------|-------------|
| `local` | Local development | `application-local.yaml` (gitignored) |
| `dev` | Development server | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |
| `stg` | Staging server | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |
| `prod` | Production | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |

***Details:***

- From the project root, run `docker compose up --build`
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
  - The `GET /api/report/{username}` endpoint requires a Bearer token with `report:read` scope. Obtain one with:
    `curl -s -X POST http://localhost:9000/oauth2/token -u "gym-core:gym-core-dev-secret" -d "grant_type=client_credentials&scope=report:read" | python3 -m json.tool`
  - H2 DB available at `http://localhost:8081/h2-console`. Driver class: org.h2.Driver, JDBC URL: jdbc:h2:mem:reportdb, username: sa, password: (leave empty).


- Verify ActiveMQ at `http://localhost:8161` (credentials: `admin` / `admin`)
  - Queue `training.events` — shows enqueued/dequeued counts for workload events sent by `gym-core`
  - Queue `training.events.dlq` — appears when an invalid message (missing required fields) is routed there by the listener
    - To send an invalid message for DLQ, use `curl -u admin:admin -X POST "http://localhost:8161/api/message/training.events?type=queue&_type=WorkloadRequest" -H "Content-Type: text/plain" -d '{"firstName":"Bad","lastName":"Message","trainingDurationMinutes":30}'`
    - Check `trainer-report-service` logs to verify: look for `ERROR [txId=no-tx] c.e.g.r.m.TrainingEventListener - [txId=no-tx] Invalid message routed to DLQ — violations` and `ERROR [txId=NO_TX] c.e.g.r.m.DeadLetterQueueListener - Dead letter received`


- To trace a request end-to-end across both services, find the `txId` in `gym-core` logs after creating a training:
  `docker compose logs gym-core | grep "Published ADD"`
  - Copy the `txId` value and search across both services:
  `docker compose logs gym-core trainer-report-service | grep "<txId>"`
  - The same `txId` appears in `gym-core` (publish) and `trainer-report-service` (consume) confirming the message was delivered and processed.


- To stop the application, press `Ctrl+C`
- To remove containers, use `docker compose down`

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

