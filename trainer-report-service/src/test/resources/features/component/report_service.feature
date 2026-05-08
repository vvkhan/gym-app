Feature: Report Service

  Scenario: Processing an ADD event for a new trainer creates a summary record
    When a workload ADD event is processed for a trainer
    Then a summary record exists for that trainer
    And the duration for that training month equals the submitted duration

  Scenario: Processing a second ADD event for the same month accumulates duration
    Given a workload ADD event has been processed for a trainer
    When a second ADD event for the same trainer and month is processed
    Then the stored duration for that month equals the sum of both submitted durations

  Scenario: Processing a DELETE event decrements the stored duration
    Given a workload ADD event has been processed for a trainer
    When a DELETE event for the same trainer, month, and duration is processed
    Then the stored duration for that month is zero

  Scenario: Duration never goes below zero on DELETE
    Given a workload ADD event has been processed for a trainer with a small duration
    When a DELETE event with a larger duration is processed for the same month
    Then the stored duration for that month is zero

  Scenario: Processing an event updates stored trainer metadata
    Given a workload ADD event has been processed for a trainer
    When a workload ADD event for the same trainer is processed with a different name
    Then the stored summary reflects the updated name

  Scenario: Getting a summary for an unknown trainer throws an exception
    When the summary is requested for a trainer username that does not exist
    Then a NoSuchElementException is thrown
