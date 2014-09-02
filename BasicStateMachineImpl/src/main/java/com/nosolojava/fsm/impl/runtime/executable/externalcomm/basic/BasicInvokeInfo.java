package com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic;

import java.net.URI;
import java.net.URISyntaxException;

import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeInfo;

public class BasicInvokeInfo implements InvokeInfo {

	private final String invokeId;
	private final URI src;
	private final Object body;

	public BasicInvokeInfo(Invoke invoke, Context context) throws URISyntaxException {
		super();

		this.invokeId = invoke.getId(context);
		this.src = invoke.getSource(context);
		this.body = invoke.getBody(context);

	}

	@Override
	public String getInvokeId() {
		return invokeId;
	}

	@Override
	public URI getSource() {
		return this.src;
	}

	@Override
	public Object getBody() {
		return this.body;
	}

}
