Feature: Trainer Controller

  Scenario: Registering a trainer returns 201 with credentials
    When a trainer registration request is submitted
    Then the response status is 201
    And the response contains a non-blank username and password

  Scenario: Getting a trainer profile requires authentication
    When a GET request is sent to the trainer profile endpoint without a token
    Then the response status is 401

  Scenario: Getting an existing trainer profile returns 200 with correct data
    Given a trainer is registered and logged in
    When the trainer requests their own profile
    Then the response status is 200
    And the profile contains the same first and last name as registered

  Scenario: Getting a non-existent trainer profile returns 404
    Given a trainer is registered and logged in
    When the trainer requests a profile for a username that does not exist
    Then the response status is 404

  Scenario: Updating a trainer profile returns 200 with updated data
    Given a trainer is registered and logged in
    When the trainer submits a profile update
    Then the response status is 200
    And the response reflects the updated first and last name

  Scenario: Activating a deactivated trainer returns 200
    Given a trainer is registered and logged in
    And the trainer is deactivated
    When the trainer sends an activate request
    Then the response status is 200

  Scenario: Deactivating an active trainer returns 200
    Given a trainer is registered and logged in
    When the trainer sends a deactivate request
    Then the response status is 200

  Scenario: Activating an already-active trainer returns 409
    Given a trainer is registered and logged in
    When the trainer sends an activate request without prior deactivation
    Then the response status is 409

  Scenario: Getting trainer trainings returns 200
    Given a trainer is registered and logged in
    And a trainee is registered
    And a training session exists for the trainer
    When the trainer requests their training list
    Then the response status is 200
    And the training list is not empty
