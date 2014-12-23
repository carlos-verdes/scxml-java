package com.nosolojava.fsm.impl.model.basic.datamodel;

import java.net.URL;

import com.nosolojava.fsm.model.datamodel.Data;
import com.nosolojava.fsm.runtime.Context;

public class BasicData implements Data {
	private static final long serialVersionUID = 8673104449087028014L;

	final String id;
	final URL src;
	final String expression;
	final String value;

	Object savedData = null;

	private BasicData(String id, URL src, String expression, String value) {
		super();
		this.id = id;
		this.src = src;
		this.expression = expression;
		this.value = value;
	}

	public static BasicData createSrcData(String id, URL src) {
		return new BasicData(id, src, null, null);
	}

	public static BasicData createExpressionData(String id, String expression) {
		return new BasicData(id, null, expression, null);
	}

	public static BasicData createValueData(String id, String value) {
		return new BasicData(id, null, null, value);
	}

	@Override
	public Object evaluateData(Context context) {
		Object result = null;

		// check if there is any saved data
		if (this.savedData != null) {
			result = this.savedData;
		} else {
			if (src != null) {
				result = context.getDataFromURL(src);
			} else if (this.expression != null) {
				result = context.getDataByExpression(this.expression);
			} else {
				result = this.value;
			}
		}

		return result;
	}

	@Override
	public void saveHistoricData(Context context) {

		Object newData = context.getDataByName(this.id);
		this.savedData = newData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public URL getSrc() {
		return this.src;
	}

}
