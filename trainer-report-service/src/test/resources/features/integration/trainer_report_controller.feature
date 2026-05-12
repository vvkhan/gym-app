Feature: Trainer Report Controller

  Scenario: Updating workload with a valid token returns 200
    Given a valid JWT token is available
    When a workload ADD request is submitted for a trainer
    Then the response status is 200

  Scenario: Updating workload without a token returns 401
    When a workload ADD request is submitted without a token
    Then the response status is 401

  Scenario: Updating workload with an invalid request body returns 400
    Given a valid JWT token is available
    When a workload request is submitted with missing required fields
    Then the response status is 400

  Scenario: Getting a trainer summary returns 200 with workload data
    Given a valid JWT token is available
    And a workload ADD event has been processed for a trainer
    When the summary is requested for that trainer
    Then the response status is 200
    And the response contains the workload data for the submitted training month

  Scenario: Getting a summary for an unknown trainer returns 404
    Given a valid JWT token is available
    When the summary is requested for a trainer username that does not exist
    Then the response status is 404

  Scenario: Getting a summary without a token returns 401
    When the summary is requested without a token
    Then the response status is 401
