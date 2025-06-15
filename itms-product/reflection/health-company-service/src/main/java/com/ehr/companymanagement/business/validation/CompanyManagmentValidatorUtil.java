package com.ehr.companymanagement.business.validation;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.ehr.companymanagement.business.dto.CompanyDetailsDTO;
import com.ehr.companymanagement.business.dto.SubscriptionPlansDto;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.LogUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompanyManagmentValidatorUtil {

	private static final String ID_VALIDATOR_METHOD_CALLED = " idValidator () method called";
	private static final String INSIDE = "Inside ";

	private CompanyManagmentValidatorUtil() {
	}

	private static final String CLASSNAME = CompanyManagmentValidatorUtil.class.getSimpleName();

	public static Boolean subscriptionPlansDtoValidationUtil(SubscriptionPlansDto subscriptionPlansDto, String language)
			throws ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		try {
			if (subscriptionPlansDto == null) {
				log.error(EnglishConstants.SP0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("SP0001", language));
			}
			if (subscriptionPlansDto.getPlanName() == null) {
				log.error(EnglishConstants.SP0002);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("SP0002", language));
			}
			if (subscriptionPlansDto.getPlanName().equals(EnglishConstants.EMPTY)) {
				log.error(EnglishConstants.SP0003);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("SP0003", language));
			}

			if (subscriptionPlansDto.getNumOfUsers() == null) {
				log.error(EnglishConstants.SP0006);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("SP0006", language));
			}
			if (subscriptionPlansDto.getNumOfUsers().equals(0L)) {
				log.error(EnglishConstants.SP0007);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("SP0007", language));
			}

		} catch (ContractException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					EnglishConstants.EMPTY);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return Boolean.TRUE;
	}

	public static Boolean idValidator(Long id, String language) throws ContractException {
		if (id == null) {
			log.info(INSIDE + CLASSNAME + ID_VALIDATOR_METHOD_CALLED);
			log.error(EnglishConstants.ID0002);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("ID0002", language));
		}
		if (id == 0) {
			log.info(INSIDE + CLASSNAME + ID_VALIDATOR_METHOD_CALLED);
			log.error(EnglishConstants.ID0003);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("ID0003", language));
		}
		return Boolean.TRUE;
	}

	public static Boolean idValidator(Long id) throws ContractException {
		if (id == null) {
			log.info(INSIDE + CLASSNAME + ID_VALIDATOR_METHOD_CALLED);
			log.error(EnglishConstants.ID0002);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE, EnglishConstants.ID0002);
		}
		if (id == 0) {
			log.info(INSIDE + CLASSNAME + ID_VALIDATOR_METHOD_CALLED);
			log.error(EnglishConstants.ID0003);
			throw new ContractException(HttpStatus.NOT_ACCEPTABLE, EnglishConstants.ID0003);
		}
		return Boolean.TRUE;
	}

	public static Boolean isPrimaryKeyPresent(Long id) throws ContractException {
		if (id == null) {
			return Boolean.FALSE;
		}
		if (id == 0) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public static Boolean companyDtoValiadatorUtil(CompanyDetailsDTO companyDetailsDTO, String language)
			throws ContractException {
		if (companyDetailsDTO == null) {
			log.error(EnglishConstants.CD0002);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, ConstantsUtil.getConstant("CD0002", language),
					"CompanyDetailsDTO");
		}

		if (companyDetailsDTO.getAddressDto() == null) {
			log.error(EnglishConstants.CD0004);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, ConstantsUtil.getConstant("CD0004", language),
					"AddressDto");
		}

		if (StringUtils.isEmpty(companyDetailsDTO.getAddressDto().getAddressLine1())
				|| companyDetailsDTO.getAddressDto().getAddressLine1().length() > Constants.MAX_ADDRESS_LINE_LENGTH) {

			String msg = StringUtils.isEmpty(companyDetailsDTO.getAddressDto().getAddressLine1())
					? "Address Line 1 cannot be null or empty"
					: "Address Line 1 cannot exceed 50 characters";

			log.error(msg);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, msg, "AddressLine1");
		}

		if (StringUtils.isNotEmpty(companyDetailsDTO.getAddressDto().getAddressLine2())
				&& companyDetailsDTO.getAddressDto().getAddressLine2().length() > Constants.MAX_ADDRESS_LINE_LENGTH) {
			String msg = "Address Line 2 cannot exceed 50 characters";
			log.error(msg);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, msg, "AddressLine2");
		}

		if (companyDetailsDTO.getAddressDto().getCountryId() == null) {
			String msg = "CountryId cannot be null";
			log.error(msg);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, msg, "CountryId");
		}

		if (companyDetailsDTO.getAddressDto().getCityId() == null) {
			String msg = "CityId cannot be null";
			log.error(msg);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, msg, "CityId");
		}

		if (companyDetailsDTO.getAddressDto().getPincode() == null) {
			String msg = "Pincode cannot be null";
			log.error(msg);
			throw new ContractException(HttpStatus.EXPECTATION_FAILED, msg, "Pincode");
		}

		UserMasterDTO adminUser = companyDetailsDTO.getAdminUser();

		if (!adminUser.getUserFirstName().matches("[a-zA-Z]+")) {
			log.error("Invalid first name");
			throw new ContractException(HttpStatus.BAD_REQUEST, "Invalid First name");
		}

		if (!adminUser.getUserLastName().matches("[a-zA-Z]+")) {
			log.error("Invalid Last name format");
			throw new ContractException(HttpStatus.BAD_REQUEST, "Invalid Last name");
		}

		if (companyDetailsDTO.getValidFrom() == null) {
			log.error("Valid From timestamp is required.");
			throw new ContractException(HttpStatus.BAD_REQUEST, "Valid From timestamp is required.", "ValidFrom");
		}

		if (companyDetailsDTO.getValidTo() == null) {
			log.error("Valid To timestamp is required.");
			throw new ContractException(HttpStatus.BAD_REQUEST, "Valid To timestamp is required.", "ValidTo");
		}

		Date validFrom = new Date(companyDetailsDTO.getValidFrom());
		Date validTo = new Date(companyDetailsDTO.getValidTo());

		if (validTo.before(validFrom)) {
			log.error("Valid To date must be greater Valid From date.");
			throw new ContractException(HttpStatus.BAD_REQUEST, "Valid To date must be greater Valid From date.",
					"ValidTo");
		}

		return Boolean.TRUE;
	}
}
