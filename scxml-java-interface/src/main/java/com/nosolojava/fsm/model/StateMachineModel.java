package com.nosolojava.fsm.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.nosolojava.fsm.model.config.BindingType;
import com.nosolojava.fsm.model.config.ExMode;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.executable.Script;

public interface StateMachineModel extends Serializable{

	//name
	String getName();
	void setName(String name);
	
	//namespace
	String getNamespace();
	void setNamespace(String ns);

	//version
	BigDecimal getVersion();
	void setVersion(BigDecimal version);
	
	//binding type
	BindingType getBindingType();
	void setBindingType(BindingType bindingType);
	
	//ex mode
	ExMode getExMode();
	void setExMode(ExMode exMode);

	//get root state
	State getRootState();
	
	//script
	Script getScript();
	void setScript(Script script);
}
