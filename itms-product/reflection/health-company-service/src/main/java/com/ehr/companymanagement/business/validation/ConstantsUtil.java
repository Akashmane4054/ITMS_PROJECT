package com.ehr.companymanagement.business.validation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstantsUtil {

	private ConstantsUtil() {
	}

	public static String getConstant(String errorCode, String language) {
		try {
			Class<?> clazz = Class.forName(ConstantsUtil.class.getPackage().getName() + "." + language + "Constants");
			Constructor<?> ctor = clazz.getConstructor();
			Object object = ctor.newInstance();
			Field getterMethod = clazz.getDeclaredField(errorCode);
			return getterMethod.get(object) + "";
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
		return "";
	}
}
