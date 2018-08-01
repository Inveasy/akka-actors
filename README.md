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
```

###### Message headers
If you want to be able to include headers along with your messages,
inherit your actors with ``` AbstractHeaderActor ``` or ``` AbstractHeaderActorWithTimers ```.

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

## Where is it used in Inveasy platform ?
All actors have ```AbstractHeaderActor``` in their parents. Simple as hell.