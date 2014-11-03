package com.nosolojava.fsm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.nosolojava.fsm.impl.model.basic.BasicStateMachineModel;
import com.nosolojava.fsm.impl.model.basic.datamodel.BasicBody;
import com.nosolojava.fsm.impl.model.basic.datamodel.BasicContent;
import com.nosolojava.fsm.impl.model.basic.datamodel.BasicData;
import com.nosolojava.fsm.impl.model.basic.datamodel.BasicParam;
import com.nosolojava.fsm.impl.model.basic.state.BasicDoneData;
import com.nosolojava.fsm.impl.model.basic.state.BasicHistoryState;
import com.nosolojava.fsm.impl.model.basic.state.BasicInitialState;
import com.nosolojava.fsm.impl.model.basic.state.BasicState;
import com.nosolojava.fsm.impl.model.basic.transition.BasicTransition;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicAssign;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicElif;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicElse;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicForEach;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicIf;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicLocalScript;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicRaise;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicRemoteScript;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicFinalize;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicInvoke;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicSend;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Body;
import com.nosolojava.fsm.model.datamodel.Param;
import com.nosolojava.fsm.model.externalcomm.Finalize;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.externalcomm.Send;
import com.nosolojava.fsm.model.state.DoneData;
import com.nosolojava.fsm.model.state.HistoryTypes;
import com.nosolojava.fsm.model.state.InitialState;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.executable.Elif;
import com.nosolojava.fsm.runtime.executable.Else;
import com.nosolojava.fsm.runtime.executable.Executable;
import com.nosolojava.fsm.runtime.executable.ForEach;
import com.nosolojava.fsm.runtime.executable.If;
import com.nosolojava.fsm.runtime.executable.Log;

public class XppStateMachineParser implements StateMachineParser {
	// private final Logger logger =
	// Logger.getLogger(this.getClass().getName());

	public static final String SCXML = "scxml";
	public static final String INITIAL = "initial";
	public static final String STATE = "state";
	private static final String PARALLEL = "parallel";
	private static final String HISTORY = "history";
	private static final String FINAL = "final";
	public static final String TRANSITION = "transition";
	public static final String SEND = "send";
	public static final String INVOKE = "invoke";
	public static final String ID = "id";
	public static final String IDLOCATION = "idlocation";
	public static final String NAME = "name";
	public static final String ON_ENTRY = "onentry";
	public static final String ON_EXIT = "onexit";
	// public static final String SCXML_NAMESPACE =
	// "http://www.w3.org/2005/07/scxml";
	static final String TARGET = "target";
	static final String TARGETEXPR = "targetexpr";
	static final String EVENT = "event";
	static final String EVENTEXPR = "eventexpr";
	static final String GUARD_CONDITION = "cond";
	static final String TYPE = "type";
	static final String TYPEEXPR = "typeexpr";
	static final String INTERNAL = "internal";
	static final String RAISE = "raise";
	static final String IF_COND = "if";
	static final String ELSE_COND = "else";
	static final String ELIF_COND = "elif";
	static final String FOR_EACH = "foreach";
	static final String ASSIGN = "assign";
	static final String LOCATION = "location";
	static final String EXPRESSION = "expr";
	static final String DATAMODEL = "datamodel";
	static final String SRC = "src";
	static final String SRC_EXPRESSION = "srcexpr";
	static final String ARRAY = "array";
	static final String ITEM = "item";
	static final String INDEX = "index";
	static final String DELAY = "delay";
	static final String DELAYEXPR = "delayexpr";
	static final String NAMELIST = "namelist";
	static final String PARAM = "param";
	static final String EXPR = "expr";
	static final String CONTENT = "content";
	static final String SCRIPT = "script";
	static final String AUTO_FORWARD = "autoforward";
	static final String TRUE = "true";
	static final String FINALIZE = "finalize";
	static final String DONEDATA = "donedata";
	protected static final String LOG = "log";
	protected static final String LABEL = "label";

	public final static String CLASSPATH = "classpath";
	public final static String HTTP = "http";

	private final Map<String, XppActionParser> actionParsers = new ConcurrentHashMap<String, XppActionParser>();

	public XppStateMachineParser() throws ConfigurationException {
		this(null);
	}

