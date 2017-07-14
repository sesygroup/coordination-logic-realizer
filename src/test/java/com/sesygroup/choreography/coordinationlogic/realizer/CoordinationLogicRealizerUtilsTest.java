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

import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sesygroup.choreography.choreographyspecification.model.Message;
import com.sesygroup.choreography.choreographyspecification.model.Participant;
import com.sesygroup.choreography.choreographyspecification.model.State;
import com.sesygroup.choreography.choreographyspecification.model.action.SendingMessageActionTransition;
import com.sesygroup.choreography.coordinationlogic.realizer.CoordinationLogicRealizerUtils;
import com.sesygroup.choreography.coordinationlogic.realizer.mock.ChoreographySpecificationMocks;

/**
 *
 * @author Alexander Perucci (http://www.alexanderperucci.com/)
 *
 */
public class CoordinationLogicRealizerUtilsTest {

   @BeforeClass
   public static void setUp() {
   }

   @Test
   public void testFindCoordinationDelegatesToBeCreated() {
      Assert.assertTrue(CollectionUtils.isEqualCollection(
            CoordinationLogicRealizerUtils
                  .findCoordinationDelegatesToBeCreated(ChoreographySpecificationMocks.sample()),
            Arrays.asList(new ImmutablePair<Participant, Participant>(new Participant("p1"), new Participant("p3")),
                  new ImmutablePair<Participant, Participant>(new Participant("p2"), new Participant("p3")),
                  new ImmutablePair<Participant, Participant>(new Participant("p4"), new Participant("p6")),
                  new ImmutablePair<Participant, Participant>(new Participant("p5"), new Participant("p6")),
                  new ImmutablePair<Participant, Participant>(new Participant("p3"), new Participant("p6")))));
   }

   @Test
   public void testFindBranchingStates() {
      Assert.assertTrue(CollectionUtils.isEqualCollection(
            CoordinationLogicRealizerUtils.findBranchingStates(ChoreographySpecificationMocks.sample()),
            Arrays.asList(new State("s2"))));
   }

   @Test
   public void testFindAllTransitionOfBranchingState() {
      Assert.assertTrue(CollectionUtils.isEqualCollection(
            CoordinationLogicRealizerUtils
                  .findAllOutgoingTransition(ChoreographySpecificationMocks.sample(), new State("s2")),
            Arrays.asList(
                  new SendingMessageActionTransition(new State("s2"), new State("s4"), new Participant("p5"),
                        new Participant("p6"), new Message("m4")),
                  new SendingMessageActionTransition(new State("s2"), new State("s3"), new Participant("p4"),
                        new Participant("p6"), new Message("m3")),
                  new SendingMessageActionTransition(new State("s2"), new State("s5"), new Participant("p2"),
                        new Participant("p3"), new Message("m5")))));
   }

}
