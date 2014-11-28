package com.nosolojava.fsm.impl.model.basic.jexl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl2.JexlContext;

/**
 * Wraps a map in a context.
 * <p>
 * Each entry in the map is considered a variable name, value pair.
 * </p>
 */
public class SerializableMapContext implements JexlContext,
		Serializable {
	private static final long serialVersionUID = -208047995047845985L;

	private final HashMap<String, Object>  innerMap;
	
	public SerializableMapContext() {
		this(new HashMap<String, Object>());
	}

	
	public SerializableMapContext(Map<String, Object> vars) {
		super();
		this.innerMap=new HashMap<String, Object>(vars);
	}
	
	public SerializableMapContext createNewFromCurrent(){
		SerializableMapContext result = new SerializableMapContext(this.innerMap);
		return result;
	}
	
	public Set<String> dataModelKeySet(){
		return this.innerMap.keySet();
	}
	

	@Override
	public Object get(String name) {
		return this.innerMap.get(name);
	}

	@Override
	public void set(String name, Object value) {
		this.innerMap.put(name, value);
	}

	@Override
	public boolean has(String name) {
		return this.innerMap.containsKey(name);
	}
	
	
	
}
