package com.nosolojava.fsm.model.externalcomm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;

/**
 * Interface that represents the http://www.w3.org/TR/scxml/#invoke element.
 * 
 * @author Carlos Verdes
 * 
 */
public interface Invoke {

	String getId(Context context);

	URI getSource(Context context) throws URISyntaxException;

	String getType(Context context);

	/**
	 * When a {@link Finalize} exists but is empty, {@link Finalize} should update the parent data model. To do this the
	 * {@link Finalize} should get the namelist and the locations for each invoke Param.
	 * 
	 * @return the list of names that reference the datamodel
	 */
	List<String> getLocations();

	boolean isAutoforward();

	Finalize getFinalize();

	Object getBody(Context context);

	void call(String stateId, Context context);

	void manageEvent(Event event, Context context);

	void cancel(Context context);

}
