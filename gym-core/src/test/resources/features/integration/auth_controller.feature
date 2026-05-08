Feature: Auth Controller

  Scenario: Successful login returns a JWT token
    Given a trainee is registered
    When the trainee logs in with valid credentials
    Then the response status is 200
    And the response contains a non-blank token

  Scenario: Login with wrong password returns 401
    Given a trainee is registered
    When the trainee logs in with an incorrect password
    Then the response status is 401

  Scenario: Account is locked after 3 consecutive failed login attempts
    Given a trainee is registered
    When the trainee fails to log in 3 times in a row
    Then a subsequent login attempt returns 401

  Scenario: Unauthenticated request to a protected endpoint returns 401
    When a GET request is sent to a protected endpoint without a token
    Then the response status is 401

  Scenario: Logout succeeds and the token is invalidated
    Given a trainee is registered and logged in
    When the trainee logs out
    Then the response status is 200
    And a subsequent request with the same token returns 401

  Scenario: Changing password succeeds with a valid token
    Given a trainee is registered and logged in
    When the trainee changes their password
    Then the response status is 200
    And the trainee can log in with the new password
