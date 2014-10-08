package com.nosolojava.fsm.impl.model.basic;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.executable.CustomAction;

public abstract class AbstractBasicAction implements CustomAction, Serializable {
	private static final long serialVersionUID = 8337365423249077173L;

	private final String namespace;

	public AbstractBasicAction(String namespace) {
		super();
		this.namespace = namespace;
	}

	@Override
	public String getNamespace() {
		return this.namespace;
	}
}
