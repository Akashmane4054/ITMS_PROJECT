package com.ehr.assessment.business.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelValidator {
	private ExcelValidator() {
	}

	private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]+$");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public static List<String> validateRow(String firstName, String lastName, String gender, String email,
			String mobileNo, String dateOfBirth) {
		List<String> errors = new ArrayList<>();

		validateField(firstName, "firstName", NAME_PATTERN, errors);
		validateField(lastName, "lastName", NAME_PATTERN, errors);
		validateField(gender, "Gender", NAME_PATTERN, errors);
		validateField(email, "Email Address", EMAIL_PATTERN, errors);
		validateField(mobileNo, "Mobile Number", MOBILE_PATTERN, errors);
		validateDateOfBirth(dateOfBirth, errors);

		return errors.isEmpty() ? null : errors;
	}

	private static void validateField(String value, String fieldName, Pattern pattern, List<String> errors) {
		if (value == null || value.trim().isEmpty()) {
			String error = fieldName + " cannot be empty";
			log.error(error);
			errors.add(error);
			return;
		}

		if (!pattern.matcher(value).matches()) {
			String error = "Invalid " + fieldName + " format - " + value;
			log.error(error);
			errors.add(error);
		}
	}

	private static void validateDateOfBirth(String dateOfBirth, List<String> errors) {
		if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
			String error = "Date of Birth cannot be empty";
			log.error(error);
			errors.add(error);
			return;
		}

		try {
			LocalDate dob = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern(DATE_FORMAT));

			if (dob.isAfter(LocalDate.now())) {
				String error = "Date of Birth cannot be in the future - " + dateOfBirth;
				log.error(error);
				errors.add(error);
			}
		} catch (DateTimeParseException e) {
			String error = " Invalid Date of Birth format, expected " + DATE_FORMAT;
			log.error(error);
			errors.add(error);
		}
	}
}
