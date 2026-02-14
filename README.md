# Gym Application

## Part 1: Spring Core CRM System

### Requirements Note

**1. Three service classes implemented:**
- Trainee Service (create/update/delete/select), 
- Trainer Service (create/update/select), 
- Training Service (create/select)

Service layer implements Template Method design pattern (https://refactoring.guru/design-patterns/template-method). `AbstractService` class is extended with concrete classes `TraineeService`, `TrainerService`, and `TrainingService`.

**2. Application context configured with Spring annotation and Java based approach:**
- Java-based configuration (`@Configuration` classes with `@Bean` methods):
  - `AppConfig`
  - `StorageConfig`
- Annotation-based configuration:
  - `@Repository` - DAO implementations
  - `@Service` - service layer concrete classes
  - `@Component` - facade, util, storage classes

**3. DAO objects for each domain model entities implemented. They store in and retrieve data from common in-memory storage (Java `Map`). Each entity stored under separate namespace.**

Check DAO classes.

**4. Storage implemented as separate Spring bean:** 
- Initialized with data from   `.json` file - `/resource/data/initial-data.json` (`Jackson` used for work with `.json`)
- Path to the file set using property placeholder and external property file - check `PropertySourcesPlaceholderConfigurer` bean (`AppConfig`) for use of `@Value` placeholder in `StorageInitializer` class (references to `storage.data.file.path` in `application.properties')
- Every storage implemented as separate Spring bean - check `SotrageConfig`

**5. Injections:**
- DAO with storage bean inserted into services beans using auto wiring - check DAO classes receiving storage beans via `@Autowired` constructor with `@Qualifier`
- Services beans injected into the facade using constructor-based injections - check facade class receiving services via `@Autowired` constructor
- The rest of the injections done in a setter-based way - check the rest injections in DAOs and util classes

**6. Unit Tests:**

`Jacoco` is utilized for evaluating tests coverage.
- `util` - 100% coverage
- `dao/impl` - 100% coverage
- `service` - 84%
- `service/impl` - 89%
- `storage` - 94%

Unit tests for `model` classes were not implemented since project Lombok was utilised for those classes and creating unit tests here would be redundant testing of Lombok work.
Unit tests for `exception` classes were not implemented since there's no logic, just creation of custom extensions.
Unit tests for `facade` class were not implemented since it does not add business logic (as per my understanding, `facade` is required only for practicing injection via constructor).

**7. Logging:**

- Implemented with `Logback` and `SLF4J`
- Logging layers set up in `/resources/logback.xml` with writing logs into files
- No sensitive data is included into logs (logs include only generated password's length and exclude; `@ToString.Exclude` used on the password field in User to suppress it from all Lombok-generated
  `toString()` calls)

**8. Username and Password calculation:**

- Check `util/UsernameGeneratorImpl` (implements the relevant interface) - calculates Username by concatenation of first and last name with dot as a separator (adds serial number when necessary)
- Check `util/PasswordGeneratorImpl` (implements the relevant interface) - generates Password as a random 10 chars lenth string