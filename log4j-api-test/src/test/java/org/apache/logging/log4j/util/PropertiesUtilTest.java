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
package org.apache.logging.log4j.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junitpioneer.jupiter.ReadsSystemProperty;

public class PropertiesUtilTest {

    private final Properties properties = new Properties();

    @BeforeEach
    public void setUp() throws Exception {
        properties.load(ClassLoader.getSystemResourceAsStream("PropertiesUtilTest.properties"));
    }

    @Test
    public void testExtractSubset() {
        assertHasAllProperties(PropertiesUtil.extractSubset(properties, "a"));
        assertHasAllProperties(PropertiesUtil.extractSubset(properties, "b."));
        assertHasAllProperties(PropertiesUtil.extractSubset(properties, "c.1"));
        assertHasAllProperties(PropertiesUtil.extractSubset(properties, "dd"));
        assertThat(properties).containsOnly(Map.entry("a", "invalid"));
    }

    @Test
    public void testPartitionOnCommonPrefix() {
        final Map<String, Properties> parts = PropertiesUtil.partitionOnCommonPrefixes(properties);
        assertEquals(4, parts.size());
        assertHasAllProperties(parts.get("a"));
        assertHasAllProperties(parts.get("b"));
        assertHasAllProperties(
                PropertiesUtil.partitionOnCommonPrefixes(parts.get("c")).get("1"));
        assertHasAllProperties(parts.get("dd"));
    }

    private static void assertHasAllProperties(final Properties properties) {
        assertNotNull(properties);
        assertEquals("1", properties.getProperty("1"));
        assertEquals("2", properties.getProperty("2"));
        assertEquals("3", properties.getProperty("3"));
    }

    @Test
    public void testGetCharsetProperty() {
        final Properties p = new Properties();
        p.setProperty("e.1", StandardCharsets.US_ASCII.name());
        p.setProperty("e.2", "wrong-charset-name");
        final PropertiesUtil pu = new PropertiesUtil(p, true);

        assertEquals(Charset.defaultCharset(), pu.getCharsetProperty("e.0"));
        assertEquals(StandardCharsets.US_ASCII, pu.getCharsetProperty("e.1"));
        assertEquals(Charset.defaultCharset(), pu.getCharsetProperty("e.2"));
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
    public void testGetMappedProperty_sun_stdout_encoding() {
        final PropertiesUtil pu = new PropertiesUtil(System.getProperties());
        final Charset expected = System.console() == null ? Charset.defaultCharset() : StandardCharsets.UTF_8;
        assertEquals(expected, pu.getCharsetProperty("sun.stdout.encoding"));
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
    public void testGetMappedProperty_sun_stderr_encoding() {
        final PropertiesUtil pu = new PropertiesUtil(System.getProperties());
        final Charset expected = System.console() == null ? Charset.defaultCharset() : StandardCharsets.UTF_8;
        assertEquals(expected, pu.getCharsetProperty("sun.err.encoding"));
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    public void testNonStringSystemProperties() {
        final Object key1 = "1";
        final Object key2 = new Object();
        System.getProperties().put(key1, new Object());
        System.getProperties().put(key2, "value-2");
        try {
            final PropertiesUtil util = new PropertiesUtil(new Properties());
            assertNull(util.getStringProperty("1"));
        } finally {
            System.getProperties().remove(key1);
            System.getProperties().remove(key2);
        }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    public void testJsonProperties() {
        final PropertiesUtil util = new PropertiesUtil("my-app", "PropertiesUtilTest.json");
        assertNull(util.getStringProperty("log4j2", null));
        assertEquals(true, util.getBooleanProperty("JNDI.enableJMS", false));
        assertEquals("Groovy,JavaScript", util.getStringProperty("Script.enableLanguages"));
        assertEquals("com.acme.log4j.CustomMergeStrategy", util.getStringProperty("Configuration.mergeStrategy"));
        assertEquals("Info", util.getStringProperty("StatusLogger.defaultStatusLevel"));
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    public void testEnhancedJsonProperties() {
        final PropertiesUtil util = PropertiesUtil.getContextProperties("my-app");
        assertEquals(true, util.getBooleanProperty("JNDI.enableJMS"));
        assertEquals("Groovy,JavaScript", util.getStringProperty("Script.enableLanguages"));
        assertEquals("com.acme.log4j.CustomMergeStrategy", util.getStringProperty("Configuration.mergeStrategy"));
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
    public void testPublish() {
        final Properties props = new Properties();
        final PropertiesUtil util = new PropertiesUtil(props);
        final String value = System.getProperty("Application");
        assertNotNull(value, "System property was not published");
        assertEquals("Log4j", value);
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    public void testBadPropertysource() {
        final String key1 = "testKey";
        System.getProperties().put(key1, "test");
        final PropertiesUtil util = new PropertiesUtil(new Properties());
        ErrorPropertySource source = new ErrorPropertySource();
        util.addPropertySource(source);
        try {
            assertEquals("test", util.getStringProperty(key1));
            assertTrue(source.exceptionThrown);
        } finally {
            util.removePropertySource(source);
            System.getProperties().remove(key1);
        }
    }

    private static final String[][] data = {
        {null, "org.apache.logging.log4j.level"},
        {null, "Log4jAnotherProperty"},
        {null, "log4j2.catalinaBase"},
        {"ok", "Configuration.file"},
        {"ok", "Configuration.level"},
        {null, "log4j2.newLevel"},
        {"ok", "AsyncLogger.timeout"},
        {"ok", "AsyncLoggerConfig.ringBufferSize"},
        {"ok", "ThreadContext.enable"},
        {"ok", "ThreadContext.enableStack"},
        {"ok", "ThreadContext.enableMap"},
        {"ok", "ThreadContext.mapInheritable"}
    };

    /**
     * LOG4J2-3413: Log4j should only resolve properties that start with a 'log4j'
     * prefix or similar.
     */
    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
    public void testResolvesOnlyLog4jProperties() {
        final PropertiesUtil util = new PropertiesUtil("Jira3413Test.properties");
        for (final String[] pair : data) {
            final String value = util.getStringProperty(pair[1]);
            assertEquals(pair[0], value, "Unexpected value for " + pair[1]);
        }
    }

    /**
     * LOG4J2-3559 - no longer applies
     * This changes in Log4j 3.0. incorrect properties are no longer include in the results by default.
     */
    @Test
    @ReadsSystemProperty
    public void testLog4jProperty() {
        final Properties props = new Properties();
        final String incorrect = "log4j2.";
        final String correct = "not.starting.with.log4j";
        props.setProperty(incorrect, incorrect);
        props.setProperty(correct, correct);
        final PropertiesUtil util = new PropertiesUtil(props);
        assertNull(util.getStringProperty(correct));
    }

    private class ErrorPropertySource implements PropertySource {
        public boolean exceptionThrown = false;

        @Override
        public int getPriority() {
            return Integer.MIN_VALUE;
        }

        @Override
        public String getProperty(String key) {
            exceptionThrown = true;
            throw new InstantiationError("Test");
        }

        @Override
        public boolean containsProperty(String key) {
            exceptionThrown = true;
            throw new InstantiationError("Test");
        }
    }
}
