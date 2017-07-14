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
package com.sesygroup.choreography.coordinationlogic.realizer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.sesygroup.choreography.choreographyspecification.model.ChoreographySpecification;
import com.sesygroup.choreography.choreographyspecification.model.Participant;
import com.sesygroup.choreography.choreographyspecification.model.State;
import com.sesygroup.choreography.choreographyspecification.model.Transition;
import com.sesygroup.choreography.choreographyspecification.model.action.SendingMessageActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.ConcreteParticipantBehavior;
import com.sesygroup.choreography.concreteparticipantbehavior.model.Message;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.AsynchronousReceiveActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.AsynchronousSendActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.SynchronousReceiveActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.action.SynchronousSendActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.message.InputMessage;

/**
 *
 * @author Alexander Perucci (http://www.alexanderperucci.com/)
 *
 */
public class CoordinationLogicRealizerUtils {

   public static Collection<Pair<Participant, Participant>> findCoordinationDelegatesToBeCreated(
         final ChoreographySpecification choreographySpecification) {
      List<Pair<Participant, Participant>> coordinationDelegatesToBeCreated
            = new LinkedList<Pair<Participant, Participant>>();

      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            Pair<Participant, Participant> pair = new ImmutablePair<Participant, Participant>(
                  ((SendingMessageActionTransition) transition).getSourceParticipant(),
                  ((SendingMessageActionTransition) transition).getTargetParticipant());
            if (!coordinationDelegatesToBeCreated.contains(pair)) {
               coordinationDelegatesToBeCreated.add(pair);
            }
         }
      });
      return coordinationDelegatesToBeCreated;
   }

   public static List<State> findBranchingStates(final ChoreographySpecification choreographySpecification) {
      return ListUtils.select(choreographySpecification.getStates(), new Predicate<State>() {
         @Override
         public boolean evaluate(final State object) {
            return isBranchingState(choreographySpecification, object);
         }
      });
   }

   public static boolean isBranchingState(final ChoreographySpecification choreographySpecification,
         final State state) {

      return findAllOutgoingTransition(choreographySpecification, state).size() >= 2
            ? true
            : false;
   }

   public static List<Transition> findAllOutgoingTransition(final ChoreographySpecification choreographySpecification,
         final State state) {
      return ListUtils.select(choreographySpecification.getTransitions(), new Predicate<Transition>() {
         @Override
         public boolean evaluate(final Transition object) {
            return object.getSourceState().equals(state)
                  ? true
                  : false;
         }
      });
   }

   public static List<Transition> findAllIncomingTransition(final ChoreographySpecification choreographySpecification,
         final State state) {
      return ListUtils.select(choreographySpecification.getTransitions(), new Predicate<Transition>() {
         @Override
         public boolean evaluate(final Transition object) {
            return object.getTargetState().equals(state)
                  ? true
                  : false;
         }
      });
   }

   public static com.sesygroup.choreography.concreteparticipantbehavior.model.Transition findConcreteTransition(
         final ConcreteParticipantBehavior concreteParticipantBehavior,
         final com.sesygroup.choreography.concreteparticipantbehavior.model.State sourceState, final Message message) {

      Validate.notNull(concreteParticipantBehavior,
            ValidationMessages.IS_NULL_CONCRETE_PARTICIPANT_BEHAVIOR_EXCEPTION_MESSAGE, concreteParticipantBehavior);

      return IterableUtils.find(concreteParticipantBehavior.getTransitions(),
            new Predicate<com.sesygroup.choreography.concreteparticipantbehavior.model.Transition>() {
               @Override
               public boolean evaluate(
                     final com.sesygroup.choreography.concreteparticipantbehavior.model.Transition object) {
                  if (message instanceof InputMessage) {
                     if (object instanceof SynchronousReceiveActionTransition) {
                        return message.equals(((SynchronousReceiveActionTransition) object).getInputMessage());
                     } else if (object instanceof AsynchronousReceiveActionTransition) {
                        return message.equals(((AsynchronousReceiveActionTransition) object).getInputMessage());
                     } else {
                        return false;
                     }
                  } else {
                     if (object instanceof SynchronousSendActionTransition) {
                        return message.equals(((SynchronousSendActionTransition) object).getOutputMessage());
                     } else if (object instanceof AsynchronousSendActionTransition) {
                        return message.equals(((AsynchronousSendActionTransition) object).getOutputMessage());
                     } else {
                        return false;
                     }
                  }
               }
            });

   }

   // -----------------------------------------------------------------------
   /**
    * <p>
    * {@code CoordinationDelegateGeneratorUtils} instances should NOT be constructed in standard programming. Instead,
    * the class should be used statically.
    * </p>
    *
    * <p>
    * This constructor is public to permit tools that require a JavaBean instance to operate.
    * </p>
    */
   public CoordinationLogicRealizerUtils() {
      super();
   }

}
