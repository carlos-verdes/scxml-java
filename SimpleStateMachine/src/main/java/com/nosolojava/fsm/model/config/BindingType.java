package com.nosolojava.fsm.model.config;

public enum BindingType {

	EARLY("early"), LATE("late");

	private BindingType(String value) {
		this.value = value;
	}

	private String value;

	public static BindingType parse(String bindingType) {
		BindingType result = null;
		String aux;
		if (bindingType != null) {
			aux = bindingType.toLowerCase();
			if ("early".equals(aux)) {
				result = BindingType.EARLY;
			} else if ("late".equals(aux)) {
				result = BindingType.LATE;
			}

		}
		return result;
	}

	@Override
	public String toString() {
		return value;
	}

}
