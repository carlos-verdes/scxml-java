scxml-java
==========

#Introduction

Java library that implements SCXML standard (http://www.w3.org/TR/scxml).

This implementation has only dependency with Apache JEXL to evaluate context expressions so it's easier to use with antoher frameworks like Android.

Android lib has been publised to use this lib in Android apps.
You can clone from https://github.com/nosolojava/scxml-android.git


#How to include in your project

This module will be in a public repository, right now you should download and do a clean-install with Maven:

```
    git clone https://github.com/nosolojava/scxml-java.git
    cd scxml-java
    mvn clean install
```

Maven dependency:
```xml
<dependency>
	<artifactId>scxml-java-implementation</artifactId>
	<groupId>com.nosolojava.fsm</groupId>
	<version>1.0.1-SNAPSHOT</version>
</dependency>
```
	
 
#How to start/stop a session
 
You  can create an SCXML resource like this (in the example simpleSM.xml):
```xml
<scxml name="basicStates" version="1.0"
	xmlns="http://www.w3.org/2005/07/scxml"
	initial="main-state">
	<datamodel>
		<data id="salute" expr="'hello world'" />
		<data id="state" expr="'none'" />
		<data id="user" expr="null" />
	</datamodel>
	<state id="main-state" initial="disconnected">
		<state id="connected-state">
			<onentry>
				<assign location="salute" expr="'connected'" />
				<send type="console" eventexpr="salute+' '+user" />
			</onentry>
			<transition event="disconnect" target="disconnected-state">
			</transition>
		</state>
		<state id="disconnected-state">
			<onentry>
				<assign location="salute" expr="'disconnected'" />
			</onentry>
			<transition event="connect" target="connected-state">
				<assign location="user" expr="_event.data" />
			</transition>
		</state>
		<transition event="exit" target="off-state" />
	</state>
	<final id="off-state">
	</final>
</scxml>
```

And then you can start SM session with the next code:
```java
// set DEBUG true|false
BasicStateMachineFramework.DEBUG.set(true);
// init engine
StateMachineEngine engine = new BasicStateMachineEngine();
engine.start();
	
// start from classpath uri
URI uri=new URI("classpath:simpleSM.xml");
// ... or start from file uri
URI uri= new URI("file:///c:/scxml-java/simpleSM.xml");
// ... or start from http uri
URI uri= new URI("http://nosolojava.com/simpleSM.xml");

//start normal session
Context ctx = engine.startFSMSession(uri);

//start a children session (parentSessionId should exists in the engine)
Context childrenCtx= engine.startFSMSession(parentSessionId,uri);


```


When you finish you can stop the engine waiting the current sessions to finish or just interrupt them:
```java
//shutdown the engine and wait 50 milliseconds for the sessions to finish
engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);


//force shutdown (no wait)
engine.forceShutdown();
```

#How to communicate with a session


The communication between SCXML sessions with other sessions/applications is done with events (inputs) and messages (outputs), and are performed by I/O event processors. Each I/O processor is able to handle a diferent type of events/messages and transport mechanisms (for example the scxml-android module has I/O processors for intent events).

##Send an event to a session
To send events to a session from your IO proccesor you could offer a message to a context or send to the engine with the session id.

Some examples using the context
```java

Context ctx= ...;

//create an event (name and data) and offer to external queue
Event event = new BasicEvent("connect.event", new SomeClass());
ctx.offerExternalEvent(event);

//use some helper methods
ctx.offerExternalEvent("connect.event");
ctx.offerExternalEvent("connect.event",new SomeClass());

```

But sometimes you just have the sessionId or a session URI (the IO processors should send with the messages a response URI so the called system can answer to the fsm session, but the responsability to extract the session ID is for the IO processor).

Some examples with the engine:
```java
StateMachineEngine engine=...;
Context ctx=...;
Principal userPrincipal=...;

engine.pushEvent(ctx.getSessionId(), new BasicEvent("connect.event", userPrincipal));
engine.pushEvent(sessionId, new BasicEvent("connect.event", userPrincipal));

```

Example of IO processor implementation
```java
public void onNewEvent(URI destinationSession,String eventName,Object data){
	URI sessionURI=...;

	String sessionId= extractSessionFromUri(sessionURI);
	engine.pushEvent(sessionId,new BasicEvent(eventName,data));

}
```

##Send events between sessions

SCXML allows to invoke a new SCXML session so it's possible to send messages between them.

To invoke a new session the <invoke element should be used with type="scxml" and a valid source uri.
Any valir URL could be used and an special scheme classpath://{classname} has been added to the engine.

Example:
```xml
...
<state id="invoking-calculator-state">
	<!-- invoke another session with id session2 -->
	<invoke type="scxml" id="session2" autoforward="false" srcexpr="'classpath:calculatorSM.xml'" namelist="result" />
	<!-- state waiting user operation -->
	<state id="preparingInput-state">
		<!-- if a new operation event arrives -->
		<transition event="operation.event" target="calculating-state">
			<!-- send the new operation to the invoked session2 using the fragment identifier of the uri -->
			<send type="scxml" targetexpr="'#session2'" event="operation.event">
		</transition>
	</state>
	<!-- state waiting calculator result -->
	<state id="calculating-state">
		<transition event="operation.result.event" target="preparingInput-state">
			<!-- stores the result in the context and wait for next operation -->
			<assign location="result" expr="_event.data.result" />
		</transition>
	</state>
</state>
```

Finally the called session could answer to the parent session with the special id _parent:
```xml
<transition event="operation.event" type="internal">
	<!-- assign some local variables -->
	<assign location="lastOperator" expr="_event.data.operator" />
	<assign location="lastOperands" expr="_event.data.operands" />
	<!-- execute some custom action -->
	<custom-actions:calculate operands="lastOperands" operator="lastOperator" resultLocation="result" />
	<!-- send the result back to the parent -->
	<send target="#_parent" event="operation.result.event" namelist="result" />
</transition>
```


