package com.nosolojava.fsm.runtime;

import java.io.Serializable;
import java.util.Map;

import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;

public interface ContextFactory {

	Context createContext(String sessionId, String parentSessionId, StateMachineModel model, StateMachineEngine engine,
			Map<String, Serializable> initValues) throws ConfigurationException;

}
