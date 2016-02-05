package com.nosolojava.fsm.impl.model.basic.jexl;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.SerializableContextInstance;
import org.apache.commons.jexl2.JexlEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

public class JexlMapFSMContextInstance implements SerializableContextInstance {
    private static final long serialVersionUID = -8517548919319424511L;

    private transient Logger logger = Logger.getLogger(this.getClass().getName());

    private final String sessionId;
    private final String parentSessionId;
    private final ArrayList<String> activeStates;
    private transient JexlEngine jexl;

    public JexlMapFSMContextInstance(String sessionId, String parentSessionId, SerializableMapContext innerContext,
                                     SortedSet<State> activeStates) {
        super();

        this.sessionId = sessionId;
        this.parentSessionId = parentSessionId;

        this.activeStates = new ArrayList<String>(activeStates != null ? activeStates.size() : 0);
        for (State activeState : activeStates) {
            this.activeStates.add(activeState.getName());
        }

        jexl = new JexlEngine();
    }

//	@Override
//	public void writeExternal(final ObjectOutput out) throws IOException {
//		out.writeObject(sessionId);
//		out.writeObject(parentSessionId);
//		out.writeInt(activeStates.size());
//		activeStates.forEach(state -> writeString(out,state));
//
//		StringBuilder serializableKeysSB= new StringBuilder("keys:");
//		innerContext.dataModelKeySet().forEach(key->{
//
//		});
//
//	}
//
//	protected void writeString(ObjectOutput out,String aux){
//		try {
//			out.writeObject(aux);
//		} catch (IOException e) {
//			throw new RuntimeException("Error writing string",e);
//		}
//	}
//
//	@Override
//	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//
//	}


    private JexlEngine getEngineInstance() {
        if (jexl == null) {
            jexl = new JexlEngine();
        }

        return this.jexl;
    }

    @Override
    public boolean isStateActive(String... stateNames) {

        boolean result = false;

        for (String stateName : stateNames) {
            if (this.activeStates.contains(stateName)) {
                result = true;
                break;
            }
        }

        return result;
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

}
