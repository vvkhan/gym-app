Feature: Trainer Service

  Scenario: Registering a trainer generates a username and a raw password
    Given a training type exists in the system
    When a trainer is registered
    Then the returned trainer username is not blank
    And the trainer raw password is a 10-character alphanumeric string

  Scenario: Registering a trainer with an unknown specialization throws an exception
    When a trainer is registered with an unknown specialization UUID
    Then a NoSuchElementException is thrown

  Scenario: Retrieving an existing trainer by username returns the correct profile
    Given a trainer is registered
    When I look up the trainer by their generated username
    Then the returned profile has the same first and last name as the registered trainer

  Scenario: Retrieving a non-existent trainer returns an empty Optional
    When I look up a trainer by a username that does not exist
    Then the returned Optional is empty

  Scenario: Updating a trainer profile persists the changes
    Given a trainer is registered
    When the trainer first and last name are updated
    Then the stored trainer reflects the updated names

  Scenario: Updating a non-existent trainer throws an exception
    When I update a trainer that does not exist
    Then a NoSuchElementException is thrown

  Scenario: Activating a deactivated trainer succeeds
    Given a trainer is registered
    And the trainer is deactivated
    When the trainer is activated
    Then the trainer is active

  Scenario: Deactivating an active trainer succeeds
    Given a trainer is registered
    When the trainer is deactivated
    Then the trainer is inactive

  Scenario: Deactivating an already-inactive trainer throws an exception
    Given a trainer is registered
    And the trainer is deactivated
    When the trainer is deactivated again
    Then an IllegalStateException is thrown

  Scenario: Activating an already-active trainer throws an exception
    Given a trainer is registered
    When the trainer is activated without prior deactivation
    Then an IllegalStateException is thrown