	public XppStateMachineParser(List<XppActionParser> actionParsers)
			throws ConfigurationException {
		super();

		loadDefaultActionParsers();

		if (actionParsers != null) {
			for (XppActionParser actionParser : actionParsers) {
				String ns = actionParser.getNamespace();
				if (this.actionParsers.containsKey(ns)) {
					throw new ConfigurationException(
							"Action parser repeated for ns {0}",
							new Object[] { ns });
				}
				this.actionParsers.put(ns, actionParser);
			}
		}

	}

	private void loadDefaultActionParsers() {
		this.actionParsers.put(AssertCustomActionParser.NS,
				new AssertCustomActionParser());
	}

	@Override
	public boolean validURI(URI source) {

		boolean result = false;
		if (source != null) {
			if (isClasspathURI(source)) {
				result = true;
			} else {
				// try to create a url from URI so an inputstream could be
				// obtained
				try {
					new URL(source.toString());
					result = true;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		}

		return result;
	}

	protected boolean isClasspathURI(URI source) {
		return hasURIScheme(source, CLASSPATH);
	}

	protected boolean isHttpURI(URI source) {
		return hasURIScheme(source, HTTP);
	}

	protected boolean hasURIScheme(URI source, String scheme) {
		return source != null && source.getScheme().equals(scheme);
	}

	@Override
	public StateMachineModel parseScxml(URI source)
			throws ConfigurationException, IOException, SCXMLParserException {

		if (source == null) {
			throw new SCXMLParserException(
					"Error parsing SCXML, source is null");
		}

		InputStream is = null;
		if (isClasspathURI(source)) {
			String location = source.getSchemeSpecificPart();

			is = ClassLoader.getSystemClassLoader().getResourceAsStream(
					location);

		} else {

			try {
				URL url = new URL(source.toString());
				is = (InputStream) url.getContent();
			} catch (Exception e) {
				throw new SCXMLParserException(String.format(
						"SCXML uri %s is not supported.", source), e);

			}
		}

		StateMachineModel model = null;
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			parser.setInput(is, null);

			model = this.parseXPP(parser, "");
		} catch (XmlPullParserException e) {
			throw new ConfigurationException(
					"Error parsing scxml from classpath with xpp, uri: "
							+ source, e);
		} finally {
			if (is != null) {
				is.close();
			}
		}

		return model;
	}

	// methods to extend
	protected StateMachineModel createStateMachine(State rootState) {
		StateMachineModel smm = new BasicStateMachineModel(rootState);
		return smm;
	}

	protected State createRootState() throws ConfigurationException {
		State rootState = BasicState.createRootState();
		return rootState;
	}

	protected Executable createAssignByValue(String location, String value) {
		Executable currentExec;
		currentExec = BasicAssign.assignByValue(location, value);
		return currentExec;
	}

	protected Executable createAssignByExpression(String location, String expr) {
		Executable currentExec;
		currentExec = BasicAssign.assignByExpression(location, expr);
		return currentExec;
	}

	public StateMachineModel parseXPP(XmlPullParser xpp)
			throws XmlPullParserException, IOException, ConfigurationException {
		return parseXPP(xpp, null);

	}

	public StateMachineModel parseXPP(XmlPullParser xpp, String namespace)
			throws XmlPullParserException, IOException, ConfigurationException {

		xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

		StateMachineModel smm = null;
		State currentState = null;
		Transition currentTransition = null;
		List<Executable> executables;
		Invoke invoke = null;

		while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
			String tagName = xpp.getName();
			// start tag
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				// parse scxml tag
				if (SCXML.equals(tagName)) {
					smm = parseRootState(namespace, xpp);
					currentState = smm.getRootState();
				}
				// parse root state
				else if (INITIAL.equals(tagName)) {
					parseInitialState(namespace, xpp, currentState);
					// parse state or parallel state tag
				} else if (HISTORY.equals(tagName)) {
					parseHistoryState(namespace, xpp, currentState);
				} else if (STATE.equals(tagName) || PARALLEL.equals(tagName)
						|| FINAL.equals(tagName)) {
					currentState = parseState(namespace, xpp, currentState,
							tagName);
					if (currentState.isFinal()) {
						currentState = currentState.getParent();
					}
				}
				// parse transition tag
				else if (TRANSITION.equals(tagName)) {
					currentTransition = parseTransition(namespace, xpp,
							currentState);
					currentState.addTransition(currentTransition);

					executables = parseExecutables(namespace, xpp);
					currentTransition.addExecutables(executables);
				} else if (ON_ENTRY.equals(tagName)) {
					executables = parseExecutables(namespace, xpp);
					currentState.addOnEntryExecutables(executables);
				} else if (ON_EXIT.equals(tagName)) {
					executables = parseExecutables(namespace, xpp);
					currentState.addOnExitExecutables(executables);

				} else if (DATAMODEL.equals(tagName)) {
					parseDatamodel(namespace, xpp, currentState);

				} else if (INVOKE.equals(tagName)) {
					invoke = parseInvoke(namespace, xpp);
					currentState.addInvoke(invoke);
				}

				// end tag
			} else if (xpp.getEventType() == XmlPullParser.END_TAG) {

				// if a state has been parsed
				if (STATE.equals(tagName) || PARALLEL.equals(tagName)
						|| FINAL.equals(tagName)) {
					// current state is the parent state
					currentState = currentState.getParent();
				}
				// if a transition has been parsed
				if (TRANSITION.equals(tagName)) {
					// current transition is null
					currentTransition = null;
				}
				// text
			} else if (xpp.getEventType() == XmlPullParser.TEXT) {

			}

			xpp.next();

		}

