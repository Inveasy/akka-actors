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
 * Basic implementation of AbstractHeader class
 * @see AbstractHeader
 */
public class SimpleHeader extends AbstractHeader
{
	public SimpleHeader(boolean passthru, String content)
	{
		super(passthru, content);
	}
	
	@Override
	public void preProcess()
	{
		// Simple header does nothing
	}
	
	@Override
	public void postProcess()
	{
		// Simple header does nothing
	}
	
	@Override
	/* Overridden for builder pattern convenience */
	public SimpleHeader setPassthru(boolean passthru)
	{
		super.setPassthru(passthru);
		return this;
	}
	
	@Override
	/* Overridden for builder pattern convenience */
	public SimpleHeader setContent(String content)
	{
		super.setContent(content);
		return this;
	}
}
