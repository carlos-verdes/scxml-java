package com.nosolojava.fsm.runtime.executable.externalcomm;

import java.io.Serializable;
import java.net.URI;

import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.externalcomm.Send;
import com.nosolojava.fsm.model.state.DoneData;

public interface Message extends Serializable {

	String getId();

	String getName();

	URI getTarget();

	URI getSource();

	/**
	 * Message body that is defined by the scxml, could be the &lt;<a
	 * href="http://www.w3.org/TR/scxml/#content">content</a>&gt; element or a combination of &lt;param&gt;'s and
	 * namelist.
	 * <p>
	 * This will be used in {@link DoneData}, {@link Send} and {@link Invoke}.
	 * 
	 * @return
	 */
	<T> T getBody();

}
