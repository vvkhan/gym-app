# Gym Application

## Part 2: ORM/Hibernate

### Requirements Note

**1. Hibernate Config added.**

**2. H2 in-memory database utilised since there were no instruction to set up external database.**

**3. User table added to model.**

**4. DAO objects for each domain model (except User) entities implemented.**

**5. Service and Facade reworked to meet functionality requirements.**

**6. Unit Tests:**

`Jacoco` is utilized for evaluating tests coverage.
- `util` - 100% coverage
- `dao/impl` - 99% coverage
- `service` - 83% coverage

Unit tests for `model` classes were not implemented since project Lombok was utilised for those classes and creating unit tests here would be redundant testing of Lombok work.
Unit tests for `facade` class were not implemented since it does not add business logic.

**7. Logging:**

- Implemented with `Logback` and `SLF4J`
- Logging layers set up in `/resources/logback.xml`
- Spring `@Aspect` is used (check `LoggingAspect` class)
- No sensitive data is included into logs (logs include only generated password's length and exclude; `@ToString.Exclude` used on the password field in User to suppress it from all Lombok-generated
  `toString()` calls)

**8. Username and Password calculation:**

- Check `util/UsernameGeneratorImpl` (implements the relevant interface) - calculates Username by concatenation of first and last name with dot as a separator (adds serial number when necessary)
- Check `util/PasswordGeneratorImpl` (implements the relevant interface) - generates Password as a random 10 chars lenth string. NOTE: password is not hashed for storing since Spring Security is covered only with future parts of the module.