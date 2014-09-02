package com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Body;
import com.nosolojava.fsm.model.externalcomm.Finalize;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class BasicInvoke implements Invoke {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private String id;
	private final String idLocation;
	private final URI src;
	private final String srcExpression;
	private final String type;
	private final String typeExpression;
	private final boolean autoForward;
	private final Finalize finalize;
	private InvokeHandler invokeHandler;
	private Body body;

	private MessageFormat targetMF = new MessageFormat("#{0}");

	public BasicInvoke(String id, String idLocation, URI src, String srcExpression, String type, String typeExpression,
			boolean autoForward, Finalize finalize, Body body) throws ConfigurationException {
		super();

		if (id != null && idLocation != null) {
			throw new ConfigurationException("Cant be Id and Idlocation in the same invoke element.");
		}
		if (src != null && srcExpression != null) {
			throw new ConfigurationException("Cant be src and srcExpression in the same invoke element.");
		}
		if (type != null && typeExpression != null) {
			throw new ConfigurationException("Cant be type and typeExpression in the same invoke element.");
		}

		this.id = id;
		this.idLocation = idLocation;
		this.src = src;
		this.type = type;
		this.typeExpression = typeExpression;
		this.srcExpression = srcExpression;
		this.autoForward = autoForward;
		this.finalize = finalize;
		if (this.finalize != null) {
			this.finalize.setInvoke(this);
		}

		this.body = body;
	}

	@Override
	public String getType(Context context) {
		String result;
		if (this.type != null) {
			result = this.type;
		} else {
			result = (String) context.getDataByExpression(this.typeExpression);

		}

		return result;
	}

	@Override
	public URI getSource(Context context) throws URISyntaxException {
		URI result = null;

		if (this.src != null) {
			result = this.src;
		} else {
			if (this.srcExpression != null) {
				Object res = context.getDataByExpression(srcExpression);
				if (res != null) {
					String aux = (String) res;
					result = new URI(aux);
				}
			}
		}
		return result;
	}

	@Override
	public String getId(Context context) {
		String result = null;
		if (this.id != null) {
			result = this.id;
		} else {
			result = context.getDataByName(this.idLocation);
		}

		return result;
	}

	@Override
	public boolean isAutoforward() {
		return this.autoForward;
	}

	@Override
	public Finalize getFinalize() {
		return this.finalize;
	}

	@Override
	public Object getBody(Context context) {
		Serializable result = null;

		if (this.body != null) {
			result = this.body.evaluateBody(context);
		}
		return result;
	}

	@Override
	public void call(String stateId, Context context) {

		//generate id if necessary
		if (this.idLocation != null) {
			//the id should be generated and stored in idlocation
			String result = stateId + "." + UUID.randomUUID().toString();
			context.updateData(this.idLocation, result);
		}

		// get the invoke handler
		String type = this.getType(context);

		// if the invoke handler hasn't been initialized or the type has changed (could be an expression)
		if (this.invokeHandler == null || !this.invokeHandler.getType().equals(type)) {
			//init invoke handler
			this.invokeHandler = context.getInvokeHandler(type);

		}

		//get the information for the invoked service
		BasicInvokeInfo invokeInfo = null;
		try {
			invokeInfo = new BasicInvokeInfo(this, context);

			// call onCall event
			invokeHandler.invokeService(invokeInfo, context);

		} catch (URISyntaxException e) {
			// TODO manage url exception on invokes
			logger.log(Level.SEVERE, "Error parsing invoke uri. {0}", new Object[] { this });
		}
	}

	protected Message createMessage(Event event, Context context) {
		String id = this.getId(context);
		String name = event.getName();

		//source is the event origin
		URI sourceURI = event.getOrigin();
		//uri fragment is used to include the invokeid
		URI targetURI = URI.create(this.targetMF.format(new Object[] { id }));

		//data will be a map with all the params and the content (with content key)
		Object body = this.getBody(context);

		BasicMessage message = new BasicMessage(id, name, sourceURI, targetURI, body);

		return message;
	}

	@Override
	public void manageEvent(Event event, Context context) {

		Message message = createMessage(event, context);

		this.invokeHandler.sendMessageToService(message);

	}

	@Override
	public void cancel(Context context) {
		this.invokeHandler.endSession(this.getId(context));
	}

	@Override
	public List<String> getLocations() {

		List<String> locations = new ArrayList<String>();

		if (this.body != null) {
			locations.addAll(this.body.getLocations());
		}
		return locations;
	}

}
