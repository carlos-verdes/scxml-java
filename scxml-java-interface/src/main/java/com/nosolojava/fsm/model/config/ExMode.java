package com.nosolojava.fsm.model.config;

public enum ExMode {

	LAX("lax"), STRICT("strict");
	private String value;

	private ExMode(String value) {
		this.value = value;
	}

	public static ExMode parse(String exMode) {
		ExMode result = null;
		String aux;
		if (exMode != null) {
			aux = exMode.toLowerCase();
			if ("lax".equals(aux)) {
				result = ExMode.LAX;
			} else if ("strict".equals(aux)) {
				result = ExMode.STRICT;
			}

		}
		return result;
	}

	@Override
	public String toString() {
		return value;
	}

}
