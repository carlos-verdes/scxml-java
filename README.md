scxml-java
==========

Java library that implements SCXML standard (http://www.w3.org/TR/scxml).

This implementation has only dependency with Apache JEXL to evaluate context expressions so it's easier to use with antoher frameworks like Android.

Android lib has been publised to use this lib in Android apps.
You can clone from https://github.com/nosolojava/scxml-android.git


This module will be in a public repository, right now you should download and do a clean-install with Maven:

```
    git clone https://github.com/nosolojava/scxml-java.git
    cd scxml-java
    mvn clean install
```

 
Then you can create in the classpath an SCXML resource like this (in the example simpleSM.xml):
```
<scxml name="basicStates" version="1.0" android:version="2" xmlns="http://www.w3.org/2005/07/scxml" xmlns:android="http://com.nosolojava.schemas.android/scxml">
	<datamodel>
		<data id="salute" expr="'hello world'" />
		<data id="state" expr="'none'" />
	</datamodel>
	<initial id="init1">
		<transition target="connected" />
	</initial>
	<state id="connected">
		<onentry>
			<assign location="salute" expr="'connected'" />
		</onentry>
		<transition event="connect" target="disconnected">
		</transition>
	</state>
	<state id="disconnected">
		<onentry>
			<assign location="salute" expr="'disconnected'" />
		</onentry>
		<transition event="disconnect" target="connected">
		</transition>
    </state>
</scxml>
```

And then you can start SM session like this:
```
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
```
//shutdown the engine and wait 50 milliseconds for the sessions to finish
engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);


//force shutdown (no wait)
engine.forceShutdown();
```
