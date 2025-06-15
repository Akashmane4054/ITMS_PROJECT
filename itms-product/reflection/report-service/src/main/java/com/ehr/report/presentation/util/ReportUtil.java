package com.ehr.report.presentation.util;

import java.util.Map;

public class ReportUtil {
	public static String createTemplate(Map<String, String> values, String template) {
		for (Map.Entry<String, String> value : values.entrySet()) {
			template = template.replace(value.getKey(), value.getValue());
		}
		return template;
	}
}
