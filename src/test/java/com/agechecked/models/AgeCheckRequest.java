package com.agechecked.models;

import lombok.Builder;
import lombok.Data;

/**
 * Request payload for the AgeChecked AC0137 remote check endpoint.
 * Field names intentionally match the API's JSON contract.
 */
@Data
@Builder
public class AgeCheckRequest {
    private String merchantSecretKey;
    private String countrycode;
    private String reference;
    private String fullname;
    private String firstname;
    private String middlename;
    private String lastname;
    private String dateofbirth;
    private String gender;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String address5;
    private String address6;
    private String additionalfield1;
    private String additionalfield2;
    private String additionalfield3;
    private String additionalfield4;
    private String additionalfield5;
    private String additionalfield6;
    private String additionalfield7;
    private String additionalfield8;
    private String email;
    private String consentobtained;
    private String withforce;
}
