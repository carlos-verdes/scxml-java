package com.nosolojava.fsm.impl.model.basic.jexl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.SerializableContextInstance;

public class JexlMapFSMContextInstance implements SerializableContextInstance {
	private static final long serialVersionUID = -8517548919319424511L;

	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	private final String sessionId;
	private final String parentSessionId;
	private final List<String> activeStates;
	private final SerializableMapContext innerContext;
	private transient JexlEngine jexl;

	public JexlMapFSMContextInstance(String sessionId,String parentSessionId,SerializableMapContext innerContext,SortedSet<State> activeStates) {
		super();
		
		this.sessionId=sessionId;
		this.parentSessionId= parentSessionId;
		this.innerContext = innerContext;
		
		this.activeStates= new ArrayList<String>(activeStates!=null?activeStates.size():0);
		for(State activeState:activeStates){
			this.activeStates.add(activeState.getName());
		}
		
		//remove current event
		this.innerContext.removeEntry(Context.EVENT_NAME);
		
		jexl = new JexlEngine();
	}

	private JexlEngine getEngineInstance() {
		if (jexl == null) {
			jexl = new JexlEngine();
		}
		
		return this.jexl;
	}

	@Override
	public String getSessionId() {
		return this.sessionId;
	}

	@Override
	public String getParentSessionId() {
		return this.parentSessionId;
	}

	@Override
	public List<String> getActiveStates() {
		return this.activeStates;
	}

	@Override
	public Set<String> dataModelKeySet() {
		return this.innerContext.dataModelKeySet();
	}

	@Override
	public <T> T getDataByName(String name) {
		@SuppressWarnings("unchecked")
		T result = (T) this.innerContext.get(name);
		return result;
	}

	@Override
	public <T> T getDataByExpression(String expression) {
		Expression e;

		try {
			e = getEngineInstance().createExpression(expression);
		} catch (NullPointerException ex) {
			logger.log(Level.SEVERE, String.format("Error evaluating expresion %s", expression), ex);
			return null;
		}
		@SuppressWarnings("unchecked")
		T data = (T) e.evaluate(this.innerContext);

		return data;
	}

}
