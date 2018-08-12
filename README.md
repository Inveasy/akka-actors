# Error Management
[![Build Status](https://travis-ci.org/Inveasy/akka-actors.svg?branch=master)](https://travis-ci.org/Inveasy/akka-actors)
[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=io.inveasy%3Aakka-actors&metric=alert_status)](https://sonarcloud.io/dashboard?id=io.inveasy%3Aakka-actors)
[![Download](https://api.bintray.com/packages/inveasy/maven/akka-actors/images/download.svg) ](https://bintray.com/inveasy/maven/akka-actors/_latestVersion)

## What is it ?
This project provides helper Akka actors for Inveasy framework features.

## How To
First, include the maven dependency in your build :

```xml
<dependency>
  <groupId>io.inveasy</groupId>
  <artifactId>akka-actors</artifactId>
  <version>1.0.0</version>
</dependency>

<repositories>
  <repository>
    <id>bintray-inveasy-maven</id>
    <name>inveasy-maven</name>
    <url>https://dl.bintray.com/inveasy/maven</url>
  </repository>
</repositories>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <!-- Version must be >= 3.6.2 -->
      <version>3.7.0</version>
      <configuration>
        <source>8</source>
        <target>8</target>
    	<parameters>true</parameters>
      </configuration>
    </plugin>
  </plugins>
</build>

```

###### Message headers
If you want to be able to include headers along with your messages,
inherit your actors with ``` AbstractHeaderActor ``` or ``` AbstractHeaderActorWithScheduler ```.

Here is a sample implementation of a header actor :
```java
public class SomeActor extends AbstractHeaderActor
{
	@Override
	public ReceiveBuilder receiveBuilder()
	{
		return receiveBuilder()
		            // Receive builder as usual
		            .match(SomeMessage.class, this::processMessage)
		            .build();
	}
	
	public void processMessage(SomeMessage message)
	{
		// To access headers
		AbstractHeader header = getRequestHeaders().get("HeaderName");
		
		// To put headers for the message you will send
		headers.put("HeaderName", header);
		
		// Send a message to something
		// Headers will automatically be sent with it
		// Reminder : ALWAYS use provided methods (tell, forward, reply) when you want to send headers with messages
		// Otherwise, it will NOT send them (by using actor.tell() for example)
		tell(someActor, message);
		
		// Headers are not cleaned up when message is sent, so if you want to send another message, clean it
		// That way you can send multiple messages without resetting headers
		// Don't worry for passthru headers, as they are processed when message is sent
		headers = new TreeMap<>();
	}
}
```

###### Asynchronous processing
Process async messages is easy using ```AbstractYieldActor```. Its use is really simple, as follow :
```java
public class AsyncActor extends AbstractYieldActor
{
	// 1) Mark your methods with @YieldReceiver, indicating which message class it should expect
	@YieldReceiver(expectedMessageType = SomeMessage.class)
	// Request the message in parameters, in any order you want, ask for the context
	// You can even request some vars in the context args by using the same name you registered them as parameter
	private void someHandlingMethod(SomeMessage message, Yield context, String theParam)
	{
		// 2) When you need to send a message, call yield() before
	    yield();
	    tell(target, aMessage);
	    
	    // You can call yield() multiple times in the same method without problems
	    yield();
	    tell(anotherTarget, anotherMessage);
	    
	    // To pass context args (which you can get as method parameters, send them using yield()
	    yield(new Pair<>("theParam", "someValue"));
	    tell(target, aMessage);
	    // When the method receiving the reply has theParam as String parameter name
	    // value will be automagically applied
	    // It resolves args by name, then checks if types are compatible (by using Class.isAssignableFrom)
	}
}
```

## Where is it used in Inveasy platform ?
All actors have ```AbstractHeaderActor``` in their parents. Simple as hell.
