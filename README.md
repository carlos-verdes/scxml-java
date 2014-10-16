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

And then you can start SM session like this:
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

So, the I/O processor will listen from external resources messages, try to search the target session and push the correspondent event to it. At the same time is able to receive a message from a SCXML session and send it to another system (which actually could be another SCXML session).

Now get the previous example of a SM with two states, connected and disconnected. 
If an event "connect" is received on  disconnected the state machine transitions to connected state.
Connected state shows a console message on enter.

```
init-state:  disconnected
state-disconnected
  on-event: connect --> connected
state-connected
  on-entry: 
    send to console welcome message
```

With the previous engine and session we could do something like:
```java
//create a user credentials
Principal userCredentials= new Principal() {
	@Override
	public String getName() {
		return "John";
	}
};
//push a connect event
engine.pushEvent(ctx.getSessionId(), new BasicEvent("connect", userCredentials));
engine.pushEvent(ctx.getSessionId(), new BasicEvent("exit"));
```

With next output:
```
> connected John
```

