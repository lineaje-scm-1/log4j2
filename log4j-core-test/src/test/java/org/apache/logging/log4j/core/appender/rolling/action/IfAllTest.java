/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the And composite condition.
 */
public class IfAllTest {

    @Test
    public void testAccept() {
        final PathCondition TRUE = new FixedCondition(true);
        final PathCondition FALSE = new FixedCondition(false);
        assertTrue(IfAll.createAndCondition(TRUE, TRUE).accept(null, null, null));
        assertFalse(IfAll.createAndCondition(FALSE, TRUE).accept(null, null, null));
        assertFalse(IfAll.createAndCondition(TRUE, FALSE).accept(null, null, null));
        assertFalse(IfAll.createAndCondition(FALSE, FALSE).accept(null, null, null));
    }

    @Test
    public void testEmptyIsFalse() {
        assertFalse(IfAll.createAndCondition().accept(null, null, null));
    }

    @Test
    public void testBeforeTreeWalk() {
        final CountingCondition counter = new CountingCondition(true);
        final IfAll and = IfAll.createAndCondition(counter, counter, counter);
        and.beforeFileTreeWalk();
        assertEquals(3, counter.getBeforeFileTreeWalkCount());
    }
}
