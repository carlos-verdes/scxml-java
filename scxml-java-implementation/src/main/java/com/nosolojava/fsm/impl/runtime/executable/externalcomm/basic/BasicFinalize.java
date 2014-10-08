package com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic;

import java.text.MessageFormat;
import java.util.List;

import com.nosolojava.fsm.model.externalcomm.Finalize;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicFinalize implements Finalize {
	private static final long serialVersionUID = 5063734037915308094L;

	private final List<Executable> executables;
	private Invoke invoke = null;
	private MessageFormat eventParamMF = new MessageFormat("_event.data.{0}");

	public BasicFinalize(List<Executable> executables) {
		super();
		this.executables = executables;
	}

	@Override
	public void run(Context context) {
		if (this.executables != null && !this.executables.isEmpty()) {
			for (Executable executable : this.executables) {
				executable.run(context);
			}

		} else {
			//if there is no executable then update parent data model

			//get the locations to update
			List<String> locations = this.invoke.getLocations();

			for (String location : locations) {
				String expression = eventParamMF.format(new String[] { location });
				Object value = context.getDataByExpression(expression);
				if (value != null) {
					context.updateData(location, value);
				}
			}

		}
	}

	@Override
	public void setInvoke(Invoke invoke) {
		this.invoke = invoke;

	}

}
