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

import com.sesygroup.choreography.choreographyspecification.model.ChoreographySpecification;
import com.sesygroup.choreography.choreographyspecification.model.Message;
import com.sesygroup.choreography.choreographyspecification.model.Participant;
import com.sesygroup.choreography.choreographyspecification.model.State;
import com.sesygroup.choreography.choreographyspecification.model.Transition;
import com.sesygroup.choreography.choreographyspecification.model.action.SendingMessageActionTransition;

/**
 *
 * @author Alexander Perucci (http://www.alexanderperucci.com/)
 *
 */
public class ChoreographySpecificationMocks {

   public static ChoreographySpecification sample() {
      Set<Participant> participants
            = new LinkedHashSet<Participant>(Arrays.asList(new Participant("p1"), new Participant("p2"),
                  new Participant("p3"), new Participant("p4"), new Participant("p5"), new Participant("p6")));
      Set<State> states = new LinkedHashSet<State>(Arrays.asList(new State("s0"), new State("s1"), new State("s2"),
            new State("s3"), new State("s4"), new State("s5")));
      State initialState = new State("s0");
      Set<Message> messages = new LinkedHashSet<Message>(Arrays.asList(new Message("m1"), new Message("m2"),
            new Message("m3"), new Message("m4"), new Message("m5"), new Message("m6"), new Message("m7")));
      Set<Transition> transitions = new LinkedHashSet<Transition>(Arrays.asList(
            new SendingMessageActionTransition(new State("s0"), new State("s1"), new Participant("p1"), new Participant("p3"), new Message("m1")),
            new SendingMessageActionTransition(new State("s1"), new State("s2"), new Participant("p2"), new Participant("p3"), new Message("m2")),
            new SendingMessageActionTransition(new State("s2"), new State("s3"), new Participant("p4"), new Participant("p6"), new Message("m3")),
            new SendingMessageActionTransition(new State("s2"), new State("s4"), new Participant("p5"), new Participant("p6"), new Message("m4")),
            new SendingMessageActionTransition(new State("s2"), new State("s5"), new Participant("p2"), new Participant("p3"), new Message("m5")),
            new SendingMessageActionTransition(new State("s3"), new State("s4"), new Participant("p3"), new Participant("p6"), new Message("m6")),
            new SendingMessageActionTransition(new State("s4"), new State("s5"), new Participant("p5"), new Participant("p6"), new Message("m7"))
      ));

      return new ChoreographySpecification(participants, states, initialState, messages, transitions);
   }
}
