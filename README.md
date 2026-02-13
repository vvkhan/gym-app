# Gym Application

## Part 1: Spring Core CRM System

### Objects Schema

**1. User (Base entity)**
- First Name
- Last Name
- Username
- Password
- IsActive

**2. Trainer (extends User)**
- Specialization
- UserId
- Relationship: 0..1 to Training Type

**3. Trainee (extends User)**
- Date of Birth
- Address
- UserId
- Relationship: 0..1 to Training

**4. Training Type**
- Training Type Name
- Relationship: 1 to many Trainers

**5. Training**
- Trainee Id
- Trainer Id
- Training Name
- Training Type
- Training Date
- Training Duration
- Relationships: Many-to-1 with Trainee, Many-to-1 with Trainer

### Requirements

#### Service Layer

- TraineeService: Create/Update/Delete/Select Trainee profiles
- TrainerService: Create/Update/Select Trainer profiles
- TrainingService: Create/Select Training profiles

#### Technical Implementation

1. Spring configuration via annotations or Java-based approach
2. DAO layer for each entity using in-memory storage (java.util.Map)
3. Separate storage beans with data initialization from file using bean post-processing
4. Dependency injection: Auto-wiring for DAOs, constructor injection for facade, setter injection
   for the rest
5. Unit test coverage required
6. Proper logging implementation
7. Username/Password generation logic:
   - Username: FirstName.LastName (e.g., John.Smith)
   - If duplicate: append serial number (John.Smith1, John.Smith2)
   - Password: Random 10-character string

