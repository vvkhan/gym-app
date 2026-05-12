Feature: Training Event Listener

  Scenario: A valid JMS message is processed and persisted
    When a valid workload ADD event arrives on the queue
    Then a summary record exists for the trainer from that message

  Scenario: An invalid JMS message throws an exception and is not persisted
    When an invalid workload message arrives on the queue
    Then an IllegalArgumentException is thrown
    And no summary record is created
