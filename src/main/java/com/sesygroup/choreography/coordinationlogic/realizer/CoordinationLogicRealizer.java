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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.sesygroup.choreography.choreographyspecification.model.ChoreographySpecification;
import com.sesygroup.choreography.choreographyspecification.model.Participant;
import com.sesygroup.choreography.choreographyspecification.model.Transition;
import com.sesygroup.choreography.choreographyspecification.model.action.SendingMessageActionTransition;
import com.sesygroup.choreography.concreteparticipantbehavior.model.ConcreteParticipantBehavior;
import com.sesygroup.choreography.concreteparticipantbehavior.model.State;
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
public class CoordinationLogicRealizer {
   private static final String BRANCH_STATE_SUFFIX = "_branch";
   private static final String MID_STATE_SUFFIX = "_mid";
   private static final String SYNCH_STATE_SUFFIX = "_synch";
   private static final String SYNCH_MESSAGE_PREFIX = "Synch_";
   private static final String SYNCH_MESSAGE_TO = "->";

   private ChoreographySpecification choreographySpecification;
   private Map<Participant, ConcreteParticipantBehavior> participantToConcreteParticipantBehaviorMap;
   private Map<Pair<Participant, Participant>, ConcreteParticipantBehavior> cdNameToConcreteParticipantBehaviorMap;

   public CoordinationLogicRealizer(final ChoreographySpecification choreographySpecification,
         final Map<Participant, ConcreteParticipantBehavior> participantToConcreteParticipantBehaviorMap) {
      this.choreographySpecification = choreographySpecification;
      this.participantToConcreteParticipantBehaviorMap = participantToConcreteParticipantBehaviorMap;
   }

   public Map<Pair<Participant, Participant>, ConcreteParticipantBehavior> realize() {
      cdNameToConcreteParticipantBehaviorMap
            = new HashMap<Pair<Participant, Participant>, ConcreteParticipantBehavior>();

      // find all possible CD name
      Collection<Pair<Participant, Participant>> coordinationDelegateParticipantPairs
            = CoordinationLogicRealizerUtils.findCoordinationDelegatesToBeCreated(choreographySpecification);

      // create a ConcreteParticipantBehavior for each CD and copy all ChoreographySpecification in the
      // ConcreteParticipantBehavior states
      coordinationDelegateParticipantPairs.forEach(pair -> {
         ConcreteParticipantBehavior concreteParticipantBehavior = new ConcreteParticipantBehavior();
         choreographySpecification.getStates()
               .forEach(state -> concreteParticipantBehavior.getStates().add(new State(state.getName())));
         concreteParticipantBehavior.setInitialState(new State(choreographySpecification.getInitialState().getName()));
         cdNameToConcreteParticipantBehaviorMap.put(pair, concreteParticipantBehavior);
      });

      // add all necessary state to the CDs
      createMidState();
      createSynchState();
      createBranchingState();

      // add all necessary transition to the CDs
      // createSynchTransitionsForBranchingState();
      createSynchTransitionsForIndipendentSequence();
      createSynchTransitionsThatReachBranchingState();
      createSynchTransitionsForBranchingStateToItsState();
      createSynchTransitionsForBranchingStateToOtherSate();
      createMessageTransitions();

      return cdNameToConcreteParticipantBehaviorMap;

   }

