package com.nosolojava.fsm.model.datamodel;

import com.nosolojava.fsm.runtime.executable.Executable;

public interface Log extends Executable {

	String getLabel();

	void setLabel(String label);

	String getExpression();

	void setExpression(String expr);

}
