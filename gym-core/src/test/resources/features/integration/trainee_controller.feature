Feature: Trainee Controller

  Scenario: Registering a trainee returns 201 with credentials
    When a trainee registration request is submitted
    Then the response status is 201
    And the response contains a non-blank username and password

  Scenario: Getting a trainee profile requires authentication
    When a GET request is sent to the trainee profile endpoint without a token
    Then the response status is 401

  Scenario: Getting an existing trainee profile returns 200 with correct data
    Given a trainee is registered and logged in
    When the trainee requests their own profile
    Then the response status is 200
    And the profile contains the same first and last name as registered

  Scenario: Getting a non-existent trainee profile returns 404
    Given a trainee is registered and logged in
    When the trainee requests a profile for a username that does not exist
    Then the response status is 404

  Scenario: Updating a trainee profile returns 200 with updated data
    Given a trainee is registered and logged in
    When the trainee submits a profile update
    Then the response status is 200
    And the response reflects the updated address

  Scenario: Deleting a trainee returns 200
    Given a trainer is registered and logged in
    And a trainee is registered and logged in
    When the trainee deletes their own profile
    Then the response status is 200
    And getting the profile afterward returns 404

  Scenario: Activating a deactivated trainee returns 200
    Given a trainee is registered and logged in
    And the trainee is deactivated
    When the trainee sends an activate request
    Then the response status is 200

  Scenario: Deactivating an active trainee returns 200
    Given a trainee is registered and logged in
    When the trainee sends a deactivate request
    Then the response status is 200

  Scenario: Activating an already-active trainee returns 409
    Given a trainee is registered and logged in
    When the trainee sends an activate request without prior deactivation
    Then the response status is 409

  Scenario: Getting not-assigned trainers returns 200
    Given a trainee is registered and logged in
    When the trainee requests the list of not-assigned trainers
    Then the response status is 200
    And the response is a list

  Scenario: Updating the trainer list returns 200
    Given a trainee is registered and logged in
    And a trainer is registered
    When the trainee updates their trainer list with the registered trainer
    Then the response status is 200
    And the response includes the assigned trainer

  Scenario: Getting trainee trainings returns 200
    Given a trainee is registered and logged in
    And a trainer is registered
    And a training session exists for the trainee
    When the trainee requests their training list
    Then the response status is 200
    And the training list is not empty
