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

package io.inveasy.akka.actors.domain;

/**
 * Base class for the header functionality<br>
 * Check SimpleHeader for an out-of-the-box implementation
 * @see SimpleHeader
 */
public abstract class AbstractHeader
{
	private boolean passthru;
	private String content;
	
	/**
	 * Creates a new header
	 * @param passthru Tells whether this header should be passed along or dropped when forwarding unless specific behaviour
	 * @param content The content of the header
	 */
	public AbstractHeader(boolean passthru, String content)
	{
		this.passthru = passthru;
		this.content = content;
	}
	
	/** This method will be called when headers are received */
	public abstract void preProcess();
	/** This method will be called before headers are sent */
	public abstract void postProcess();
	
	/**
	 * Tells whether this header should be passed along or dropped when forwarding unless specific behaviour
	 */
	public boolean isPassthru()
	{
		return passthru;
	}
	
	/**
	 * Tells whether this header should be passed along or dropped when forwarding unless specific behaviour
	 */
	public AbstractHeader setPassthru(boolean passthru)
	{
		this.passthru = passthru;
		return this;
	}
	
	/**
	 * Returns the content of the header
	 */
	public String getContent()
	{
		return content;
	}
	
	/**
	 * Sets the content of the header
	 */
	public AbstractHeader setContent(String content)
	{
		this.content = content;
		return this;
	}
}
