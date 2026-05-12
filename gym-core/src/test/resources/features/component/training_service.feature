Feature: Training Service

  Scenario: Creating a training session persists the record
    Given a trainee and a trainer exist
    When a training session is created
    Then the training is persisted with the correct name and duration

  Scenario: Getting all training types returns the seeded reference data
    When all training types are retrieved
    Then the list is not empty
    And every entry has a non-blank name
