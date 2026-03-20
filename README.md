# Gym Application

## Part 2: REST API

### Notes

**Unit Tests:**

`Jacoco` is utilized for evaluating tests coverage.
- `util` - 100% coverage
- `exception/handler` - 100%
- `service` - 82% coverage
- `controller` - 98%

Unit tests for `model`, `dto`, `mapper` and `facade` classes were not implemented since it does not add business logic.

**FOR DEMO**

***Preconditions:***
- Apache Tomcat 10.1.x 
- PostgreSQL running on localhost:5432 with credentials configured in `src/mein/resources/application.properties`

***Steps:***
- Build .WAR file with `mvn package -DskipTests` (produced at `target` folder)
- Deploy to external Tomcat (adjust to your path) with `cp target/gym-app-1.0.0-SNAPSHOT.war /path/to/tomcat/webapps/ROOT.war`
- Start Tomcat (adjust to your path) with `/path/to/tomcat/bin/catalina.sh run`
- The application will be available at `http://localhost:8080/api/...`
- Import Postman collection and run it (final test run result as well as console logs are attached for the convenience)
- To stop application, press `Ctrl+C`