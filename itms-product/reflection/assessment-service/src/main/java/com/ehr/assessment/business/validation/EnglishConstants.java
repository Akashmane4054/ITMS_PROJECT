package com.ehr.assessment.business.validation;

;

public class EnglishConstants {
	private EnglishConstants() {
	}

	/*----------Common-------*/
	public static final String EMPTY = "";
	public static final String IS_NUMBER_NULL_OR_EMPTY_ERROR = "Id cannot be null or empty";
	public static final String IS_ID_NULL_ERROR = "id cannot be null in the request";
	public static final String IS_ID_ZERO_ERROR = "id cannot be zero in the request";
	public static final String IS_ERROR = "";
	public static final String DTO_NULL_ERROR = "Dto cannot be null";
	public static final String IS_NAME_NULL_ERROR = "Name cannot be null in request";
	public static final String IS_NAME_EMPTY_ERROR = "Name cannot be empty in request";
	public static final String IS_ROLE_EMPTY_ERROR = "Role cannot be empty in request";
	public static final String IS_ROLE_NULL_ERROR = "Role cannot be null in request";

	public static final String NRF0001 = "No such a record found with id : ";
	public static final String NRF0002 = "No such a record found with name : ";

	public static final String SV0001 = "Assessment can be added.";
	public static final String SV0002 = "Your assessment limit has reached, please contact admin for adding new assessment";

	public static final String SV0003 = "Vendor Email already exists";

	public static final String NAME_EXIT_ERROR = "Name already exists";
	public static final String CM0001 = "Category Master name already exists";
	public static final String CM0002 = "Category Master not found";

	public static final String DM0001 = "Domain Master name already exists";
	public static final String DM0002 = "Domain Master not found";

	public static final String TM0002 = "Template Master not found";

}
