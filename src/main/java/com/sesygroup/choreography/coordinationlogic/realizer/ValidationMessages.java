package com.sesygroup.choreography.coordinationlogic.realizer;
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


/**
 *
 * @author Alexander Perucci (http://www.alexanderperucci.com/)
 *
 */
public class ValidationMessages {
   public static final String IS_CD_NOT_IN_SET_OF_CDS_EXCEPTION_MESSAGE
         = "The coordination delegate %s is not contained in the set of coordination delegates";
   public static final String IS_NULL_CONCRETE_PARTICIPANT_BEHAVIOR_EXCEPTION_MESSAGE
   = "Null concrete participant behavior %s";
   public static final String IS_NULL_CONCRETE_PARTICIPANT_BEHAVIOR_TRANSITION_EXCEPTION_MESSAGE
   = "Null concrete participant behavior transition for message %s";

   // -----------------------------------------------------------------------

   /**
    * <p>
    * {@code ValidationMessages} instances should NOT be constructed in standard programming. Instead, the class should
    * be used statically.
    * </p>
    *
    * <p>
    * This constructor is public to permit tools that require a JavaBean instance to operate.
    * </p>
    */
   public ValidationMessages() {
      super();
   }

}