		return smm;
	}

	protected Executable parseLog(String namespace, XmlPullParser xpp) {
		Executable result = null;
		String label = xpp.getAttributeValue(namespace, LABEL);
		String expr = xpp.getAttributeValue(namespace, EXPR);

		if (label == null) {
			label = this.getClass().getCanonicalName();
		}

		result = createLog(label, expr);
		return result;
	}

	protected Executable createLog(String label, String expr) {
		Executable result;
		result = new Log(label, expr);
		return result;
	}

	private void parseDatamodel(String namespace, XmlPullParser xpp,
			State currentState) throws XmlPullParserException, IOException,
			ConfigurationException {
		String endTag = xpp.getName();

		while (!endTag.equals(xpp.getName())
				|| xpp.getEventType() != XmlPullParser.END_TAG) {
			xpp.next();

			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				String id = xpp.getAttributeValue(namespace, ID);
				if (id == null) {
					throw new ConfigurationException(
							"ID is mandatory for data element");
				}

				String src = xpp.getAttributeValue(namespace, SRC);

				BasicData data;
				if (src != null) {
					data = BasicData.createSrcData(id, new URL(src));

				} else {
					String expr = xpp.getAttributeValue(namespace, EXPRESSION);

					if (expr != null) {
						data = BasicData.createExpressionData(id, expr);
					} else {
						String content = xpp.nextText();
						data = BasicData.createValueData(id, content);
					}
				}
				currentState.addData(data);
			}

		}

	}

	private Invoke parseInvoke(String ns, XmlPullParser xpp)
			throws XmlPullParserException, IOException, ConfigurationException {
		// get the invoke tag
		String endTag = xpp.getName();

		String id = xpp.getAttributeValue(ns, ID);
		String idLocation = xpp.getAttributeValue(ns, IDLOCATION);
		String srcString = xpp.getAttributeValue(ns, SRC);
		URI src = null;
		if (srcString != null) {
			try {
				src = new URI(srcString);
			} catch (URISyntaxException e) {
				throw new ConfigurationException(e);
			}

		}
		String srcExpression = xpp.getAttributeValue(ns, SRC_EXPRESSION);
		String type = xpp.getAttributeValue(ns, TYPE);
		String typeExpression = xpp.getAttributeValue(ns, TYPEEXPR);
		String namelist = xpp.getAttributeValue(ns, NAMELIST);
		String autoForwardString = xpp.getAttributeValue(ns, AUTO_FORWARD);
		boolean autoForward = autoForwardString != null
				&& TRUE.equals(autoForwardString) ? true : false;
		Finalize finalize = null;
		String content = null;
		String contentExpr = null;

		// while not invoke end tag--> parse invoke
		Param param;
		Set<Param> params = new HashSet<Param>();
		while (!endTag.equals(xpp.getName())
				|| xpp.getEventType() != XmlPullParser.END_TAG) {
			xpp.next();
			// if we have a start tag we have more to parse
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				// parse param
				if (PARAM.equals(xpp.getName())
						&& xpp.getEventType() != XmlPullParser.END_TAG) {
					String name = xpp.getAttributeValue(ns, NAME);
					String expr = xpp.getAttributeValue(ns, EXPR);
					String location = xpp.getAttributeValue(ns, LOCATION);

					param = new BasicParam(name, expr, location);
					params.add(param);
				}

				// parse content
				if (CONTENT.equals(xpp.getName())
						&& xpp.getEventType() != XmlPullParser.END_TAG) {
					contentExpr = xpp.getAttributeValue(ns, EXPR);
					if (contentExpr == null) {
						xpp.next();
						content = xpp.getText();
					}
				}

				// parse finalize
				if (FINALIZE.equals(xpp.getName())
						&& xpp.getEventType() != XmlPullParser.END_TAG) {
					List<Executable> executables = parseExecutables(ns, xpp);
					finalize = new BasicFinalize(executables);
				}
			}

		}

		Body body = extractBody(namelist, content, contentExpr, params);

		Invoke result = new BasicInvoke(id, idLocation, src, srcExpression,
				type, typeExpression, autoForward, finalize, body);

		return result;
	}

	private List<Executable> parseExecutables(String scxmlNamespace,
			XmlPullParser xpp) throws XmlPullParserException, IOException,
			ConfigurationException {
		// get the parent tag for executables
		String endTag = xpp.getName();

		List<Executable> result = new ArrayList<Executable>();
		Executable currentExec = null;

		// while not parent end tag--> parse executables
		while (!endTag.equals(xpp.getName())
				|| xpp.getEventType() != XmlPullParser.END_TAG) {
			xpp.next();

			// if we have a start tag we have executables
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				currentExec = parseExecutable(scxmlNamespace, xpp);
				if (currentExec != null) {
					result.add(currentExec);
				}

			} else if (xpp.getEventType() == XmlPullParser.END_TAG) {
				currentExec = null;
			}
		}

		return result;
	}

	private Executable parseExecutable(String scxmlNamespace, XmlPullParser xpp)
			throws XmlPullParserException, IOException, ConfigurationException {
		String tagName;
		tagName = xpp.getName();
		Executable currentExec = null;
		if (RAISE.equals(tagName)) {
			String eventName = xpp.getAttributeValue(scxmlNamespace, EVENT);
			currentExec = new BasicRaise(eventName);

		} else if (SEND.equals(tagName)) {
			currentExec = parseSend(scxmlNamespace, xpp);

		} else if (IF_COND.equals(tagName)) {
			currentExec = parseIf(scxmlNamespace, xpp);

		} else if (ASSIGN.equals(tagName)) {
			currentExec = parseAssign(scxmlNamespace, xpp);

		} else if (FOR_EACH.equals(tagName)) {

			currentExec = parseForEach(scxmlNamespace, xpp);
		} else if (LOG.equals(tagName)) {
			currentExec = parseLog(scxmlNamespace, xpp);
		} else if (SCRIPT.equals(tagName)) {
			currentExec = parseScript(scxmlNamespace, xpp);
		} else {
			// if nothing else... then custom action?�
			String ns = xpp.getNamespace();
			if (!actionParsers.containsKey(ns)) {
				throw new ConfigurationException(
						"Can''t parse tag {0}, it should be a custom action. Try to register a custom action for namespace: {1}",
						new Object[] { tagName, ns });
			}
			XppActionParser parser = actionParsers.get(ns);
			currentExec = parser.parseAction(xpp);

		}
		return currentExec;
	}

	private Executable parseScript(String namespace, XmlPullParser xpp)
			throws ConfigurationException, XmlPullParserException, IOException {
		Executable result = null;
		String urlExpr = xpp.getAttributeValue(namespace, SRC);
		String content = xpp.nextText();
		if (content == null) {
			content = "";
		}

		if (urlExpr != null && !"".equals(content.trim())) {
			throw new ConfigurationException(
					"Script can't have src attribute and content");
		}

		if (urlExpr != null) {
			result = new BasicRemoteScript(urlExpr);
		} else {
			result = new BasicLocalScript(content);
		}

		return result;
	}

	private Executable parseSend(String ns, XmlPullParser xpp)
			throws XmlPullParserException, IOException, ConfigurationException {
		String eventName = xpp.getAttributeValue(ns, EVENT);
		String eventexpr = xpp.getAttributeValue(ns, EVENTEXPR);

		String targetString = xpp.getAttributeValue(ns, TARGET);
		URI target;
		try {
			target = targetString != null ? new URI(targetString) : null;
		} catch (URISyntaxException e) {
			throw new ConfigurationException(e);
		}
		String targetexpr = xpp.getAttributeValue(ns, TARGETEXPR);
		String type = xpp.getAttributeValue(ns, TYPE);
		String typeexpr = xpp.getAttributeValue(ns, TYPEEXPR);
		String id = xpp.getAttributeValue(ns, ID);
		String idlocation = xpp.getAttributeValue(ns, IDLOCATION);
		String delayString = xpp.getAttributeValue(ns, DELAY);
		Long delay = delayString != null ? parseLong(delayString) : null;
		String delayexpr = xpp.getAttributeValue(ns, DELAYEXPR);
		String namelist = xpp.getAttributeValue(ns, NAMELIST);

		String content = null;
		String contentExpr = null;
		Set<Param> params = new HashSet<Param>();
		Param param;
		while (!SEND.equals(xpp.getName())
				|| xpp.getEventType() != XmlPullParser.END_TAG) {
			xpp.next();

			if (PARAM.equals(xpp.getName())
					&& xpp.getEventType() != XmlPullParser.END_TAG) {
				String name = xpp.getAttributeValue(ns, NAME);
				String expr = xpp.getAttributeValue(ns, EXPR);
				String location = xpp.getAttributeValue(ns, LOCATION);

				param = new BasicParam(name, expr, location);
				params.add(param);
			}

			if (CONTENT.equals(xpp.getName())
					&& xpp.getEventType() != XmlPullParser.END_TAG) {
				contentExpr = xpp.getAttributeValue(ns, EXPR);
				if (contentExpr == null) {
					xpp.next();
					content = xpp.getText();
				}
			}

		}

		Body body = extractBody(namelist, content, contentExpr, params);

		Send result = new BasicSend(id, idlocation, eventName, eventexpr, type,
				typeexpr, target, targetexpr, delay, delayexpr, body);

		return result;
	}

	protected Body extractBody(String namelist, String content,
			String contentExpr, Set<Param> params)
			throws ConfigurationException {
		Body body = null;
		if (content != null && contentExpr != null) {
			throw new ConfigurationException(
					"Can't be both content children and content expression");
		}

		if ((content != null || contentExpr != null) && params != null
				&& !params.isEmpty()) {
			throw new ConfigurationException("Can't be both content and params");
		}

		if (content != null) {
			body = BasicBody.createContentBody(BasicContent
					.createSimpleContent(content));
		} else if (contentExpr != null) {
			body = BasicBody.createContentBody(BasicContent
					.createContentExpression(contentExpr));
		} else {
			body = BasicBody.createParamsBody(params, namelist);
		}
		return body;
	}

	private Executable parseForEach(String scxmlNamespace, XmlPullParser xpp)
			throws XmlPullParserException, IOException, ConfigurationException {
		String array = xpp.getAttributeValue(scxmlNamespace, ARRAY);
		String item = xpp.getAttributeValue(scxmlNamespace, ITEM);
		String index = xpp.getAttributeValue(scxmlNamespace, INDEX);

		List<Executable> executables = parseExecutables(scxmlNamespace, xpp);

		ForEach result = new BasicForEach(array, item, index, executables);

		return result;
	}

	private Executable parseAssign(String scxmlNamespace, XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		Executable currentExec;
		String location = xpp.getAttributeValue(scxmlNamespace, LOCATION);
		String expr = xpp.getAttributeValue(scxmlNamespace, EXPRESSION);
		if (expr != null) {
			currentExec = createAssignByExpression(location, expr);
		} else {
			String value = xpp.nextText();
			currentExec = createAssignByValue(location, value);
		}
		return currentExec;
	}

	private If parseIf(String scxmlNamespace, XmlPullParser xpp)
			throws XmlPullParserException, IOException, ConfigurationException {
		String ifcond = xpp.getAttributeValue(scxmlNamespace, GUARD_CONDITION);
		List<Elif> elifs = new ArrayList<Elif>();
		String elifcond = null;

		Else elseOperation = null;
		List<Executable> ifExecutables = new ArrayList<Executable>();
		List<Executable> elifExecutables = new ArrayList<Executable>();
		List<Executable> elseExecutables = new ArrayList<Executable>();

		boolean foundElif = false;
		boolean foundElse = false;

		while (!xpp.getName().equals(IF_COND)
				|| xpp.getEventType() != XmlPullParser.END_TAG) {
			xpp.nextTag();
			String tagName = xpp.getName();

			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				// if elif
				if (ELIF_COND.equals(tagName)) {

					// manage previous elif
					if (foundElif) {
						elifExecutables = loadPreviousElif(scxmlNamespace, xpp,
								elifs, elifExecutables, elifcond);
					}
					// flag elif found
					elifcond = xpp.getAttributeValue(scxmlNamespace,
							GUARD_CONDITION);
					foundElif = true;

				} else if (ELSE_COND.equals(tagName)) {
					// flag else found
					foundElse = true;

				} else {
					// get exec
					Executable exec = parseExecutable(scxmlNamespace, xpp);

					// add exec to if or elif or else
					if (!foundElif && !foundElse) {
						ifExecutables.add(exec);
					} else if (foundElif && !foundElse) {
						elifExecutables.add(exec);
					} else {
						elseExecutables.add(exec);

					}
				}
			}
		}

		// create last elif
		if (foundElif) {
			elifExecutables = loadPreviousElif(scxmlNamespace, xpp, elifs,
					elifExecutables, elifcond);
		}
		// create else
		if (foundElse) {
			elseOperation = new BasicElse(elseExecutables);
		}

		// create if
		If result = new BasicIf(ifcond, elifs, elseOperation, ifExecutables);

		return result;
	}

	private List<Executable> loadPreviousElif(String scxmlNamespace,
			XmlPullParser xpp, List<Elif> elifs,
			List<Executable> elifExecutables, String elifcond) {
		Elif elif = new BasicElif(elifcond, elifExecutables);
		elifs.add(elif);
		elifExecutables = new ArrayList<Executable>();
		return elifExecutables;
	}

	private Transition parseTransition(String ns, XmlPullParser xpp,
			State currentState) {
		Transition result = null;

		String targetState = xpp.getAttributeValue(ns, TARGET);

		String eventName = xpp.getAttributeValue(ns, EVENT);
		String guardCondition = xpp.getAttributeValue(ns, GUARD_CONDITION);
		String type = xpp.getAttributeValue(ns, TYPE);
		boolean internal = type != null && INTERNAL.equals(type);
		result = new BasicTransition(currentState.getName(), eventName,
				targetState, guardCondition, internal);

		return result;
	}

	private void parseInitialState(String ns, XmlPullParser xpp, State state)
			throws ConfigurationException, XmlPullParserException, IOException {
		String id = xpp.getAttributeValue(ns, ID);

		goUntil(xpp, XmlPullParser.START_TAG);

		if (!TRANSITION.equals(xpp.getName())) {
			tagNotExpectedError(xpp, TRANSITION, xpp.getName());
		}

		Transition initialTransition = parseTransition(ns, xpp, state);
		List<Executable> executables = parseExecutables(ns, xpp);
		if (executables.size() > 0) {
			initialTransition.addExecutables(executables);
		}
		InitialState initial = new BasicInitialState(id, initialTransition);

		state.setInitialState(initial);
	}

	private void parseHistoryState(String ns, XmlPullParser xpp, State state)
			throws ConfigurationException, XmlPullParserException, IOException {
		String id = xpp.getAttributeValue(ns, ID);
		String historyTypeString = xpp.getAttributeValue(ns, TYPE);

		HistoryTypes historyType;
		if (historyTypeString == null || historyTypeString.equals("")) {
			historyType = HistoryTypes.SHALLOW;
		} else {
			historyType = HistoryTypes.fromString(historyTypeString);
		}

		goUntil(xpp, XmlPullParser.START_TAG);

		if (!TRANSITION.equals(xpp.getName())) {
			tagNotExpectedError(xpp, TRANSITION, xpp.getName());
		}
		String targetState = xpp.getAttributeValue(ns, TARGET);

		new BasicHistoryState(state, id, historyType, targetState);

	}

	private void tagNotExpectedError(XmlPullParser scxmlParser,
			String expected, String found) throws ConfigurationException {
		throw new ConfigurationException(
				"Tag {0} not expected. {1} expected. Line number: {2}",
				new Object[] { found, expected, scxmlParser.getLineNumber() });
	}

	private State parseState(String ns, XmlPullParser xpp, State parent,
			String tagName) throws XmlPullParserException, IOException,
			ConfigurationException {

		String id = xpp.getAttributeValue(ns, ID);
		if (id == null) {
			id = UUID.randomUUID().toString();
		}

		State state;

		if (STATE.equals(tagName)) {
			state = BasicState.createBasicState(id, parent);
			String initialStateName = xpp.getAttributeValue(ns, INITIAL);
			if (initialStateName != null && !"".equals(initialStateName)) {
				state.setInitialStateName(initialStateName);
			}
		} else if (FINAL.equals(tagName)) {

			// check if final has content or params

			Param param;
			String content = null;
			String contentExpr = null;
			Set<Param> params = new HashSet<Param>();
			List<Executable> onEntryExec = null;
			List<Executable> onExitExec = null;

			while (!FINAL.equals(xpp.getName())
					|| xpp.getEventType() != XmlPullParser.END_TAG) {
				xpp.next();
				// if we have a start tag we have more to parse
				if (xpp.getEventType() == XmlPullParser.START_TAG) {
					// parse param
					if (PARAM.equals(xpp.getName())
							&& xpp.getEventType() != XmlPullParser.END_TAG) {
						String name = xpp.getAttributeValue(ns, NAME);
						String expr = xpp.getAttributeValue(ns, EXPR);
						String location = xpp.getAttributeValue(ns, LOCATION);

						param = new BasicParam(name, expr, location);
						params.add(param);
					}

					// parse content
					if (CONTENT.equals(xpp.getName())
							&& xpp.getEventType() != XmlPullParser.END_TAG) {
						contentExpr = xpp.getAttributeValue(ns, EXPR);
						if (contentExpr == null) {
							xpp.next();
							content = xpp.getText();
						}
					} else if (ON_ENTRY.equals(xpp.getName())) {
						onEntryExec = parseExecutables(ns, xpp);
					} else if (ON_EXIT.equals(xpp.getName())) {
						onExitExec = parseExecutables(ns, xpp);
					}

				}

			}

			Body body = extractBody(null, content, contentExpr, params);
			DoneData doneData = new BasicDoneData(body);

			state = BasicState.createFinalState(id, parent, doneData);

			if (onEntryExec != null) {
				state.addOnEntryExecutables(onEntryExec);
			}
			if (onExitExec != null) {
				state.addOnExitExecutables(onExitExec);
			}

		} else {
			state = BasicState.createParallelState(id, parent);
		}

		return state;
	}

	private void goUntil(XmlPullParser scxmlParser, int startTag)
			throws XmlPullParserException, IOException {
		int next = scxmlParser.next();
		while (next != XmlPullParser.END_DOCUMENT && next != startTag) {
			next = scxmlParser.next();
		}

	}

	private StateMachineModel parseRootState(String namespace,
			XmlPullParser scxmlParser) throws ConfigurationException,
			XmlPullParserException {
		State rootState = createRootState();
		StateMachineModel smm = createStateMachine(rootState);

		smm.setNamespace(namespace);
		String auxString;
		if ((auxString = scxmlParser.getAttributeValue(namespace, NAME)) != null) {
			smm.setName(auxString);
		}

		String initial = scxmlParser.getAttributeValue(namespace, INITIAL);
		if (initial != null) {
			rootState.setInitialStateName(initial);
		}

		float auxFloat;
		String attributeValue = scxmlParser.getAttributeValue(namespace,
				"version");
		if (attributeValue == null) {
			throw new ConfigurationException(
					"The version attribute is mandatory.");
		}
		if ((auxFloat = parseFloat(attributeValue)) != -1) {
			smm.setVersion(new BigDecimal(auxFloat));
		}
		return smm;
	}

	private float parseFloat(String attributeValue)
			throws ConfigurationException {

		float result = -1;

		try {
			result = Float.parseFloat(attributeValue);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Error parsing float", e);
		}

		return result;
	}

	private Long parseLong(String attributeValue) throws ConfigurationException {

		Long result = null;

		try {
			result = Long.parseLong(attributeValue);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Error parsing long", e);
		}

		return result;
	}

	public static void main(String[] args) {

		URI uri = URI.create("fsm://sessionId/sendEvent/eventName");

		String schemeSpecificPart = uri.getSchemeSpecificPart();
		System.out.println("schemeSpecificPart: " + schemeSpecificPart);
		System.out.println("path: " + uri.getPath());
		String[] pathParts = uri.getPath().split("/");
		System.out.println("action: " + pathParts[1]);
		System.out.println("event: " + pathParts[2]);
		System.out.println("host: " + uri.getHost());
		System.out.println("authority: " + uri.getAuthority());

		uri = URI.create("fsm:///sendEvent");
		System.out.println("host2: " + uri.getHost());
	}
}
