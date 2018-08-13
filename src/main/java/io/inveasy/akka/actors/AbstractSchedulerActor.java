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
import akka.actor.Cancellable;

import java.time.Duration;
import java.util.TreeMap;

public abstract class AbstractSchedulerActor extends AbstractActor
{
	private TreeMap<String, Cancellable> schedules = new TreeMap<>();
	
	/////////////////// Schedule Runnable ////////////////////
	///////// Schedule multiple /////////
	/** Runs the runnable every interval, with initial delay being interval */
	protected void schedule(String name, Duration interval, Runnable runnable)
	{
		schedule(name, interval, interval, runnable);
	}
	
	/** Runs the runnable every interval, first run being after initialDelay */
	protected void schedule(String name, Duration initialDelay, Duration interval, Runnable runnable)
	{
		Cancellable lastScheduled = schedules.put(name,
				getContext().getSystem().scheduler().schedule(initialDelay, interval, runnable, getContext().dispatcher()));
		
		if(lastScheduled != null && !lastScheduled.isCancelled())
			lastScheduled.cancel();
	}
	
	////////// Schedule once /////////
	protected void scheduleOnce(String name, Duration delay, Runnable runnable)
	{
		Cancellable lastScheduled = schedules.put(name,
				getContext().getSystem().scheduler().scheduleOnce(delay, runnable, getContext().dispatcher()));
		
		if(lastScheduled != null && !lastScheduled.isCancelled())
			lastScheduled.cancel();
	}
	
	/////////////////// Schedule Message ////////////////////
	///////// Schedule multiple /////////
	/** Sends the message to self every interval, sending first message after initialDelay, sender being self */
	protected void scheduleToSelf(String name, Duration initialDelay, Duration interval, Object message)
	{
		schedule(name, initialDelay, interval, getSelf(), message);
	}
	
	/** Sends the message to self every interval, sender being self */
	protected void scheduleToSelf(String name, Duration interval, Object message)
	{
		schedule(name, interval, getSelf(), message);
	}
	
	/** Sends the message to the target every interval, sending first message after initialDelay, sender being self */
	protected void schedule(String name, Duration initialDelay, Duration interval, ActorRef target, Object message)
	{
		schedule(name, initialDelay, interval, target, message, getSelf());
	}
	
	/** Sends the message to the target every interval, sender being self */
	protected void schedule(String name, Duration interval, ActorRef target, Object message)
	{
		schedule(name, interval, target, message, getSelf());
	}
	
	/** Sends the message to the target every interval */
	protected void schedule(String name, Duration interval, ActorRef target, Object message, ActorRef sender)
	{
		schedule(name, interval, interval, target, message, sender);
	}
	
	/** Sends the message to the target every interval, sending first message after initialDelay */
	protected void schedule(String name, Duration initialDelay, Duration interval, ActorRef target, Object message, ActorRef sender)
	{
		Cancellable lastScheduled = schedules.put(name,
				getContext().getSystem().scheduler().schedule(initialDelay, interval, target, message, getContext().dispatcher(), sender));
		
		if(lastScheduled != null && !lastScheduled.isCancelled())
			lastScheduled.cancel();
	}
	
	////////// Schedule once /////////
	protected void scheduleOnceToSelf(String name, Duration delay, Object message)
	{
		scheduleOnce(name, delay, getSelf(), message);
	}
	
	protected void scheduleOnce(String name, Duration delay, ActorRef target, Object message)
	{
		scheduleOnce(name, delay, target, message, getSelf());
	}
	
	protected void scheduleOnce(String name, Duration delay, ActorRef target, Object message, ActorRef sender)
	{
		Cancellable lastScheduled = schedules.put(name,
				getContext().getSystem().scheduler().scheduleOnce(delay, target, message, getContext().dispatcher(), sender));
		
		if(lastScheduled != null && !lastScheduled.isCancelled())
			lastScheduled.cancel();
	}
	
	public void cancelSchedule(String name)
	{
		Cancellable scheduled = schedules.remove(name);
		
		if(scheduled != null && !scheduled.isCancelled())
			scheduled.cancel();
	}
	
	@Override
	public void postStop()
	{
		schedules.values().forEach(Cancellable::cancel);
	}
}
