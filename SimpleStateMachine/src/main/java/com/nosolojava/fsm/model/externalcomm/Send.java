package com.nosolojava.fsm.model.externalcomm;

import java.net.URI;
import java.net.URISyntaxException;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Executable;

/**
 * <p>
 * This interface represents a send element (http://www.w3.org/TR/scxml/#send). At runtime, the FSM will create a
 * {@link SendSnapshot} with the current configuration and pass it to the {@link EventDispatcher} (so it can chose a proper send handler and send the event).
 * <p>
 * For example imagine the next send scxml element: <send id="'author_'+i" ... />. If the current config has that i=5, then the
 * {@link SendSnapshot} will return and id="author_5". This is important to let the event dispatcher send the event
 * while the FSM could continue with its processing (so, if another FSM action changes the value of i in the FSM configuration the {@link EventDispatcher} is not affected).
 * 
 * @author Carlos Verdes
 * 
 */
public interface Send extends Executable {

	String getEventName(Context context);

	String getType(Context context);

	URI getTarget(Context context) throws URISyntaxException;

	String getId(Context context);

	long getDelay(Context context);

	Object getBody(Context context);

}
