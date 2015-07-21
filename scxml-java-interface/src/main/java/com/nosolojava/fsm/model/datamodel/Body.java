package com.nosolojava.fsm.model.datamodel;

import java.util.Set;

import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.externalcomm.Send;
import com.nosolojava.fsm.model.state.DoneData;
import com.nosolojava.fsm.runtime.Context;

/**
 * This class is used when an elemen can have both &lt;param&gt; and &lt;content&gt; children. When in evaluated it
 * retrieves the content or the params depending on the configuration (can't be both params and content in the same
 * element).
 * <p> This will be used in {@link DoneData}, {@link Send} and {@link Invoke}.</p>
 * 
 * @author Carlos Verdes
 * 
 */
public interface Body {

	Set<String> getLocations();
	
	/**
	 * @return true if the method evaluateBody will return a {@code Map<String,Serializable>}.
	 */
	boolean isParamsBody();
	
	<T> T evaluateBody(Context context);
}
