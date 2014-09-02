package com.nosolojava.fsm.impl.model.basic.datamodel;

import com.nosolojava.fsm.model.datamodel.DataValue;

public class BasicDataValue implements DataValue {
	private static final long serialVersionUID = -7047350169636078797L;
	private final String id;
	private final Object value;
	
	
	public BasicDataValue(String id, Object value) {
		super();
		this.id = id;
		this.value = value;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

}
