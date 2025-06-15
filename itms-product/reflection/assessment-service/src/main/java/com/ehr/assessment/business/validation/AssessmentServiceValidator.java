package com.ehr.assessment.business.validation;

import org.springframework.http.HttpStatus;

import com.ehr.assessment.business.dto.AssessmentDto;
import com.ehr.assessment.business.dto.AssessmentElementResponseDto;
import com.ehr.assessment.business.dto.ElementsDto;
import com.ehr.assessment.business.dto.SectionDto;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.LogUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssessmentServiceValidator {

	private AssessmentServiceValidator() {
	}

	private static final String DTO_NULL_ERROR = "DTO_NULL_ERROR";
	public static final String CLASSNAME = AssessmentServiceValidator.class.getSimpleName();

	public static void dtoValidationUtil(AssessmentDto dto, String language) throws ContractException {
		try {
			if (dto == null) {
				log.error(EnglishConstants.DTO_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant(DTO_NULL_ERROR, language), "Assessment-Dto");
			}
			if (dto.getName() == null) {
				log.error(EnglishConstants.IS_NAME_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_NAME_NULL_ERROR", language), Constants.NAME);
			}
			if (dto.getName().isEmpty()) {
				log.error(EnglishConstants.IS_NAME_EMPTY_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_NAME_EMPTY_ERROR", language), Constants.NAME);
			}
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		}
	}

	public static void assessmentSectionDtoValidationUtil(SectionDto dto, String language)
			throws ContractException, BussinessException {
		try {
			if (dto == null) {
				log.error(EnglishConstants.DTO_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant(DTO_NULL_ERROR, language), "Assessment-Dto");
			}

			if (dto.getName() == null) {
				log.error(EnglishConstants.IS_NAME_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_NAME_NULL_ERROR", language), Constants.NAME);
			}
			if (dto.getName().isEmpty()) {
				log.error(EnglishConstants.IS_NAME_EMPTY_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_NAME_EMPTY_ERROR", language), Constants.NAME);
			}

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		}
	}

	public static void assessmentElementDtoValidationUtil(ElementsDto dto, String language) throws ContractException {
		try {
			if (dto == null) {
				log.error(EnglishConstants.DTO_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant(DTO_NULL_ERROR, language), "Assessment-Dto");
			}
			if (dto.getId() == null) {
				log.error(EnglishConstants.IS_ID_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_ID_NULL_ERROR", language), Constants.ID);

			}

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		}
	}

	public static void assessmentElementResponseValidation(AssessmentElementResponseDto dto, String language)
			throws ContractException {
		try {
			if (dto == null) {
				log.error(EnglishConstants.DTO_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant(DTO_NULL_ERROR, language), "AssessmentElementResponseDto-Dto");
			}

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		}
	}

	public static void isNamePresent(String name, String language) throws ContractException {
		if (name == null) {
			log.error(EnglishConstants.IS_NAME_NULL_ERROR);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
					ConstantsUtil.getConstant("IS_NAME_NULL_ERROR", language), Constants.NAME);
		}
		if (name.equals(Constants.SPACE)) {
			log.error(EnglishConstants.IS_NAME_EMPTY_ERROR);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
					ConstantsUtil.getConstant("IS_NAME_EMPTY_ERROR", language), Constants.NAME);
		}
	}

	public static Boolean checkStringBoolean(String s) {
		if (s != null && !s.isEmpty() && s.equalsIgnoreCase("yes")) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public static Long checkStringIsNumeric(String s) {
		try {
			return Long.parseLong(s);
		} catch (Exception e) {
			return null;
		}
	}

}
