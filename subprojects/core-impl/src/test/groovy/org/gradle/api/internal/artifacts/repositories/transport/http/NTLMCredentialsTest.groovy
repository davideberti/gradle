/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.repositories.transport.http;


import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.util.SetSystemProperties
import org.junit.Rule
import spock.lang.Specification

public class NTLMCredentialsTest extends Specification {
    final PasswordCredentials credentials = Mock()

    @Rule
    public SetSystemProperties systemProperties = new SetSystemProperties()

    def "uses domain when encoded in username"() {
        when:
        credentials.username >> "domain\\username"
        credentials.password >> "password"
        def ntlmCredentials = new NTLMCredentials(credentials)

        then:
        ntlmCredentials.domain == 'DOMAIN'
        ntlmCredentials.username == 'username'
        ntlmCredentials.password == 'password'
    }

    def "uses domain when encoded in username with forward slash"() {
        when:
        credentials.username >> "domain/username"
        credentials.password >> "password"
        def ntlmCredentials = new NTLMCredentials(credentials)

        then:
        ntlmCredentials.domain == 'DOMAIN'
        ntlmCredentials.username == 'username'
        ntlmCredentials.password == 'password'
    }

    def "uses default domain when not encoded in username"() {
        when:
        credentials.username >> "username"
        credentials.password >> "password"
        def ntlmCredentials = new NTLMCredentials(credentials)

        then:
        ntlmCredentials.domain == ''
        ntlmCredentials.username == 'username'
        ntlmCredentials.password == 'password'
    }

    def "uses system property for domain when not encoded in username"() {
        when:
        System.setProperty("http.auth.ntlm.domain", "domain")
        credentials.username >> "username"
        credentials.password >> "password"
        def ntlmCredentials = new NTLMCredentials(credentials)

        then:
        ntlmCredentials.domain == 'DOMAIN'
        ntlmCredentials.username == 'username'
        ntlmCredentials.password == 'password'
    }

    def "uses default workstation"() {
        when:
        credentials.username >> "username"
        credentials.password >> "password"
        def ntlmCredentials = new NTLMCredentials(credentials)

        then:
        ntlmCredentials.domain == ''
        ntlmCredentials.username == 'username'
        ntlmCredentials.password == 'password'
        ntlmCredentials.workstation == ''
    }
}
