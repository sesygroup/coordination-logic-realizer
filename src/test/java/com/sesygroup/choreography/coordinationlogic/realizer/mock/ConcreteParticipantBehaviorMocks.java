/*
 * Copyright 2017 Software Engineering and Synthesis Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sesygroup.choreography.coordinationlogic.realizer.mock;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sesygroup.choreography.concreteparticipantbehavior.model.ConcreteParticipantBehavior;
import com.sesygroup.choreography.concreteparticipantbehavior.model.Message;
import com.sesygroup.choreography.concreteparticipantbehavior.model.State;
import com.sesygroup.choreography.concreteparticipantbehavior.model.Transition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.AsynchronousReceiveActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.AsynchronousSendActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.InternalActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.SynchronousReceiveActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.SynchronousSendActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.message.InputMessage;
import com.sesygroup.choreography.concreteparticipantbehavior.model.message.OutputMessage;

/**
 *
 * @author Alexander Perucci (http://www.alexanderperucci.com/)
 *
 */
public class ConcreteParticipantBehaviorMocks {

   public static ConcreteParticipantBehavior p1() {
      State initialState = new State("s0");
      Set<State> states = new LinkedHashSet<State>(Arrays.asList(new State("s0"), new State("s1")));
      Set<Message> messages = new LinkedHashSet<Message>(Arrays.asList(new OutputMessage("m1")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays
            .asList(new AsynchronousSendActionTransition(new State("s0"), new State("s1"), new OutputMessage("m1"))));

      return new ConcreteParticipantBehavior(states, initialState, messages, transitions);
   }

   public static ConcreteParticipantBehavior p2() {
      State initialState = new State("s0");
      Set<State> states = new LinkedHashSet<State>(Arrays.asList(new State("s0"), new State("s2"), new State("s5")));
      Set<Message> messages
            = new LinkedHashSet<Message>(Arrays.asList(new OutputMessage("m2"), new OutputMessage("m5")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays.asList(
            new AsynchronousSendActionTransition(new State("s0"), new State("s2"), new OutputMessage("m2")),
            new InternalActionTransition(new State("s2"), new State("s5")),
            new AsynchronousSendActionTransition(new State("s2"), new State("s5"), new OutputMessage("m5"))));

      return new ConcreteParticipantBehavior(states, initialState, messages, transitions);
   }

   public static ConcreteParticipantBehavior p3() {
      State initialState = new State("s0");
      Set<State> states = new LinkedHashSet<State>(
            Arrays.asList(new State("s0"), new State("s1"), new State("s2"), new State("s5")));
      Set<Message> messages = new LinkedHashSet<Message>(Arrays.asList(new InputMessage("m1"), new InputMessage("m2"),
            new InputMessage("m5"), new OutputMessage("m6")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays.asList(
            new AsynchronousReceiveActionTransition(new State("s0"), new State("s1"), new InputMessage("m1")),
            new AsynchronousReceiveActionTransition(new State("s1"), new State("s2"), new InputMessage("m2")),
            new AsynchronousReceiveActionTransition(new State("s2"), new State("s5"), new InputMessage("m5")),
            new InternalActionTransition(new State("s2"), new State("s5")),
            new SynchronousSendActionTransition(new State("s2"), new State("s5"), new OutputMessage("m6"))));

      return new ConcreteParticipantBehavior(states, initialState, messages, transitions);
   }

   public static ConcreteParticipantBehavior p4() {
      State initialState = new State("s0");
      Set<State> states = new LinkedHashSet<State>(Arrays.asList(new State("s0"), new State("s3")));
      Set<Message> messages = new LinkedHashSet<Message>(Arrays.asList(new OutputMessage("m3")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays.asList(
            new SynchronousSendActionTransition(new State("s0"), new State("s3"), new OutputMessage("m3")),
            new InternalActionTransition(new State("s0"), new State("s3"))));

      return new ConcreteParticipantBehavior(states, initialState, messages, transitions);
   }

   public static ConcreteParticipantBehavior p5() {
      State initialState = new State("s0");
      Set<State> states = new LinkedHashSet<State>(Arrays.asList(new State("s0"), new State("s4"), new State("s5")));
      Set<Message> messages
            = new LinkedHashSet<Message>(Arrays.asList(new OutputMessage("m4"), new OutputMessage("m7")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays.asList(
            new SynchronousSendActionTransition(new State("s0"), new State("s4"), new OutputMessage("m4")),
            new SynchronousSendActionTransition(new State("s0"), new State("s5"), new OutputMessage("m7")),
            new InternalActionTransition(new State("s0"), new State("s5")),
            new SynchronousSendActionTransition(new State("s4"), new State("s5"), new OutputMessage("m7"))));

      return new ConcreteParticipantBehavior(states, initialState, messages, transitions);
   }

   public static ConcreteParticipantBehavior p6() {
      State initialState = new State("s0");
      Set<State> states = new LinkedHashSet<State>(
            Arrays.asList(new State("s0"), new State("s3"), new State("s4"), new State("s5")));
      Set<Message> messages = new LinkedHashSet<Message>(Arrays.asList(new InputMessage("m3"), new InputMessage("m4"),
            new InputMessage("m6"), new InputMessage("m7")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays.asList(
            new SynchronousReceiveActionTransition(new State("s0"), new State("s3"), new InputMessage("m3")),
            new SynchronousReceiveActionTransition(new State("s0"), new State("s4"), new InputMessage("m4")),
            new InternalActionTransition(new State("s0"), new State("s5")),
            new SynchronousReceiveActionTransition(new State("s3"), new State("s4"), new InputMessage("m6")),
            new SynchronousReceiveActionTransition(new State("s4"), new State("s5"), new InputMessage("m7"))));

      return new ConcreteParticipantBehavior(states, initialState, messages, transitions);
   }
}
