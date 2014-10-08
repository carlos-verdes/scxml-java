package com.nosolojava.fsm.impl.model.basic.datamodel;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.PlatformEvents;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Param;
import com.nosolojava.fsm.runtime.Context;

public class BasicParam implements Param {
	private static final long serialVersionUID = -6187448160597734524L;
	private final String name;
	private final String expr;
	private final String location;

	public BasicParam(String name, String expr, String location) throws ConfigurationException {
		super();

		if (expr != null && location != null) {
			throw new ConfigurationException("Can't be expr and location in the same param element.");
		}
		if (expr == null && location == null) {
			throw new ConfigurationException("Expr and location can't be null in a param element.");
		}

		this.name = name;
		this.expr = expr;
		this.location = location;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object evaluateParam(Context context) {
		Object result = null;
		try {
			if (expr != null) {
				result = context.getDataByExpression(expr);
			} else {
				result = context.getDataByName(location);
			}

		} catch (Exception e) {
			context.offerInternalEvent(new BasicEvent(PlatformEvents.EXECUTION_ERROR.getEventName(), e));
		}

		return result;
	}

	@Override
	public String getLocation() {
		return this.location;
	}

}
