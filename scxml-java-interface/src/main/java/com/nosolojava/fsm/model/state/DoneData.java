package com.nosolojava.fsm.model.state;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.Context;

/**
 * This interface represents the scxml <a href="http://www.w3.org/TR/scxml/#donedata">#donedata</a> element.
 * @author Carlos Verdes
 *
 */
public interface DoneData extends Serializable{

	/**
	 * 
	 * @param context
	 * @return the &lt;content&gt; tag in a Serializable object or the &lt;param&gt;'s in a Map&lt;String,Serializable&gt;. See scxml <a href="http://www.w3.org/TR/scxml/#donedata">#donedata</a>.
	 */
	Serializable evaluateDoneData(Context context);

}
