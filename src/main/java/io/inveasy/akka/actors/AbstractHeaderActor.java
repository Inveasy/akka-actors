/*
 * Copyright 2018 Guillaume Gravetot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.inveasy.akka.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.japi.pf.ReceiveBuilder;
import io.inveasy.akka.actors.domain.AbstractHeader;
import io.inveasy.akka.actors.messages.HeaderMessage;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractHeaderActor extends AbstractActor
{
	private Map<String, AbstractHeader> requestHeaders;
	protected Map<String, AbstractHeader> getRequestHeaders() { return requestHeaders; }
	
	protected Map<String, AbstractHeader> headers;
	
	public ReceiveBuilder mainReceiveBuilder()
	{
		return receiveBuilder();
	}
	
	@Override
	public void aroundReceive(PartialFunction<Object, BoxedUnit> receive, Object msg)
	{
		if(msg instanceof HeaderMessage)
		{
			HeaderMessage headerMessage = (HeaderMessage)msg;
			requestHeaders = headerMessage.getHeaders();
			msg = headerMessage.getOriginalMessage();
		}
		else
			requestHeaders = new TreeMap<>();
		
		// TODO Default headers
		
		if(requestHeaders != null)
			requestHeaders.forEach((s, abstractHeader) -> abstractHeader.preProcess());
		headers = new TreeMap<>();
		
		super.aroundReceive(receive, msg);
	}
	
	/** Processes headers and returns them, ready to be sent */
	private Map<String, AbstractHeader> processHeaders()
	{
		Map<String, AbstractHeader> headerz = headers;
		
		if(requestHeaders != null)
		{
			requestHeaders.forEach((key, header) ->
			{
				header.postProcess();
				if(header.isPassthru() && !headerz.containsKey(key))
					headerz.put(key, header);
			});
		}
		
		return headerz;
	}
	
	/**
	 * Sends a message to the target, sender being this actor
	 * @param target The target of the message
	 * @param message The message to send
	 */
	public void tell(ActorRef target, Object message)
	{
		tell(target, message, getSelf());
	}
	
	/**
	 * Sends a message to target
	 * @param target The target of the message
	 * @param message The message to send
	 * @param sender Who is sending this
	 */
	public void tell(ActorRef target, Object message, ActorRef sender)
	{
		target.tell(new HeaderMessage(processHeaders(), message), sender);
	}
	
	/**
	 * Replies to the current message's sender
	 * @param message The response
	 */
	public void reply(Object message)
	{
		tell(getSender(), message, getSelf());
	}
	
	/**
	 * Forwards a message to the target (the sender will be the sender of the currently processed message)
	 * @param target The target of the message
	 * @param message The message to forward
	 */
	public void forward(ActorRef target, Object message)
	{
		target.forward(new HeaderMessage(processHeaders(), message), getContext());
	}
	
	/**
	 * Sends a message to the target, sender being this actor
	 * @param target The target of the message
	 * @param message The message to send
	 */
	public void tell(ActorSelection target, Object message)
	{
		tell(target, message, getSelf());
	}
	
	/**
	 * Sends a message to target
	 * @param target The target of the message
	 * @param message The message to send
	 * @param sender Who is sending this
	 */
	public void tell(ActorSelection target, Object message, ActorRef sender)
	{
		target.tell(new HeaderMessage(processHeaders(), message), sender);
	}
	
	/**
	 * Forwards a message to the target (the sender will be the sender of the currently processed message)
	 * @param target The target of the message
	 * @param message The message to forward
	 */
	public void forward(ActorSelection target, Object message)
	{
		target.forward(new HeaderMessage(processHeaders(), message), getContext());
	}
}
