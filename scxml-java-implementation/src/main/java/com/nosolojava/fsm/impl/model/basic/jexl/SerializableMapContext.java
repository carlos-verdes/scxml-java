package com.nosolojava.fsm.impl.model.basic.jexl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jexl2.JexlContext;

/**
 * Wraps a map in a context.
 * <p>
 * Each entry in the map is considered a variable name, value pair.
 * </p>
 */
public class SerializableMapContext implements JexlContext, Serializable {
    private static final long serialVersionUID = -208047995047845985L;

    private final ConcurrentHashMap<String, Object> innerMap;

    public SerializableMapContext() {
        this(new HashMap<String, Object>());
    }


    public SerializableMapContext(Map<String, Object> vars) {
        super();
        this.innerMap = new ConcurrentHashMap<String, Object>(vars);
    }

    public SerializableMapContext createNewFromCurrent() {
        SerializableMapContext result = new SerializableMapContext(this.innerMap);
        return result;
    }

    public Set<String> dataModelKeySet() {
        return this.innerMap.keySet();
    }

    public void removeEntry(String key) {
        this.innerMap.remove(key);
    }

    @Override
    public Object get(String name) {
        if (name != null) {
            Object val = this.innerMap.get(name);
            return val != VOID ? val : null;
        } else {
            return null;
        }
    }

    @Override
    public void set(String name, Object value) {
        if (value == null) {
            this.innerMap.put(name, VOID);
        } else {
            this.innerMap.put(name, value);
        }
    }

    @Override
    public boolean has(String name) {

        return name != null ? this.innerMap.containsKey(name) : null;
    }

    protected static class VoidValueClass {
    }

    protected static final VoidValueClass VOID = new VoidValueClass();

}
