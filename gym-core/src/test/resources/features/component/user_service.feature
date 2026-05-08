Feature: User Service

  Scenario: Changing a password stores an encoded value
    Given a trainee is registered
    When the password is changed
    Then the stored password is not the plain-text new value
    And the stored password matches the new value when verified with BCrypt

  Scenario: Changing the password for a non-existent user throws an exception
    When the password is changed for a username that does not exist
    Then a NoSuchElementException is thrown
