package com.nosolojava.fsm.impl.model.basic;

import java.math.BigDecimal;
import java.util.logging.Logger;

import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.BindingType;
import com.nosolojava.fsm.model.config.ExMode;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.executable.Script;

public class BasicStateMachineModel implements StateMachineModel {
	private static final long serialVersionUID = 9160151597785635578L;

	transient private Logger logger = Logger.getLogger(this.getClass().getName());

	private String name;
	private String namespace;
	private BigDecimal version;
	private BindingType bindingType;
	private ExMode exMode;
	private Script script;

	private final State rootState;

	public BasicStateMachineModel(State rootState) {
		super();
		this.rootState = rootState;
	}

	@Override
	public State getRootState() {
		return this.rootState;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public BigDecimal getVersion() {
		return version;
	}

	@Override
	public void setVersion(BigDecimal version) {
		this.version = version;
	}

	@Override
	public BindingType getBindingType() {
		return bindingType;
	}

	@Override
	public void setBindingType(BindingType bindingType) {
		this.bindingType = bindingType;
	}

	@Override
	public ExMode getExMode() {
		return exMode;
	}

	@Override
	public void setExMode(ExMode exMode) {
		this.exMode = exMode;
	}

	@Override
	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}
	
}
