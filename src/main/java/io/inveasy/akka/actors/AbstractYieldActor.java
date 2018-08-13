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

import akka.actor.ActorRef;
import akka.japi.Pair;
import akka.japi.pf.ReceiveBuilder;
import io.inveasy.akka.actors.annotations.OriginalSender;
import io.inveasy.akka.actors.annotations.Param;
import io.inveasy.akka.actors.annotations.YieldReceiver;
import io.inveasy.akka.actors.domain.AbstractHeader;
import io.inveasy.akka.actors.domain.SimpleHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class AbstractYieldActor extends AbstractHeaderActor
{
	public static final String ACTOR_UUID = UUID.randomUUID().toString();
	public static final String ACTOR_YIELD_UUID = "yield-" + ACTOR_UUID;
	
	public static class Yield
	{
		private String yieldId;
		private Object originalMessage;
		private ActorRef originalSender;
		private Map<String, AbstractHeader> originalRequestHeaders;
		private TreeMap<String, Object> context = new TreeMap<>();
		
		private Yield(String yieldId, Object originalMessage, ActorRef originalSender, Map<String, AbstractHeader> requestHeaders, Pair<String, Object>[] contextArgs)
		{
			this.yieldId = yieldId;
			this.originalMessage = originalMessage;
			this.originalSender = originalSender;
			this.originalRequestHeaders = requestHeaders;
			
			for(Pair<String, Object> contextArg : contextArgs)
				context.put(contextArg.first(), contextArg.second());
		}
		
		public String getYieldId() { return this.yieldId; }
		public <T> T getOriginalTypedMessage(Class<T> cls)
		{
			return cls.cast(getOriginalMessage());
		}
		public Object getOriginalMessage()
		{
			return originalMessage;
		}
		public ActorRef getOriginalSender()
		{
			return originalSender;
		}
		public Map<String, AbstractHeader> getRequestHeaders()
		{
			return originalRequestHeaders;
		}
		public Object get(String key)
		{
			return context.get(key);
		}
		public <T> T getVar(String key, Class<T> cls)
		{
			return cls.cast(get(key));
		}
		
		public static class YieldBuilder
		{
			private String yieldId;
			private Object originalMessage;
			private ActorRef originalSender;
			private Map<String, AbstractHeader> requestHeaders;
			private Pair<String, Object>[] contextArgs;
			
			public YieldBuilder(Object originalMessage)
			{
				this.originalMessage = originalMessage;
			}
			
			public YieldBuilder setYieldId(String yieldId)
			{
				this.yieldId = yieldId;
				return this;
			}
			public YieldBuilder setOriginalSender(ActorRef originalSender)
			{
				this.originalSender = originalSender;
				return this;
			}
			public YieldBuilder setRequestHeaders(Map<String, AbstractHeader> requestHeaders)
			{
				this.requestHeaders = requestHeaders;
				return this;
			}
			public YieldBuilder setContextArgs(Pair<String, Object>[] contextArgs)
			{
				this.contextArgs = contextArgs;
				return this;
			}
			
			public Yield create()
			{
				return new Yield(yieldId, originalMessage, originalSender, requestHeaders, contextArgs);
			}
		}
	}
	
	@Override
	public ReceiveBuilder mainReceiveBuilder()
	{
		ReceiveBuilder receiveBuilder = super.mainReceiveBuilder();
		
		for(Method method : getClass().getDeclaredMethods())
		{
			YieldReceiver yieldReceiver = method.getAnnotation(YieldReceiver.class);
			
			if(yieldReceiver != null)
			{
				Object[] methodParams = new Object[method.getParameterCount()];
				int messageParameterPosition = -1;
				final Parameter[] parameters = method.getParameters();
				int yieldContextPosition = -1;
				int originalSenderPosition = -1;
				
				List<Integer> unresolvedParams = new ArrayList<>();
				
				// Find parameter order
				for(int i = 0; i < methodParams.length; i++)
				{
					if(parameters[i].getType().isAssignableFrom(yieldReceiver.expectedMessageType()))
						messageParameterPosition = i;
					else if(parameters[i].getType().isAssignableFrom(Yield.class))
						yieldContextPosition = i;
					else if(parameters[i].getAnnotation(OriginalSender.class) != null)
						originalSenderPosition = i;
					else // We add the unresolved type, we'll try to find if it is in the yield context
						unresolvedParams.add(i);
				}
				
				final int mParamPos = messageParameterPosition;
				final int yCP = yieldContextPosition;
				final int oSP = originalSenderPosition;
				receiveBuilder.match(yieldReceiver.expectedMessageType(), o -> {
					this.currentYield = new Yield.YieldBuilder(o)
							.setRequestHeaders(getRequestHeaders());
					
					// Try to get the context
					Yield context = null;
					AbstractHeader header = getRequestHeaders().get(ACTOR_YIELD_UUID);
					if(header != null)
						context = yields.remove(header.getContent());
					
					if(mParamPos != -1)
						methodParams[mParamPos] = o;
					if(yCP != -1)
						methodParams[yCP] = context;
					if(oSP != -1 && context != null)
						methodParams[oSP] = context.getOriginalSender();
					
					// Try to resolve unresolved params
					for(int i : unresolvedParams)
					{
						if(context == null) // If we don't have any context, resolve all remaining params as null
							methodParams[i] = null;
						else
						{
							// If the parameter has the @Param annotation, use its value as param name
							Param paramName = parameters[i].getAnnotation(Param.class);
							String name = parameters[i].getName();
							if(paramName != null)
								name = paramName.value();
							
							Object contextObject = context.get(name);
							if(context.getOriginalMessage() != null && parameters[i].getType().isAssignableFrom(context.getOriginalMessage().getClass()))
								methodParams[i] = context.getOriginalMessage();
							else if(contextObject != null && parameters[i].getType().isAssignableFrom(contextObject.getClass()))
								methodParams[i] = contextObject;
							else // TODO Make a second pass to search by type
								methodParams[i] = null;
						}
					}
					
					method.setAccessible(true);
					method.invoke(this, methodParams);
					method.setAccessible(false);
				});
			}
		}
		
		return receiveBuilder;
	}
	
	@Override
	public Receive createReceive()
	{
		return mainReceiveBuilder().build();
	}
	
	private Map<String, Yield> yields = new TreeMap<>();
	private Yield.YieldBuilder currentYield;
	
	@SafeVarargs
	// TODO This method is final !!!!!
	protected final void yield(Pair<String, Object>... contextArgs)
	{
		if(currentYield == null)
			currentYield = new Yield.YieldBuilder(null);
		
		// Generate a uid for this yield
		String yieldId = UUID.randomUUID().toString();
		
		Yield yield = currentYield.setYieldId(yieldId)
				.setOriginalSender(getSender())
				.setContextArgs(contextArgs)
				.create();
		
		yields.put(yieldId, yield);
		headers.put(ACTOR_YIELD_UUID, new SimpleHeader(true, yieldId));
	}
}
