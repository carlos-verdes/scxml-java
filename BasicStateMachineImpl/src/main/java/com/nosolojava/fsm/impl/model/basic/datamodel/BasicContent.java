package com.nosolojava.fsm.impl.model.basic.datamodel;

import com.nosolojava.fsm.model.datamodel.Content;
import com.nosolojava.fsm.runtime.Context;

public class BasicContent implements Content {
	private final String content;
	private final String contentExpr;

	private BasicContent(String content, String contentExpr) {
		super();
		this.content = content;
		this.contentExpr = contentExpr;
	}

	public static BasicContent createSimpleContent(String content) {
		BasicContent result = new BasicContent(content, null);
		return result;
	}

	public static BasicContent createContentExpression(String contentExpr) {
		BasicContent result = new BasicContent(null, contentExpr);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T evaluateContent(Context context) {
		T result = null;
		if (this.content != null) {
			result = (T)this.content;
		} else if (this.contentExpr != null) {
			result = context.getDataByExpression(this.contentExpr);
		}

		return result;
	}

}
