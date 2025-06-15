package com.ehr.assessment.business.serviceimpl;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.GenderResponseDTO;
import com.ehr.assessment.business.dto.RespondentProjectionDTO;
import com.ehr.assessment.business.enums.AssessmentResponseStatus;
import com.ehr.assessment.business.service.AssessmentDashboardService;
import com.ehr.assessment.integration.domain.AssessmentUserMapping;
import com.ehr.assessment.integration.domain.SectionWiseRating;
import com.ehr.assessment.integration.repository.AssessmentUserRepository;
import com.ehr.assessment.integration.repository.RespondentDetailsRepository;
import com.ehr.assessment.integration.repository.SectionWiseRatingRepository;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.enums.AgeGroup;
import com.ehr.core.enums.PerformanceCategory;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.SvmUtil;
import com.ehr.core.util.UserUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentDashboardServiceImpl implements AssessmentDashboardService {

	private static final String INCOMPLETE = "Incomplete";
	private static final String COMPLETE = "Complete";
	private static final String MALE = "Male";
	private static final String FEMALE = "Female";

	private static final String CLASSNAME = AssessmentDashboardServiceImpl.class.getSimpleName();

	private final UserUtil userUtil;
	private final AssessmentUserRepository assessmentUserMappingRepository;
	private final RespondentDetailsRepository respondentDetailsRepository;
	private final SectionWiseRatingRepository sectionWiseRatingRepository;
	private final SvmUtil svmUtil;

	private Map<String, Object> verifyToken(MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	@Override
	public Map<String, Object> getParticipationByStatusAndAge(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));

		Map<String, Object> map = new HashMap<>();
		Map<String, Map<String, Map<String, Integer>>> genderAgeParticipation = new HashMap<>();
		genderAgeParticipation.put(MALE, initializeStatusAgeMap());
		genderAgeParticipation.put(FEMALE, initializeStatusAgeMap());

		verifyToken(headers);

		try {
			if (assessmentId == null) {
				throw new BussinessException(HttpStatus.BAD_REQUEST, "Assessment ID cannot be null");
			}

			List<AssessmentUserMapping> userMappings = getAssessmentUserMappings(assessmentId);
			if (CollectionUtils.isEmpty(userMappings)) {
				throw new BussinessException(HttpStatus.NOT_FOUND,
						"No user mappings found for the given assessment ID");
			}

			List<Long> userIds = extractUserIds(userMappings);
			List<RespondentProjectionDTO> respondents = getRespondentDetails(userIds);

			int totalUsers = 0;
			int maleCount = 0;
			int femaleCount = 0;

			for (RespondentProjectionDTO respondent : respondents) {
				if (respondent != null && respondent.getUserId() != null) {
					AssessmentUserMapping matchingMapping = userMappings.stream()
							.filter(mapping -> mapping.getUserId().equals(respondent.getUserId())).findFirst()
							.orElse(null);

					if (matchingMapping != null) {
						String status = (matchingMapping.getAssessmentResponseStatus() != null && matchingMapping
								.getAssessmentResponseStatus().equals(AssessmentResponseStatus.COMPLETED)) ? COMPLETE
										: INCOMPLETE;

						if (respondent.getDateOfBirth() != null) {
							int age = calculateAge(respondent.getDateOfBirth());
							AgeGroup ageGroup = determineAgeGroup(age);

							if (StringUtils.isNotEmpty(respondent.getGender())) {
								String value = svmUtil.getValueBySerialId(Long.parseLong(respondent.getGender()));
								respondent.setGender(value);
							}

							if (ageGroup != null) {
								String gender = respondent.getGender();
								if (MALE.equalsIgnoreCase(gender)) {
									maleCount++;
								} else if (FEMALE.equalsIgnoreCase(gender)) {
									femaleCount++;
								}

								totalUsers++;

								if (MALE.equalsIgnoreCase(gender) || FEMALE.equalsIgnoreCase(gender)) {
									genderAgeParticipation.get(gender).get(status).merge(ageGroup.getAgeRange(), 1,
											Integer::sum);
								}
							}
						}
					}
				}
			}

			for (String gender : genderAgeParticipation.keySet()) {
				Map<String, Map<String, Integer>> statusMap = genderAgeParticipation.get(gender);
				Map<String, Integer> grandTotalMap = statusMap.get("GrandTotal");
				if (grandTotalMap != null) {
					for (AgeGroup ageGroup : AgeGroup.values()) {
						String ageRange = ageGroup.getAgeRange();
						int completeCount = statusMap.get(COMPLETE).getOrDefault(ageRange, 0);
						int incompleteCount = statusMap.get(INCOMPLETE).getOrDefault(ageRange, 0);
						grandTotalMap.put(ageRange, completeCount + incompleteCount);
					}
				}
			}

			if (totalUsers > 0) {
				double malePercentage = (maleCount * 100.0) / totalUsers;
				double femalePercentage = (femaleCount * 100.0) / totalUsers;

				GenderResponseDTO genderResponseDTO = new GenderResponseDTO(maleCount, malePercentage, femaleCount,
						femalePercentage);

				map.put("generalAverage", genderAgeParticipation);
				map.put("genderAverage", genderResponseDTO);
				map.put(Constants.SUCCESS, new SuccessResponse("General Average Info Found!"));
			} else {
				map.put(Constants.ERROR, "No users found to calculate participation");
			}
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return map;
	}

	private List<AssessmentUserMapping> getAssessmentUserMappings(Long assessmentId) {
		return assessmentUserMappingRepository.findByAssessmentIds(assessmentId);
	}

	private List<Long> extractUserIds(List<AssessmentUserMapping> userMappings) {
		if (userMappings == null)
			return Collections.emptyList();
		return userMappings.stream().map(AssessmentUserMapping::getUserId).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private List<RespondentProjectionDTO> getRespondentDetails(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty())
			return Collections.emptyList();
		return respondentDetailsRepository.findDetailsByUserIds(userIds);
	}

	private int calculateAge(Date dateOfBirth) {
		if (dateOfBirth == null)
			return 0;
		LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate currentDate = LocalDate.now();
		return Period.between(birthDate, currentDate).getYears();
	}

	private AgeGroup determineAgeGroup(int age) {
		if (age >= 18 && age <= 20) {
			return AgeGroup.AGE_18_20;
		} else if (age >= 21 && age <= 30) {
			return AgeGroup.AGE_21_30;
		} else if (age >= 31 && age <= 40) {
			return AgeGroup.AGE_31_40;
		} else if (age >= 41 && age <= 50) {
			return AgeGroup.AGE_41_50;
		} else if (age >= 51 && age <= 56) {
			return AgeGroup.AGE_51_56;
		}
		return null;
	}

	private Map<String, Map<String, Integer>> initializeStatusAgeMap() {
		Map<String, Map<String, Integer>> statusAgeMap = new LinkedHashMap<>();
		for (String status : Arrays.asList(COMPLETE, INCOMPLETE, "GrandTotal")) {
			Map<String, Integer> ageSlotMap = new LinkedHashMap<>();
			for (AgeGroup ageGroup : AgeGroup.values()) {
				ageSlotMap.put(ageGroup.getAgeRange(), 0);
			}
			statusAgeMap.put(status, ageSlotMap);
		}
		return statusAgeMap;
	}

	@Override
	public Map<String, Object> getSectionRecordsCalculation(Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> result = new LinkedHashMap<>();
		Map<String, Map<String, Map<String, Map<String, Integer>>>> genderAgeParticipation = new HashMap<>();
		genderAgeParticipation.put(MALE, initializeParentCategories());
		genderAgeParticipation.put(FEMALE, initializeParentCategories());

		try {
			List<AssessmentUserMapping> userMappings = getAssessmentUserMappings(assessmentId);
			if (CollectionUtils.isEmpty(userMappings)) {
				throw new BussinessException(HttpStatus.NOT_FOUND,
						"No user mappings found for the given assessment ID");
			}

			List<Long> userIds = extractUserIds(userMappings);
			List<RespondentProjectionDTO> respondents = getRespondentDetails(userIds);

			int totalUsers = 0;
			int maleCount = 0;
			int femaleCount = 0;

			for (RespondentProjectionDTO respondent : respondents) {
				if (respondent != null && respondent.getUserId() != null) {
					SectionWiseRating sectionWiseRating = sectionWiseRatingRepository
							.findBySectionIdAndAssessmentIdAndUserId(sectionId, assessmentId, respondent.getUserId());

					if (sectionWiseRating != null) {
						double sectionAverage = sectionWiseRating.getSectionAverage();
						PerformanceCategory category = PerformanceCategory.fromScore(sectionAverage);
						String parentCategory = determineParentCategory(category);
						String subcategory = determineSubCategory(category);

						if (respondent.getDateOfBirth() != null) {
							int age = calculateAge(respondent.getDateOfBirth());
							AgeGroup ageGroup = determineAgeGroup(age);

							if (StringUtils.isNotEmpty(respondent.getGender())) {
								String value = svmUtil.getValueBySerialId(Long.parseLong(respondent.getGender()));
								respondent.setGender(value);
							}

							if (ageGroup != null) {
								String gender = respondent.getGender();
								if (MALE.equalsIgnoreCase(gender)) {
									maleCount++;
								} else if (FEMALE.equalsIgnoreCase(gender)) {
									femaleCount++;
								}

								totalUsers++;

								if (MALE.equalsIgnoreCase(gender) || FEMALE.equalsIgnoreCase(gender)) {
									genderAgeParticipation.get(gender).get(parentCategory).get(subcategory)
											.merge(ageGroup.getAgeRange(), 1, Integer::sum);
								}
							}
						}
					}
				}
			}

			Map<String, Integer> overallCounts = new HashMap<>();
			processGenderAgeParticipation(genderAgeParticipation, overallCounts);

			Map<String, Map<String, Double>> percentages = new HashMap<>();
			calculateSubcategoryPercentages(genderAgeParticipation, maleCount, femaleCount, totalUsers, percentages);

			result.put("Percentages", percentages);
			result.put("overAllCounts", overallCounts);
			result.put("GenderAgeBand", genderAgeParticipation);
			result.put("Female Total", femaleCount);
			result.put("Male Total", maleCount);
			result.put("Overall Total", totalUsers);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return result;
	}

	private void initializePercentages(Map<String, Map<String, Double>> percentages) {
		List<String> subcategories = Arrays.asList("Neutral", "Need to Improve a lot", "Outstanding",
				"You're doing well", "Need immediate intervention");

		List<String> categories = Arrays.asList("Male %", "Female %", "Overall %");

		for (String category : categories) {
			percentages.putIfAbsent(category, new HashMap<>());
			Map<String, Double> subcategoryMap = percentages.get(category);
			for (String subcategory : subcategories) {
				subcategoryMap.putIfAbsent(subcategory, 0.0);
			}
		}
	}

	private void calculateSubcategoryPercentages(
			Map<String, Map<String, Map<String, Map<String, Integer>>>> genderAgeParticipation, int maleCount,
			int femaleCount, int totalUsers, Map<String, Map<String, Double>> percentages) {
		initializePercentages(percentages);
		for (Map.Entry<String, Map<String, Map<String, Map<String, Integer>>>> genderEntry : genderAgeParticipation
				.entrySet()) {
			String gender = genderEntry.getKey();
			int genderTotal = MALE.equalsIgnoreCase(gender) ? maleCount : femaleCount;
			Map<String, Map<String, Map<String, Integer>>> parentCategories = genderEntry.getValue();

			for (Map.Entry<String, Map<String, Map<String, Integer>>> parentCategoryEntry : parentCategories
					.entrySet()) {
				Map<String, Map<String, Integer>> subcategories = parentCategoryEntry.getValue();

				for (Map.Entry<String, Map<String, Integer>> subcategoryEntry : subcategories.entrySet()) {
					String subcategory = subcategoryEntry.getKey();
					Map<String, Integer> ageGroupCounts = subcategoryEntry.getValue();
					int subcategoryTotal = ageGroupCounts.getOrDefault(gender + " Total", 0);

					// Calculate Male % or Female %
					if (genderTotal > 0) {
						percentages.computeIfAbsent(gender + " %", k -> new HashMap<>()).put(subcategory,
								(subcategoryTotal * 100.0) / genderTotal);
					}

					// Calculate Overall %
					if (totalUsers > 0) {
						percentages.computeIfAbsent("Overall %", k -> new HashMap<>()).merge(subcategory,
								(subcategoryTotal * 100.0) / totalUsers, Double::sum);
					}
				}
			}
		}
	}

	private Map<String, Map<String, Map<String, Integer>>> initializeParentCategories() {
		Map<String, Map<String, Map<String, Integer>>> parentCategories = new LinkedHashMap<>();
		parentCategories.put("Low",
				initializeSubcategories(Arrays.asList("Outstanding", "You're doing well", "Neutral")));
		parentCategories.put("Moderate", initializeSubcategories(Arrays.asList("Need to Improve a lot")));
		parentCategories.put("High", initializeSubcategories(Arrays.asList("Need immediate intervention")));
		return parentCategories;
	}

	private Map<String, Map<String, Integer>> initializeSubcategories(List<String> subcategories) {
		Map<String, Map<String, Integer>> subcategoryMap = new LinkedHashMap<>();
		for (String subcategory : subcategories) {
			Map<String, Integer> ageSlotMap = new LinkedHashMap<>();
			for (AgeGroup ageGroup : AgeGroup.values()) {
				ageSlotMap.put(ageGroup.getAgeRange(), 0);
			}
			subcategoryMap.put(subcategory, ageSlotMap);
		}
		return subcategoryMap;
	}

	private void processGenderAgeParticipation(
			Map<String, Map<String, Map<String, Map<String, Integer>>>> genderAgeParticipation,
			Map<String, Integer> overallCounts) {

		for (Map.Entry<String, Map<String, Map<String, Map<String, Integer>>>> genderEntry : genderAgeParticipation
				.entrySet()) {
			String gender = genderEntry.getKey();
			Map<String, Map<String, Map<String, Integer>>> parentCategories = genderEntry.getValue();

			for (Map.Entry<String, Map<String, Map<String, Integer>>> parentCategoryEntry : parentCategories
					.entrySet()) {
				Map<String, Map<String, Integer>> subcategories = parentCategoryEntry.getValue();

				for (Map.Entry<String, Map<String, Integer>> subcategoryEntry : subcategories.entrySet()) {
					String subcategory = subcategoryEntry.getKey();
					Map<String, Integer> ageGroupCounts = subcategoryEntry.getValue();

					int totalCount = calculateTotalCount(ageGroupCounts);
					ageGroupCounts.put(gender + " Total", totalCount);

					overallCounts.merge(subcategory, totalCount, Integer::sum);
				}
			}
		}
	}

	private int calculateTotalCount(Map<String, Integer> ageGroupCounts) {
		return ageGroupCounts.values().stream().mapToInt(Integer::intValue).sum();
	}

	private String determineParentCategory(PerformanceCategory category) {
		switch (category) {
		case OUTSTANDING:
		case DOING_WELL:
		case NEUTRAL:
			return "Low";
		case NEED_TO_IMPROVE:
			return "Moderate";
		case NEED_IMMEDIATE_INTERVENTION:
			return "High";
		default:
		}
		return null;
	}

	private String determineSubCategory(PerformanceCategory category) {
		switch (category) {
		case OUTSTANDING:
			return "Outstanding";
		case DOING_WELL:
			return "You're doing well";
		case NEUTRAL:
			return "Neutral";
		case NEED_TO_IMPROVE:
			return "Need to Improve a lot";
		case NEED_IMMEDIATE_INTERVENTION:
			return "Need immediate intervention";
		default:
		}
		return null;
	}

}
