Feature: Training Type Controller

  Scenario: Getting all training types returns 200 with a non-empty list
    Given a trainee is registered and logged in
    When the training types are requested
    Then the response status is 200
    And the response contains a non-empty list of training types

  Scenario: Getting training types without a token returns 401
    When the training types are requested without a token
    Then the response status is 401
