package org.xserver.component.json;

import java.util.ArrayList;
import java.util.List;

import org.xserver.common.util.JsonEngine;
import org.xserver.common.util.ReflectionUtil;
import org.xserver.common.util.StringUtil;
import org.xserver.component.json.bean.JQueryTab;
import org.xserver.component.json.util.JsonUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

/**
 * Json to XServerHttpResponse, the class support {@code mapper} method to
 * generate Object Json.
 * 
 * @author postonzhang
 * 
 */
public class JsonManager {
	public static String mapper(Object[] targets)
			throws JsonProcessingException {
		if (StringUtil.isEmpty(targets)) {
			return "{}";
		}

		if (targets.length % 2 != 0) {
			throw new IllegalArgumentException(
					"Argument should be key-value pair.");
		}

		StringBuilder result = new StringBuilder();
		result.append(JsonUtil.LEFT_BRACE);
		for (int i = 0; i < targets.length; i++) {
			if (i % 2 == 0) {
				result.append(JsonUtil.QUOTATION).append(targets[i])
						.append(JsonUtil.QUOTATION).append(JsonUtil.COLON);
			} else {
				if (targets[i] instanceof String) {
					String value = (String) targets[i];
					if (value.startsWith(JsonUtil.LEFT_BRACE)
							|| value.startsWith(JsonUtil.LEFT_BRACKET)) {
						result.append(value).append(JsonUtil.COMMA);
						continue;
					}
				}
				result.append(
						JsonEngine.DEFAULT_JACKSON_MAPPER
								.writeValueAsString(targets[i])).append(
						JsonUtil.COMMA);
			}
		}
		return result.substring(0, result.length() - 1) + JsonUtil.RIGHT_BRACE;
	}

	/**
	 * Serialized specified bean to json
	 * 
	 * @param target
	 *            specified bean
	 * @return json data
	 * @throws JsonProcessingException
	 */
	public static String json(Object target) throws JsonProcessingException {
		// when XServer use jsonExceptField to get json, must add the
		// @JsonFilter annotation to the JavaBean, According to this when we
		// want to serialize all field in the JavaBean will cause a
		// JsonMappingException: Can not resolve BeanPropertyFilter with id
		// 'exception-field-filter'. Then put the
		// DEFAULT_JACKSON_MAPPER.writeValueAsString and jsonExceptField(target,
		// new String[]{}) in try-catch block will solve the problem
		try {
			return JsonEngine.DEFAULT_JACKSON_MAPPER.writeValueAsString(target);
		} catch (JsonMappingException e) {
			return jsonExceptField(target, new String[] {});
		}
	}

	/**
	 * Serialized bean to json, but exclude some fields of specified bean target
	 * 
	 * @param target
	 *            specified bean
	 * @param fields
	 *            exclude field name
	 * @return json data
	 * @throws JsonProcessingException
	 */
	public static String jsonExceptField(Object target, String[] fields)
			throws JsonProcessingException {
		FilterProvider fp = new SimpleFilterProvider().addFilter(
				"except-field-filter",
				SimpleBeanPropertyFilter.serializeAllExcept(fields));
		JsonEngine.FILTER_JACKSON_MAPPER.setFilters(fp);
		return JsonEngine.FILTER_JACKSON_MAPPER.writeValueAsString(target);
	}

	/**
	 * Serialized bean to json, just include specified fields
	 * 
	 * @param target
	 *            target bean
	 * @param clazz
	 *            the serialized bean class
	 * @param fields
	 *            included fields
	 * @return json data
	 * @throws JsonProcessingException
	 */
	public static String jsonIncludeField(Object target, Class<?> clazz,
			String[] fields) throws JsonProcessingException {
		String[] exceptFields = ReflectionUtil.exceptFields(clazz, fields);
		return jsonExceptField(target, exceptFields);
	}

	public static void main(String[] args) throws Exception {
		JQueryTab tab = new JQueryTab();
		tab.setContent("123");
		tab.setId("1");

		List<JQueryTab> lists = new ArrayList<JQueryTab>();
		lists.add(tab);
		System.out.println(json(lists));
		System.out.println(jsonExceptField(lists, new String[] { "id" }));
	}
}
