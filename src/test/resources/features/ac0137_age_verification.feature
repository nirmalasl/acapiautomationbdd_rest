Feature: AC0137 age verification

  As a merchant
  I want to submit an applicant's identity details to the AgeChecked AC0137 remote API
  So that I can confirm the applicant's age has been verified

  Background:
    Given the AgeChecked API base URL is configured

  Scenario Outline: Successfully verify an adult applicant's age
    Given an AC0137 request for the following applicant
      | field       | value          |
      | reference   | <reference>    |
      | firstname   | <firstname>    |
      | lastname    | <lastname>     |
      | dateofbirth | <dateofbirth>  |
      | address1    | <address1>     |
      | address3    | <address3>     |
      | address4    | <address4>     |
      | address5    | <address5>     |
      | additionalfield1 | <phone>   |
      | email       | <email>        |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "reference" with value "<reference>"
    And the response should contain field "checkstatus.avstatustext" with value "Approved"
    And the service response "Canada Credit Bureau 2" should include scores
    And the service response "Canada Residential" should include scores

    Examples:
      | reference         | firstname | lastname   | dateofbirth | address1                | address3           | address4 | address5 | phone         | email                             |
      | njtest_ac0137_338  | Latanya   | Mamudosk   | 09-09-1971  | 2799 Fort Campbell Blvd | Portage la Prairie | MB       | R1N3T4   | +12040000252 | njtest_ac0137_338@agechecked.com  |
      | ac2270_jenny_338   | JENNY     | SEAL       | 02-03-1985  | 11132 Park View Dr      | Dawson Creek       | BC       | V1G4A3   | +12040000252 | ac2270_338@agechecked.com         |
      | ac2270_olivia_338  | OLIVIA    | DEPONTENUF | 12-08-1994  | 541 MCKERCHER DR        | SASKATOON          | SK       | S7H4G3   | +12040000252 | ac2270_338@agechecked.com         |

  Scenario Outline: Reject an underage applicant's age verification
    Given an AC0137 request for the following applicant
      | field       | value          |
      | reference   | <reference>    |
      | firstname   | <firstname>    |
      | lastname    | <lastname>     |
      | dateofbirth | <dateofbirth>  |
      | address1    | <address1>     |
      | address3    | <address3>     |
      | address4    | <address4>     |
      | address5    | <address5>     |
      | additionalfield1 | <phone>   |
      | email       | <email>        |
    When I send the AC0137 age verification request
    Then the response status code should be 200
    And the response should contain field "reference" with value "<reference>"
    And the response should contain field "checkstatus.avstatustext" with value "NotApproved"

    Examples:
      | reference                  | firstname | lastname   | dateofbirth | address1                | address3           | address4 | address5 | phone         | email                                      |
      | njtest_ac0137_underage_338  | Latanya   | Mamudosk   | 09-09-2010  | 2799 Fort Campbell Blvd | Portage la Prairie | MB       | R1N3T4   | +12040000252 | njtest_ac0137_underage_338@agechecked.com  |

