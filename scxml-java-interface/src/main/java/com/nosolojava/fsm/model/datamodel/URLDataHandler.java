package com.nosolojava.fsm.model.datamodel;

import java.net.URL;

public interface URLDataHandler {

	String getProtocol();

	<T> T getData(URL url);
}
