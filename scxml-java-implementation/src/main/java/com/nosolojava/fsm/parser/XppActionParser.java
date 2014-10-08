package com.nosolojava.fsm.parser;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nosolojava.fsm.runtime.executable.CustomAction;

public interface XppActionParser {	
	String getNamespace();
	
	CustomAction parseAction(XmlPullParser xpp) throws XmlPullParserException, IOException;
}
