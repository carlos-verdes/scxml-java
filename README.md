scxml-java
==========

#Introduction

Java library that implements SCXML standard (http://www.w3.org/TR/scxml).


There is already an Apache implementation but the problem is that this library has a lot of dependencies that are really not needed and makes the integration with Android framework really hard. This was my original motivation when I started this project, to develop an SCXML lib so I can create a finite state machine Android framework. This implementation has only a dependency with Apache JEXL to evaluate context expressions so it's easier to integrate in any Java project.

State machine pattern is perfect for applications when different scenarios require different behavior. A good example are mobile applications where different states like "full/low battery", "connected/disconnected to a network", "authenticated/unauthenticated user status", etc. should change user interfaces or background service behavior (things like show login/disconnect button, blur screen on disconnected, don't download huge files if not under WIFI, slow frequency updates on low battery, etc.).  

The advantage of FSM (Finite State Machine) oriented design is that you don't need to create lots of nested if/then/else blocks with horrible boolean flags to control the behavior of your application, instead, your system has one/many active states which react to events in a different way. This events target transitions (or not) which could execute actions (or not). Also when a state is entered or exited some action could be executed. 

A good example could be a chat application with two states like awake/silent. If the state is "awake" when a new event "user.message" arrives a transition will be triggered executing actions like "vibrate twice" or "show notification". In the other hand, if the same event arrives on "silent" state, a transition will execute the action "save notification" with no user notification at all. 

When the user wants to "awake" the application (for example an event "switch on screen" arrives), then there would be a transition from "silent" state to "awake" state and in the "onentry" of "awake" state all the saved notifications are showed to the user (notifications that were saved in the FSM context).

To finish, we could improve our chat application with two new states "low/full battery", being full battery the parent of previous states. On low battery the application doesn't handle any event and when a transition to full battery is done, the application can send a notification to the server to receive all the pending messages. To include this new feature in a FSM is really simple and doesn't affect previous implementation (just include previous states as childrens of a full battery state), and... because this is an XML implementation you could update your transitions/actions/states online just updating the FSM XML, and all of this with no software update at all!! Isn't that interesting?

One workmate always says "never (use) if my friend"... and the truth is that once you understand and change your way of thinking to FSM you can't see anymore code that implements behavior with if/then/else. I hope I can transmit this to you and start a nice community of FSM developers. 


The other library (the android FSM framework), has features like show/hide views depending on state, send events to FSM when "onclick" event occurs or bind view components with fsm data with only a couple of attributes on your Android xml view layout (for example bind a TextView value with a FSM datamodel attribute, send a "login" event when a button onclick is triggered or show a ProgressBat only when state "loading" is active).

More information about Android FSM framework on: 
https://github.com/nosolojava/scxml-android.git


#How to include in your project

This module will be in a public repository, right now you should download and do a clean-install with Maven:


	git clone https://github.com/nosolojava/scxml-java.git
	cd scxml-java
	mvn clean install


Maven dependency:

```xml

	<dependency>
		<artifactId>scxml-java-implementation</artifactId>
		<groupId>com.nosolojava.fsm</groupId>
		<version>1.0.1-SNAPSHOT</version>
	</dependency>
	
```
	
 
#How to start/stop a session

You application could have different FSM sessions depending on how many behaviors you want to model. Each session will have his associated SCXML resource describing the different states, data model, actions and transitions. 
We call "the context" to the current configuration of a session in a concrete moment (which states are active, what information is loaded in the model, etc.). There is an StateMachineEngine which is the responsible to parse the SCXML resource and start a new session (creating a Context instance). One SCXML session can send messages to another SCXML session or even start it if needed (invoking a new SCXML session). This is handful when you have a FSM utility that you want to use in different applications. An SCXML session ends when all the active states are a special "final" state.
 
You  can create an SCXML resource like the next example and save it in the classpath, on your server, or any valid URL location.

Example of SCXML resource:

```xml

	<scxml name="basicStates" version="1.0"
		xmlns="http://www.w3.org/2005/07/scxml"
		initial="main-state">
		<datamodel>
			<data id="salute" expr="'hello world'" />
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

#Context and data




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