   private void createMidState() {
      // create mid state and synch state
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            Pair<Participant, Participant> cd = new ImmutablePair<Participant, Participant>(
                  ((SendingMessageActionTransition) transition).getSourceParticipant(),
                  ((SendingMessageActionTransition) transition).getTargetParticipant());

            // get ConcreteParticipantBehavior of the CD
            ConcreteParticipantBehavior concreteParticipantBehavior = cdNameToConcreteParticipantBehaviorMap.get(cd);
            // check if the ConcreteParticipantBehavior exists, should be always true
            Validate.notNull(concreteParticipantBehavior, ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE,
                  cd);
            // create mid state in the ConcreteParticipantBehavior
            concreteParticipantBehavior.getStates()
                  .add(new State(transition.getSourceState().getName() + MID_STATE_SUFFIX));
         }
      });
   }

   private void createSynchState() {
      // create synch state
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            Pair<Participant, Participant> cd = new ImmutablePair<Participant, Participant>(
                  ((SendingMessageActionTransition) transition).getSourceParticipant(),
                  ((SendingMessageActionTransition) transition).getTargetParticipant());

            // get ConcreteParticipantBehavior of the CD
            ConcreteParticipantBehavior concreteParticipantBehavior = cdNameToConcreteParticipantBehaviorMap.get(cd);
            // check if the ConcreteParticipantBehavior exists, should be always true
            Validate.notNull(concreteParticipantBehavior, ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE,
                  cd);

            // create synch state in the ConcreteParticipantBehavior if the source participant of the outgoing is not
            // equal to the source participant of the target transition
            for (Transition outgoingTransition : CoordinationLogicRealizerUtils
                  .findAllOutgoingTransition(choreographySpecification, transition.getTargetState())) {
               State synchState = new State(transition.getTargetState().getName() + SYNCH_STATE_SUFFIX);
               if (outgoingTransition instanceof SendingMessageActionTransition
                     && !((SendingMessageActionTransition) outgoingTransition).getSourceParticipant()
                           .equals(((SendingMessageActionTransition) transition).getSourceParticipant())) {

                  if (!concreteParticipantBehavior.getStates().contains(synchState)) {
                     concreteParticipantBehavior.getStates().add(synchState);
                  }

                  // add the synch state to the target CD
                  Pair<Participant, Participant> outgoingCd = new ImmutablePair<Participant, Participant>(
                        ((SendingMessageActionTransition) outgoingTransition).getSourceParticipant(),
                        ((SendingMessageActionTransition) outgoingTransition).getTargetParticipant());
                  ConcreteParticipantBehavior outgoingConcreteParticipantBehavior
                        = cdNameToConcreteParticipantBehaviorMap.get(outgoingCd);
                  // check if the ConcreteParticipantBehavior exists, should be always true
                  Validate.notNull(outgoingConcreteParticipantBehavior,
                        ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, outgoingCd);

                  if (!outgoingConcreteParticipantBehavior.getStates().contains(synchState)) {
                     outgoingConcreteParticipantBehavior.getStates().add(synchState);
                  }
               }

            }

         }
      });

   }

   private void createBranchingState() {
      // find branching states
      List<com.sesygroup.choreography.choreographyspecification.model.State> branchingStates
            = CoordinationLogicRealizerUtils.findBranchingStates(choreographySpecification);

      // create branch state for each branching state
      branchingStates.forEach(state -> {
         List<Transition> outgoingTransitionsOfBranchingState
               = CoordinationLogicRealizerUtils.findAllOutgoingTransition(choreographySpecification, state);
         outgoingTransitionsOfBranchingState.forEach(transition -> {
            if (transition instanceof SendingMessageActionTransition) {
               Pair<Participant, Participant> cd = new ImmutablePair<Participant, Participant>(
                     ((SendingMessageActionTransition) transition).getSourceParticipant(),
                     ((SendingMessageActionTransition) transition).getTargetParticipant());

               // get ConcreteParticipantBehavior of the CD
               ConcreteParticipantBehavior concreteParticipantBehavior = cdNameToConcreteParticipantBehaviorMap.get(cd);
               // check if the ConcreteParticipantBehavior exists, should be always true
               Validate.notNull(concreteParticipantBehavior,
                     ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, cd);
               // create branch state in the ConcreteParticipantBehavior
               concreteParticipantBehavior.getStates().add(new State(state.getName() + BRANCH_STATE_SUFFIX));
            }
         });
      });
   }

   private void createSynchTransitionsForIndipendentSequence() {
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            Pair<Participant, Participant> incomingCd = new ImmutablePair<Participant, Participant>(
                  ((SendingMessageActionTransition) transition).getSourceParticipant(),
                  ((SendingMessageActionTransition) transition).getTargetParticipant());
            // check target is not a branching state we consider later this situation
            if (!CoordinationLogicRealizerUtils.isBranchingState(choreographySpecification,
                  transition.getTargetState())) {
               // get ConcreteParticipantBehavior of the CD
               ConcreteParticipantBehavior incomingConcreteParticipantBehavior
                     = cdNameToConcreteParticipantBehaviorMap.get(incomingCd);
               // check if the ConcreteParticipantBehavior exists, should be always true
               Validate.notNull(incomingConcreteParticipantBehavior,
                     ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, incomingCd);

               // create synch state in the ConcreteParticipantBehavior if the source participant of the outgoing is not
               // equal to the source participant of the target transition
               for (Transition outgoingTransition : CoordinationLogicRealizerUtils
                     .findAllOutgoingTransition(choreographySpecification, transition.getTargetState())) {

                  State synchState = new State(transition.getTargetState().getName() + SYNCH_STATE_SUFFIX);

                  if (outgoingTransition instanceof SendingMessageActionTransition
                        && !((SendingMessageActionTransition) outgoingTransition).getSourceParticipant()
                              .equals(((SendingMessageActionTransition) transition).getSourceParticipant())) {

                     // add the synch state to the target CD
                     Pair<Participant, Participant> outgoingCd = new ImmutablePair<Participant, Participant>(
                           ((SendingMessageActionTransition) outgoingTransition).getSourceParticipant(),
                           ((SendingMessageActionTransition) outgoingTransition).getTargetParticipant());
                     ConcreteParticipantBehavior outgoingConcreteParticipantBehavior
                           = cdNameToConcreteParticipantBehaviorMap.get(outgoingCd);
                     // check if the ConcreteParticipantBehavior exists, should be always true
                     Validate.notNull(outgoingConcreteParticipantBehavior,
                           ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, outgoingCd);

                     // here we have the synch state and the source and target CDs,
                     if (incomingConcreteParticipantBehavior.getStates().contains(synchState)
                           && outgoingConcreteParticipantBehavior.getStates().contains(synchState)) {

                        // we need to create transition from the state to the synch
                        SynchronousSendActionTransition incomingSynchronousSendActionTransition
                              = new SynchronousSendActionTransition(
                                    IterableUtils.find(incomingConcreteParticipantBehavior.getStates(),
                                          new EqualPredicate<State>(new State(transition.getTargetState().getName()))),
                                    IterableUtils.find(incomingConcreteParticipantBehavior.getStates(),
                                          new EqualPredicate<State>(synchState)),
                                    new OutputMessage(SYNCH_MESSAGE_PREFIX + "{" + incomingCd.getLeft().getName() + ","
                                          + incomingCd.getRight().getName() + "}" + SYNCH_MESSAGE_TO + "{" + outgoingCd
                                                .getLeft().getName()
                                          + "," + outgoingCd.getRight().getName() + "}"));
                        incomingConcreteParticipantBehavior.getTransitions()
                              .add(incomingSynchronousSendActionTransition);

                        // we need to create transition from the synch to the state
                        SynchronousReceiveActionTransition outgoingSynchronousReceiveActionTransition
                              = new SynchronousReceiveActionTransition(
                                    IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                          new EqualPredicate<State>(synchState)),
                                    IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                          new EqualPredicate<State>(new State(transition.getTargetState().getName()))),
                                    new InputMessage(SYNCH_MESSAGE_PREFIX + "{" + incomingCd.getLeft().getName() + ","
                                          + incomingCd.getRight().getName() + "}" + SYNCH_MESSAGE_TO + "{" + outgoingCd
                                                .getLeft().getName()
                                          + "," + outgoingCd.getRight().getName() + "}"));
                        outgoingConcreteParticipantBehavior.getTransitions()
                              .add(outgoingSynchronousReceiveActionTransition);
                     } else {
                        // TODO remove if and use Validate.x in order to throw exception.
                     }
                  }

               }
            }
         }
      });

   }

   private void createSynchTransitionsThatReachBranchingState() {
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            if (CoordinationLogicRealizerUtils.isBranchingState(choreographySpecification,
                  transition.getTargetState())) {
               Pair<Participant, Participant> incomingCd = new ImmutablePair<Participant, Participant>(
                     ((SendingMessageActionTransition) transition).getSourceParticipant(),
                     ((SendingMessageActionTransition) transition).getTargetParticipant());
               // get ConcreteParticipantBehavior of the CD
               ConcreteParticipantBehavior incomingConcreteParticipantBehavior
                     = cdNameToConcreteParticipantBehaviorMap.get(incomingCd);
               // check if the ConcreteParticipantBehavior exists, should be always true
               Validate.notNull(incomingConcreteParticipantBehavior,
                     ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, incomingCd);

               // create synch state in the ConcreteParticipantBehavior if the source participant of the outgoing is not
               // equal to the source participant of the target transition

               List<Transition> outgoingTransitions = CoordinationLogicRealizerUtils
                     .findAllOutgoingTransition(choreographySpecification, transition.getTargetState());

               // add sending transition to the incoming CD
               State synchState = new State(transition.getTargetState().getName() + SYNCH_STATE_SUFFIX);
               State branchState = new State(transition.getTargetState().getName() + BRANCH_STATE_SUFFIX);

               StringBuilder nameOutputMessage = new StringBuilder();
               nameOutputMessage.append(SYNCH_MESSAGE_PREFIX + "{" + incomingCd.getLeft().getName() + ","
                     + incomingCd.getRight().getName() + "}" + SYNCH_MESSAGE_TO);
               getTargetCDs(outgoingTransitions, transition).forEach(pair -> {
                  nameOutputMessage.append("{" + pair.getLeft().getName() + "," + pair.getRight().getName() + "}");
               });

               // we need to create transition from the synch to the branch
               SynchronousSendActionTransition incomingSynchronousSendActionTransition
                     = new SynchronousSendActionTransition(
                           IterableUtils.find(incomingConcreteParticipantBehavior.getStates(),
                                 new EqualPredicate<State>(synchState)),
                           IterableUtils.find(incomingConcreteParticipantBehavior.getStates(),
                                 new EqualPredicate<State>(branchState)),
                           new OutputMessage(nameOutputMessage.toString()));
               incomingConcreteParticipantBehavior.getTransitions().add(incomingSynchronousSendActionTransition);

               for (Transition outgoingTransition : outgoingTransitions) {
                  Pair<Participant, Participant> outgoingCd = new ImmutablePair<Participant, Participant>(
                        ((SendingMessageActionTransition) outgoingTransition).getSourceParticipant(),
                        ((SendingMessageActionTransition) outgoingTransition).getTargetParticipant());
                  if (!incomingCd.equals(outgoingCd)) {
                     ConcreteParticipantBehavior outgoingConcreteParticipantBehavior
                           = cdNameToConcreteParticipantBehaviorMap.get(outgoingCd);
                     // check if the ConcreteParticipantBehavior exists, should be always true
                     Validate.notNull(outgoingConcreteParticipantBehavior,
                           ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, outgoingCd);

                     // we need to create transition from the synch to the branch
                     SynchronousReceiveActionTransition outgoingSynchronousReceiveActionTransition
                           = new SynchronousReceiveActionTransition(
                                 IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                       new EqualPredicate<State>(synchState)),
                                 IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                       new EqualPredicate<State>(branchState)),
                                 new InputMessage(SYNCH_MESSAGE_PREFIX + "{" + incomingCd.getLeft().getName() + ","
                                       + incomingCd.getRight().getName() + "}" + SYNCH_MESSAGE_TO + "{"
                                       + outgoingCd.getLeft().getName() + "," + outgoingCd.getRight().getName() + "}"));
                     outgoingConcreteParticipantBehavior.getTransitions()
                           .add(outgoingSynchronousReceiveActionTransition);
                  }
               }

            }
         }
      });
   }

   private void createSynchTransitionsForBranchingStateToItsState() {
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            if (CoordinationLogicRealizerUtils.isBranchingState(choreographySpecification,
                  transition.getTargetState())) {
               Pair<Participant, Participant> incomingCd = new ImmutablePair<Participant, Participant>(
                     ((SendingMessageActionTransition) transition).getSourceParticipant(),
                     ((SendingMessageActionTransition) transition).getTargetParticipant());
               // get ConcreteParticipantBehavior of the CD
               ConcreteParticipantBehavior incomingConcreteParticipantBehavior
                     = cdNameToConcreteParticipantBehaviorMap.get(incomingCd);
               // check if the ConcreteParticipantBehavior exists, should be always true
               Validate.notNull(incomingConcreteParticipantBehavior,
                     ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, incomingCd);

               // create synch state in the ConcreteParticipantBehavior if the source participant of the outgoing is not
               // equal to the source participant of the target transition

               List<Transition> outgoingTransitions = CoordinationLogicRealizerUtils
                     .findAllOutgoingTransition(choreographySpecification, transition.getTargetState());

               // add sending transition to the incoming CD
               State branchState = new State(transition.getTargetState().getName() + BRANCH_STATE_SUFFIX);

               for (Transition outgoingTransition : outgoingTransitions) {
                  Pair<Participant, Participant> outgoingCd = new ImmutablePair<Participant, Participant>(
                        ((SendingMessageActionTransition) outgoingTransition).getSourceParticipant(),
                        ((SendingMessageActionTransition) outgoingTransition).getTargetParticipant());
                  ConcreteParticipantBehavior outgoingConcreteParticipantBehavior
                        = cdNameToConcreteParticipantBehaviorMap.get(outgoingCd);
                  // check if the ConcreteParticipantBehavior exists, should be always true
                  Validate.notNull(outgoingConcreteParticipantBehavior,
                        ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, outgoingCd);

                  // we need to create transition from the branch to its state
                  StringBuilder nameInputMessage = new StringBuilder();

                  nameInputMessage.append(SYNCH_MESSAGE_PREFIX + "{" + outgoingCd.getLeft().getName() + ","
                        + outgoingCd.getRight().getName() + "}" + SYNCH_MESSAGE_TO);
                  getTargetCDs(outgoingTransitions, outgoingTransition).forEach(pair -> {
                     nameInputMessage.append("{" + pair.getLeft().getName() + "," + pair.getRight().getName() + "}");
                  });
                  SynchronousSendActionTransition outgoingSynchronousReceiveActionTransition
                        = new SynchronousSendActionTransition(
                              IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                    new EqualPredicate<State>(branchState)),
                              IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                    new EqualPredicate<State>(new State(transition.getTargetState().getName()))),
                              new OutputMessage(nameInputMessage.toString()));
                  outgoingConcreteParticipantBehavior.getTransitions().add(outgoingSynchronousReceiveActionTransition);
               }
            }

         }

      });
   }

   private void createSynchTransitionsForBranchingStateToOtherSate() {
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            if (CoordinationLogicRealizerUtils.isBranchingState(choreographySpecification,
                  transition.getTargetState())) {

               List<Transition> outgoingTransitions = CoordinationLogicRealizerUtils
                     .findAllOutgoingTransition(choreographySpecification, transition.getTargetState());

               State branchState = new State(transition.getTargetState().getName() + BRANCH_STATE_SUFFIX);
               for (Transition outgoingTransition : outgoingTransitions) {
                  Pair<Participant, Participant> outgoingCd = new ImmutablePair<Participant, Participant>(
                        ((SendingMessageActionTransition) outgoingTransition).getSourceParticipant(),
                        ((SendingMessageActionTransition) outgoingTransition).getTargetParticipant());
                  ConcreteParticipantBehavior outgoingConcreteParticipantBehavior
                        = cdNameToConcreteParticipantBehaviorMap.get(outgoingCd);
                  // check if the ConcreteParticipantBehavior exists, should be always true
                  Validate.notNull(outgoingConcreteParticipantBehavior,
                        ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, outgoingCd);

                  // consider all the transition by excluding the actual transition
                  for (Transition transitionToAdd : ListUtils.removeAll(outgoingTransitions,
                        Arrays.asList(outgoingTransition))) {
                     Pair<Participant, Participant> incomingCd = new ImmutablePair<Participant, Participant>(
                           ((SendingMessageActionTransition) transitionToAdd).getSourceParticipant(),
                           ((SendingMessageActionTransition) transitionToAdd).getTargetParticipant());
                     ConcreteParticipantBehavior inputConcreteParticipantBehavior
                           = cdNameToConcreteParticipantBehaviorMap.get(outgoingCd);
                     // check if the ConcreteParticipantBehavior exists, should be always true
                     Validate.notNull(inputConcreteParticipantBehavior,
                           ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, incomingCd);

                     // add transition from branch state to transitionToAdd.target state
                     SynchronousReceiveActionTransition outgoingSynchronousReceiveActionTransition
                           = new SynchronousReceiveActionTransition(
                                 IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                       new EqualPredicate<State>(branchState)),
                                 IterableUtils.find(outgoingConcreteParticipantBehavior.getStates(),
                                       new EqualPredicate<State>(
                                             new State(transitionToAdd.getTargetState().getName()))),
                                 new InputMessage(SYNCH_MESSAGE_PREFIX + "{" + incomingCd.getLeft().getName() + ","
                                       + incomingCd.getRight().getName() + "}" + SYNCH_MESSAGE_TO + "{" + outgoingCd
                                             .getLeft().getName()
                                       + "," + outgoingCd.getRight().getName() + "}"));
                     outgoingConcreteParticipantBehavior.getTransitions()
                           .add(outgoingSynchronousReceiveActionTransition);

                  }

               }
            }
         }
      });
   }

   private void createMessageTransitions() {
      choreographySpecification.getTransitions().forEach(transition -> {
         if (transition instanceof SendingMessageActionTransition) {
            Pair<Participant, Participant> cd = new ImmutablePair<Participant, Participant>(
                  ((SendingMessageActionTransition) transition).getSourceParticipant(),
                  ((SendingMessageActionTransition) transition).getTargetParticipant());
            ConcreteParticipantBehavior concreteParticipantBehavior = cdNameToConcreteParticipantBehaviorMap.get(cd);
            // check if the ConcreteParticipantBehavior exists, should be always true
            Validate.notNull(concreteParticipantBehavior, ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE,
                  cd);

            State midState = new State(transition.getSourceState().getName() + MID_STATE_SUFFIX);
            State synchState = new State(transition.getTargetState().getName());
            if (CoordinationLogicRealizerUtils.isBranchingState(choreographySpecification,
                  transition.getTargetState())) {
               synchState = new State(transition.getTargetState().getName() + SYNCH_STATE_SUFFIX);
            }

            // add synch or asynch send transition from midState state to synch state
            InputMessage inputMessage
                  = new InputMessage(((SendingMessageActionTransition) transition).getMessage().getName());
            com.sesygroup.choreography.concreteparticipantbehavior.model.Transition foundedReceiveActionTransition
                  = CoordinationLogicRealizerUtils.findConcreteTransition(
                        participantToConcreteParticipantBehaviorMap.get(cd.getRight()),
                        new State(transition.getSourceState().getName()), inputMessage);
            Validate.notNull(foundedReceiveActionTransition,
                  ValidationMessages.IS_NULL_CONCRETE_PARTICIPANT_BEHAVIOR_TRANSITION_EXCEPTION_MESSAGE,
                  inputMessage.getName());

            if (foundedReceiveActionTransition instanceof SynchronousReceiveActionTransition) {
               SynchronousReceiveActionTransition receiveActionTransition = new SynchronousReceiveActionTransition(
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(transition.getSourceState().getName()))),
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(midState.getName()))),
                     inputMessage);
               concreteParticipantBehavior.getTransitions().add(receiveActionTransition);
            } else if (foundedReceiveActionTransition instanceof AsynchronousReceiveActionTransition) {
               AsynchronousReceiveActionTransition receiveActionTransition = new AsynchronousReceiveActionTransition(
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(transition.getSourceState().getName()))),
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(midState.getName()))),
                     inputMessage);
               concreteParticipantBehavior.getTransitions().add(receiveActionTransition);
            }

            // add synch or asynch send transition from midState state to synch state

            OutputMessage outputMessage
                  = new OutputMessage(((SendingMessageActionTransition) transition).getMessage().getName());
            com.sesygroup.choreography.concreteparticipantbehavior.model.Transition foundedSendActionTransition
                  = CoordinationLogicRealizerUtils.findConcreteTransition(
                        participantToConcreteParticipantBehaviorMap.get(cd.getLeft()),
                        new State(transition.getSourceState().getName()), outputMessage);
            Validate.notNull(foundedSendActionTransition,
                  ValidationMessages.IS_NULL_CONCRETE_PARTICIPANT_BEHAVIOR_TRANSITION_EXCEPTION_MESSAGE,
                  outputMessage.getName());
            if (foundedSendActionTransition instanceof SynchronousSendActionTransition) {
               SynchronousSendActionTransition sendActionTransition = new SynchronousSendActionTransition(
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(midState.getName()))),
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(synchState.getName()))),
                     outputMessage);
               concreteParticipantBehavior.getTransitions().add(sendActionTransition);
            } else if (foundedSendActionTransition instanceof AsynchronousSendActionTransition) {
               AsynchronousSendActionTransition sendActionTransition = new AsynchronousSendActionTransition(
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(midState.getName()))),
                     IterableUtils.find(concreteParticipantBehavior.getStates(),
                           new EqualPredicate<State>(new State(synchState.getName()))),
                     outputMessage);
               concreteParticipantBehavior.getTransitions().add(sendActionTransition);
            }

            getTargetCDs(choreographySpecification.getTransitions().stream().collect(Collectors.toList()), transition)
                  .forEach(pair -> {
                     ConcreteParticipantBehavior otherParticipantBehavior
                           = cdNameToConcreteParticipantBehaviorMap.get(pair);
                     // check if the ConcreteParticipantBehavior exists, should be always true
                     Validate.notNull(otherParticipantBehavior,
                           ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, pair);

                     if (IterableUtils.find(otherParticipantBehavior.getStates(), new EqualPredicate<State>(
                           new State(transition.getSourceState().getName() + BRANCH_STATE_SUFFIX))) == null) {

                        State foundedSourceState
                              = IterableUtils.find(otherParticipantBehavior.getStates(), new EqualPredicate<State>(
                                    new State(transition.getSourceState().getName() + SYNCH_STATE_SUFFIX)));
                        if (foundedSourceState == null) {
                           foundedSourceState = new State(transition.getSourceState().getName());
                        }

                        State foundedTargetState
                              = IterableUtils.find(otherParticipantBehavior.getStates(), new EqualPredicate<State>(
                                    new State(transition.getTargetState().getName() + SYNCH_STATE_SUFFIX)));
                        if (foundedTargetState == null) {
                           foundedTargetState = new State(transition.getTargetState().getName());
                        }

                        InternalActionTransition internalActionTransition = new InternalActionTransition(
                              IterableUtils.find(otherParticipantBehavior.getStates(),
                                    new EqualPredicate<State>(foundedSourceState)),
                              IterableUtils.find(otherParticipantBehavior.getStates(),
                                    new EqualPredicate<State>(foundedTargetState)));
                        otherParticipantBehavior.getTransitions().add(internalActionTransition);

                     }

                  });
         }
      });

   }

   private List<Pair<Participant, Participant>> getTargetCDs(final List<Transition> transitions,
         final Transition transitionToExclude) {
      List<Pair<Participant, Participant>> cdTargets = new ArrayList<Pair<Participant, Participant>>();
      Pair<Participant, Participant> cdToExclude = new ImmutablePair<Participant, Participant>(
            ((SendingMessageActionTransition) transitionToExclude).getSourceParticipant(),
            ((SendingMessageActionTransition) transitionToExclude).getTargetParticipant());
      transitions.forEach(transition -> {
         Pair<Participant, Participant> cdTarget = new ImmutablePair<Participant, Participant>(
               ((SendingMessageActionTransition) transition).getSourceParticipant(),
               ((SendingMessageActionTransition) transition).getTargetParticipant());
         if (!cdToExclude.equals(cdTarget)) {
            // get ConcreteParticipantBehavior of the CD
            ConcreteParticipantBehavior incomingConcreteParticipantBehavior
                  = cdNameToConcreteParticipantBehaviorMap.get(cdTarget);
            // check if the ConcreteParticipantBehavior exists, should be always true
            Validate.notNull(incomingConcreteParticipantBehavior,
                  ValidationMessages.IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE, cdTarget);

            cdTargets.add(cdTarget);
         }
      });

      return cdTargets;
   }

}
