package com.nosolojava.fsm.parser;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nosolojava.fsm.impl.runtime.executable.action.AssertEqualsAction;
import com.nosolojava.fsm.impl.runtime.executable.action.AssertNullAction;
import com.nosolojava.fsm.runtime.executable.CustomAction;

public class AssertCustomActionParser implements XppActionParser {

	private static final String CURRENT_EXPR = "currentExpr";
	private static final String EXPECTED_EXPR = "expectedExpr";
	private static final String EXPR = "expr";
	private static final String ASSERT_NULL = "assertNull";
	private static final String ASSERT_EQUALS = "assertEquals";
	public static final String NS = "http://nosolojava.com/customActions/assert";

	@Override
	public String getNamespace() {
		return NS;
	}

	@Override
	public CustomAction parseAction(XmlPullParser xpp)
			throws XmlPullParserException, IOException {

		CustomAction action = null;

		String tagName = xpp.getName();
		if (tagName.equals(ASSERT_EQUALS)) {
			String expectedExp = xpp.getAttributeValue(null, EXPECTED_EXPR);
			String currentExp = xpp.getAttributeValue(null, CURRENT_EXPR);
			action = new AssertEqualsAction(expectedExp, currentExp);
		} else if (tagName.equals(ASSERT_NULL)) {
			String currentExp = xpp.getAttributeValue(null, EXPR);
			action = new AssertNullAction(currentExp);

		}

		return action;
	}

}
