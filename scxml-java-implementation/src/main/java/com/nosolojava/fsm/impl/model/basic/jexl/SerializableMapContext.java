package com.nosolojava.fsm.impl.model.basic.jexl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.MapContext;

/**
 * Wraps a map in a context.
 * <p>
 * Each entry in the map is considered a variable name, value pair.
 * </p>
 */
public class SerializableMapContext extends MapContext implements JexlContext,
		Serializable {
	private static final long serialVersionUID = -208047995047845985L;

	public SerializableMapContext() {
		super();
	}

	public SerializableMapContext(Map<String, Object> vars) {
		super(vars);
	}
	
	public SerializableMapContext createNewFromCurrent(){
		Map<String, Object> copiedInfo = new HashMap<String, 
				Object>(this.map);
		SerializableMapContext result = new SerializableMapContext(copiedInfo);
		return result;
	}
	
}
