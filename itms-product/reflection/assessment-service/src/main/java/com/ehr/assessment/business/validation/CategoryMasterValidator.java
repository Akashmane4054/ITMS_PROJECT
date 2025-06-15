package com.ehr.assessment.business.validation;

import org.springframework.http.HttpStatus;

import com.ehr.assessment.business.dto.CategoryMasterDto;
import com.ehr.core.exception.ContractException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.LogUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CategoryMasterValidator {

	public static final String CLASSNAME = CategoryMasterValidator.class.getSimpleName();

	private CategoryMasterValidator() {
	}

	public static void dtoValidationUtil(CategoryMasterDto dto, String language) throws ContractException {
		try {
			if (dto == null) {
				log.error(EnglishConstants.DTO_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("DTO_NULL_ERROR", language), "CategoryMasterDto");
			}
			if (dto.getId() == null) {
				log.error(EnglishConstants.IS_ID_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_ID_NULL_ERROR", language), "Id");

			}
			if (dto.getName() == null) {
				log.error(EnglishConstants.IS_NAME_NULL_ERROR);
				throw new ContractException(HttpStatus.EXPECTATION_FAILED,
						ConstantsUtil.getConstant("IS_NAME_NULL_ERROR", language), "Name");
			}
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		}
	}

	public static void isPrimaryIdPresent(Long id, String language) throws ContractException {
		if (id == null) {
			log.error(EnglishConstants.IS_ID_NULL_ERROR);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
					ConstantsUtil.getConstant("IS_ID_NULL_ERROR", language), "Id");
		}
		if (id.equals(0L)) {
			log.error(EnglishConstants.IS_ID_ZERO_ERROR);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
					ConstantsUtil.getConstant("IS_ID_ZERO_ERROR", language), "Id");
		}
	}

	public static void isNamePresent(String name, String language) throws ContractException {
		if (name == null) {
			log.error(EnglishConstants.IS_NAME_NULL_ERROR);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
					ConstantsUtil.getConstant("IS_NAME_NULL_ERROR", language), "Name");
		}
		if (name.equals(Constants.SPACE)) {
			log.error(EnglishConstants.IS_NAME_EMPTY_ERROR);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
					ConstantsUtil.getConstant("IS_NAME_EMPTY_ERROR", language), "Name");
		}
	}
}
