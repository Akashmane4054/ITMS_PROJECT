package com.ehr.assessment.presentation.util;

import com.ehr.core.dto.ColumnDetailsDTO;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.dto.SearchDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.dto.SortDto;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.feignclients.ColumnServiceFeignProxy;
import com.ehr.core.util.Constants;
import com.ehr.core.util.LanguageUtil;
import com.ehr.core.util.ObjectUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class TemplatePaginate {

	private TemplatePaginate() {
	}

	public static <T> Page<T> paginate(EntityManager em, ListingDto listingDto, Class<T> clazz) {

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
							LocalDateTime localDateTime = LocalDateTime.ofInstant(
									Instant.ofEpochMilli(Long.valueOf(decodedSearch)), ZoneId.systemDefault());
							ZonedDateTime startOfDay = localDateTime.atZone(ZoneId.systemDefault()).withHour(0)
									.withMinute(0).withSecond(0).withNano(0);
							ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
							Instant startInstant = startOfDay.toInstant();
							Instant endInstant = endOfDay.toInstant();

							Date startDate = Date.from(startInstant);
							Date endDate = Date.from(endInstant);
							Expression<Date> startDateExpr = builder.literal(startDate);
							Expression<Date> endDateExpr = builder.literal(endDate);

							predicates.add(builder.between(root.get(searchDto.getSearchCol()).as(java.util.Date.class),
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
			Predicate searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
			criteria.where(builder.and(searchpredicatevar));
		}
		List<T> result1 = em.createQuery(criteria).getResultList();
		criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
		TypedQuery<T> createQuery = em.createQuery(criteria);

		List<T> result = em.createQuery(criteria).setFirstResult((int) pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();

//		log.info("QueryString => ", createQuery.unwrap(Query.class).getQueryString());
//		log.info("HibernateQuery => ", createQuery.unwrap(QueryImpl.class).getHibernateQuery().getQueryString());
//		log.info("AbstractQueryImpl => ", createQuery.unwrap(org.hibernate.internal.QueryImpl.class).getQueryString());
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

	@SuppressWarnings("unchecked")
	public static String getValueFromDto(Map<String, Object> map, Long id, String key) {
		if (ObjectUtils.isPositiveNonZero(id) && MapUtils.isNotEmpty(map)) {
			Object dto = map.get(String.valueOf(id));
			if (dto != null) {
				String val = ((Map<String, String>) dto).get(key);
				if (StringUtils.isNotEmpty(val)) {
					return val;
				}
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

}
