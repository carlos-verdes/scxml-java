package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Assign;

public class BasicAssign implements Assign {
	private static final long serialVersionUID = 1300495592980349446L;
	transient Logger logger = Logger.getLogger(this.getClass().getName());
	protected final String location;
	private final String expression;
	private final String value;
	private final String toText;

	protected BasicAssign(String location, String expression, String value, String toText) {
		super();
		this.location = location;
		this.expression = expression;
		this.value = value;
		this.toText = toText;

	}

	public static BasicAssign assignByExpression(String location, String expression) {
		String toText = "BasicAssign [" + location + "= " + expression + "]";
		return new BasicAssign(location, expression, null, toText);
	}

	public static BasicAssign assignByValue(String location, String value) {
		String toText = "BasicAssign [" + location + "= " + value + "]";
		return new BasicAssign(location, null, value, toText);
	}

	@Override
	public void run(Context context) {
		logger.log(Level.FINE, "Running assign {0}", new Object[] { this });
		Object valueAux = null;
		if (this.expression != null) {
			valueAux = context.getDataByExpression(this.expression);
		} else {
			valueAux = this.value;
		}

		String realLocation = getRealLocation(context);
		logger.log(Level.FINE, "Value= {0}, location= {1} and realLocatin= {2}", new Object[] { valueAux, location,
				realLocation });

		if (context.existsVarName(realLocation)) {
			context.updateData(realLocation, valueAux);
			logger.log(Level.FINE, "Context updated {0}={1}",
					new Object[] { realLocation, context.getDataByName(realLocation) });
		} else {
			logger.log(Level.SEVERE, "Context COULD NOT BE updated, var name {0} not found.",
					new Object[] { realLocation });

		}

	}

	protected String getRealLocation(Context context) {
		String realLocation = this.location;
		//allow expressions for locations (when location is not found only)
		if (!context.existsVarName(this.location)) {
			realLocation = context.getDataByExpression(this.location);
		}
		return realLocation;
	}

	@Override
	public String toString() {
		return toText;
	}

}
