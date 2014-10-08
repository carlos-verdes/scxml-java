package com.nosolojava.fsm.model.datamodel;

import java.io.Serializable;
import java.net.URL;

public interface URLDataHandler {

	String getProtocol();
	Serializable getData(URL url);
}
