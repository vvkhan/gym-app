Feature: Trainee Service

  Scenario: Registering a trainee generates a username and a raw password
    When a trainee is registered
    Then the returned username is not blank
    And the raw password is a 10-character alphanumeric string
    And the trainee is active by default

  Scenario: Two trainees with identical names receive unique usernames
    When two trainees are registered with identical first and last names
    Then their usernames are different

  Scenario: Retrieving an existing trainee by username returns the correct profile
    Given a trainee is registered
    When I look up the trainee by their generated username
    Then the returned profile has the same first and last name as the registered trainee

  Scenario: Retrieving a non-existent trainee returns an empty Optional
    When I look up a trainee by a username that does not exist
    Then the returned Optional is empty

  Scenario: Updating a trainee profile persists the changes
    Given a trainee is registered
    When the trainee address and date of birth are updated
    Then the stored address matches the updated value

  Scenario: Updating a non-existent trainee throws an exception
    When I update a trainee that does not exist
    Then a NoSuchElementException is thrown

  Scenario: Activating a deactivated trainee succeeds
    Given a trainee is registered
    And the trainee is deactivated
    When the trainee is activated
    Then the trainee is active

  Scenario: Activating an already-active trainee throws an exception
    Given a trainee is registered
    When the trainee is activated without prior deactivation
    Then an IllegalStateException is thrown

  Scenario: Deactivating an active trainee succeeds
    Given a trainee is registered
    When the trainee is deactivated
    Then the trainee is inactive

  Scenario: Deactivating an already-inactive trainee throws an exception
    Given a trainee is registered
    And the trainee is deactivated
    When the trainee is deactivated again
    Then an IllegalStateException is thrown

  Scenario: Deleting a non-existent trainee throws an exception
    When I delete a trainee that does not exist
    Then a NoSuchElementException is thrown

  Scenario: Deleting a trainee removes them from the system
    Given a trainee is registered
    When the trainee is deleted
    Then looking up the trainee by their username returns empty

  Scenario: Not-assigned trainers list excludes already-assigned trainers
    Given a trainee is registered
    And a trainer is registered
    And the trainer is assigned to the trainee
    When not-assigned trainers are requested for the trainee
    Then the list does not include the assigned trainer

  Scenario: Filtering trainee trainings by date returns only matching records
    Given a trainee is registered
    And a trainer is registered
    And a training is created before the filter date
    And another training is created after the filter date
    When trainings are requested for the trainee with a from-date filter
    Then only the training created after the filter date is returned
