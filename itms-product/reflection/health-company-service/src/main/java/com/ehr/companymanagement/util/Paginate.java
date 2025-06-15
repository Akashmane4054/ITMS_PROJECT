package com.ehr.companymanagement.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

//import javax.persistence.EntityManager;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
//import org.hibernate.Query;
//import org.hibernate.jpa.internal.QueryImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

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
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by rahul on 17/7/20.
 */
@Slf4j
public class Paginate {

	public static <T> Page<T> paginate(EntityManager em, ListingDto listingDto, Long companyId, Class<T> clazz) {

		log.info("Listing Dto {}", listingDto);
		Pageable pageable = null;
		Sort sort = null;
		if (CollectionUtils.isNotEmpty(listingDto.getSort())) {
			log.info("Sort columns for entity {} => {}", clazz.getCanonicalName(), listingDto.getSort());
			for (SortDto sortDto : listingDto.getSort()) {

				Sort currentSort = Sort.by(Sort.Direction.fromString(sortDto.getSortOrder()), sortDto.getSortField());

				if (sort == null) {
					sort = currentSort;
				} else {
					sort = sort.and(currentSort);
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
		Predicate activepredicate = builder.equal(root.get(Constants.ACTIVE), listingDto.getStatus());
		Predicate companypredicate = builder.equal(root.get(Constants.COMPANY_ID), companyId);
		Predicate typePredicate = null;
		if (listingDto != null && listingDto.getType() != null) {
			typePredicate = builder.equal(root.get(Constants.STATUS), listingDto.getType());
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
							try {
								Date startDate = new Date(Long.valueOf(decodedSearch));
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(startDate);
								calendar.set(Calendar.HOUR_OF_DAY, 23);
								calendar.set(Calendar.MINUTE, 59);
								calendar.set(Calendar.SECOND, 59);
								calendar.set(Calendar.MILLISECOND, 999);
								Date endDate = calendar.getTime();
								predicates.add(builder.greaterThanOrEqualTo(
										root.get(searchDto.getSearchCol()).as(java.util.Date.class), startDate));
								predicates.add(builder.lessThanOrEqualTo(
										root.get(searchDto.getSearchCol()).as(java.util.Date.class), endDate));
							} catch (NumberFormatException e) {
								log.error("Invalid timestamp format: " + decodedSearch);
							}
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
			if (typePredicate != null) {
				criteria.where(builder.and(searchpredicatevar, typePredicate, activepredicate, companypredicate));
			} else {
				criteria.where(builder.and(searchpredicatevar, activepredicate, companypredicate));
			}

		} else {
			if (typePredicate != null) {
				criteria.where(builder.and(activepredicate, typePredicate, companypredicate));
			} else {
				criteria.where(builder.and(activepredicate, companypredicate));
			}

		}
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
