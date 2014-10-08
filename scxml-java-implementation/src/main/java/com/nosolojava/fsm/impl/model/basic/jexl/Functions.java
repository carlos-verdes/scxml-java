package com.nosolojava.fsm.impl.model.basic.jexl;

import java.text.MessageFormat;
import java.util.Map;

import com.nosolojava.fsm.runtime.Context;

public class Functions {
	private Context ctx;
	public static ThreadLocal<MessageFormat> IN_FUNCTION = new ThreadLocal<MessageFormat>() {
		private static final String pattern = "In(''{0}'')";
		private final MessageFormat mf = new MessageFormat(pattern);

		@Override
		public MessageFormat initialValue() {
			return mf;
		}

	};

	public Functions(Context ctx) {
		super();
		this.ctx = ctx;
	}

	public boolean In(String stateName) {
		return ctx.isActiveStateByName(stateName);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map PutOnMap(Map originalMap, Object key, Object value) {
		originalMap.put(key, value);
		return originalMap;
	}

}
