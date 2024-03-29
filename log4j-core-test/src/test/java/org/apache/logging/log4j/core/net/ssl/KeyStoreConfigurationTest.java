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
package org.apache.logging.log4j.core.net.ssl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyStore;
import org.apache.logging.log4j.core.test.net.ssl.TestConstants;
import org.apache.logging.log4j.test.junit.StatusLoggerLevel;
import org.junit.jupiter.api.Test;

@StatusLoggerLevel("OFF")
public class KeyStoreConfigurationTest {
    @SuppressWarnings("deprecation")
    @Test
    public void loadEmptyConfigurationDeprecated() {
        assertThrows(
                StoreConfigurationException.class,
                () -> new KeyStoreConfiguration(null, TestConstants.NULL_PWD, null, null));
    }

    @Test
    public void loadEmptyConfiguration() {
        assertThrows(
                StoreConfigurationException.class,
                () -> new KeyStoreConfiguration(null, new MemoryPasswordProvider(TestConstants.NULL_PWD), null, null));
    }

    @Test
    public void loadNotEmptyConfigurationDeprecated() throws StoreConfigurationException {
        @SuppressWarnings("deprecation")
        final KeyStoreConfiguration ksc = new KeyStoreConfiguration(
                TestConstants.KEYSTORE_FILE, TestConstants.KEYSTORE_PWD(), TestConstants.KEYSTORE_TYPE, null);
        final KeyStore ks = ksc.getKeyStore();
        assertNotNull(ks);
    }

    @Test
    public void loadNotEmptyConfiguration() throws StoreConfigurationException {
        final KeyStoreConfiguration ksc = new KeyStoreConfiguration(
                TestConstants.KEYSTORE_FILE,
                new MemoryPasswordProvider(TestConstants.KEYSTORE_PWD()),
                TestConstants.KEYSTORE_TYPE,
                null);
        final KeyStore ks = ksc.getKeyStore();
        assertNotNull(ks);
    }

    @Test
    public void returnTheSameKeyStoreAfterMultipleLoadsDeprecated() throws StoreConfigurationException {
        @SuppressWarnings("deprecation")
        final KeyStoreConfiguration ksc = new KeyStoreConfiguration(
                TestConstants.KEYSTORE_FILE, TestConstants.KEYSTORE_PWD(), TestConstants.KEYSTORE_TYPE, null);
        final KeyStore ks = ksc.getKeyStore();
        final KeyStore ks2 = ksc.getKeyStore();
        assertSame(ks, ks2);
    }

    @Test
    public void returnTheSameKeyStoreAfterMultipleLoads() throws StoreConfigurationException {
        final KeyStoreConfiguration ksc = new KeyStoreConfiguration(
                TestConstants.KEYSTORE_FILE,
                new MemoryPasswordProvider(TestConstants.KEYSTORE_PWD()),
                TestConstants.KEYSTORE_TYPE,
                null);
        final KeyStore ks = ksc.getKeyStore();
        final KeyStore ks2 = ksc.getKeyStore();
        assertSame(ks, ks2);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void wrongPasswordDeprecated() {
        assertThrows(
                StoreConfigurationException.class,
                () -> new KeyStoreConfiguration(TestConstants.KEYSTORE_FILE, "wrongPassword!", null, null));
    }

    @Test
    public void wrongPassword() {
        assertThrows(
                StoreConfigurationException.class,
                () -> new KeyStoreConfiguration(
                        TestConstants.KEYSTORE_FILE,
                        new MemoryPasswordProvider("wrongPassword!".toCharArray()),
                        null,
                        null));
    }
}
