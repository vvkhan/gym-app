# Gym Application

## Part 2: ORM/Hibernate

### Requirements Note

**1. Hibernate Config added.**

**2. External PostgreSQL DB running in Docker container utilized.**

**3. User table added to model.**

**4. Repository objects implemented. For queries, Spring Data JPA and Specifications used.**

**5. Service and Facade reworked to meet functionality requirements.**

**6. Unit Tests:**

`Jacoco` is utilized for evaluating tests coverage.
- `util` - 100% coverage
- `service` - 81% coverage

Unit tests for `model` and `facade` classes were not implemented since it does not add business logic.

**7. Logging:**

- Implemented with `Logback` and `SLF4J`
- Logging layers set up in `/resources/logback.xml`
- Spring `@Aspect` is used (check `LoggingAspect` class)
- No sensitive data is included into logs (`sanitizeArgs` method was introduced in `LoggingAspect` class)

**8. Username and Password calculation:**

- Check `util/UsernameGeneratorImpl` (implements the relevant interface) - calculates Username by concatenation of first and last name with dot as a separator (adds serial number when necessary)
- Check `util/PasswordGeneratorImpl` (implements the relevant interface) - generates Password as a random 10 chars length string. NOTE: password is not hashed YET for storing since Spring Security is covered only with future parts of the module.

**9. `Main` class introduced for demo purpose.**

Run `mvn compile java:exec` to see the logs with SQL queries or refer to ***Log-with-SQL-queries.pdf*** file.

**10. Use `.sql` scripts in *sql* folder to create database and populate it with toy data.**