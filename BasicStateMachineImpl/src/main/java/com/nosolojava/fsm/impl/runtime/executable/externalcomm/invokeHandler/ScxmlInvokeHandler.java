package com.nosolojava.fsm.impl.runtime.executable.externalcomm.invokeHandler;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.AbstractBasicInvokeHandler;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeInfo;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class ScxmlInvokeHandler extends AbstractBasicInvokeHandler {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private static final long serialVersionUID = 8225274975110994110L;

	public static final String NAME = "scxml";

	private StateMachineEngine engine;

	public ScxmlInvokeHandler() {
		super();
	}

	public ScxmlInvokeHandler(StateMachineEngine engine) {
		super();
		this.engine = engine;
	}

	@Override
	public String getType() {
		return NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void invokeServiceInternal(InvokeInfo invokeInfo, Context context) {

		// get the fsm session id
		String fsmSessionId = invokeInfo.getInvokeId();

		context.registerInvokeSessionId(fsmSessionId);

		// get the source for state machine
		URI fsmModelSource = invokeInfo.getSource();

		Map<String, Serializable> initValues = null;

		if (invokeInfo.getBody() != null && Map.class.isAssignableFrom(invokeInfo.getBody().getClass())) {
			try {
				initValues = (Map<String, Serializable>) invokeInfo.getBody();
			} catch (ClassCastException e) {
				//should never occur
				logger.log(Level.FINEST, "Error getting params map from invoke, invokeinfo: {0}",
						new Object[] { invokeInfo });
			}

		}

		//create a new FSM session
		try {
			this.engine.startFSMSession(fsmSessionId, context.getSessionId(), fsmModelSource, initValues);
		} catch (ConfigurationException e) {
			// TODO manage errors on invoke handler
			e.printStackTrace();
		} catch (IOException e) {
			// TODO manage errors on invoke handler
			e.printStackTrace();
		}

	}

	@Override
	public void onEndSession(String invokeId, Context context) {
		context.unRegisterInvokeSessionId(invokeId);
	}

	@Override
	public void sendMessageToService(Message message, Context context) {
		context.getScxmlIOProcessor().sendMessage(message);
	}

}
