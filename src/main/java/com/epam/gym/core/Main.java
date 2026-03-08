package com.epam.gym.core;

import com.epam.gym.core.config.AppConfig;
import com.epam.gym.core.facade.GymFacade;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Main {

    // Existing user credentials — must match records currently in the Docker DB
    private static final String ALICE_USERNAME = "Alice.Johnson";
    private static final String ALICE_PASSWORD = "xK9mP2qRnT";
    private static final String JOHN_USERNAME  = "John.Smith";
    private static final String JOHN_PASSWORD  = "rW3eA6uXkG";

    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);

        // Fetch training types from DB — needed for trainer creation and update
        List<TrainingType> trainingTypes = facade.getAllTrainingTypes();
        UUID firstTypeId  = trainingTypes.get(0).getId();
        UUID secondTypeId = trainingTypes.get(1).getId();

        // 1. Create Trainer profile
        section("1. Create Trainer profile");
        Trainer trainer = facade.registerTrainer("Mike", "Brown", firstTypeId);
        String trainerUsername = trainer.getUser().getUsername();
        String trainerPassword = trainer.getUser().getPassword();
        print("Username: " + trainerUsername
                + "  Password: [REDACTED]"
                + "  Specialization: " + trainer.getSpecialization().getTrainingTypeName());

        // 2. Create Trainee profile
        section("2. Create Trainee profile");
        Trainee trainee = facade.registerTrainee("Jane", "Doe",
                LocalDate.of(1995, 6, 15), "10 Main St");
        String traineeUsername = trainee.getUser().getUsername();
        String traineePassword = trainee.getUser().getPassword();
        print("Username: " + traineeUsername
                + "  Password: [REDACTED]"
                + "  DOB: " + trainee.getDateOfBirth());

        // 3. Trainee username and password matching
        section("3. Trainee username and password matching");
        print("Valid credentials:   " + facade.authenticateTrainee(traineeUsername, traineePassword));
        print("Invalid credentials: " + facade.authenticateTrainee(traineeUsername, "wrongPass"));

        // 4. Trainer username and password matching
        section("4. Trainer username and password matching");
        print("Valid credentials:   " + facade.authenticateTrainer(trainerUsername, trainerPassword));
        print("Invalid credentials: " + facade.authenticateTrainer(trainerUsername, "wrongPass"));

        // 5. Select Trainer profile by username
        section("5. Select Trainer profile by username");
        facade.getTrainerByUsername(trainerUsername).ifPresent(t ->
                print(t.getUser().getUsername()
                        + " | " + t.getUser().getFirstName() + " " + t.getUser().getLastName()
                        + " | Specialization: " + t.getSpecialization().getTrainingTypeName()
                        + " | Active: " + t.getUser().getIsActive()));

        // 6. Select Trainee profile by username
        section("6. Select Trainee profile by username");
        facade.getTraineeByUsername(traineeUsername).ifPresent(t ->
                print(t.getUser().getUsername()
                        + " | " + t.getUser().getFirstName() + " " + t.getUser().getLastName()
                        + " | Address: " + t.getAddress()
                        + " | Active: " + t.getUser().getIsActive()));

        // 7. Trainee password change
        section("7. Trainee password change");
        String newTraineePassword = "traineeNewPass1";
        facade.changeTraineePassword(traineeUsername, traineePassword, newTraineePassword);
        traineePassword = newTraineePassword;
        print("Password changed. Authentication with new password: "
                + facade.authenticateTrainee(traineeUsername, traineePassword));

        // 8. Trainer password change
        section("8. Trainer password change");
        String newTrainerPassword = "trainerNewPass1";
        facade.changeTrainerPassword(trainerUsername, trainerPassword, newTrainerPassword);
        trainerPassword = newTrainerPassword;
        print("Password changed. Authentication with new password: "
                + facade.authenticateTrainer(trainerUsername, trainerPassword));

        // 9. Update trainer profile
        section("9. Update trainer profile");
        Trainer updatedTrainer = facade.updateTrainerProfile(
                trainerUsername, trainerPassword,
                "Michael", "Brown", secondTypeId, true);
        print("Updated: " + updatedTrainer.getUser().getFirstName() + " " + updatedTrainer.getUser().getLastName()
                + " | Specialization: " + updatedTrainer.getSpecialization().getTrainingTypeName());

        // 10. Update trainee profile
        section("10. Update trainee profile");
        Trainee updatedTrainee = facade.updateTraineeProfile(
                traineeUsername, traineePassword,
                "Janet", "Doe", LocalDate.of(1995, 6, 15), "20 New St", true);
        print("Updated: " + updatedTrainee.getUser().getFirstName() + " " + updatedTrainee.getUser().getLastName()
                + " | Address: " + updatedTrainee.getAddress());

        // 11. Activate/De-activate trainee
        section("11. Activate/De-activate trainee");
        facade.deactivateTrainee(traineeUsername, traineePassword);
        print("After deactivation — isActive: "
                + facade.getTraineeByUsername(traineeUsername)
                        .map(t -> t.getUser().getIsActive()).orElse(null));
        facade.activateTrainee(traineeUsername, traineePassword);
        print("After activation   — isActive: "
                + facade.getTraineeByUsername(traineeUsername)
                        .map(t -> t.getUser().getIsActive()).orElse(null));

        // 12. Activate/De-activate trainer
        section("12. Activate/De-activate trainer");
        facade.deactivateTrainer(trainerUsername, trainerPassword);
        print("After deactivation — isActive: "
                + facade.getTrainerByUsername(trainerUsername)
                        .map(t -> t.getUser().getIsActive()).orElse(null));
        facade.activateTrainer(trainerUsername, trainerPassword);
        print("After activation   — isActive: "
                + facade.getTrainerByUsername(trainerUsername)
                        .map(t -> t.getUser().getIsActive()).orElse(null));

        // 13. Delete trainee profile by username
        section("13. Delete trainee profile by username");
        facade.deleteTrainee(traineeUsername, traineePassword);
        print("Deleted: " + traineeUsername);
        print("Profile exists after delete: "
                + facade.getTraineeByUsername(traineeUsername).isPresent());

        // 14. Get Trainee Trainings List by criteria
        // (uses Alice.Johnson having existing trainings in Docker DB)
        section("14. Get Trainee Trainings List by criteria");
        print("— No filters (all trainings for " + ALICE_USERNAME + "):");
        facade.getTraineeTrainings(ALICE_USERNAME, ALICE_PASSWORD,
                null, null, null, null)
              .forEach(tr -> print("  " + tr.getTrainingName()
                      + " | " + tr.getTrainingDate()
                      + " | trainer: " + tr.getTrainer().getUser().getUsername()
                      + " | type: " + tr.getTrainingType().getTrainingTypeName()));
        print("— Filtered by date range [2025-01-01, 2025-01-11]:");
        facade.getTraineeTrainings(ALICE_USERNAME, ALICE_PASSWORD,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 11), null, null)
              .forEach(tr -> print("  " + tr.getTrainingName() + " on " + tr.getTrainingDate()));
        print("— Filtered by trainer name [" + JOHN_USERNAME + "]:");
        facade.getTraineeTrainings(ALICE_USERNAME, ALICE_PASSWORD,
                null, null, JOHN_USERNAME, null)
              .forEach(tr -> print("  " + tr.getTrainingName()));

        // 15. Get Trainer Trainings List by criteria
        // (uses John.Smith having existing trainings in Docker DB)
        section("15. Get Trainer Trainings List by criteria");
        print("— No filters (all trainings for " + JOHN_USERNAME + "):");
        facade.getTrainerTrainings(JOHN_USERNAME, JOHN_PASSWORD,
                null, null, null)
              .forEach(tr -> print("  " + tr.getTrainingName()
                      + " | " + tr.getTrainingDate()
                      + " | trainee: " + tr.getTrainee().getUser().getUsername()));
        print("— Filtered by trainee name [" + ALICE_USERNAME + "]:");
        facade.getTrainerTrainings(JOHN_USERNAME, JOHN_PASSWORD,
                null, null, ALICE_USERNAME)
              .forEach(tr -> print("  " + tr.getTrainingName() + " on " + tr.getTrainingDate()));

        // 16. Add training
        // (trainer created in op 1 (Mike.Brown) + Alice.Johnson as the trainee)
        section("16. Add training");
        UUID aliceId = facade.getTraineeByUsername(ALICE_USERNAME)
                .orElseThrow(() -> new IllegalStateException(ALICE_USERNAME + " not found in DB"))
                .getId();
        Training training = facade.createTraining(
                aliceId, trainer.getId(),
                "Morning Fitness", firstTypeId,
                LocalDate.of(2025, 4, 1), 45);
        print("Created: " + training.getTrainingName()
                + " | trainee: " + training.getTrainee().getUser().getUsername()
                + " | trainer: " + training.getTrainer().getUser().getUsername()
                + " | date: " + training.getTrainingDate()
                + " | duration: " + training.getDuration() + " min");

        // 17. Get trainers list not assigned on trainee
        section("17. Get trainers list not assigned on trainee by trainee's username");
        print("Trainers not assigned to " + ALICE_USERNAME + ":");
        facade.getNotAssignedTrainers(ALICE_USERNAME, ALICE_PASSWORD)
              .forEach(t -> print("  " + t.getUser().getUsername()
                      + " | " + t.getSpecialization().getTrainingTypeName()));

        // 18. Update Trainee's trainers list
        section("18. Update Trainee's trainers list");
        Trainee alice = facade.updateTraineeTrainers(
                ALICE_USERNAME, ALICE_PASSWORD, Set.of(trainerUsername, JOHN_USERNAME));
        print("Updated trainers list for " + ALICE_USERNAME + ":");
        alice.getTrainers().forEach(t -> print("  " + t.getUser().getUsername()
                + " | " + t.getSpecialization().getTrainingTypeName()));

        context.close();
        System.out.println("\n------------------------------------------");
        System.out.println("All 18 operations completed successfully");
        System.out.println("------------------------------------------\n");
    }

    private static void section(String title) {
        System.out.println("\n------------------------------------------");
        System.out.println("  " + title);
        System.out.println("------------------------------------------");
    }

    private static void print(String message) {
        System.out.println("  > " + message);
    }
}
