package com.ehr.assessment.presentation.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.http.HttpStatus;

import com.ehr.assessment.business.enums.AssessmentStatus;
import com.ehr.core.dto.ColumnDetailsDTO;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.dto.SearchDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.dto.SortDto;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.enums.SearchType;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.ColumnServiceFeignProxy;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.LanguageUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Paginate {
	private Paginate() {
	}

	private static final String CLASSNAME = Paginate.class.getSimpleName();

	public static <T> Page<T> paginate(EntityManager em, ListingDto listingDto, Class<T> clazz, Long companyId,
			Long userId, Long assessmentStatus) {

		log.info("Listing Dto {}", listingDto);
		Pageable pageable = null;
		Sort sort = null;

		if (CollectionUtils.isNotEmpty(listingDto.getSort())) {
			log.info("Sort columns for entity {} => {}", clazz.getCanonicalName(), listingDto.getSort());
			for (SortDto sortDto : listingDto.getSort()) {
				if (sort == null) {

					sort = Sort.by(Sort.Direction.fromString(sortDto.getSortOrder()), sortDto.getSortField());
				} else {

					sort = sort.and(Sort.by(Sort.Direction.fromString(sortDto.getSortOrder()), sortDto.getSortField()));

				}
			}

			pageable = PageRequest.of(listingDto.getStart(), listingDto.getLength(), sort);
		} else {

			pageable = PageRequest.of(listingDto.getStart(), listingDto.getLength(),
					Sort.Direction.fromString(listingDto.getDefaultSort().getSortOrder()),
					listingDto.getDefaultSort().getSortField());

		}

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(clazz);
		Root<T> root = criteria.from(clazz);
		List<Predicate> predicates = new ArrayList<>();

		if (companyId != null) {
			predicates.add(builder.equal(root.get(Constants.COMPANY_ID), companyId));
		}

		if (assessmentStatus != null) {
			AssessmentStatus status = AssessmentStatus.getByStatusCode(assessmentStatus.intValue());

			if (status != null && assessmentStatus != 0 && assessmentStatus != 6) {
				predicates.add(builder.equal(root.get("status"), status));
			}
		}

		if (listingDto.getStatus() != null) {
			predicates.add(builder.equal(root.get(Constants.ACTIVE), listingDto.getStatus()));
		}
		if (CollectionUtils.isNotEmpty(listingDto.getSearch())) {
			log.info("Search columns for entity {} => {}", clazz.getCanonicalName(), listingDto.getSearch());
			for (SearchDto searchDto : listingDto.getSearch()) {
				String decodedSearch = LanguageUtil.decodeValue(searchDto.getSearch());
				if (StringUtils.isNotEmpty(searchDto.getSearchCol()) && StringUtils.isNoneEmpty(decodedSearch)) {
					try {
						Field f = clazz.getDeclaredField(searchDto.getSearchCol());

						switch (f.getType().toString()) {
						case "byte":
						case "class java.lang.Byte":
							predicates.add(
									builder.equal(root.get(searchDto.getSearchCol()), Byte.parseByte(decodedSearch)));
							break;
						case "short":
						case "class java.lang.Short":
							predicates.add(
									builder.equal(root.get(searchDto.getSearchCol()), Short.parseShort(decodedSearch)));
							break;
						case "int":
						case "class java.lang.Integer":
							predicates.add(
									builder.equal(root.get(searchDto.getSearchCol()), Integer.parseInt(decodedSearch)));
							break;
						case "long":
						case "class java.lang.Long":
							predicates.add(
									builder.equal(root.get(searchDto.getSearchCol()), Long.parseLong(decodedSearch)));
							break;
						case "double":
						case "class java.lang.Double":
							predicates.add(builder.equal(root.get(searchDto.getSearchCol()),
									Double.parseDouble(decodedSearch)));
							break;
						case "float":
						case "class java.lang.Float":
							predicates.add(
									builder.equal(root.get(searchDto.getSearchCol()), Float.parseFloat(decodedSearch)));
							break;
						case "class java.util.Date":

							LocalDate localDate = LocalDate.parse(decodedSearch);

							// Get the start and end of the day
							ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.systemDefault());
							ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

							// Convert to Instant
							Instant startInstant = startOfDay.toInstant();
							Instant endInstant = endOfDay.toInstant();

							// Convert to Date
							Date startDate = Date.from(startInstant);
							Date endDate = Date.from(endInstant);

							// Create expressions
							Expression<Date> startDateExpr = builder.literal(startDate);
							Expression<Date> endDateExpr = builder.literal(endDate);

							// Add the predicate
							predicates.add(builder.between(root.get(searchDto.getSearchCol()).as(Date.class),
									startDateExpr, endDateExpr));

							break;
						default:
							predicates.add(builder.like(builder.lower(root.get(searchDto.getSearchCol())),
									Constants.LIKE_STRING.replace(Constants.LIKE, decodedSearch.toLowerCase())));
							break;
						}
					} catch (NoSuchFieldException | SecurityException | NumberFormatException e) {
						log.info("Error while adding search criteria for {} with  {} => {}", clazz.getCanonicalName(),
								e.getLocalizedMessage(), e.getStackTrace());
					}
				}
			}
		}
		Predicate searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
		criteria.where(builder.and(searchpredicatevar));

		List<T> result1 = em.createQuery(criteria).getResultList();
		criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
		TypedQuery<T> createQuery = em.createQuery(criteria);

		List<T> result = em.createQuery(criteria).setFirstResult((int) pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();

		Integer count = result1.size();
		log.info("Size of list {} => {}", clazz.getCanonicalName(), result1.size());
		return new PageImpl<T>(result, pageable, count);
	}

	public static String getFullName(Map<String, UserMasterDTO> users, Long userId) {
		if (ObjectUtils.isPositiveNonZero(userId)) {
			UserMasterDTO userDto = users.get(String.valueOf(userId));
			if (userDto != null && StringUtils.isNotEmpty(userDto.getFullName())) {
				return userDto.getFullName();
			}
		}
		return "-";
	}

	public static List<ColumnDetailsDTO> getColumns(ColumnServiceFeignProxy columnServiceFeignProxy,
			ListingDto listingDto, String columnType) {
		List<ColumnDetailsDTO> columnDetailsDTOList = null;
		if (CollectionUtils.isNotEmpty(listingDto.getSequenceColumnDTOs())) {
			columnDetailsDTOList = new ArrayList<>();
			for (SequenceColumnDTO columnDTO : listingDto.getSequenceColumnDTOs()) {
				if (ObjectUtils.isPositiveNonZero(columnDTO.getColumnID())
						&& ObjectUtils.isPositiveNonZero(columnDTO.getSequenceColumn())) {
					ColumnDetailsDTO columnDetails = columnServiceFeignProxy.findBycolumnId(columnDTO.getColumnID());
					if (columnDetails != null) {
						ColumnDetailsDTO columnDetailsDTO = new ColumnDetailsDTO();
						BeanUtils.copyProperties(columnDetails, columnDetailsDTO);
						columnDetailsDTO.setSequenceColumn(columnDTO.getSequenceColumn());
						columnDetailsDTOList.add(columnDetailsDTO);
					}
				}
			}
		} else {
			log.info("columnServiceFeignProxy called for type => {}", columnType);
			columnDetailsDTOList = columnServiceFeignProxy.defaultRender(columnType);
		}
		return columnDetailsDTOList;
	}

	public static String getDate(Long reviewedOn) {
		long timestampInMillis = reviewedOn * 1000;

		LocalDateTime dateTime = Instant.ofEpochMilli(timestampInMillis).atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		String formattedDate = dateTime.format(formatter);

		log.info("Formatted Date: " + formattedDate);
		return formattedDate;
	}

	public static <T, S, J> List<S> columnPaginate(EntityManager em, ListingDto listingDto, Class<T> domainClazz,
			Class<S> resultDomainClazz, String primaryKeyRef, Class<J> joinTable, String joinTableColumnForJoin,
			Long assessmentId)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, TechnicalException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {

		log.info("Listing Dto {}", listingDto);
		Pageable pageable = null;
		Sort sort = null;

		if (CollectionUtils.isNotEmpty(listingDto.getSort())) {
			log.info("Sort columns for entity {} => {}", domainClazz.getCanonicalName(), listingDto.getSort());
			for (SortDto sortDto : listingDto.getSort()) {
				if (sort == null) {
					sort = Sort.by(Sort.Direction.fromString(sortDto.getSortOrder()), sortDto.getSortField());
				} else {
					sort.and(Sort.by(Sort.Direction.fromString(sortDto.getSortOrder()), sortDto.getSortField()));
				}
			}
			pageable = PageRequest.of(listingDto.getStart(), listingDto.getLength(), sort);
		} else {
			pageable = PageRequest.of(listingDto.getStart(), listingDto.getLength(),
					Sort.Direction.fromString(listingDto.getDefaultSort().getSortOrder()),
					listingDto.getDefaultSort().getSortField());
		}

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> criteria = builder.createQuery(Object[].class);
		Root<T> root = criteria.from(domainClazz);
		Root<J> joinRoot = null;

		addRequiredPredicates(listingDto, domainClazz, primaryKeyRef, joinTable, joinTableColumnForJoin, builder,
				criteria, root, joinRoot, assessmentId);

		criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
		criteria.groupBy(root.get(primaryKeyRef));

		TypedQuery<Object[]> typedQuery = em.createQuery(criteria);

		List<Object[]> result = typedQuery.setFirstResult((int) pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();

		log.info("QueryString => ", typedQuery.unwrap(Query.class).getQueryString());

		List<S> resultDomainList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(result)) {
			if (resultDomainClazz == null) {
				log.error("======> Result Domain Class not provided to pagination");
				throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
						Constants.SPACE);
			}
			setResultDomainList(listingDto, resultDomainClazz, result, resultDomainList);
		}
		return resultDomainList;
	}

	private static <T, J> void addRequiredPredicates(ListingDto listingDto, Class<T> domainClazz, String primaryKeyRef,
			Class<J> joinTable, String joinTableColumnForJoin, CriteriaBuilder builder, CriteriaQuery<?> criteria,
			Root<T> root, Root<J> joinRoot, Long assessmentId) throws NoSuchFieldException {

		List<Predicate> andingPredicates = new ArrayList<>();

		if (assessmentId != null) {
			andingPredicates.add(builder.equal(root.get("assessmentId"), assessmentId));
		}
		if (joinTable != null) {
			joinRoot = criteria.from(joinTable);
			andingPredicates.add(builder.equal(root.get(primaryKeyRef), joinRoot.get(joinTableColumnForJoin)));
		}

		List<Selection<?>> selections = setSelections(listingDto, root, joinRoot);
		criteria.multiselect(selections);

		addSerchPredicatesForPrimaryOrJoinTable(listingDto, domainClazz, joinTable, builder, root, joinRoot,
				andingPredicates);

		Predicate andSearchPredicateVar = builder.and(andingPredicates.toArray(new Predicate[andingPredicates.size()]));
		criteria.where(andSearchPredicateVar);
	}

	private static <T, J> void addSerchPredicatesForPrimaryOrJoinTable(ListingDto listingDto, Class<T> domainClazz,
			Class<J> joinTable, CriteriaBuilder builder, Root<T> root, Root<J> joinRoot,
			List<Predicate> andingPredicates) throws NoSuchFieldException {
		if (CollectionUtils.isNotEmpty(listingDto.getSearch())) {
			boolean isJoinTableColumnsPresent = CollectionUtils.isNotEmpty(listingDto.getJoinTableColumnNames());
			for (SearchDto searchDto : listingDto.getSearch()) {
				if (isJoinTableColumnsPresent
						&& listingDto.getJoinTableColumnNames().contains(searchDto.getSearchCol())) {
					addSearchPredicates(builder, joinRoot, searchDto, joinTable, andingPredicates);
				} else {
					addSearchPredicates(builder, root, searchDto, domainClazz, andingPredicates);
				}
			}
		}
	}

	private static <T, J> List<Selection<?>> setSelections(ListingDto listingDto, Root<T> root, Root<J> joinRoot) {
		List<Selection<?>> selections = new LinkedList<>();
		if (CollectionUtils.isNotEmpty(listingDto.getColumnNames())) {
			for (String columnName : listingDto.getColumnNames()) {
				selections.add(root.get(columnName));
			}
		}
		if (joinRoot != null && CollectionUtils.isNotEmpty(listingDto.getJoinTableColumnNames())) {
			for (String columnName : listingDto.getJoinTableColumnNames()) {
				selections.add(joinRoot.get(columnName));
			}
		}
		return selections;
	}

	private static <U> void setResultDomainList(ListingDto listingDto, Class<U> resultClazz, List<Object[]> result,
			List<U> resultDomainList) throws InstantiationException, IllegalAccessException, NoSuchFieldException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {

		if (CollectionUtils.isEmpty(listingDto.getColumnNames())) {
			return;
		}

		List<String> columnNames = new ArrayList<>(listingDto.getColumnNames());

		if (CollectionUtils.isNotEmpty(listingDto.getJoinTableColumnNames())) {
			columnNames.addAll(listingDto.getJoinTableColumnNames());
		}

		log.info("Setting data for Requested Columns : {}", columnNames);

		for (Object[] obj : result) {
			Constructor<U> constructor = resultClazz.getDeclaredConstructor();
			U claz = constructor.newInstance();
			for (int i = 0; i < obj.length; i++) {
				Field field = resultClazz.getDeclaredField(columnNames.get(i));
				field.setAccessible(true);
				if (setDateValueAsStringIfResultFieldIsDateType(claz, field, obj[i])) {
					continue;
				}
				field.set(claz, obj[i]);
			}
			resultDomainList.add(claz);
		}
	}

	private static <U> boolean setDateValueAsStringIfResultFieldIsDateType(U claz, Field field, Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		if (obj instanceof Date date) {
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dd_MM_yyyy);
			String formattedDate = sdf.format(date);

			if (field.getType().equals(String.class)) {
				field.set(claz, formattedDate);
			} else if (field.getType().equals(Date.class)) {
				try {
					Date parsedDate = sdf.parse(formattedDate);
					field.set(claz, parsedDate);
				} catch (ParseException e) {
					throw new IllegalArgumentException("Error parsing formatted date: " + formattedDate, e);
				}
			}
			return true;
		}
		return false;
	}

	public static <T> void addSearchPredicates(CriteriaBuilder builder, Root<T> root, SearchDto searchDto,
			Class<T> clazz, List<Predicate> predicates) throws NoSuchFieldException, SecurityException {

		String likesymbol = "%";
		String decodedSearch = LanguageUtil.decodeValue(searchDto.getSearch());

		Field f = clazz.getDeclaredField(searchDto.getSearchCol());

		switch (f.getType().toString()) {
		case "byte", "class java.lang.Byte":
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()), Byte.parseByte(decodedSearch)));
			break;
		case "short", "class java.lang.Short":
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()), Short.parseShort(decodedSearch)));
			break;
		case "int", "class java.lang.Integer":
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()), Integer.parseInt(decodedSearch)));
			break;
		case "long", "class java.lang.Long":
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()), Long.parseLong(decodedSearch)));
			break;
		case "double", "class java.lang.Double":
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()), Double.parseDouble(decodedSearch)));
			break;
		case "float", "class java.lang.Float":
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()), Float.parseFloat(decodedSearch)));
			break;
		case "class java.util.Date":
			Date date = DateUtil.getDateByEpochTime(Long.valueOf(decodedSearch));
			addDatePredicateBySearchType(builder, root, searchDto, predicates, searchDto.getSearchType(), date);
			break;
		default:
			predicates.add(builder.like(builder.lower(root.get(searchDto.getSearchCol())),
					likesymbol.concat(decodedSearch.toLowerCase()).concat(likesymbol)));
			break;
		}
	}

	private static <T> void addDatePredicateBySearchType(CriteriaBuilder builder, Root<T> root, SearchDto searchDto,
			List<Predicate> predicates, SearchType searchType, Date date) {
		if (searchType == null) {
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()).as(java.sql.Date.class), date));
			return;
		}
		switch (searchType) {
		case LESS_THAN:
			predicates.add(builder.lessThan(root.get(searchDto.getSearchCol()).as(java.sql.Date.class), date));
			break;
		case LESS_THAN_OR_EQUAL_TO:
			predicates.add(builder.lessThanOrEqualTo(root.get(searchDto.getSearchCol()).as(java.sql.Date.class), date));
			break;
		case GREATER_THAN:
			predicates.add(builder.greaterThan(root.get(searchDto.getSearchCol()).as(java.sql.Date.class), date));
			break;
		case GREATER_THAN_OR_EQUAL_TO:
			predicates.add(
					builder.greaterThanOrEqualTo(root.get(searchDto.getSearchCol()).as(java.sql.Date.class), date));
			break;
		default:
			predicates.add(builder.equal(root.get(searchDto.getSearchCol()).as(java.sql.Date.class), date));
			break;
		}
	}

	public static <T, J> Long fetchRowsCount(EntityManager em, ListingDto listingDto, Class<T> domainClazz,
			String primaryKeyRef, Class<J> joinTable, String joinTableColumnForJoin, Long companyId)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, TechnicalException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {

		log.info(LogUtil.startLog(CLASSNAME));
		log.info("Listing Dto {}", listingDto);
		Long totalCount = null;

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> countCriteriaQuery = builder.createQuery(Long.class);
		Root<T> root = countCriteriaQuery.from(domainClazz);
		Root<J> joinRoot = null;

		addRequiredPredicates(listingDto, domainClazz, primaryKeyRef, joinTable, joinTableColumnForJoin, builder,
				countCriteriaQuery, root, joinRoot, companyId);

		countCriteriaQuery.select(builder.count(root));
		totalCount = em.createQuery(countCriteriaQuery).getSingleResult();

		log.info("totalCount {}", totalCount);

		log.info(LogUtil.exitLog(CLASSNAME));
		return totalCount;
	}

}
