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
package org.apache.logging.log4j.core.layout;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

public class PatternSelectorTest {

    public static class FauxLogger {
        public String formatEvent(final LogEvent event, final Layout layout) {
            return new String(layout.toByteArray(event));
        }
    }

    LoggerContext ctx = LoggerContext.getContext();

    @Test
    public void testMarkerPatternSelector() throws Exception {
        final PatternMatch[] patterns = new PatternMatch[1];
        patterns[0] = new PatternMatch("FLOW", "%d %-5p [%t]: ====== %C{1}.%M:%L %m ======%n");
        // @formatter:off
        final PatternSelector selector = MarkerPatternSelector.newBuilder()
                .setProperties(patterns)
                .setDefaultPattern("%d %-5p [%t]: %m%n")
                .setAlwaysWriteExceptions(true)
                .setNoConsoleNoAnsi(true)
                .setConfiguration(ctx.getConfiguration())
                .build();
        // @formatter:on
        final PatternLayout layout = PatternLayout.newBuilder()
                .setPatternSelector(selector)
                .setConfiguration(ctx.getConfiguration())
                .build();
        final LogEvent event1 = Log4jLogEvent.newBuilder() //
                .setLoggerName(this.getClass().getName())
                .setLoggerFqcn("org.apache.logging.log4j.core.layout.PatternSelectorTest$FauxLogger")
                .setMarker(MarkerManager.getMarker("FLOW"))
                .setLevel(Level.TRACE) //
                .setIncludeLocation(true)
                .setMessage(new SimpleMessage("entry"))
                .build();
        final String result1 = new FauxLogger().formatEvent(event1, layout);
        final String expectSuffix1 =
                String.format("====== PatternSelectorTest.testMarkerPatternSelector:65 entry ======%n");
        assertThat(result1).endsWith(expectSuffix1);
        final LogEvent event2 = Log4jLogEvent.newBuilder() //
                .setLoggerName(this.getClass().getName())
                .setLoggerFqcn("org.apache.logging.log4j.core.Logger") //
                .setLevel(Level.INFO) //
                .setMessage(new SimpleMessage("Hello, world 1!"))
                .build();
        final String result2 = new String(layout.toByteArray(event2));
        final String expectSuffix2 = String.format("Hello, world 1!%n");
        assertThat(result2).endsWith(expectSuffix2);
    }

    @Test
    public void testLevelPatternSelector() throws Exception {
        final PatternMatch[] patterns = new PatternMatch[1];
        patterns[0] = new PatternMatch("TRACE", "%d %-5p [%t]: ====== %C{1}.%M:%L %m ======%n");
        final LevelPatternSelector selector = LevelPatternSelector.newBuilder()
                .setConfiguration(ctx.getConfiguration())
                .setDefaultPattern("%d %-5p [%t]: %m%n")
                .setDisableAnsi(true)
                .setProperties(patterns)
                .build();
        final PatternLayout layout = PatternLayout.newBuilder()
                .setPatternSelector(selector)
                .setConfiguration(ctx.getConfiguration())
                .build();
        final LogEvent event1 = Log4jLogEvent.newBuilder() //
                .setLoggerName(this.getClass().getName())
                .setLoggerFqcn("org.apache.logging.log4j.core.layout.PatternSelectorTest$FauxLogger")
                .setLevel(Level.TRACE) //
                .setIncludeLocation(true)
                .setMessage(new SimpleMessage("entry"))
                .build();
        final String result1 = new FauxLogger().formatEvent(event1, layout);
        final String expectSuffix1 =
                String.format("====== PatternSelectorTest.testLevelPatternSelector:101 entry ======%n");
        assertThat(result1).endsWith(expectSuffix1);
        final LogEvent event2 = Log4jLogEvent.newBuilder() //
                .setLoggerName(this.getClass().getName())
                .setLoggerFqcn("org.apache.logging.log4j.core.Logger") //
                .setLevel(Level.INFO) //
                .setMessage(new SimpleMessage("Hello, world 1!"))
                .build();
        final String result2 = new String(layout.toByteArray(event2));
        final String expectSuffix2 = String.format("Hello, world 1!%n");
        assertThat(result2).endsWith(expectSuffix2);
    }
}
