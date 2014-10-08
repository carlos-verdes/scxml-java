package com.nosolojava.fsm.impl.model.basic.datamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Body;
import com.nosolojava.fsm.model.datamodel.Content;
import com.nosolojava.fsm.model.datamodel.Param;
import com.nosolojava.fsm.runtime.Context;

public class BasicBody implements Body {

	private final Content content;

	private final boolean isParamsBody;

	// TODO study if is better option to has a set of params instead of a map
	private final Map<String, Param> params;

	private BasicBody(Content content, Map<String, Param> params, boolean isParamsBody) {
		super();
		this.content = content;
		this.params = params;
		this.isParamsBody = isParamsBody;
	}

	public static BasicBody createContentBody(Content content) {
		BasicBody result = new BasicBody(content, null, false);
		return result;
	}

	public static BasicBody createParamsBody(Set<Param> params, String namelist) throws ConfigurationException {

		HashMap<String, Param> paramMap = new HashMap<String, Param>();
		for (Param param : params) {
			paramMap.put(param.getName(), param);
		}

		//if namelist != ""
		if (namelist != null && !"".equals(namelist.trim())) {
			//	then each name is a param with location = name
			String[] names = namelist.split(" ");
			for (String name : names) {
				Param aux = new BasicParam(name, null, name);
				paramMap.put(name, aux);
			}

		}

		BasicBody result = new BasicBody(null, paramMap, true);

		return result;

	}



	@SuppressWarnings("unchecked")
	@Override
	public <T> T evaluateBody(Context context) {

		T result = null;
		if (this.content != null) {
			result = this.content.evaluateContent(context);
		} else if (this.params != null) {
			result = (T) evaluateParams(context);
		}

		return result;
	}

	// create runtime params
	private HashMap<String, Object> evaluateParams(Context context) {
		HashMap<String, Object> runtimeParams = new HashMap<String, Object>();

		if (!this.params.isEmpty()) {
			Object value;
			for (Entry<String, Param> entry : this.params.entrySet()) {
				// evaluate param value with this context
				value = entry.getValue().evaluateParam(context);
				runtimeParams.put(entry.getKey(), value);
			}
		}

		return runtimeParams;
	}

	@Override
	public Set<String> getLocations() {
		Set<String> result = new HashSet<String>();

		if (this.params != null && !this.params.isEmpty()) {

			for (Param param : this.params.values()) {
				if (param.getLocation() != null) {
					result.add(param.getLocation());
				}
			}
		}

		return result;
	}

	@Override
	public boolean isParamsBody() {
		return this.isParamsBody;
	}
}
