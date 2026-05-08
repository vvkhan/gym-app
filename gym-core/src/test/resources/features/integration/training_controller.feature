Feature: Training Controller

  Scenario: Adding a training session returns 200
    Given a trainee is registered and logged in
    And a trainer is registered
    When a training session is submitted
    Then the response status is 200

  Scenario: Adding a training session without a token returns 401
    Given a trainee is registered
    And a trainer is registered
    When a training session is submitted without a token
    Then the response status is 401

  Scenario: Adding a training session with an invalid request body returns 400
    Given a trainee is registered and logged in
    When a training session is submitted with missing required fields
    Then the response status is 400
