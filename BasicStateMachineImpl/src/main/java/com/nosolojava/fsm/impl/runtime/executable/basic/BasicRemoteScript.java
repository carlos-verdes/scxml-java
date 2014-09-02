package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.io.IOException;
import java.net.URL;

import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicMessage;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Script;

public class BasicRemoteScript implements Script {
	private static final long serialVersionUID = -7654496973339387873L;
	private final String codeContentURLExpr;

	public BasicRemoteScript(String codeContentURLExpr) {
		super();
		this.codeContentURLExpr = codeContentURLExpr;
	}

	@Override
	public void run(Context context) {
		try {
			String urlValue = context.getDataByExpression(codeContentURLExpr);
			URL codeContentURL = new URL(urlValue);
			String content = (String) codeContentURL.getContent();
			context.executeScript(content);
		} catch (IOException e) {
			context.getScxmlIOProcessor().sendMessage(BasicMessage.createSimpleSCXMLMessage("error.executable.remoteScript", context));
		}
	}

}
