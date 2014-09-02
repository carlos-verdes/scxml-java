package com.nosolojava.fsm.model.datamodel;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.Context;

public interface Param extends Serializable {
	String getName();
	String getLocation();
	Object evaluateParam(Context context);
}
