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

package io.inveasy.akka.actors.messages;

import io.inveasy.akka.actors.domain.AbstractHeader;
import io.inveasy.cluster.serialization.ProtostuffSerializable;

import java.util.Map;

/**
 * Message class, used when sending a message (as a payload) with headers
 */
public class HeaderMessage implements ProtostuffSerializable
{
	private Map<String, AbstractHeader> headers;
	private Object originalMessage;
	
	/**
	 * Creates a new message with headers
	 * @param headers The headers of this message
	 * @param originalMessage The message which will be sent as payload along with headers
	 */
	public HeaderMessage(Map<String, AbstractHeader> headers, Object originalMessage)
	{
		this.headers = headers;
		this.originalMessage = originalMessage;
	}
	
	/**
	 * Returns the header map of this message
	 */
	public Map<String, AbstractHeader> getHeaders()
	{
		return headers;
	}
	
	/**
	 * Returns the original message, which was transported as a payload
	 */
	public Object getOriginalMessage()
	{
		return originalMessage;
	}
}
