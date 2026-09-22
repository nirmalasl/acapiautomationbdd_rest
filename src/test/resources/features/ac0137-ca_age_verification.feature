Feature: AC0137 age verification
  As a merchant
  I want to submit an applicant's identity details to the AgeChecked AC0137 remote API
  So that I can confirm the applicant's age has been verified

  Background:
    Given the AgeChecked API base URL is configured
    And a valid merchant key is configured

  Scenario Outline: Reject age verification request when a mandatory field is missing
    Given an AC0137 request for the following applicant
      | field            | value              |
      | reference        | AUTO               |
      | firstname        | <firstname>        |
      | lastname         | <lastname>         |
      | dateofbirth      | <dateofbirth>      |
      | address1         | <address1>         |
      | address3         | Portage la Prairie |
      | address4         | MB                 |
      | address5         | R1N3T4             |
      | additionalfield1 |       +12040000252 |
      | email            | <email>            |
      | countrycode      | <countrycode>      |
      | merchantkey      | <merchantkey>      |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "error.code" with value "<errorCode>"
    And the response should contain field "error.message" with value "<errorMessage>"
    And the response should contain field "error.details" with value "<missingFieldDetails>" when present

    Examples:
      | firstname | lastname | dateofbirth | address1                | countrycode | email | merchantkey | errorCode | errorMessage         | missingFieldDetails |
      |           | Mamudosk |  09-09-1971 | 2799 Fort Campbell Blvd | CA          | AUTO  | AUTO        |      1008 | Required field(s)    | firstName           |
      | Latanya   |          |  09-09-1971 | 2799 Fort Campbell Blvd | CA          | AUTO  | AUTO        |      1008 | Required field(s)    | lastName            |
      | Latanya   | Mamudosk |             | 2799 Fort Campbell Blvd | CA          | AUTO  | AUTO        |      1008 | Required field(s)    | dateOfBirth         |
      | Latanya   | Mamudosk |  09-09-1971 |                         | CA          | AUTO  | AUTO        |      1008 | Required field(s)    | addressElement1     |
      | Latanya   | Mamudosk |  09-09-1971 | 2799 Fort Campbell Blvd |             | AUTO  | AUTO        |      1008 | Required field(s)    | countrycode         |
      | Latanya   | Mamudosk |  09-09-1971 | 2799 Fort Campbell Blvd | CA          |       | AUTO        |      1008 | Required field(s)    | email               |
      | Latanya   | Mamudosk |  09-09-1971 | 2799 Fort Campbell Blvd | CA          | AUTO  |             |      1039 | Invalid Merchant Key |                     |

  Scenario Outline: Successfully verify an adult applicant's age
    Given an AC0137 request for the following applicant
      | field            | value         |
      | reference        | <reference>   |
      | firstname        | <firstname>   |
      | lastname         | <lastname>    |
      | dateofbirth      | <dateofbirth> |
      | address1         | <address1>    |
      | address3         | <address3>    |
      | address4         | <address4>    |
      | address5         | <address5>    |
      | additionalfield1 | <phone>       |
      | email            | <email>       |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "reference" with value "<reference>"
    And the response should contain field "checkstatus.avstatustext" with value "Approved"

    Examples:
      | reference | firstname | lastname | dateofbirth | address1                | address3           | address4 | address5 | phone        | email |
      | AUTO      | Latanya   | Mamudosk |  09-09-1971 | 2799 Fort Campbell Blvd | Portage la Prairie | MB       | R1N3T4   | +12040000252 | AUTO  |

  Scenario Outline: Reject an underage applicant's age verification
    Given an AC0137 request for the following applicant
      | field            | value         |
      | reference        | <reference>   |
      | firstname        | <firstname>   |
      | lastname         | <lastname>    |
      | dateofbirth      | <dateofbirth> |
      | address1         | <address1>    |
      | address3         | <address3>    |
      | address4         | <address4>    |
      | address5         | <address5>    |
      | additionalfield1 | <phone>       |
      | email            | <email>       |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "reference" with value "<reference>"
    And the response should contain field "checkstatus.avstatustext" with value "NotApproved"

    Examples:
      | reference | firstname | lastname | dateofbirth | address1         | address3  | address4 | address5 | phone        | email |
      | AUTO      | OLIVIA    | LITA     |  12-08-1994 | 541 MCKERCHER DR | SASKATOON | SK       | S7H4G3   | +12040000252 | AUTO  |

  Scenario: Reject age verification when consent has not been obtained
    Given an AC0137 request for the following applicant
      | field            | value                   |
      | reference        | AUTO                    |
      | firstname        | Latanya                 |
      | lastname         | Mamudosk                |
      | dateofbirth      |              09-09-1971 |
      | address1         | 2799 Fort Campbell Blvd |
      | address3         | Portage la Prairie      |
      | address4         | MB                      |
      | address5         | R1N3T4                  |
      | additionalfield1 |            +12040000252 |
      | email            | AUTO                    |
      | consentobtained  | No                      |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "checkstatus.avstatustext" with value "NotRecognised"
    And the response should contain field "datacontent.searchErrorMessage" with value "The following data sources require consent: canada residential, canada credit bureau 2"

  Scenario: Reject age verification request with an invalid merchant key
    Given an AC0137 request for the following applicant
      | field            | value                           |
      | reference        | AUTO                            |
      | firstname        | Latanya                         |
      | lastname         | Mamudosk                        |
      | dateofbirth      |                      09-09-1971 |
      | address1         |         2799 Fort Campbell Blvd |
      | address3         | Portage la Prairie              |
      | address4         | MB                              |
      | address5         | R1N3T4                          |
      | additionalfield1 |                    +12040000252 |
      | email            | AUTO                            |
      | merchantkey      | INVALID_MERCHANT_KEY_1234567890 |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "error.code" with value "1039"
    And the response should contain field "error.message" with value "Invalid Merchant Key"

  Scenario: Reject age verification request with an invalid email address format
    Given an AC0137 request for the following applicant
      | field            | value                   |
      | reference        | AUTO                    |
      | firstname        | Latanya                 |
      | lastname         | Mamudosk                |
      | dateofbirth      |              09-09-1971 |
      | address1         | 2799 Fort Campbell Blvd |
      | address3         | Portage la Prairie      |
      | address4         | MB                      |
      | address5         | R1N3T4                  |
      | additionalfield1 |            +12040000252 |
      | email            | not-a-valid-email       |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "error.code" with value "1057"
    And the response should contain field "error.message" with value "Invalid Email Address Format"
